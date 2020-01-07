package cz.cvut.kozlovsky.topology;

import cz.cvut.kozlovsky.model.Node;
import cz.cvut.kozlovsky.model.NodeStub;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Log
@Getter
public class NodeTopologyHandlerImpl extends UnicastRemoteObject implements NodeTopologyHandler {

    private NodeStub myself;
    private NodeStub leftNeighbour;
    private NodeStub rightNeighbour;

    @Builder
    public NodeTopologyHandlerImpl(Node myself) throws RemoteException {
        this.myself = new NodeStub(myself);
    }

    /**
     * Fix the ring network topology.
     */
    private void fixRingTopology() {

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
        fixRingTopology();
        NodeStub node = electNewLeader();

        return node;
    }

    @Override
    public void setLeftNeighbour(Node node) throws RemoteException {
        leftNeighbour = new NodeStub(node);
    }

    @Override
    public void setRightNeighbour(Node node) throws RemoteException {
        rightNeighbour = new NodeStub(node);
    }

    @Override
    public void setMyself(Node node) throws RemoteException {
        myself = new NodeStub(node);
    }

    @SneakyThrows
    @Override
    public String toString() {
        return "NeighboursImpl{meId=" + myself.getId() + ", lId=" + leftNeighbour.getId() + ", rId=" + rightNeighbour.getId() + '}';
    }


}
