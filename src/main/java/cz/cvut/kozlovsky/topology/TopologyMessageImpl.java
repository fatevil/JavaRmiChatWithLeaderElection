package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.NodeStub;
import lombok.Builder;
import lombok.Data;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Data
public class TopologyMessageImpl extends UnicastRemoteObject implements TopologyMessage {

    private final TopologyMessagePurpose purpose;
    private final TopologyMessageDirection direction;

    private int distanceSoFar;
    private int phase;

    private final int originId;
    private final String originIpAddress;
    private final int originPort;

    @Builder
    public TopologyMessageImpl(TopologyMessagePurpose purpose, TopologyMessageDirection direction, NodeStub originalNode) throws RemoteException {
        this.purpose = purpose;
        this.direction = direction;
        this.originId = originalNode.getId();
        this.originIpAddress = originalNode.getIpAddress();
        this.originPort = originalNode.getPort();
    }

    @Builder
    public TopologyMessageImpl(TopologyMessagePurpose purpose, TopologyMessageDirection direction, int distanceSoFar, int phase, NodeStub originalNode) throws RemoteException {
        this.purpose = purpose;
        this.direction = direction;
        this.distanceSoFar = distanceSoFar;
        this.phase = phase;
        this.originId = originalNode.getId();
        this.originIpAddress = originalNode.getIpAddress();
        this.originPort = originalNode.getPort();
    }


}
