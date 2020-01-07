package cz.cvut.kozlovsky.topology;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TopologyMessage extends Remote {

    int getOriginId() throws RemoteException;

    String getOriginIpAddress() throws RemoteException;

    int getOriginPort() throws RemoteException;

    TopologyMessagePurpose getPurpose() throws RemoteException;
}
