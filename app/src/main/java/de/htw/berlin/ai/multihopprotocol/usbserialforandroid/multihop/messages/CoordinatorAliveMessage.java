package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

public class CoordinatorAliveMessage extends MultihopMessage {

    public static final String CODE = "ALIV";

    public CoordinatorAliveMessage(String payload, int TTL, int hoppedNodes) {
        super(payload, TTL, hoppedNodes);
        code = CODE;
    }

    public CoordinatorAliveMessage(String message) throws NumberFormatException {
        super(message);
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes;
    }
}
