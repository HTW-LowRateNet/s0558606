package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class AcknowledgeFixedAddressMessage extends MultihopMessage {

    public static final String CODE = "AACK";

    public AcknowledgeFixedAddressMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        super(payload, TTL, hoppedNodes, originalSourceAddress, targetAddress);
        code = CODE;
    }

    public AcknowledgeFixedAddressMessage(String message) throws NumberFormatException {
        super(message);
    }

}
