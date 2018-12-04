package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class FixedAddressMessage extends MultihopMessage {

    public FixedAddressMessage(Address fixedAddress, int messageID, int TTL, int hoppedNodes) {
        super(Integer.toString(fixedAddress.getAddress()), messageID, TTL, hoppedNodes);
        code = "ADDR";
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes + "," + payload;
    }
}
