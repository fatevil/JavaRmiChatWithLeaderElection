package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeStub;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import static cz.cvut.kozlovsky.topology.TopologyMessageDirection.LEFT;
import static cz.cvut.kozlovsky.topology.TopologyMessageDirection.RIGHT;
import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.TOPOLOGY_NOT_OKAY;


@Log
@Data
public class NodeTopologyHandlerImpl extends UnicastRemoteObject implements NodeTopologyHandler {

    private NodeStub myself;
    private NodeStub leftNeighbour;
    private NodeStub rightNeighbour;

    private LeaderElectionStrategy leaderElectionStrategy;

    private boolean fixInProgress = false;

    @Builder
    public NodeTopologyHandlerImpl(Node myself) throws RemoteException {
        this.myself = new NodeStub(myself);
        this.leaderElectionStrategy = new HirschbergSinclairElectionStrategy(this);
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

        if (fixInProgress) {
            return;
        }

        System.out.println(leftNeighbour);
        System.out.println(myself);
        System.out.println(rightNeighbour);

        try {
            leftNeighbour.setTopologyHandler(getTopologyHandler(leftNeighbour));
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            log.info("Node with ID " + myself.getId() + " lost left neighbour.");
            leftNeighbour = null;
        }

        try {
            rightNeighbour.setTopologyHandler(getTopologyHandler(rightNeighbour));
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            log.info("Node with ID " + myself.getId() + " lost right neighbour.");
            rightNeighbour = null;
        }

        if (rightNeighbour == null && leftNeighbour == null) {
            log.info("Lost both my neighbours. Gotta terminate.");
            System.exit(1);
        } else if (leftNeighbour == null && !fixInProgress) {
            TopologyMessage message = TopologyMessageImpl.builder()
                    .originId(myself.getId())
                    .originIpAddress(myself.getIpAddress())
                    .originPort(myself.getPort())
                    .purpose(TOPOLOGY_NOT_OKAY)
                    .direction(RIGHT)
                    .build();

            fixInProgress = true;
            sendMessage(message);
        } else if (rightNeighbour == null && !fixInProgress) {
            TopologyMessage message = TopologyMessageImpl.builder()
                    .originId(myself.getId())
                    .originIpAddress(myself.getIpAddress())
                    .originPort(myself.getPort())
                    .purpose(TOPOLOGY_NOT_OKAY)
                    .direction(LEFT)
                    .build();

            fixInProgress = true;
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
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        fixRingTopology();

        switch (message.getPurpose()) {
            case TOPOLOGY_NOT_OKAY:
                if (rightNeighbour == null) {
                    rightNeighbour = new NodeStub(message.getOriginId(), message.getOriginIpAddress(), message.getOriginPort());
                    fixInProgress = false;

                } else if (leftNeighbour == null) {
                    leftNeighbour = new NodeStub(message.getOriginId(), message.getOriginIpAddress(), message.getOriginPort());
                    fixInProgress = false;

                } else {
                    System.out.println(message);
                    sendMessage(message);
                }
                break;

            case START_ELECTION:
                leaderElectionStrategy.receiveMessage(message);
        }
    }

    @Override
    public void sendMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        NodeTopologyHandler handler;
        if (message.getDirection().equals(LEFT)) {
            //handler = getTopologyHandler(leftNeighbour);
            //handler.receiveMessage(message);
            leftNeighbour.getTopologyHandler().receiveMessage(message);

        } else {
            //handler = getTopologyHandler(rightNeighbour);
            //handler.receiveMessage(message);
            rightNeighbour.getTopologyHandler().receiveMessage(message);
        }
    }

    public NodeTopologyHandler getTopologyHandler(NodeStub node) throws RemoteException, MalformedURLException, NotBoundException {
        log.info("Lookup NodeTopologyHandler at: " + "//" + node.getIpAddress() + ":" + node.getPort() + "/TopologyHandler");

        return (NodeTopologyHandler) Naming.lookup("//" + node.getIpAddress() + ":" + node.getPort() + "/NodeTopologyHandler");
    }

    @Override
    public String toString() {
        return "NodeTopologyHandlerImpl{" +
                "myself=" + myself.getId() +
                ", lN=" + leftNeighbour.getId() +
                ", rN=" + rightNeighbour.getId() +
                '}';
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

}
