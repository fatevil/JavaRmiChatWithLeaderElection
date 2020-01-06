package cz.cvut.kozlovsky.chat;

import cz.cvut.kozlovsky.communication.NetworkTracker;
import cz.cvut.kozlovsky.communication.Node;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

@Log
public class ChatConsole {

    private boolean receiving;
    private Node node;
    private NetworkTracker networkTracker;

    public ChatConsole(NetworkTracker networkTracker, Node node) throws RemoteException {
        this.networkTracker = networkTracker;
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

    public void sendMessage(String message) throws RemoteException {
        final String prefix = "==== " + node.getNickname() + ": ";

        networkTracker.destributeChatMessage(prefix + message);
    }

}
