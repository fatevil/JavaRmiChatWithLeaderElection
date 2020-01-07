package cz.cvut.kozlovsky.network;

import java.rmi.RemoteException;

public interface Reachable {

    /**
     * Do nothing. Is useful for determining availability.
     */
    default void touch() throws RemoteException {
        assert true;
    }
}
