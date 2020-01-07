package cz.cvut.kozlovsky.network;

import cz.cvut.kozlovsky.communication.Node;
import cz.cvut.kozlovsky.network.Reachable;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EstablishedNetwork extends Remote, Reachable {

    void acceptMember(Node newComer) throws RemoteException;

    void destributeChatMessage(String message) throws RemoteException;

}
