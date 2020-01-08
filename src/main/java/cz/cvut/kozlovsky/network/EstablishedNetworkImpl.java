package cz.cvut.kozlovsky.network;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeImpl;
import cz.cvut.kozlovsky.topology.NodeTopologyHandler;
import lombok.Builder;
import lombok.extern.java.Log;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track of registered network nodes. Distributes chat messages between clients.
 * <p>
 * Checks availability everytime before invoking remote object method.
 * If it finds non-available nodes, stops keeping track of them (removes them from the concurrent map).
 * After each check, if any node was removed, set new topology neighbours to all nodes.
 */
@Log
public
class EstablishedNetworkImpl extends UnicastRemoteObject implements EstablishedNetwork {

    private Node leader;
    private final Map<Integer, Node> nodes = new ConcurrentHashMap<>();

    @Builder
    public EstablishedNetworkImpl(NodeImpl leader) throws RemoteException {
        super();
        log.info(String.format("Creating network with leader ID %s", leader.getId()));
        this.leader = leader;
        acceptMember(leader);
    }

    @Override
    public void acceptMember(Node newComer) throws RemoteException {
        log.info(String.format("Accepting node with ID %s", newComer.getId()));

        // no need for synchronized block for put, thanks to concurrent data structure
        nodes.put(newComer.getId(), newComer);
        fixNodeNeighbours();
    }

    @Override
    public List<Node> getActiveNodes() throws RemoteException {
        checkEveryoneAvailable();
        return new ArrayList<>(nodes.values());
    }

    private void checkEveryoneAvailable() throws RemoteException {
        // check if everyone is still available, otherwise remove them from the chat and fix neighbours

        final boolean[] needNeighboursFix = {false};  // lambda closure hack
        nodes.forEach((id, node) -> {

            try {
                if (id == leader.getId()) return;

                boolean isAvailable = StatusCheck.isAvaliable(node, 50, TimeUnit.MILLISECONDS);
                if (!isAvailable) {
                    nodes.remove(id);
                    needNeighboursFix[0] = true;
                    log.info("Node with ID " + id + " not reachable! REMOVED FROM CHAT.");
                }
            } catch (RemoteException e) {
                // should not happen (leader is always reachable)
                e.printStackTrace();
            }
        });

        if (needNeighboursFix[0]) fixNodeNeighbours();
    }

    /**
     * Reassign left and right neighbour to every available node.
     */
    public void fixNodeNeighbours() throws RemoteException {
        if (nodes.size() == 1) {
            NodeTopologyHandler nodeTopologyHandler = leader.getNodeTopologyHandler();
            nodeTopologyHandler.setLeftNeighbour(leader);
            nodeTopologyHandler.setRightNeighbour(leader);

        } else {
            List<Node> nodesCopy = new ArrayList<>(nodes.values());

            setNeighbours(nodesCopy, nodesCopy.size() - 1, 0, 1);
            for (int i = 1; i < nodesCopy.size() - 1; i++) {
                setNeighbours(nodesCopy, i - 1, i, i + 1);
            }
            setNeighbours(nodesCopy, nodesCopy.size() - 2, nodesCopy.size() - 1, 0);
        }
    }

    private void setNeighbours(List<Node> nodesCopy, int left, int myself, int right) throws RemoteException {
        NodeTopologyHandler nodeTopologyHandler;

        synchronized (nodeTopologyHandler = nodesCopy.get(myself).getNodeTopologyHandler()) {
            nodeTopologyHandler.setLeftNeighbour(nodesCopy.get(left));
            nodeTopologyHandler.setRightNeighbour(nodesCopy.get(right));
        }
    }

}