package cz.cvut.kozlovsky.chat;

import com.sun.xml.internal.ws.wsdl.writer.document.Message;
import cz.cvut.kozlovsky.network.EstablishedNetwork;
import cz.cvut.kozlovsky.communication.Node;
import cz.cvut.kozlovsky.network.StatusCheck;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log
public class ChatConsole extends UnicastRemoteObject implements MessageHandler {

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
    public void startChatting() throws RemoteException {
        log.info("Just joining the chat with name: " + node.getNickname());

        receiving = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        sendMessage("Hi to everyone, glad to be joining the chatter! All ready gossip!");
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
                    default:
                        if (receiving) {
                            sendMessage(line);
                        }
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void receiveMessage(String message) {
        if (receiving) {
            System.out.println(message);
        }
    }

    /**
     * Hands the message to the NetworkTracker.
     * <p>
     * If the connection is broken, ask for a fix until it's fixed.
     *
     * @param message
     * @throws RemoteException
     */
    public void sendMessage(String message) throws RemoteException {
        final String prefix = "==== " + node.getNickname() + ": ";

        boolean isSend = false;
        while (!isSend) {
            try {
                // first check if master is still available
                StatusCheck.checkAvailability(establishedNetwork, 50, TimeUnit.MILLISECONDS);

                // then send message
                establishedNetwork.destributeChatMessage(prefix + message);
                isSend = true;

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.severe("==== GOT DISCONNECTED FROM MASTER ==== ");
                //e.printStackTrace();

                node.fixNetwork();
            }
        }

    }

}
