package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address;

import java.util.Objects;

public class Address {

    private static int nodeIDCounter = 0;

    private int nodeID;
    private int address;

    public Address(int address) {
        this.address = address;
        nodeID = nodeIDCounter++;
    }

    public int getNodeID() {
        return nodeID;
    }

    public int getAddress() {
        return address;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address1 = (Address) o;
        return address == address1.address;
    }

    @Override
    public int hashCode() {

        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "Address{" +
                "nodeID=" + nodeID +
                ", address=" + address +
                '}';
    }
}