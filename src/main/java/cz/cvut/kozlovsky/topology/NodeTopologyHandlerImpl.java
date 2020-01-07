package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeStub;
import cz.cvut.kozlovsky.network.StatusCheck;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.TOPOLOGY_NOT_OKAY_LEFT;
import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.TOPOLOGY_NOT_OKAY_RIGHT;

@Log
@Data
public class NodeTopologyHandlerImpl extends UnicastRemoteObject implements NodeTopologyHandler {

    private NodeStub myself;
    private NodeStub leftNeighbour;
    private NodeStub rightNeighbour;

    @Builder
    public NodeTopologyHandlerImpl(Node myself) throws RemoteException {
        this.myself = new NodeStub(myself);
    }

    /**
     * Fix the ring network topology.
     */
    private void fixRingTopology() throws RemoteException, MalformedURLException, NotBoundException {
        // check if i have left and right
        // if not send "topology not okay - looking for left or right"
        // if okay send okay

        // if previously okay and got my own message, start electing

        // if previously not okay and got some else not okay, assign him

        System.out.println(leftNeighbour);
        System.out.println(myself);
        System.out.println(rightNeighbour);

        try {
            NodeTopologyHandler handler = getTopologyHandler(leftNeighbour);
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            log.info("Node with ID " + myself.getId() + " lost left neighbour.");
            leftNeighbour = null;
        }

        try {
            NodeTopologyHandler handler = getTopologyHandler(rightNeighbour);
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            log.info("Node with ID " + myself.getId() + " lost right neighbour.");
            rightNeighbour = null;
        }

        if (leftNeighbour == null && rightNeighbour == null) {
            log.info("Lost both my neighbours. Gotta terminate.");
            System.exit(1);
        } else if (leftNeighbour == null) {
            TopologyMessage message = new TopologyMessageImpl(TOPOLOGY_NOT_OKAY_LEFT, myself.getId(), myself.getIpAddress(), myself.getPort());
            sendMessage(message);
        } else if (rightNeighbour == null) {
            TopologyMessage message = new TopologyMessageImpl(TOPOLOGY_NOT_OKAY_RIGHT, myself.getId(), myself.getIpAddress(), myself.getPort());
            sendMessage(message);
        }


    }

    /**
     * Use the Hirschberg–Sinclair algorithm for electing new leder. Elected leader will create new Network.
     */
    private NodeStub electNewLeader() {
        return null;
    }

    /**
     * Fixes network topology (connects disconnected nodes) and participates in electing new leader.
     *
     * @return leader stub
     */
    public NodeStub getNewLeader() throws RemoteException, MalformedURLException, NotBoundException {
        fixRingTopology();
        NodeStub node = electNewLeader();

        return node;
    }

    @Override
    public void setLeftNeighbour(Node node) throws RemoteException {
        leftNeighbour = new NodeStub(node);
    }

    @Override
    public void setRightNeighbour(Node node) throws RemoteException {
        rightNeighbour = new NodeStub(node);
    }

    @Override
    public void setMyself(Node node) throws RemoteException {
        myself = new NodeStub(node);
    }

    @SneakyThrows
    @Override
    public String toString() {
        return "NeighboursImpl{meId=" + myself.getId() + ", lId=" + leftNeighbour.getId() + ", rId=" + rightNeighbour.getId() + '}';
    }

    @Override
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        switch (message.getPurpose()) {
            case TOPOLOGY_NOT_OKAY_LEFT:
                if (rightNeighbour == null) {
                    synchronized (rightNeighbour) {
                        rightNeighbour = new NodeStub(message.getOriginId(), message.getOriginIpAddress(), message.getOriginPort());
                    }
                } else {
                    sendMessage(message);
                }
            case TOPOLOGY_NOT_OKAY_RIGHT:
                if (leftNeighbour == null) {
                    synchronized (leftNeighbour) {
                        leftNeighbour = new NodeStub(message.getOriginId(), message.getOriginIpAddress(), message.getOriginPort());
                    }
                } else {
                    sendMessage(message);

                }
                break;
        }

    }

    @Override
    public void sendMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        NodeTopologyHandler handler;

        switch (message.getPurpose()) {
            case TOPOLOGY_NOT_OKAY_LEFT:
                // send message to right
                handler = getTopologyHandler(rightNeighbour);
                handler.receiveMessage(message);
                break;

            case TOPOLOGY_NOT_OKAY_RIGHT:
                // send message to left
                handler = getTopologyHandler(leftNeighbour);
                handler.receiveMessage(message);
                break;

        }
    }

    private NodeTopologyHandler getTopologyHandler(NodeStub node) throws RemoteException, MalformedURLException, NotBoundException {
        log.info("Lookup TopologyHandler at: " + "//" + node.getIpAddress() + ":" + node.getPort() + "/TopologyHandler");

        return (NodeTopologyHandler) Naming.lookup("//" + node.getIpAddress() + ":" + node.getPort() + "/TopologyHandler");

    }

}
