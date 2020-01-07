package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.communication.Node;
import cz.cvut.kozlovsky.dto.NodeStub;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Log
@Getter
public class NeighboursImpl extends UnicastRemoteObject implements Neighbours {

    private NodeStub myself;
    private NodeStub left;
    private NodeStub right;

    @Builder
    public NeighboursImpl(Node myself) throws RemoteException {
        this.myself = new NodeStub(myself);
    }

    /**
     * Fix the ring network topology.
     */
    private void fixTopology() {

    }

    /**
     * Use the Hirschberg–Sinclair algorithm for electing new leder. Elected leader will create new Network.
     */
    private NodeStub electNewLeader() {
        return null;
    }

    /**
     * Fixes network topology (connects disconnected nodes) and participates in electing new leader.
     *
     * @return leader stub
     */
    public NodeStub getNewLeader() {
        fixTopology();
        NodeStub node = electNewLeader();

        return node;
    }

    @Override
    public void setLeft(Node node) throws RemoteException {
        left = new NodeStub(node);
    }

    @Override
    public void setRight(Node node) throws RemoteException {
        right = new NodeStub(node);
    }

    @Override
    public void setMyself(Node node) throws RemoteException {
        myself = new NodeStub(node);
    }

    @SneakyThrows
    @Override
    public String toString() {
        return "NeighboursImpl{meId=" + myself.getId() + ", lId=" + left.getId() + ", rId=" + right.getId() + '}';
    }


}
