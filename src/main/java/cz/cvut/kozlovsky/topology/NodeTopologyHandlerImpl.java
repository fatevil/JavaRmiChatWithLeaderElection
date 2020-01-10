package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeStub;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

import static cz.cvut.kozlovsky.topology.TopologyMessageDirection.LEFT;
import static cz.cvut.kozlovsky.topology.TopologyMessageDirection.RIGHT;
import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.TOPOLOGY_NOT_OKAY;


@Log
@Data
public class NodeTopologyHandlerImpl extends UnicastRemoteObject implements NodeTopologyHandler {

    private final Node node;

    private NodeStub myself;
    private NodeStub leftNeighbour;
    private NodeStub rightNeighbour;

    private LeaderElectionStrategy leaderElectionStrategy;

    private boolean fixInProgress = false;
    private Queue<TopologyMessage> messageQueue = new LinkedList<>();

    @Builder
    public NodeTopologyHandlerImpl(Node node) throws RemoteException {
        this.node = node;
        this.myself = new NodeStub(node);
        this.leaderElectionStrategy = new HirschbergSinclairElectionStrategy(this);
    }

    /**
     * Participates in fixing the ring topology, if broken. Proposes leader election.
     */
    public void requestNetworkFixed() throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {
        fixRingTopologyIfBroken();
    }

    /**
     * Fix the ring network topology.
     */
    private void fixRingTopologyIfBroken() throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        if (fixInProgress) {
            return;
        }

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
                    .originalNode(myself)
                    .purpose(TOPOLOGY_NOT_OKAY)
                    .direction(RIGHT)
                    .build();

            fixInProgress = true;
            sendMessage(message);
        } else if (rightNeighbour == null && !fixInProgress) {
            TopologyMessage message = TopologyMessageImpl.builder()
                    .originalNode(myself)
                    .purpose(TOPOLOGY_NOT_OKAY)
                    .direction(LEFT)
                    .build();

            fixInProgress = true;
            sendMessage(message);
        }
    }

    @Override
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        if (node.isConnectedToNetwork()) {
            System.out.println("dont need messages, got connection: " + message.getOriginId() + " " + message.getPurpose() + " ");
            return;
        }
        fixRingTopologyIfBroken();

        switch (message.getPurpose()) {
            case TOPOLOGY_NOT_OKAY:
                if (rightNeighbour == null) {
                    rightNeighbour = new NodeStub(message.getOriginId(), message.getOriginIpAddress(), message.getOriginPort());
                    fixInProgress = false;

                    log.info("Fixed RIGHT neighbour with ID " + message.getOriginId());
                    leaderElectionStrategy.startElection();
                    solveQueuedMessages();
                } else if (leftNeighbour == null) {
                    leftNeighbour = new NodeStub(message.getOriginId(), message.getOriginIpAddress(), message.getOriginPort());
                    fixInProgress = false;

                    log.info("Fixed LEFT neighbour with ID " + message.getOriginId());
                    leaderElectionStrategy.startElection();
                    solveQueuedMessages();
                } else {
                    log.info("Passing further message neighbour inquiry from ID  " + message.getOriginId());
                    sendMessage(message);
                    leaderElectionStrategy.startElection();
                }
                break;

            case START_ELECTION:
            case ELECTED:
            case ELECT:
                if (fixInProgress) {
                    System.out.println("= push to stack");
                    messageQueue.add(message);
                } else {
                    leaderElectionStrategy.receiveMessage(message);
                }
                break;
        }
    }

    private void solveQueuedMessages() throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {
        while (!messageQueue.isEmpty()) {
            System.out.println("pop from stack");
            receiveMessage(messageQueue.remove());
        }
    }

    @Override
    public void sendMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        if (message.getDirection().equals(LEFT)) {
            if (leftNeighbour.getTopologyHandler() == null) {
                leftNeighbour.setTopologyHandler(getTopologyHandler(leftNeighbour));
            }
            leftNeighbour.getTopologyHandler().receiveMessage(message);

        } else {
            if (rightNeighbour.getTopologyHandler() == null) {
                rightNeighbour.setTopologyHandler(getTopologyHandler(rightNeighbour));
            }
            rightNeighbour.getTopologyHandler().receiveMessage(message);
        }
    }

    public NodeTopologyHandler getTopologyHandler(NodeStub node) throws RemoteException, MalformedURLException, NotBoundException {
//        log.info("Lookup NodeTopologyHandler at: " + "//" + node.getIpAddress() + ":" + node.getPort() + "/TopologyHandler");

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

}
