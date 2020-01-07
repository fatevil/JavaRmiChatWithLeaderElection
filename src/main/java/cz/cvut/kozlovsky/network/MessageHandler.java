package cz.cvut.kozlovsky.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageHandler<T> extends Remote {

    void receiveMessage(T message) throws RemoteException;

    void sendMessage(T message) throws RemoteException;

}
