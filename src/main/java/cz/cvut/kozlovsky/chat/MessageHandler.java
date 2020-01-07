package cz.cvut.kozlovsky.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageHandler extends Remote {

    void receiveMessage(String message) throws RemoteException;

    void sendMessage(String message) throws RemoteException;

}
