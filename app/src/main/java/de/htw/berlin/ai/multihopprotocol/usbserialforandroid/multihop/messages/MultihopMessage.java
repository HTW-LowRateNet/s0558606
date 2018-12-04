package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

public abstract class MultihopMessage {

    protected String code;
    protected String payload;
    protected int messageID;
    protected int TTL;
    protected int hoppedNodes;

    public MultihopMessage() {

    }

    public MultihopMessage(String message) throws NumberFormatException {
        String[] strings = message.split(",");

        code = strings[3];
        messageID = Integer.parseInt(strings[4]);
        TTL = Integer.parseInt(strings[5]);
        hoppedNodes = Integer.parseInt(strings[6]);
        payload = strings[7];
    }

    public MultihopMessage(String payload, int messageID, int TTL, int hoppedNodes) {
        this.payload = payload;
        this.messageID = messageID;
        this.TTL = TTL;
        this.hoppedNodes = hoppedNodes;
    }

    public abstract String createStringMessage();
}
