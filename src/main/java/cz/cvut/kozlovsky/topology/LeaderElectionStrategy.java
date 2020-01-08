package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.NodeStub;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface LeaderElectionStrategy {

    void startElection() throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException;

    NodeTopologyHandler getNodeTopologyHandler();

    void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException;

}
