package cz.cvut.kozlovsky.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Log
class NetworkTrackerImpl extends UnicastRemoteObject implements NetworkTracker {

    private Node leader;
    private final Map<Integer, Node> nodes = new ConcurrentHashMap<>();

    @Builder
    public NetworkTrackerImpl(NodeImpl leader) throws RemoteException {
        super();
        log.info(String.format("Creating network with leader ID ", leader.getId()));
        this.nodes.put(leader.getId(), leader);
        this.leader = leader;
    }

    private void addNode(Node newComer) throws RemoteException {
        nodes.put(newComer.getId(), newComer);
    }

    @Override
    public void acceptMember(Node newComer) throws RemoteException {
        log.info(String.format("Accepting node with ID ", newComer.getId()));
        addNode(newComer);
    }

    @Override
    public void destributeChatMessage(String chatMessage) throws RemoteException {
        checkEveryone();

        nodes.forEach((id, node) -> {
            try {
                node.receiveChatMessage(chatMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void checkEveryone() {
        nodes.forEach((id, node) -> {
            try {
                StatusCheck.checkAvailability(node, 50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                nodes.remove(id);
                log.info("Node with ID " + id + " not reachable! REMOVED FROM CHAT.");
            }
        });
    }
}