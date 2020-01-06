package cz.cvut.kozlovsky.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote, Touchable {

    int getId() throws RemoteException;

    String getNickname() throws RemoteException;

    String getIpAddress() throws RemoteException;

    int getPort() throws RemoteException;

    void receiveChatMessage(String message) throws RemoteException;

}
