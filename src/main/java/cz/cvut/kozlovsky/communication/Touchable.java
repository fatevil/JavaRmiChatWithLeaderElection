package cz.cvut.kozlovsky.communication;

import java.rmi.RemoteException;

public interface Touchable {

    /**
     * Do nothing. Is useful for determining availability.
     */
    default void touch() throws RemoteException {
        assert true;
    }
}
