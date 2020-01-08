package cz.cvut.kozlovsky.topology;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TopologyMessage extends Remote {

    int getOriginId() throws RemoteException;

    String getOriginIpAddress() throws RemoteException;

    int getOriginPort() throws RemoteException;

    TopologyMessagePurpose getPurpose() throws RemoteException;

    TopologyMessageDirection getDirection() throws RemoteException;

    int getPhase() throws RemoteException;

    int getDistanceSoFar() throws RemoteException;

    void setDistanceSoFar(int distanceSoFar) throws RemoteException;

    void setDirection(TopologyMessageDirection direction) throws RemoteException;

    boolean getChangedDirection() throws RemoteException;

    void setChangedDirection(boolean changedDirection) throws RemoteException;
}
