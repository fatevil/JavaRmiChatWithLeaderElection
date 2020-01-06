package cz.cvut.kozlovsky.communication;

import cz.cvut.kozlovsky.chat.ChatConsole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

@Log
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"id", "ipAddress", "port", "nickname"})
public class NodeImpl extends UnicastRemoteObject implements Node {

    @EqualsAndHashCode.Include
    private final int id;
    private final String ipAddress;
    private final int port;
    private final String nickname;

    private NetworkTracker networkTracker;
    private boolean isLeader = false;

    private ChatConsole chatConsole;

    public NodeImpl(int id, String ipAddress, int port, String nickname) throws RemoteException, MalformedURLException, AlreadyBoundException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nickname = nickname;

        createNetwork();
        this.chatConsole = new ChatConsole(networkTracker, this);
    }

    public NodeImpl(int id, String ipAddress, int port, String nickname, String remoteAddress, int remotePort) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nickname = nickname;

        joinNetwork(remoteAddress, remotePort);
        this.chatConsole = new ChatConsole(networkTracker, this);
    }

    private void createNetwork() throws RemoteException, MalformedURLException, AlreadyBoundException {
        log.info("Register NetworkTracker at: " + "//" + this.getIpAddress() + ":" + this.getPort() + "/NetworkTracker");

        this.isLeader = true;
        this.networkTracker = new NetworkTrackerImpl(this);

        final Registry registry = LocateRegistry.createRegistry(this.getPort());
        registry.bind("NetworkTracker", this.networkTracker);
    }

    public void joinNetwork(String remoteAddress, int remotePort) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        log.info("Lookup NetworkTracker at: " + "//" + remoteAddress + ":" + remotePort + "/NetworkTracker");

        final Registry remoteRegistry = LocateRegistry.getRegistry(remotePort);
        networkTracker = (NetworkTracker) remoteRegistry.lookup("NetworkTracker");
        networkTracker.acceptMember(this);
    }

    @Override
    public void receiveChatMessage(String message) {
        chatConsole.receiveMessage(message);
    }

    public void startChatting() throws RemoteException {
        chatConsole.startChatting();
    }
}

