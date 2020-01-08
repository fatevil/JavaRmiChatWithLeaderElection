package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeStub;
import lombok.Builder;
import lombok.Data;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.ELECT;
import static cz.cvut.kozlovsky.topology.TopologyMessagePurpose.START_ELECTION;

@Data
public class HirschbergSinclairElectionStrategy implements LeaderElectionStrategy {

    private final NodeTopologyHandler nodeTopologyHandler;
    boolean isElectionOn = false;

    @Builder
    public HirschbergSinclairElectionStrategy(NodeTopologyHandler nodeTopologyHandler) {
        this.nodeTopologyHandler = nodeTopologyHandler;
    }

    @Override
    public void startElection() throws RemoteException, MalformedURLException, NotBoundException {
        NodeStub originalNode = nodeTopologyHandler.getMyself();
        TopologyMessage message = TopologyMessageImpl.builder()
                .originId(originalNode.getId())
                .originIpAddress(originalNode.getIpAddress())
                .originPort(originalNode.getPort())
                .purpose(START_ELECTION).build();

        nodeTopologyHandler.sendMessage(message);
    }

    public void sendVote() throws RemoteException, MalformedURLException, NotBoundException {
        NodeStub originalNode = nodeTopologyHandler.getMyself();
        TopologyMessage message = TopologyMessageImpl.builder()
                .originId(originalNode.getId())
                .originIpAddress(originalNode.getIpAddress())
                .originPort(originalNode.getPort())
                .purpose(ELECT).build();

        nodeTopologyHandler.sendMessage(message);


    }

    @Override
    public void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {
        switch (message.getPurpose()) {
            case START_ELECTION:

                // pass election on if its a new information
                if (!isElectionOn) {
                    isElectionOn = true;
                    sendMessage(message);
                }

        }
    }

    @Override
    public void sendMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException {

    }
}
