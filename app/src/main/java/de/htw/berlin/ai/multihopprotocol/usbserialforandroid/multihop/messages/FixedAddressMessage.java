package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class FixedAddressMessage extends MultihopMessage {

    public static final String CODE = "ADDR";

    public FixedAddressMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        super(payload, TTL, hoppedNodes, originalSourceAddress, targetAddress);
        code = CODE;
    }

    public FixedAddressMessage(String message) throws NumberFormatException {
        super(message);
        code = CODE;
    }

}
