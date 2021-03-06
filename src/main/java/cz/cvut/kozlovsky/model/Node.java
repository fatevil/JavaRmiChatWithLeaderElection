package cz.cvut.kozlovsky.model;

import cz.cvut.kozlovsky.network.MessageHandler;
import cz.cvut.kozlovsky.network.Reachable;
import cz.cvut.kozlovsky.topology.NodeTopologyHandler;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Reachable {

    int getId() throws RemoteException;

    String getNickname() throws RemoteException;

    String getIpAddress() throws RemoteException;

    int getPort() throws RemoteException;

    NodeTopologyHandler getNodeTopologyHandler() throws RemoteException;

    void fixNetwork() throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException;

    boolean isConnectedToNetwork() throws RemoteException;

    void createEstablishedNetwork(boolean assignNeigbours) throws RemoteException, MalformedURLException, AlreadyBoundException;

    void joinEstablishedNetwork(String remoteAddress, int remotePort, boolean assignNeigbours) throws RemoteException, NotBoundException, MalformedURLException;

    void restoreChatConsole() throws RemoteException;

    MessageHandler getChatConsole() throws RemoteException;

}
