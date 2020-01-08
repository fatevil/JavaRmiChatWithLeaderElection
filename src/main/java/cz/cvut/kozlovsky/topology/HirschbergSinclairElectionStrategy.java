package cz.cvut.kozlovsky.topology;

import lombok.Builder;
import lombok.Data;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static cz.cvut.kozlovsky.topology.TopologyMessageDirection.LEFT;
import static cz.cvut.kozlovsky.topology.TopologyMessageDirection.RIGHT;
import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.ELECT;
import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.START_ELECTION;

@Data
public class HirschbergSinclairElectionStrategy implements LeaderElectionStrategy {

    private final NodeTopologyHandler nodeTopologyHandler;
    private volatile boolean isElectionOn = false;
    private boolean stillHasPotential = true;

    private boolean gotResponseFromLeft = false;
    private boolean gotResponseFromRight = false;

    @Builder
    public HirschbergSinclairElectionStrategy(NodeTopologyHandler nodeTopologyHandler) {
        this.nodeTopologyHandler = nodeTopologyHandler;
    }

    /**
     * Send election started notificatino to both sides. Send its own vote.
     *
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    @Override
    public void startElection() throws RemoteException, MalformedURLException, NotBoundException {
        if (isElectionOn) {
            return;
        }
        System.out.println("Start election!");
        isElectionOn = true;
        stillHasPotential = true;
        callElectionLeft();
        callElectionRight();
        sendMyVote(0, 1);
    }

    public void sendMyVote(int phase, int distanceSoFar) throws RemoteException, MalformedURLException, NotBoundException {
        if (!stillHasPotential) {
            return;
        }

        TopologyMessage messageLeft = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(ELECT).direction(LEFT)
                .distanceSoFar(distanceSoFar).phase(phase)
                .build();
        System.out.println("sending to LEFT with id " + nodeTopologyHandler.getMyself().getId() + " phase and distance " + phase + " " + distanceSoFar);
        nodeTopologyHandler.sendMessage(messageLeft);

        TopologyMessage messageRight = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(ELECT).direction(RIGHT)
                .distanceSoFar(distanceSoFar).phase(phase)
                .build();

        System.out.println("sending to RIGHT with id " + nodeTopologyHandler.getMyself().getId()+ " phase and distance " + phase + " " + distanceSoFar);        nodeTopologyHandler.sendMessage(messageRight);

    }

    @Override
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        switch (message.getPurpose()) {
            case START_ELECTION:
                System.out.println("got start election message directed to " + message.getDirection());

                if (!isElectionOn) {
                    isElectionOn = true;

                    if (message.getDirection() == LEFT) {
                        callElectionLeft();
                    } else if (message.getDirection() == RIGHT) {
                        callElectionRight();
                    }
                    stillHasPotential = true;
                    sendMyVote(0, 1);
                } else {
                    System.out.println("We already have election up and running!");
                }
                break;
            case ELECT:
                System.out.println("got election message with ID " + message.getOriginId() + "   " + message.getDirection() + " with disrtance " + message.getDistanceSoFar() + "at phase " + message.getPhase());

                if (message.getOriginId() == nodeTopologyHandler.getMyself().getId() && !message.getChangedDirection()) {

                    System.out.println("WE GOT OURSELVES A LEADER");

                } else if (message.getOriginId() == nodeTopologyHandler.getMyself().getId() && message.getChangedDirection()) {

                    if (gotResponseFromLeft || gotResponseFromRight) {
                        System.out.println("===== great! send again");
                        gotResponseFromRight = false;
                        gotResponseFromLeft = false;

                        sendMyVote(message.getPhase() + 1, 1);
                    } else if (message.getDirection().equals(LEFT)) {
                        System.out.println("set got message from right");
                        gotResponseFromRight = true;
                    } else {
                        System.out.println("set got message from left");
                        gotResponseFromLeft = true;
                    }


                } else if (message.getOriginId() < nodeTopologyHandler.getMyself().getId()) {
                    System.out.println("Not gonna pass that on.");
                } else if (message.getOriginId() > nodeTopologyHandler.getMyself().getId()) {
                    stillHasPotential = false;
                    //gotResponseFromRight = false;
                    //gotResponseFromLeft = false;

                    System.out.println("Im losing potential");
                    System.out.println("the message is at phase " + message.getPhase() + " distance " + message.getDistanceSoFar() + " changed? " + message.getChangedDirection())
                    ;
                    if (Math.pow(2, message.getPhase()) == message.getDistanceSoFar() && !message.getChangedDirection()) {
                        System.out.println("CHANGE DIRECTION ");

                        // the message has reached its phase distance
                        if (message.getDirection() == LEFT) {
                            message.setDirection(RIGHT);
                        } else {
                            message.setDirection(LEFT);
                        }
                        message.setChangedDirection(true);
                        message.setDistanceSoFar(1);
                        nodeTopologyHandler.sendMessage(message);
                    } else {
                        System.out.println("forward the message!");
                        message.setDistanceSoFar(message.getDistanceSoFar() + 1);
                        nodeTopologyHandler.sendMessage(message);
                    }
                }

                break;
        }
    }

    private void callElectionLeft() throws RemoteException, NotBoundException, MalformedURLException {
        TopologyMessage message = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(START_ELECTION).direction(LEFT).build();

        nodeTopologyHandler.sendMessage(message);
    }

    private void callElectionRight() throws RemoteException, NotBoundException, MalformedURLException {
        TopologyMessage message = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(START_ELECTION).direction(RIGHT).build();

        nodeTopologyHandler.sendMessage(message);
    }
}
