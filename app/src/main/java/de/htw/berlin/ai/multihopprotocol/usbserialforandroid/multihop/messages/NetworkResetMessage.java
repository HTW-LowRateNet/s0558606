package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;

public class NetworkResetMessage extends MultihopMessage {

    public static final String CODE = "NRST";

    public NetworkResetMessage(String payload, int TTL, int hoppedNodes, Address originalSourceAddress, Address targetAddress) {
        super(payload, TTL, hoppedNodes, originalSourceAddress, targetAddress);
        code = CODE;
    }

    public NetworkResetMessage(String message) throws NumberFormatException {
        super(message);
    }

}