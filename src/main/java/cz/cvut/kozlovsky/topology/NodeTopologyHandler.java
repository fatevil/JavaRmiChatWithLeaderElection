package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeStub;
import cz.cvut.kozlovsky.network.MessageHandler;
import cz.cvut.kozlovsky.network.Reachable;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeTopologyHandler extends Reachable, Remote, MessageHandler<TopologyMessage> {

    NodeStub getMyself() throws RemoteException;

    void setLeftNeighbour(Node node) throws RemoteException;

    void setRightNeighbour(Node node) throws RemoteException;

    void requestNetworkFixed() throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException;

    Node getNode() throws RemoteException;

}
