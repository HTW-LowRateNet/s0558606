package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class CoordinatorAliveMessage extends MultihopMessage {

    public static final String CODE = "ALIV";

    public CoordinatorAliveMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        super(payload, TTL, hoppedNodes, originalSourceAddress, targetAddress);
        code = CODE;
    }

    public CoordinatorAliveMessage(String message) throws NumberFormatException {
        super(message);
    }
}
