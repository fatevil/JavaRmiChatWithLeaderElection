package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.communication.Node;
import cz.cvut.kozlovsky.communication.Reachable;
import cz.cvut.kozlovsky.dto.NodeStub;

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
