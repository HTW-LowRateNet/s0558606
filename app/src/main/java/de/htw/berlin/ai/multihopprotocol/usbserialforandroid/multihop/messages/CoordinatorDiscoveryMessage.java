package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

public class CoordinatorDiscoveryMessage extends MultihopMessage {

    public CoordinatorDiscoveryMessage(String payload, int messageID, int TTL, int hoppedNodes) {
        super(payload, messageID, TTL, hoppedNodes);
        code = "KDIS";
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes;
    }
}
