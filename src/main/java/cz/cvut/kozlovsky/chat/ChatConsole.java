package cz.cvut.kozlovsky.chat;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.network.EstablishedNetwork;
import cz.cvut.kozlovsky.network.MessageHandler;
import cz.cvut.kozlovsky.network.StatusCheck;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log
@Data
public class ChatConsole extends UnicastRemoteObject implements MessageHandler<String> {

    private boolean receiving;
    private Node node;
    private EstablishedNetwork establishedNetwork;

    public ChatConsole(EstablishedNetwork establishedNetwork, Node node) throws RemoteException {
        this.establishedNetwork = establishedNetwork;
        this.node = node;
    }

    /**
     * Joins the chat.
     * <p>
     * Keywords for interacting with the console: EXIT, PAUSE, CONTINUE.
     *
     * @throws RemoteException
     */
    public void startChatting() throws RemoteException, AlreadyBoundException, NotBoundException, MalformedURLException {
        log.info("Just joining the chat with name: " + node.getNickname());

        receiving = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        sendMessage("Hi to everyone, glad to be joining the chatter! All ready for gossip!");
        while (true) {
            String line;
            try {
                line = br.readLine();

                switch (line) {
                    case "EXIT":
                        System.exit(0);
                    case "PAUSE":
                        receiving = false;
                        System.out.println("Stopped receiving chat. Use CONTINUE to get back to discussion.");
                        break;
                    case "CONTINUE":
                        receiving = true;
                        System.out.println("Back in business my friend.");
                        break;
                    case "REGISTRY":
                        final Registry registry = LocateRegistry.getRegistry(node.getPort());
                        System.out.println("== REGISTRY ==");
                        ;
                        for (String s : registry.list()) {
                            System.out.println(s);
                        }
                        System.out.println("== ==");
                        break;
                    default:
                        if (receiving) {
                            sendMessage(line);
                        }
                        break;
                }

            } catch (IOException | NotBoundException | AlreadyBoundException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Prints chat message to standard output.
     *
     * @param message
     */
    public void receiveMessage(String message) {
        if (receiving) {
            System.out.println(message);
        }
    }

    /**
     * Makes copy of currently active nodes (their proxy objects) and distributes given text with chat prefix.
     * <p>
     * If the connection to leader is broken, participate in fixing it.
     *
     * @param text
     * @throws RemoteException
     */
    public void sendMessage(String text) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException {
        final String prefix = "==== " + node.getNickname() + ": ";
        final String message = prefix + text;

        List<Node> nodes = null;

        // first check if master is still available
        boolean isAvailable = StatusCheck.isAvaliable(establishedNetwork, 50, TimeUnit.MILLISECONDS);
        if (!isAvailable) {
            log.severe("==== GOT DISCONNECTED FROM MASTER ==== ");
            node.fixNetwork();
        }

        // then make copy of the currently active nodes
        nodes = establishedNetwork.getActiveNodes();

        for (Node n : nodes) {
            n.getChatConsole().receiveMessage(message);
        }
    }
}
