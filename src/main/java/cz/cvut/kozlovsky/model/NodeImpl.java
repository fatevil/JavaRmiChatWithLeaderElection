package cz.cvut.kozlovsky.model;

import cz.cvut.kozlovsky.chat.ChatConsole;
import cz.cvut.kozlovsky.network.EstablishedNetwork;
import cz.cvut.kozlovsky.network.EstablishedNetworkImpl;
import cz.cvut.kozlovsky.network.StatusCheck;
import cz.cvut.kozlovsky.topology.NodeTopologyHandler;
import cz.cvut.kozlovsky.topology.NodeTopologyHandlerImpl;
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
import java.util.concurrent.TimeUnit;

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

    private final NodeTopologyHandler nodeTopologyHandler;
    private EstablishedNetwork establishedNetwork;
    private ChatConsole chatConsole;

    /**
     * Constructor for leaders.s
     */
    public NodeImpl(int id, String ipAddress, int port, String nickname) throws RemoteException, MalformedURLException, AlreadyBoundException, NotBoundException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nickname = nickname;
        this.nodeTopologyHandler = new NodeTopologyHandlerImpl(this);
        createTopologyHandlerEndpoint();

        createEstablishedNetwork(true);
        this.chatConsole = new ChatConsole(establishedNetwork, this);
        this.chatConsole.startChatting();
    }

    /**
     * Constructor for connecting to existing node.
     */
    public NodeImpl(int id, String ipAddress, int port, String nickname, String remoteAddress, int remotePort) throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nickname = nickname;
        this.nodeTopologyHandler = new NodeTopologyHandlerImpl(this);
        createTopologyHandlerEndpoint();

        joinEstablishedNetwork(remoteAddress, remotePort, true);
        this.chatConsole = new ChatConsole(establishedNetwork, this);
        this.chatConsole.startChatting();
    }

    public void createEstablishedNetwork(boolean assignNeigbours) throws RemoteException, MalformedURLException, AlreadyBoundException {
        log.info("Register NetworkTracker at: " + "//" + this.getIpAddress() + ":" + this.getPort() + "/NetworkTracker");
        this.establishedNetwork = new EstablishedNetworkImpl(this, assignNeigbours);

        final Registry registry = LocateRegistry.getRegistry(this.getPort());
        registry.bind("NetworkTracker", this.establishedNetwork);
    }

    public void joinEstablishedNetwork(String remoteAddress, int remotePort, boolean assignNeighbours) throws RemoteException, NotBoundException, MalformedURLException {
        log.info("Lookup NetworkTracker at: " + "//" + remoteAddress + ":" + remotePort + "/NetworkTracker");

        establishedNetwork = (EstablishedNetwork) Naming.lookup("//" + remoteAddress + ":" + remotePort + "/NetworkTracker");
        establishedNetwork.acceptMember(this, assignNeighbours);
    }

    private void createTopologyHandlerEndpoint() throws RemoteException, MalformedURLException, AlreadyBoundException {
        log.info("Create TopologyHandler endpoint at: " + "//" + this.getIpAddress() + ":" + this.getPort() + "/TopologyHandler");

        final Registry registry = LocateRegistry.createRegistry(this.getPort());
        registry.bind("NodeTopologyHandler", this.nodeTopologyHandler);
    }

    @Override
    public void fixNetwork() throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        nodeTopologyHandler.requestNetworkFixed();
    }

    public void reassignChatConsole() {
        chatConsole.setEstablishedNetwork(establishedNetwork);
    }

    @Override
    public boolean isConnectedToNetwork() throws RemoteException {
        return StatusCheck.isAvaliable(establishedNetwork, 50, TimeUnit.MILLISECONDS);
    }

}

