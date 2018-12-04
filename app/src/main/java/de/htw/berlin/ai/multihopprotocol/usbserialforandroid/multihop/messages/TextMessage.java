package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class TextMessage extends MultihopMessage {

    public TextMessage(String message, Address targetAddress, int messageID, int TTL, int hoppedNodes) {
        super(Integer.toString(targetAddress.getAddress()) + "+" + message, messageID, TTL, hoppedNodes);
        code = "MSSG";
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + payload;
    }
}
