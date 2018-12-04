package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import java.util.concurrent.Semaphore;

import timber.log.Timber;

/**
 * Ensures that AT commands are received properly by transceiver.
 * If error responds with error, runnable will try until response is OK.
 */
public class WriteSerialRunnable implements Runnable {

    private final String data;
    private final LoraTransceiver loraTransceiver;

    private String lastSerialMessage;

    private Semaphore semaphore = new Semaphore(0);
    SerialMessageListener listener = new SerialMessageListener() {
        @Override
        public void onMessageReceived(String message) {
            lastSerialMessage = message;
            semaphore.release();
        }
    };

    public WriteSerialRunnable(LoraTransceiver loraTransceiver, String data) {
        this.loraTransceiver = loraTransceiver;
        this.data = data;
    }

    @Override
    public void run() {
        loraTransceiver.addListener(listener);

        do {
            sendSerial(data);

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Timber.e(e, "Error in WriteSerialRunnable");
            }

            if (lastSerialMessage.startsWith("ERR")) {
                resetCommand();
            }

        } while (!lastSerialMessage.equals("AT,OK"));

        loraTransceiver.removeListener(listener);
    }

    private void sendSerial(String command) {
        loraTransceiver.writeSerial(command);
    }

    private void resetCommand() {
        loraTransceiver.writeSerial("AT+RST");
    }
}
