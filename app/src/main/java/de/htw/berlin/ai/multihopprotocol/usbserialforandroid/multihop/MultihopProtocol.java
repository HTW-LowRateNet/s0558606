package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

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
import timber.log.Timber;

public class MultihopProtocol {

    public enum ProtocolState {
        COORDINATOR_DISCOVERY, SELF_COORDINATOR, COORDINATOR_KNOWN;
    }

    private boolean coordinator;
    private boolean running;

    private MutableLiveData<ProtocolState> protocolState;

    AddressProvider addressProvider = new AddressProvider();

    TransceiverDevice transceiverDevice;

    NetworkMessageListener currentNetworkMessageListener;

    private Thread coordinatorThread;

    public MultihopProtocol(TransceiverDevice transceiverDevice) {
        this.transceiverDevice = transceiverDevice;
        coordinator = false;

        protocolState = new MutableLiveData<>();
    }

    public void start() {
        running = true;
        transceiverDevice.setDestinationAddress(addressProvider.getBroadcastAddress().getAddress());

        chooseTempAddressForSelf();

        initNetwork();

        startMessageHandling();

        if (coordinator) {
            startCoordinatorThread();
        } else {
        }
    }

    public void sendTextMessage(String data) {
        TextMessage textMessage = new TextMessage(data, 10, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());
        transceiverDevice.send(textMessage.createStringMessage());
    }

    private void startCoordinatorThread() {
        coordinatorThread = new Thread(new CoordinatorHandler());
        coordinatorThread.start();
    }

    private void initNetwork() {
        startCoordinatorDiscovery();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        transceiverDevice.removeListener(currentNetworkMessageListener);
        decideBecomingCoordinator();

        if (coordinator) {
            addressProvider.setSelfAddress(addressProvider.getCoordinatorAddress());
            transceiverDevice.setSelfAddress(addressProvider.getCoordinatorAddress().getAddress());
        } else {
            chooseTempAddressForSelf();
            requestFixedAddressFromCoordinator();
        }
    }

    private void chooseTempAddressForSelf() {
        Address newTemporaryAddress = addressProvider.getNewTemporaryAddress();
        addressProvider.setSelfAddress(newTemporaryAddress);
        transceiverDevice.setSelfAddress(newTemporaryAddress.getAddress());
        Timber.d("Set temporary self address to %s", newTemporaryAddress);
    }

    private void requestFixedAddressFromCoordinator() {
        FixedAddressMessage fixedAddressMessage = new FixedAddressMessage("", 10, 0, addressProvider.getSelfAddress(), addressProvider.getCoordinatorAddress());
        transceiverDevice.send(fixedAddressMessage.createStringMessage());
    }

    private void startCoordinatorDiscovery() {
        protocolState.postValue(ProtocolState.COORDINATOR_DISCOVERY);

        currentNetworkMessageListener = stringMessage -> {
            try {
                MultihopMessage message = new MultihopMessage(stringMessage);
                if (message.getCode().equals(CoordinatorAliveMessage.CODE)) {
                    addressProvider.addFixedAddress(message.getOriginalSourceAddress());
                }
            } catch (NumberFormatException e) {

            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);

        CoordinatorDiscoveryMessage coordinatorDiscoveryMessage = new CoordinatorDiscoveryMessage("", 10, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());

        transceiverDevice.send(coordinatorDiscoveryMessage.createStringMessage());
        Timber.d("CoordinatorDiscoveryMessage: %s", coordinatorDiscoveryMessage.createStringMessage());
    }

    private void decideBecomingCoordinator() {
        if (addressProvider.getFixedAddresses().size() == 0) {
            becomeCoordinator();
        } else {
            coordinator = false;
            protocolState.postValue(ProtocolState.COORDINATOR_KNOWN);
        }
    }

    private void becomeCoordinator() {
        transceiverDevice.setSelfAddress(0x0000);
        coordinator = true;
        protocolState.postValue(ProtocolState.SELF_COORDINATOR);
    }

    public void startNeighborDiscovery() {
        protocolState.postValue(ProtocolState.COORDINATOR_DISCOVERY);

        currentNetworkMessageListener = stringMessage -> {
            MultihopMessage message = new MultihopMessage(stringMessage);
            if (message.getCode().equals(NeighborDiscoveryMessage.CODE)) {
                NeighborDiscoveryMessage neighborDiscoveryMessage = new NeighborDiscoveryMessage(stringMessage);
                Address neighborAddress = neighborDiscoveryMessage.getNeighborAddress();
                addressProvider.addFixedAddress(neighborAddress);
            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);

        NeighborDiscoveryMessage coordinatorDiscoveryMessage = new NeighborDiscoveryMessage("", 10, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());

        transceiverDevice.send(coordinatorDiscoveryMessage.createStringMessage());
        Timber.d("CoordinatorDiscoveryMessage: %s", coordinatorDiscoveryMessage.createStringMessage());
    }

    private void startMessageHandling() {
        currentNetworkMessageListener = stringMessage -> {
            try {
                MultihopMessage multihopMessage = new MultihopMessage(stringMessage);
                Timber.d("New message received: %s", stringMessage);

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

            } catch (NumberFormatException e) {

            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);
    }

    private void handleCoordinatorAliveMessage(CoordinatorAliveMessage message) {

    }

    private void handleCoordinatorDiscoveryMessage(CoordinatorDiscoveryMessage message) {
        sendCoordinatorKeepAlive();
    }

    private void sendCoordinatorKeepAlive() {
        CoordinatorAliveMessage coordinatorAliveMessage = new CoordinatorAliveMessage("", 10, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());
        transceiverDevice.send(coordinatorAliveMessage.createStringMessage());
    }

    private void handleFixedAddressMessage(FixedAddressMessage message) {
        if (message.getTargetAddress().equals(addressProvider.getSelfAddress())) {
            String payload = message.getPayload();
            try {
                Integer newFixedSelfAddress = Integer.parseInt(payload);
                addressProvider.setSelfAddress(new Address(newFixedSelfAddress));
                transceiverDevice.setSelfAddress(newFixedSelfAddress);
            } catch (NumberFormatException e) {
                Timber.e(e, "Error parsing payload from FixedAddressMessage");
            }
        } else {
            handMessageOverToNeighbors(message);
        }
    }

    private void handleTextMessage(TextMessage message) {

    }

    private void handleNeighborDiscoveryMessage(NeighborDiscoveryMessage message) {

    }

    private void handMessageOverToNeighbors(MultihopMessage message) {
        // TODO increase hoppednodes counter
    }

    public void stop() {
        if (coordinatorThread != null && !coordinatorThread.isInterrupted())
            coordinatorThread.interrupt();
        running = false;
    }

    public LiveData<ProtocolState> getProtocolState() {
        return protocolState;
    }

    class CoordinatorHandler implements Runnable {
        @Override
        public void run() {
            while (running) {
                sendCoordinatorKeepAlive();

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
