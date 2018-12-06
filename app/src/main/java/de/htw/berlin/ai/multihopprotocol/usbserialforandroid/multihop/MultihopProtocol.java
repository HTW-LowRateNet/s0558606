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
        initNetwork();

        startMessageHandling();
        startCoordinatorThread();
    }

    private void startCoordinatorThread() {
        coordinatorThread = new Thread(new CoordinatorHandler());
        coordinatorThread.start();
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

    public void startDiscovery() {
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

        NeighborDiscoveryMessage coordinatorDiscoveryMessage = new NeighborDiscoveryMessage("", 10, 0);

        transceiverDevice.setDestinationAddress(addressProvider.getBroadcastAddress().getAddress());
        transceiverDevice.send(coordinatorDiscoveryMessage.createStringMessage());
        Timber.d("CoordinatorDiscoveryMessage: " + coordinatorDiscoveryMessage.createStringMessage());
    }

    private void sendCoordinatorKeepAlive() {
        CoordinatorAliveMessage coordinatorAliveMessage = new CoordinatorAliveMessage("", 10, 0);
        transceiverDevice.setDestinationAddress(addressProvider.getBroadcastAddress().getAddress());
        transceiverDevice.send(coordinatorAliveMessage.createStringMessage());
    }

    class CoordinatorHandler implements Runnable {
        @Override
        public void run() {
            while (running) {
                if (coordinator) {
                    sendCoordinatorKeepAlive();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // TODO
                }
            }
        }
    }

    private void startMessageHandling() {
        currentNetworkMessageListener = stringMessage -> {
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
        if (coordinatorThread != null && !coordinatorThread.isInterrupted())
            coordinatorThread.interrupt();
        running = false;
    }

    public LiveData<ProtocolState> getProtocolState() {
        return protocolState;
    }
}
