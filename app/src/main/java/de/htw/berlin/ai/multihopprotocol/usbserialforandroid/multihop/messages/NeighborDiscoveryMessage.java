package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;
import timber.log.Timber;

public class NeighborDiscoveryMessage extends MultihopMessage {

    public static final String CODE = "DISC";

    public NeighborDiscoveryMessage(String payload, int TTL, int hoppedNodes) {
        super(payload, TTL, hoppedNodes);
        code = CODE;
    }

    public NeighborDiscoveryMessage(String message) {
        super(message);
    }

    @Override
    public String createStringMessage() {
        return code + "," + messageID + "," + TTL + "," + hoppedNodes;
    }

    public Address getNeighborAddress() {
        try {
            return new Address(Integer.parseInt(payload));
        } catch (NumberFormatException e) {
            Timber.e(e, "Error parsing neighbor address");
            return null;
        }
    }
}
