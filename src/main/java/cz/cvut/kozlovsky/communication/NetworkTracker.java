package cz.cvut.kozlovsky.communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NetworkTracker extends Remote {

    void acceptMember(Node newComer) throws RemoteException;

    void destributeChatMessage(String message) throws RemoteException;

}
