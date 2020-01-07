package cz.cvut.kozlovsky.communication;

import cz.cvut.kozlovsky.topology.Neighbours;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Reachable {

    int getId() throws RemoteException;

    String getNickname() throws RemoteException;

    String getIpAddress() throws RemoteException;

    int getPort() throws RemoteException;

    void receiveChatMessage(String message) throws RemoteException;

    void setNeighbours(Neighbours neighbours) throws RemoteException;

    Neighbours getNeighbours() throws RemoteException;

    void fixNetwork() throws RemoteException;

}
