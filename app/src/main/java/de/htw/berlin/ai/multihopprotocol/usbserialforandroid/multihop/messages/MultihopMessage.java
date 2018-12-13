package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import java.util.Random;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class MultihopMessage {

    protected String code;
    protected String payload;
    protected String messageID;
    protected int TTL;
    protected int hoppedNodes;
    protected Address originalSourceAddress;
    protected Address targetAddress;

    public MultihopMessage() {

    }

    public MultihopMessage(String message) throws NumberFormatException {
        message = message.replace('\r', ' ').replace('\n', ' ');
        String[] strings = message.split(",");

        code = strings[3].trim();
        messageID = strings[4].trim();
        TTL = Integer.parseInt(strings[5].trim());
        hoppedNodes = Integer.parseInt(strings[6].trim());
        originalSourceAddress = new Address(Integer.parseInt(strings[7].trim()));
        targetAddress = new Address(Integer.parseInt(strings[8].trim()));
        if (strings.length == 10)
            payload = strings[9];
        else
            payload = "";
    }

    public MultihopMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        this.payload = payload;
        this.TTL = TTL;
        this.hoppedNodes = hoppedNodes;
        this.originalSourceAddress = originalSourceAddress;
        this.targetAddress = targetAddress;

        messageID = generateMessageID();
    }

    private String generateMessageID() {
        Random random = new Random();
        return String.valueOf(random.nextInt(Integer.MAX_VALUE));
    }

    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + originalSourceAddress.getFourLetterHexAddress() + "," + targetAddress.getFourLetterHexAddress() + "," + payload;
    }

    public String getCode() {
        return code;
    }

    public String getPayload() {
        return payload;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getTTL() {
        return TTL;
    }

    public int getHoppedNodes() {
        return hoppedNodes;
    }

    public Address getOriginalSourceAddress() {
        return originalSourceAddress;
    }

    public Address getTargetAddress() {
        return targetAddress;
    }
}
