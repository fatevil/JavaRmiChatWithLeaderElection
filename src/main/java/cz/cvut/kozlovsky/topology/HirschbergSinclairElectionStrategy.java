package cz.cvut.kozlovsky.topology;

import lombok.Builder;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static cz.cvut.kozlovsky.topology.TopologyMessage.TopologyMessageDirection.LEFT;
import static cz.cvut.kozlovsky.topology.TopologyMessage.TopologyMessageDirection.RIGHT;
import static cz.cvut.kozlovsky.topology.TopologyMessage.TopologyMessagePurpose.*;

public class HirschbergSinclairElectionStrategy implements LeaderElectionStrategy {

    private final NodeTopologyHandler nodeTopologyHandler;
    private volatile boolean isElectionOn = false;
    private boolean stillHasPotential = true;
    private boolean returnedVotes = false;

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
    public void startElection() throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        if (isElectionOn || nodeTopologyHandler.getNode().isConnectedToNetwork()) {
            return;
        }
        isElectionOn = true;
        stillHasPotential = true;
        callElectionLeft();
        callElectionRight();
        sendMyVote(0, 1);
    }

    public void sendMyVote(int phase, int distanceSoFar) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        if (!stillHasPotential) {
            return;
        }

        TopologyMessage messageLeft = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(ELECT).direction(LEFT)
                .distanceSoFar(distanceSoFar).phase(phase)
                .build();
        nodeTopologyHandler.sendMessage(messageLeft);

        TopologyMessage messageRight = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(ELECT).direction(RIGHT)
                .distanceSoFar(distanceSoFar).phase(phase)
                .build();
        nodeTopologyHandler.sendMessage(messageRight);
    }

    @Override
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        switch (message.getPurpose()) {
            case START_ELECTION:
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
                    // we already have elecion up and running
                }
                break;
            case ELECT:
                if (message.getOriginId() == nodeTopologyHandler.getMyself().getId() && !message.getChangedDirection()) {
                    // election went over the whole circle without turning back

                    resetVars(); // set election no more

                    // create new network, don't lose current neighbours
                    nodeTopologyHandler.getNode().createEstablishedNetwork(false);
                    nodeTopologyHandler.getNode().reassignChatConsole();

                    // announce leader to other nodes
                    sendElected();
                } else if (message.getOriginId() == nodeTopologyHandler.getMyself().getId() && message.getChangedDirection()) {
                    // is back from the phase

                    if (returnedVotes) {
                        // come back from the second side as well
                        returnedVotes = false;
                        sendMyVote(message.getPhase() + 1, 1);
                    } else {
                        returnedVotes = true;
                    }
                } else if (message.getOriginId() < nodeTopologyHandler.getMyself().getId()) {
                    // do not pass the message on
                } else if (message.getOriginId() > nodeTopologyHandler.getMyself().getId()) {
                    // loses potential for a leader (found bigger ID)
                    stillHasPotential = false;

                    if (Math.pow(2, message.getPhase()) == message.getDistanceSoFar() && !message.getChangedDirection()) {
                        // the message has reached its phase distance
                        if (message.getDirection() == LEFT) {
                            message.setDirection(RIGHT);
                        } else {
                            message.setDirection(LEFT);
                        }
                        message.setChangedDirection(true);
                        message.setDistanceSoFar(1);

                        // send back
                        nodeTopologyHandler.sendMessage(message);
                    } else {
                        // pass on
                        message.setDistanceSoFar(message.getDistanceSoFar() + 1);
                        nodeTopologyHandler.sendMessage(message);
                    }
                }
                break;
            case ELECTED:
                if (message.getOriginId() == nodeTopologyHandler.getMyself().getId()) {
                    break;
                }
                // joined newly established network and set up the chat again
                nodeTopologyHandler.getNode().joinEstablishedNetwork(message.getOriginIpAddress(), message.getOriginPort(), false);
                nodeTopologyHandler.getNode().reassignChatConsole();
                nodeTopologyHandler.sendMessage(message);
                resetVars();
                break;
        }

    }

    private void sendElected() throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {
        TopologyMessage messageLeft = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(ELECTED).direction(LEFT).build();
        nodeTopologyHandler.sendMessage(messageLeft);
    }

    private void callElectionLeft() throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {
        TopologyMessage message = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(START_ELECTION).direction(LEFT).build();

        nodeTopologyHandler.sendMessage(message);
    }

    private void callElectionRight() throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {
        TopologyMessage message = TopologyMessageImpl.builder()
                .originalNode(nodeTopologyHandler.getMyself())
                .purpose(START_ELECTION).direction(RIGHT).build();

        nodeTopologyHandler.sendMessage(message);
    }

    private void resetVars() {
        isElectionOn = false;
        stillHasPotential = true;
    }
}
