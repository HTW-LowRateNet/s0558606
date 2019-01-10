package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class TextMessage extends MultihopMessage {

    public static final String CODE = "MSSG";

    public TextMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        super(payload, TTL, hoppedNodes, originalSourceAddress, targetAddress);
        code = CODE;
    }

    public TextMessage(String message) throws NumberFormatException {
        super(message);
        code = CODE;
    }
}
