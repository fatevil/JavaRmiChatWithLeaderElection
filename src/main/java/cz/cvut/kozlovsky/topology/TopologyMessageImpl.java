package cz.cvut.kozlovsky.topology;

import lombok.Builder;
import lombok.Data;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Data
public class TopologyMessageImpl extends UnicastRemoteObject implements TopologyMessage {

    private final TopologyMessagePurpose purpose;

    private final int originId;
    private final String originIpAddress;
    private final int originPort;

    @Builder
    public TopologyMessageImpl(TopologyMessagePurpose purpose, int originId, String originIpAddress, int originPort) throws RemoteException {
        this.purpose = purpose;
        this.originId = originId;
        this.originIpAddress = originIpAddress;
        this.originPort = originPort;
    }
}
