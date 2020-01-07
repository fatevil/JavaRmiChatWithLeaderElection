package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.network.Reachable;
import cz.cvut.kozlovsky.model.NodeStub;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Neighbours extends Reachable, Remote {

    NodeStub getLeft() throws RemoteException;

    NodeStub getRight() throws RemoteException;

    NodeStub getMyself() throws RemoteException;

    void setLeft(Node node) throws RemoteException;

    void setRight(Node node) throws RemoteException;

    void setMyself(Node node) throws RemoteException;

    NodeStub getNewLeader() throws RemoteException;

}
