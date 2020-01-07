package cz.cvut.kozlovsky.communication;

import cz.cvut.kozlovsky.chat.ChatConsole;
import cz.cvut.kozlovsky.network.EstablishedNetwork;
import cz.cvut.kozlovsky.network.EstablishedNetworkImpl;
import cz.cvut.kozlovsky.topology.Neighbours;
import cz.cvut.kozlovsky.topology.NeighboursImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
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

    private final Neighbours neighbours;
    private EstablishedNetwork establishedNetwork;
    private ChatConsole chatConsole;

    /**
     * Constructor for leaders.
     */
    public NodeImpl(int id, String ipAddress, int port, String nickname) throws RemoteException, MalformedURLException, AlreadyBoundException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nickname = nickname;
        this.neighbours = new NeighboursImpl(this);

        createEstablishedNetwork();
        this.chatConsole = new ChatConsole(establishedNetwork, this);
        this.chatConsole.startChatting();
    }

    /**
     * Constructor for connecting to existing node.
     */
    public NodeImpl(int id, String ipAddress, int port, String nickname, String remoteAddress, int remotePort) throws RemoteException, NotBoundException, MalformedURLException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nickname = nickname;
        this.neighbours = new NeighboursImpl(this);

        joinEstablishedNetwork(remoteAddress, remotePort);
        this.chatConsole = new ChatConsole(establishedNetwork, this);
        this.chatConsole.startChatting();
    }

    private void createEstablishedNetwork() throws RemoteException, MalformedURLException, AlreadyBoundException {
        log.info("Register NetworkTracker at: " + "//" + this.getIpAddress() + ":" + this.getPort() + "/NetworkTracker");
        this.establishedNetwork = new EstablishedNetworkImpl(this);

        final Registry registry = LocateRegistry.createRegistry(this.getPort());
        registry.bind("NetworkTracker", this.establishedNetwork);
    }

    private void joinEstablishedNetwork(String remoteAddress, int remotePort) throws RemoteException, NotBoundException, MalformedURLException {
        log.info("Lookup NetworkTracker at: " + "//" + remoteAddress + ":" + remotePort + "/NetworkTracker");

        establishedNetwork = (EstablishedNetwork) Naming.lookup("//" + remoteAddress + ":" + remotePort + "/NetworkTracker");
        establishedNetwork.acceptMember(this);
    }

    private void createMessageEndpoint() throws RemoteException, MalformedURLException, AlreadyBoundException {
        log.info("Create Message endpoint at: " + "//" + this.getIpAddress() + ":" + this.getPort() + "/Message");

        final Registry registry = LocateRegistry.createRegistry(this.getPort());
        registry.bind("Message", this.neighbours);
    }

    @Override
    public void fixNetwork() throws RemoteException {
        neighbours.getNewLeader();
    }

}

