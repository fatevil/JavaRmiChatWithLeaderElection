package cz.cvut.kozlovsky.communication;

import lombok.AllArgsConstructor;

import java.rmi.RemoteException;
import java.util.concurrent.*;

public enum StatusCheck {
    INSTANCE; // singleton

    /**
     * Check availability of network node.
     *
     * @param reachable
     * @param timeout
     * @param timeUnit
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static void checkAvailability(Reachable reachable, int timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executors = Executors.newSingleThreadExecutor();

        // see if node is still available
        Future<Boolean> f = executors.submit(new StatusCheckJob(reachable));
        f.get(timeout, timeUnit);
    }

    @AllArgsConstructor
    private static class StatusCheckJob implements Callable<Boolean> {
        private Reachable reachable;

        @Override
        public Boolean call() {
            try {
                reachable.touch();
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }
    }
}

