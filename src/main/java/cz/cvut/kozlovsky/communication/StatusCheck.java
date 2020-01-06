package cz.cvut.kozlovsky.communication;

import lombok.AllArgsConstructor;

import java.rmi.RemoteException;
import java.util.concurrent.*;

public enum StatusCheck {
    INSTANCE; // singleton

    /**
     * Check availability of network node.
     *
     * @param touchable
     * @param timeout
     * @param timeUnit
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static void checkAvailability(Touchable touchable, int timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executors = Executors.newSingleThreadExecutor();

        // see if node is still available
        Future<Boolean> f = executors.submit(new StatusCheckJob(touchable));
        f.get(timeout, timeUnit);
    }

    @AllArgsConstructor
    private static class StatusCheckJob implements Callable<Boolean> {
        private Touchable touchable;

        @Override
        public Boolean call() {
            try {
                touchable.touch();
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }
    }
}

