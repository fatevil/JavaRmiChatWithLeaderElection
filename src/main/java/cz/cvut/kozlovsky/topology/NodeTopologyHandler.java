package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.network.Reachable;
import cz.cvut.kozlovsky.model.NodeStub;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeTopologyHandler extends Reachable, Remote {

    NodeStub getLeftNeighbour() throws RemoteException;

    NodeStub getRightNeighbour() throws RemoteException;

    NodeStub getMyself() throws RemoteException;

    void setLeftNeighbour(Node node) throws RemoteException;

    void setRightNeighbour(Node node) throws RemoteException;

    void setMyself(Node node) throws RemoteException;

    NodeStub getNewLeader() throws RemoteException;

}
