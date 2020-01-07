package cz.cvut.kozlovsky.topology;

import java.rmi.RemoteException;

public interface TopologyMessage {

    int getOriginId() throws RemoteException;

    String getOriginIpAddress() throws RemoteException;

    int getOriginPort() throws RemoteException;

    TopologyMessagePurpose getPurpose() throws RemoteException;
}
