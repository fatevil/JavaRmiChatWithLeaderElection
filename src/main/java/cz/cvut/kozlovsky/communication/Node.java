package cz.cvut.kozlovsky.communication;

import cz.cvut.kozlovsky.chat.ChatConsole;
import cz.cvut.kozlovsky.chat.MessageHandler;
import cz.cvut.kozlovsky.network.Reachable;
import cz.cvut.kozlovsky.topology.Neighbours;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Reachable {

    int getId() throws RemoteException;

    String getNickname() throws RemoteException;

    String getIpAddress() throws RemoteException;

    int getPort() throws RemoteException;

    Neighbours getNeighbours() throws RemoteException;

    MessageHandler getChatConsole() throws RemoteException;

    void fixNetwork() throws RemoteException;

}
