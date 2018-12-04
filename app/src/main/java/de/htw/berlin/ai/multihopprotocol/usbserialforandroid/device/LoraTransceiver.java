package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.berlin.htw.de.seriallibrary.driver.UsbSerialPort;
import ai.berlin.htw.de.seriallibrary.util.SerialInputOutputManager;
import timber.log.Timber;

public class LoraTransceiver implements TransceiverDevice {

    static final byte[] LINE_FEED = {'\r', '\n'};

    private List<NetworkMessageListener> networkMessageListeners;
    private List<SerialMessageListener> serialMessageListeners;

    private SerialInputOutputManager serialInputOutputManager;
    private MessageCallback serialMessageCallback = message -> {
        notifySerialMessageListeners(message);
        if (message.startsWith("LR")) {
            notifyNetworkMessageListeners(message);
        }
    };

    private UsbSerialPort serialPort;
    private UsbManager usbManager;

    private SerialInputOutputManager.Listener listener = new LoraSerialListener(serialMessageCallback);

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private MutableLiveData<ConnectionStatus> connectionStatus = new MutableLiveData<>();

    public LoraTransceiver(UsbSerialPort serialPort, UsbManager usbManager) {
        this.serialPort = serialPort;
        this.usbManager = usbManager;

        networkMessageListeners = new ArrayList<>();
        serialMessageListeners = new ArrayList<>();
    }

    @Override
    public void start() {
        openPort();
        startIoManager();
        configure();
    }

    @Override
    public void stop() {
        stopIoManager();
        closePort();
    }

    private void startIoManager() {
        if (serialPort != null) {
            Timber.i("Starting io manager ..");
            serialInputOutputManager = new SerialInputOutputManager(serialPort, listener);
            executorService.submit(serialInputOutputManager);
        }
    }

    private void configure() {
        writeSerial("AT+CFG=433000000,20,9,10,1,1,0,0,0,0,3000,8,4");
    }

    private void resetCommand() {
        writeSerial("AT+RST");
    }

    private void stopIoManager() {
        if (serialInputOutputManager != null) {
            Timber.i("Stopping io manager ..");
            serialInputOutputManager.stop();
            serialInputOutputManager = null;
        }
    }

    private void openPort() {
        if (serialPort == null) {
            connectionStatus.postValue(ConnectionStatus.NO_DEVICE);
        } else {
            UsbDeviceConnection connection = usbManager.openDevice(serialPort.getDriver().getDevice());
            if (connection == null) {
                connectionStatus.postValue(ConnectionStatus.OPENING_FAILED);
                return;
            }

            try {
                serialPort.open(connection);
                serialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Timber.e(e, "Error setting up device: %s", e.getMessage());
                connectionStatus.postValue(ConnectionStatus.OPENING_FAILED);
                try {
                    serialPort.close();
                } catch (IOException e2) {
                    Timber.e(e2, "Error closing port");
                }
                serialPort = null;
                return;
            }
            connectionStatus.postValue(ConnectionStatus.READY);
        }
    }

    private void closePort() {
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException e) {
                Timber.e(e, "Error closing port");
            }
        }
    }

    @Override
    public void send(String data) {
        int length = data.getBytes().length;
        writeSerial("AT+SEND=" + length);
        writeSerial(data);
    }

    public void writeSerial(String data) {
        try {
            serialInputOutputManager.writeSync(data.getBytes());
            serialInputOutputManager.writeSync(LINE_FEED);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    public void addListener(NetworkMessageListener listener) {
        networkMessageListeners.add(listener);
    }

    public void addListener(SerialMessageListener listener) {
        serialMessageListeners.add(listener);
    }

    public void removeListener(NetworkMessageListener listener) {
        networkMessageListeners.remove(listener);
    }

    public void removeListener(SerialMessageListener listener) {
        serialMessageListeners.remove(listener);
    }

    private void notifyNetworkMessageListeners(String networkMessage) {
        for (NetworkMessageListener listener : networkMessageListeners) {
            listener.onMessageReceived(networkMessage);
        }
    }

    private void notifySerialMessageListeners(String serialMessage) {
        for (SerialMessageListener listener : serialMessageListeners) {
            listener.onMessageReceived(serialMessage);
        }
    }
}
