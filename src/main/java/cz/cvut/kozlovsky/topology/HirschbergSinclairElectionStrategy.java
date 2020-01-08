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
            System.out.println("No need to start!");
            return;
        }
        System.out.println("Start election!");
        isElectionOn = true;
        callElectionLeft();
        callElectionRight();
        //sendMyVote(0, 1);
    }

    public void sendMyVote(int phase, int distanceSoFar) throws RemoteException, MalformedURLException, NotBoundException {
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
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        switch (message.getPurpose()) {
            case START_ELECTION:
                System.out.println("got start election message directed to" + message.getDirection());

                if (!isElectionOn) {
                    System.out.println("And its good new for us!");
                    isElectionOn = true;

                    if (message.getDirection() == LEFT) {
                        System.out.println("pass on to left");
                        callElectionLeft();
                    } else if (message.getDirection() == RIGHT) {
                        System.out.println("pass on to right");
                        callElectionRight();
                    }

//                    sendMyVote(0, 1);
                } else {
                    System.out.println("But we know that!");
                }
                break;
            case ELECT:
                System.out.println("got election message" + message);
        }
    }

    @Override
    public void sendMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {

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
