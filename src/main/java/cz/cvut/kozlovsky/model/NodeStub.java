package cz.cvut.kozlovsky.model;

import cz.cvut.kozlovsky.topology.NodeTopologyHandler;
import lombok.Data;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Data
public class NodeStub {

    private NodeTopologyHandler topologyHandler;
    private final int id;
    private final String ipAddress;
    private final int port;

    public NodeStub(Node node) throws RemoteException {
        this.id = node.getId();
        this.ipAddress = node.getIpAddress();
        this.port = node.getPort();
    }

    public NodeStub(int id, String ipAddress, int port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
    }
}
