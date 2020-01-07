package cz.cvut.kozlovsky.network;

import cz.cvut.kozlovsky.model.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface EstablishedNetwork extends Remote, Reachable {

    void acceptMember(Node newComer) throws RemoteException;

    List<Node> getActiveNodes() throws RemoteException;

}
