package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import java.util.concurrent.Semaphore;

import timber.log.Timber;

public class SendMessageRunnable implements Runnable {

    private final String data;
    private final LoraTransceiver loraTransceiver;

    private String lastSerialMessage;

    public SendMessageRunnable(LoraTransceiver loraTransceiver, String data) {
        this.loraTransceiver = loraTransceiver;
        this.data = data;
    }

    @Override
    public void run() {
        Semaphore semaphore = new Semaphore(0);

        do {
            loraTransceiver.addListener(new SerialMessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    lastSerialMessage = message;
                    semaphore.release();
                }
            });

            sendMessage(data);

            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Timber.e(e, "Error in SendMessageRunnable");
            }

            if (lastSerialMessage.equals("")) {
                resetCommand();
            }

        } while (!lastSerialMessage.equals("AT,OK"));
    }

    private void sendMessage(String message) {
        int length = message.getBytes().length;
        loraTransceiver.writeSerial("AT+SEND=" + length);
        loraTransceiver.writeSerial(message);
    }

    private void resetCommand() {
        loraTransceiver.writeSerial("AT+RST");
    }
}
