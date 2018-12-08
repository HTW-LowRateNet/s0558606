package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import java.util.Random;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class MultihopMessage {

    protected String code;
    protected String payload;
    protected int messageID;
    protected int TTL;
    protected int hoppedNodes;
    protected int originalSourceAddress;
    protected int targetAddress;

    public MultihopMessage() {

    }

    public MultihopMessage(String message) throws NumberFormatException {
        message = message.replace('\r', ' ').replace('\n', ' ');
        String[] strings = message.split(",");

        code = strings[3].trim();
        messageID = Integer.parseInt(strings[4].trim());
        TTL = Integer.parseInt(strings[5].trim());
        hoppedNodes = Integer.parseInt(strings[6].trim());
        originalSourceAddress = Integer.parseInt(strings[7].trim());
        targetAddress = Integer.parseInt(strings[8].trim());
        if (strings.length == 10)
            payload = strings[9];
        else
            payload = "";
    }

    public MultihopMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        this.payload = payload;
        this.TTL = TTL;
        this.hoppedNodes = hoppedNodes;
        this.originalSourceAddress = originalSourceAddress.getAddress();
        this.targetAddress = targetAddress.getAddress();

        messageID = generateMessageID();
    }

    private int generateMessageID() {
        Random random = new Random();
        return random.nextInt(Integer.MAX_VALUE);
    }

    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + originalSourceAddress + "," + targetAddress + "," + payload;
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

    public int getOriginalSourceAddress() {
        return originalSourceAddress;
    }

    public int getTargetAddress() {
        return targetAddress;
    }
}
