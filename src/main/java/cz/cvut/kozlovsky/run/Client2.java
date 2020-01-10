package cz.cvut.kozlovsky.run;

import cz.cvut.kozlovsky.model.NodeImpl;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client2 {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, AlreadyBoundException {

        final int id = 103;
        final String ipAddress = "127.0.0.1";
        final int port = 2113;
        final String name = "Tomos";
        final String remoteAddress = "127.0.0.1";
        final int remotePort = 2110;

        NodeImpl nodeImpl = new NodeImpl(id, ipAddress, port, name, remoteAddress, remotePort);
        //nodeImpl.startChatting();

    }

}
