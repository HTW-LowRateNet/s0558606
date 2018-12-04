package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class FixedAddressMessage extends MultihopMessage {

    public static final String CODE = "ADDR";

    public FixedAddressMessage(Address fixedAddress, int TTL, int hoppedNodes) {
        super(Integer.toString(fixedAddress.getAddress()), TTL, hoppedNodes);
        code = CODE;
    }

    public FixedAddressMessage(String message) throws NumberFormatException {
        super(message);
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + payload;
    }
}
