package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.Collection;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.NetworkMessageListener;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.TransceiverDevice;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.Address;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.AddressProvider;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.AcknowledgeFixedAddressMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.CoordinatorAliveMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.CoordinatorDiscoveryMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.FixedAddressMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.MessageBook;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.MultihopMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.NetworkResetMessage;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages.TextMessage;
import timber.log.Timber;

public class MultihopProtocol {

    private static final int MAX_RETRY_COUNT = 3;
    private static final int DEFAULT_TTL = 3;

    private static final int ALIVE_MESSAGE_DELAY = 60000;
    private static final int REQUEST_ADDRESS_DELAY = 10000;

    public enum ProtocolState {
        COORDINATOR_DISCOVERY, SELF_COORDINATOR, COORDINATOR_KNOWN;
    }

    private boolean coordinator;
    private boolean running;

    private MutableLiveData<ProtocolState> protocolState;

    AddressProvider addressProvider = new AddressProvider();

    MessageBook messageBook = new MessageBook();

    TransceiverDevice transceiverDevice;

    NetworkMessageListener currentNetworkMessageListener;

    private Thread coordinatorThread, requestFixedAddressThread;

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
            addressProvider.setSelfAddress(addressProvider.getCoordinatorAddress());
            transceiverDevice.setSelfAddress(addressProvider.getCoordinatorAddress().getAddress());

