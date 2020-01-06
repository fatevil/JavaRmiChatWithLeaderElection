package cz.cvut.kozlovsky;

import cz.cvut.kozlovsky.communication.NodeImpl;
import cz.cvut.kozlovsky.exception.NotLeaderException;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    public static void main(String[] args) throws NotLeaderException, RemoteException, MalformedURLException, NotBoundException, UnknownHostException, AlreadyBoundException {
        //System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        System.setProperty("sun.rmi.transport.connectionTimeout", "200");

        final int id = 101;
        final String ipAddress = "127.0.0.1";
        final int port = 2111;
        final String name = "Bobek";
        final String remoteAddress = "127.0.0.1";
        final int remotePort = 2110;

        NodeImpl nodeImpl = new NodeImpl(id, ipAddress, port, name, remoteAddress, remotePort);
        nodeImpl.startChatting();

    }

}
