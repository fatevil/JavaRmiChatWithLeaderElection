package cz.cvut.kozlovsky.communication;

import lombok.AllArgsConstructor;

import java.rmi.RemoteException;
import java.util.concurrent.*;

enum StatusCheck {
    INSTANCE; // singleton

    /**
     * Check availability of network node.
     *
     * @param node
     * @param timeout
     * @param timeUnit
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    static void checkAvailability(Node node, int timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executors = Executors.newSingleThreadExecutor();

        // see if node is still available
        Future<Boolean> f = executors.submit(new StatusCheckJob(node));
        f.get(timeout, timeUnit);
    }

    @AllArgsConstructor
    private static class StatusCheckJob implements Callable<Boolean> {
        private Node node;

        @Override
        public Boolean call() {
            try {
                node.touch();
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }
    }
}