            startCoordinatorThread();

        } else {
            chooseTempAddressForSelf();

            startRequestFixedAddressThread();
        }
    }

    public void sendTextMessage(String data) {
        TextMessage textMessage = new TextMessage(data, DEFAULT_TTL, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());
        transceiverDevice.send(textMessage.createStringMessage());
    }

    private void startCoordinatorThread() {
        coordinatorThread = new Thread(new CoordinatorHandler());
        coordinatorThread.start();
    }

    private void startRequestFixedAddressThread() {
        requestFixedAddressThread = new Thread(new RequestFixedAddressHandler());
        requestFixedAddressThread.start();
    }

    private void initNetwork() {
        int retryCount = 0;
        coordinator = true;
        while (coordinator && retryCount <= MAX_RETRY_COUNT) {
            startCoordinatorDiscovery();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            transceiverDevice.removeListener(currentNetworkMessageListener);
            decideBecomingCoordinator();
            retryCount++;
        }

    }

    private void chooseTempAddressForSelf() {
        Address newTemporaryAddress = addressProvider.getNewTemporaryAddress();
        addressProvider.setSelfAddress(newTemporaryAddress);
        transceiverDevice.setSelfAddress(newTemporaryAddress.getAddress());
        Timber.d("Set temporary self address to %s", newTemporaryAddress);
    }

    private void requestFixedAddressFromCoordinator() {
        FixedAddressMessage fixedAddressMessage = new FixedAddressMessage("", DEFAULT_TTL, 0, addressProvider.getSelfAddress(), addressProvider.getCoordinatorAddress());
        transceiverDevice.send(fixedAddressMessage.createStringMessage());
    }

    private void startCoordinatorDiscovery() {
        currentNetworkMessageListener = stringMessage -> {
            try {
                MultihopMessage message = new MultihopMessage(stringMessage);
                if (message.getCode().equals(CoordinatorAliveMessage.CODE)) {
                    addressProvider.addFixedAddress(message.getOriginalSourceAddress());
                }
            } catch (Exception e) {
                Timber.d("Error in message handling: %s", e.getMessage());
            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);

        CoordinatorDiscoveryMessage coordinatorDiscoveryMessage = new CoordinatorDiscoveryMessage("", DEFAULT_TTL, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());

        transceiverDevice.send(coordinatorDiscoveryMessage.createStringMessage());
        Timber.d("CoordinatorDiscoveryMessage: %s", coordinatorDiscoveryMessage.createStringMessage());
    }

    private void decideBecomingCoordinator() {
        if (addressProvider.getFixedAddresses().size() == 0) {
            becomeCoordinator();
        } else {
            becomeSimpleNode();
        }
    }

    private void becomeCoordinator() {
        coordinator = true;
        transceiverDevice.setSelfAddress(addressProvider.getCoordinatorAddress().getAddress());
        protocolState.postValue(ProtocolState.SELF_COORDINATOR);
    }

    private void becomeSimpleNode() {
        coordinator = false;
        protocolState.postValue(ProtocolState.COORDINATOR_KNOWN);
    }

    private void startMessageHandling() {
        currentNetworkMessageListener = stringMessage -> {
            try {
                Timber.d("New message received: %s", stringMessage);
                MultihopMessage multihopMessage = new MultihopMessage(stringMessage);

                boolean newMessageAdded = messageBook.addMessage(multihopMessage);
                if (newMessageAdded) {
                    switch (multihopMessage.getCode()) {
                        case CoordinatorAliveMessage.CODE:
                            CoordinatorAliveMessage coordinatorAliveMessage = new CoordinatorAliveMessage(stringMessage);
                            handleCoordinatorAliveMessage(coordinatorAliveMessage);
                            break;
                        case NetworkResetMessage.CODE:
                            NetworkResetMessage networkResetMessage = new NetworkResetMessage(stringMessage);
                            handleNetworkResetMessage(networkResetMessage);
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
                        default:
                            break;
                    }
                }

            } catch (Exception e) {
                Timber.d("Exception in message handling: " + e.getMessage());
            }
        };

        transceiverDevice.addListener(currentNetworkMessageListener);
    }

    private void handleNetworkResetMessage(NetworkResetMessage networkResetMessage) {
        handMessageOverToNeighbors(networkResetMessage);
        resetNetwork();
    }

    private void handleCoordinatorAliveMessage(CoordinatorAliveMessage message) {
        if (coordinator) {
            sendNetworkResetMessage();
            resetNetwork();
        } else {
            handMessageOverToNeighbors(message);
        }
    }

    private void sendNetworkResetMessage() {
        NetworkResetMessage networkResetMessage =
                new NetworkResetMessage("WE ARE DYING!", DEFAULT_TTL, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());
        sendMessage(networkResetMessage);
    }

    private void resetNetwork() {
        stop();
        messageBook = new MessageBook();
        addressProvider = new AddressProvider();
        coordinator = false;
        start();
    }

    private void handleCoordinatorDiscoveryMessage(CoordinatorDiscoveryMessage message) {
        if (coordinator) {
            sendCoordinatorKeepAlive();
        } else {
            handMessageOverToNeighbors(message);
        }
    }

    private void sendCoordinatorKeepAlive() {
        CoordinatorAliveMessage coordinatorAliveMessage = new CoordinatorAliveMessage("Captain Marcel is coordinating!", DEFAULT_TTL, 0, addressProvider.getSelfAddress(), addressProvider.getBroadcastAddress());
        sendMessage(coordinatorAliveMessage);
    }

    private void handleFixedAddressMessage(FixedAddressMessage receivedMessage) {
        if (receivedMessage.getTargetAddress().equals(addressProvider.getSelfAddress())) {
            if (coordinator) {
                Address newFixedAddress = addressProvider.getNewFixedAddress();
                addressProvider.addFixedAddress(newFixedAddress);
                FixedAddressMessage fixedAddressMessage
                        = new FixedAddressMessage(newFixedAddress.getFourLetterHexAddress(), DEFAULT_TTL, 0, addressProvider.getSelfAddress(), receivedMessage.getOriginalSourceAddress());
                sendMessage(fixedAddressMessage);
            } else {
                String payload = receivedMessage.getPayload();
                try {
                    Address newFixedSelfAddress = getAddressFromPayload(payload);
                    addressProvider.setSelfAddress(newFixedSelfAddress);
                    transceiverDevice.setSelfAddress(newFixedSelfAddress.getAddress());

                    AcknowledgeFixedAddressMessage acknowledgeFixedAddressMessage
                            = new AcknowledgeFixedAddressMessage("", DEFAULT_TTL, 0, addressProvider.getSelfAddress(), addressProvider.getCoordinatorAddress());
                    sendMessage(acknowledgeFixedAddressMessage);

                    requestFixedAddressThread.interrupt();
                } catch (NumberFormatException e) {
                    Timber.e(e, "Error parsing payload from FixedAddressMessage");
                }
            }
        } else {
            handMessageOverToNeighbors(receivedMessage);
        }
    }

    private Address getAddressFromPayload(String payload) {
        return new Address(payload);
    }

    private void handleTextMessage(TextMessage message) {

    }

    private void handMessageOverToNeighbors(MultihopMessage message) {
        if (message.getTTL() <= message.getHoppedNodes() + 1) {
            message.setHoppedNodes(message.getHoppedNodes() + 1);
            sendMessage(message);
        }
    }

    private void sendMessage(MultihopMessage message) {
        messageBook.addMessage(message);
        transceiverDevice.send(message.createStringMessage());
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
                    Thread.sleep(ALIVE_MESSAGE_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class RequestFixedAddressHandler implements Runnable {
        @Override
        public void run() {
            try {
                while (running) {
                    requestFixedAddressFromCoordinator();

                    Thread.sleep(REQUEST_ADDRESS_DELAY);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
