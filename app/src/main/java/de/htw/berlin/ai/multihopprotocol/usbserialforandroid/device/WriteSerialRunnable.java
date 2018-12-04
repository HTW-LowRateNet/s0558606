package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import java.util.concurrent.Semaphore;

import timber.log.Timber;

/**
 * Ensures that AT commands are received properly by transceiver.
 * If module responds with error, runnable will try until response is OK.
 */
public class WriteSerialRunnable implements Runnable {

    interface Callback {
        void onSerialWriteSuccess();

        void onSerialWriteFailure();
    }

    private static final int MAX_RETRY_COUNT = 10;
    private int retryCount = 0;

    private final String command;
    private final LoraTransceiver loraTransceiver;
    private final Callback callback;

    private String lastSerialMessage;

    private Semaphore semaphore = new Semaphore(0);
    SerialMessageListener listener = new SerialMessageListener() {
        @Override
        public void onMessageReceived(String message) {
            lastSerialMessage = message;
            semaphore.release();
        }
    };

    public WriteSerialRunnable(LoraTransceiver loraTransceiver, String command, Callback callback) {
        this.loraTransceiver = loraTransceiver;
        this.command = command;
        this.callback = callback;
    }

    @Override
    public void run() {
        loraTransceiver.addListener(listener);

        do {
            sendSerial(command);

            try {
                // wait for serial message
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Timber.e(e, "Error in WriteSerialRunnable");
            }

            if (lastSerialMessage.startsWith("ERR")) {
                resetCommand();
            }

            retryCount++;
        } while (!lastSerialMessage.equals("AT,OK") && retryCount <= MAX_RETRY_COUNT);

        loraTransceiver.removeListener(listener);

        if (retryCount <= MAX_RETRY_COUNT) {
            callback.onSerialWriteSuccess();
        } else {
            callback.onSerialWriteFailure();
        }
    }

    private void sendSerial(String command) {
        loraTransceiver.writeSerial(command);
    }

    private void resetCommand() {
        loraTransceiver.writeSerial("AT+RST");
    }
}