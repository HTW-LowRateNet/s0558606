package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import timber.log.Timber;

/**
 * Ensures that AT commands are received properly by transceiver.
 * If module responds with error, runnable will try until response is OK.
 */
public class SerialWriterRunnable implements Runnable {

    private final BlockingQueue<String> queue;

    private static final int MAX_RETRY_COUNT = 10;
    private int retryCount = 0;

    private final LoraTransceiver loraTransceiver;

    private String lastSerialMessage;

    private boolean running;

    private Semaphore semaphore = new Semaphore(0);
    SerialMessageListener listener = new SerialMessageListener() {
        @Override
        public void onMessageReceived(String message) {
            lastSerialMessage = message;
            semaphore.release();
        }
    };

    public SerialWriterRunnable(LoraTransceiver loraTransceiver, BlockingQueue<String> queue) {
        this.loraTransceiver = loraTransceiver;
        this.queue = queue;

        running = true;
    }

    @Override
    public void run() {
        loraTransceiver.addListener(listener);

        while (running) {
            try {
                while (true) {
                    String command = queue.take();

                    do {
                        sendSerial(command);

                        // wait for a new message from transceiver
                        semaphore.acquire();

                        if (lastSerialMessage.contains("ERR")) {
                            resetCommand();
                        }
                        retryCount++;
                    } while (!isResponseOK(lastSerialMessage) && retryCount <= MAX_RETRY_COUNT);
                }
            } catch (InterruptedException e) {
                Timber.e(e, "InterruptedException caught in SerialWriterRunnable");
                e.printStackTrace();
            }
        }

        loraTransceiver.removeListener(listener);
    }

    public void stop() {
        running = false;
    }


    private boolean isResponseOK(String response) {
        return response.contains("AT,OK") || response.contains("AT,SENDED");
    }

    private void sendSerial(String command) {
        loraTransceiver.writeSerial(command);
    }

    private void resetCommand() {
        loraTransceiver.writeSerial("AT+RST");
    }
}
