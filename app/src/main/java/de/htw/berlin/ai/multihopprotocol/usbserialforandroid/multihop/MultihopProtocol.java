package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.NetworkMessageListener;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.TransceiverDevice;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.AddressProvider;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.CoordinatorAliveMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.CoordinatorDiscoveryMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.FixedAddressMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.MultihopMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.NeighborDiscoveryMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.TextMessage;

public class MultihopProtocol {

    enum ProtocolState {
        COORDINATOR_DISCOVERY, SELF_COORDINATOR, COORDINATOR_KNOWN;
    }

    private boolean coordinator;
    private boolean running;

    AddressProvider addressProvider = new AddressProvider();

    TransceiverDevice transceiverDevice;

    NetworkMessageListener currentNetworkMessageListener;

    public MultihopProtocol(TransceiverDevice transceiverDevice) {
        this.transceiverDevice = transceiverDevice;
        running = true;
        coordinator = false;
    }

    public void start() {
        initNetwork();

        startMessageHandling();

        while (running) {
            if (coordinator) {
                sendCoordinatorKeepAlive();
            } else {
                // TODO
            }
        }
    }


    private void initNetwork() {
        startDiscovery();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        transceiverDevice.removeListener(currentNetworkMessageListener);
        decideBecomingCoordinator();
    }

    private void decideBecomingCoordinator() {
        if (addressProvider.getFixedAddresses().size() == 0) {
            coordinator = true;
        } else {
            coordinator = false;
        }
    }

    public void startDiscovery() {
        currentNetworkMessageListener = stringMessage -> {
            MultihopMessage message = new MultihopMessage(stringMessage);
            if (message.getCode().equals(NeighborDiscoveryMessage.CODE)) {
                NeighborDiscoveryMessage neighborDiscoveryMessage = new NeighborDiscoveryMessage(stringMessage);
                Address neighborAddress = neighborDiscoveryMessage.getNeighborAddress();
                addressProvider.addFixedAddress(neighborAddress);
            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);

        NeighborDiscoveryMessage coordinatorDiscoveryMessage = new NeighborDiscoveryMessage("", 10, 0);

        transceiverDevice.setAddress(addressProvider.getBroadcastAddress().getAddress());
        transceiverDevice.send(coordinatorDiscoveryMessage.createStringMessage());
    }


    private void sendCoordinatorKeepAlive() {
        CoordinatorAliveMessage coordinatorAliveMessage = new CoordinatorAliveMessage("", 10, 0);
        transceiverDevice.setAddress(addressProvider.getBroadcastAddress().getAddress());
        transceiverDevice.send(coordinatorAliveMessage.createStringMessage());
    }

    private void startMessageHandling() {
        currentNetworkMessageListener = stringMessage -> {
            MultihopMessage multihopMessage = new MultihopMessage(stringMessage);

            switch (multihopMessage.getCode()) {
                case CoordinatorAliveMessage.CODE:
                    CoordinatorAliveMessage coordinatorAliveMessage = new CoordinatorAliveMessage(stringMessage);
                    handleCoordinatorAliveMessage(coordinatorAliveMessage);
                    break;
                case CoordinatorDiscoveryMessage.CODE:
                    CoordinatorDiscoveryMessage coordinatorDiscoveryMessage = new CoordinatorDiscoveryMessage(stringMessage);
                    handleCoordinatorDiscoveryMessage(coordinatorDiscoveryMessage);
                    break;
                case FixedAddressMessage.CODE:
                    FixedAddressMessage fixedAddressMessage = new FixedAddressMessage(stringMessage);
                    handleFixedAddressMessage(fixedAddressMessage);
                    break;
                case TextMessage.CODE:
                    TextMessage textMessage = new TextMessage(stringMessage);
                    handleTextMessage(textMessage);
                    break;
                case NeighborDiscoveryMessage.CODE:
                    NeighborDiscoveryMessage neighborDiscoveryMessage = new NeighborDiscoveryMessage(stringMessage);
                    handleNeighborDiscoveryMessage(neighborDiscoveryMessage);
                    break;
                default:
                    break;
            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);
    }

    private void handleCoordinatorAliveMessage(CoordinatorAliveMessage message) {

    }

    private void handleCoordinatorDiscoveryMessage(CoordinatorDiscoveryMessage message) {

    }

    private void handleFixedAddressMessage(FixedAddressMessage message) {

    }

    private void handleTextMessage(TextMessage message) {

    }

    private void handleNeighborDiscoveryMessage(NeighborDiscoveryMessage message) {

    }


    public void stop() {
        running = false;
    }
}
