package cz.cvut.kozlovsky.dto;

import cz.cvut.kozlovsky.communication.Node;
import lombok.Data;

import java.rmi.RemoteException;

@Data
public class NodeStub {

    private final int id;
    private final String ipAddress;
    private final int port;

    public NodeStub(Node node) throws RemoteException {
        this.id = node.getId();
        this.ipAddress = node.getIpAddress();
        this.port = node.getPort();
    }

}
