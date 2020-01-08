package cz.cvut.kozlovsky.network;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageHandler<T> extends Remote {

    void receiveMessage(T message) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException;

    void sendMessage(T message) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException;

}
