package cz.cvut.kozlovsky.topology;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface LeaderElectionStrategy {

    void startElection() throws RemoteException, MalformedURLException, NotBoundException;

    NodeTopologyHandler getNodeTopologyHandler();

    void receiveMessage(TopologyMessage message) throws RemoteException, MalformedURLException, NotBoundException;

}
