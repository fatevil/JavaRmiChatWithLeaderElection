package cz.cvut.kozlovsky;

import cz.cvut.kozlovsky.communication.NodeImpl;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Master {

    public static void main(String[] args) throws AlreadyBoundException, RemoteException, MalformedURLException, UnknownHostException {
        //System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        System.setProperty("sun.rmi.transport.connectionTimeout", "1");

        final int id = 100;
        final String ipAddress = "127.0.0.1";
        final int port = 2110;
        final String name = "Markos";

        NodeImpl nodeImpl = new NodeImpl(id, ipAddress, port, name);
        nodeImpl.startChatting();

        Thread thread = new Thread() {
            public void run() {

                final int id = 101;
                final String ipAddress = "127.0.0.1";
                final int port = 2111;
                final String name = "Bobek";
                final String remoteAddress = "127.0.0.1";
                final int remotePort = 2110;

                try {
                    NodeImpl nodeImpl = new NodeImpl(id, ipAddress, port, name, remoteAddress, remotePort);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (AlreadyBoundException e) {
                    e.printStackTrace();
                }
            }
        };

//        thread.start();


    }
}
