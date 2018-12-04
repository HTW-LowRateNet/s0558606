package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

public class CoordinatorDiscoveryMessage extends MultihopMessage {

    public static final String CODE = "KDIS";

    public CoordinatorDiscoveryMessage(String payload, int TTL, int hoppedNodes) {
        super(payload, TTL, hoppedNodes);
        code = CODE;
    }

    public CoordinatorDiscoveryMessage(String message) throws NumberFormatException {
        super(message);
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes;
    }
}
