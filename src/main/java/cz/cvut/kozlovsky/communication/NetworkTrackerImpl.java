package cz.cvut.kozlovsky.communication;

import cz.cvut.kozlovsky.topology.Neighbours;
import cz.cvut.kozlovsky.topology.NeighboursImpl;
import lombok.Builder;
import lombok.extern.java.Log;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Keeps track of registered network nodes. Distributes chat messages between clients.
 * <p>
 * Checks availability everytime before invoking remote object method.
 * If it finds non-available nodes, stops keeping track of them (removes them from the concurrent map).
 * After each check, if any node was removed, set new topology neighbours to all nodes.
 */
@Log
class NetworkTrackerImpl extends UnicastRemoteObject implements NetworkTracker {

    private Node leader;
    private final Map<Integer, Node> nodes = new ConcurrentHashMap<>();

    @Builder
    public NetworkTrackerImpl(NodeImpl leader) throws RemoteException {
        super();
        log.info(String.format("Creating network with leader ID ", leader.getId()));
        this.leader = leader;
        acceptMember(leader);
    }

    @Override
    public void acceptMember(Node newComer) throws RemoteException {
        log.info(String.format("Accepting node with ID ", newComer.getId()));

        // no need for synchronized block for put, thanks to concurrent data structure
        nodes.put(newComer.getId(), newComer);
        fixNeighbours();
    }

    @Override
    public void destributeChatMessage(String chatMessage) throws RemoteException {
        checkEveryoneAvailable();

        nodes.forEach((id, node) -> {
            try {
                node.receiveChatMessage(chatMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void checkEveryoneAvailable() throws RemoteException {
        // check if everyone is still available, otherwise remove them from the chat and fix neighbours

        final boolean[] needNeighboursFix = {false};  // lambda closure hack
        nodes.forEach((id, node) -> {
            try {
                if (id == leader.getId()) return;

                StatusCheck.checkAvailability(node, 50, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                nodes.remove(id);
                needNeighboursFix[0] = true;
                log.info("Node with ID " + id + " not reachable! REMOVED FROM CHAT.");
            } catch (InterruptedException | ExecutionException | RemoteException e) {
                e.printStackTrace();
            }
        });

        if (needNeighboursFix[0]) fixNeighbours();
    }

    /**
     * Reassign left and right neighbour to every available node.
     */
    public void fixNeighbours() throws RemoteException {
        if (nodes.size() == 1) {
            Neighbours neighbours = leader.getNeighbours();
            neighbours.setLeft(leader);
            neighbours.setRight(leader);

        } else {

            List<Node> updatedNodes = new ArrayList<>(nodes.values());
            for (int i = 0; i < updatedNodes.size(); i++) {

                int following = (i + 1) % (updatedNodes.size() - 1);
                int previous = (i - 1) % (updatedNodes.size() - 1);

                Neighbours neighbours;
                synchronized (neighbours = updatedNodes.get(i).getNeighbours()) {

                    neighbours.setLeft(updatedNodes.get(previous));
                    neighbours.setRight(updatedNodes.get(following));
                }
            }
        }
    }
}