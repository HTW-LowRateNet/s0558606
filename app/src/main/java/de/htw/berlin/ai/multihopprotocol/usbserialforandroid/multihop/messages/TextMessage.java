package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class TextMessage extends MultihopMessage {

    public static final String CODE = "MSSG";

    public TextMessage(String message, Address targetAddress, int TTL, int hoppedNodes) {
        super(Integer.toString(targetAddress.getAddress()) + "+" + message, TTL, hoppedNodes);
        code = CODE;
    }

    public TextMessage(String message) throws NumberFormatException {
        super(message);
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + payload;
    }
}
