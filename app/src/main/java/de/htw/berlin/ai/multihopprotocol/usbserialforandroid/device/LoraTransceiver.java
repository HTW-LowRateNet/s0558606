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

    private static final byte[] LINE_FEED = {'\r', '\n'};

    private SerialInputOutputManager serialInputOutputManager;
    private MessageCallback messageCallback;
    private UsbSerialPort serialPort;
    private UsbManager usbManager;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private MutableLiveData<ConnectionStatus> connectionStatus = new MutableLiveData<>();

    private List<Character> receivedDataBuffer = new ArrayList<>();

    private final SerialInputOutputManager.Listener listener = new SerialInputOutputManager.Listener() {
        @Override
        public void onRunError(Exception e) {
            Timber.d("Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            for (byte aByte : data) {
                receivedDataBuffer.add((char) aByte);
            }
            Timber.d("Current data buffer state: %s", receivedDataBuffer.toString());

            StringBuilder stringBuilder = new StringBuilder();
            for (char aChar : receivedDataBuffer) {
                stringBuilder.append(aChar);

                if (stringBuilder.toString().contains(new String(LINE_FEED))) {
                    messageCallback.onMessageReceived(stringBuilder.toString());
                    receivedDataBuffer.clear(); // might be critical when different threads call onnewdata
                }
            }
        }
    };

    public LoraTransceiver(UsbSerialPort serialPort, UsbManager usbManager) {
        this.serialPort = serialPort;
        this.usbManager = usbManager;
    }

    @Override
    public void start() {
        openPort();
        startIoManager();
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
        try {
            serialInputOutputManager.writeSync(data.getBytes());
            serialInputOutputManager.writeSync(LINE_FEED);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @Override
    public void setMessageCallback(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }
}
