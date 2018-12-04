package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import java.util.Random;

public class MultihopMessage {

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

    public MultihopMessage(String payload, int TTL, int hoppedNodes) {
        this.payload = payload;
        this.TTL = TTL;
        this.hoppedNodes = hoppedNodes;

        messageID = generateMessageID();
    }

    private int generateMessageID() {
        Random random = new Random();
        return random.nextInt();
    }

    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + payload;
    }

    public String getCode() {
        return code;
    }

    public String getPayload() {
        return payload;
    }

    public int getMessageID() {
        return messageID;
    }

    public int getTTL() {
        return TTL;
    }

    public int getHoppedNodes() {
        return hoppedNodes;
    }
}
