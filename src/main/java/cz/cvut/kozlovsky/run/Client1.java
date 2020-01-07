package cz.cvut.kozlovsky.run;

import cz.cvut.kozlovsky.model.NodeImpl;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client1 {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {

        final int id = 102;
        final String ipAddress = "127.0.0.1";
        final int port = 2112;
        final String name = "Pepa";
        final String remoteAddress = "127.0.0.1";
        final int remotePort = 2110;

        NodeImpl nodeImpl = new NodeImpl(id, ipAddress, port, name, remoteAddress, remotePort);
        //nodeImpl.startChatting();

    }

}
