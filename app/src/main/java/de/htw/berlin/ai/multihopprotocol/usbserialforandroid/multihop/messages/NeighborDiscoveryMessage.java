package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

public class NeighborDiscoveryMessage extends MultihopMessage {

    public NeighborDiscoveryMessage(String payload, int messageID, int TTL, int hoppedNodes) {
        super(payload, messageID, TTL, hoppedNodes);
        code = "DISC";
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes;
    }
}
