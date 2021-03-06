package cz.cvut.kozlovsky.run;

import cz.cvut.kozlovsky.model.NodeImpl;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Master {

    public static void main(String[] args) throws AlreadyBoundException, RemoteException, MalformedURLException, NotBoundException {

        final int id = 100;
        final String ipAddress = "127.0.0.1";
        final int port = 2110;
        final String name = "Markos";

        NodeImpl nodeImpl = new NodeImpl(id, ipAddress, port, name);

        Thread thread = new Thread(() -> {

            final int id1 = 101;
            final String ipAddress1 = "127.0.0.1";
            final int port1 = 2111;
            final String name1 = "Bobek";
            final String remoteAddress = "127.0.0.1";
            final int remotePort = 2110;

            try {
                NodeImpl nodeImpl1 = new NodeImpl(id1, ipAddress1, port1, name1, remoteAddress, remotePort);
            } catch (RemoteException | NotBoundException | MalformedURLException | AlreadyBoundException e) {
                e.printStackTrace();
            }
        });

//        thread.start();


    }
}
