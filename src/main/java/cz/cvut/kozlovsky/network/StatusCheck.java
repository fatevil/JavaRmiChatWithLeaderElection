package cz.cvut.kozlovsky.network;

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
    public static boolean isAvaliable(Reachable reachable, int timeout, TimeUnit timeUnit) {
        ExecutorService executors = Executors.newSingleThreadExecutor();

        // see if node is still available
        Future<Boolean> f = executors.submit(new StatusCheckJob(reachable));
        try {
            f.get(timeout, timeUnit);

            return true;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            return false;
        }
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

