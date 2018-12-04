package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WriteSerialRunnableTest {

    LoraTransceiver loraTransceiver;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private boolean success;

    @Before
    public void setUp() throws Exception {
        loraTransceiver = Mockito.mock(LoraTransceiver.class);
        success = false;
    }

    @Test
    public void testSuccessOnFirstTry() throws InterruptedException {
        String messageToSend = "TestMessage";
        WriteSerialRunnable writeSerialRunnable = new WriteSerialRunnable(loraTransceiver, messageToSend, new WriteSerialRunnable.Callback() {
            @Override
            public void onSerialWriteSuccess() {
                success = true;
            }
        });

        Thread thread = new Thread(writeSerialRunnable);
        thread.start();

        // wait for thread to start because listener has to be registered first
        Thread.sleep(200);

        writeSerialRunnable.listener.onMessageReceived("AT,OK");

        // assume thread ends
        Thread.sleep(1000);
        assertTrue(success);
        assertFalse(thread.isAlive());
    }

    @Test
    public void testSuccessOnSecondTry() throws InterruptedException {
        String messageToSend = "TestMessage";
        WriteSerialRunnable writeSerialRunnable = new WriteSerialRunnable(loraTransceiver, messageToSend, new WriteSerialRunnable.Callback() {
            @Override
            public void onSerialWriteSuccess() {
                success = true;
            }
        });

        Thread thread = new Thread(writeSerialRunnable);
        thread.start();

        // wait for thread to start because listener has to be registered first
        Thread.sleep(200);

        writeSerialRunnable.listener.onMessageReceived("ERR:bla");

        // assume thread staying alive waiting for next serial message
        Thread.sleep(1000);
        assertFalse(success);
        assertTrue(thread.isAlive());

        writeSerialRunnable.listener.onMessageReceived("AT,OK");

        Thread.sleep(1000);
        assertTrue(success);
        assertFalse(thread.isAlive());
    }

    @Test
    public void testSuccessOnThirdTry() throws InterruptedException {
        String messageToSend = "TestMessage";
        WriteSerialRunnable writeSerialRunnable = new WriteSerialRunnable(loraTransceiver, messageToSend, new WriteSerialRunnable.Callback() {
            @Override
            public void onSerialWriteSuccess() {
                success = true;
            }
        });

        Thread thread = new Thread(writeSerialRunnable);
        thread.start();

        // wait for thread to start because listener has to be registered first
        Thread.sleep(200);

        writeSerialRunnable.listener.onMessageReceived("ERR:bla");

        // assume thread staying alive waiting for next serial message
        Thread.sleep(1000);
        assertFalse(success);
        assertTrue(thread.isAlive());

        writeSerialRunnable.listener.onMessageReceived("LR, bla");

        // assume thread staying alive waiting for next serial message
        Thread.sleep(1000);
        assertFalse(success);
        assertTrue(thread.isAlive());

        writeSerialRunnable.listener.onMessageReceived("AT,OK");

        Thread.sleep(1000);
        assertTrue(success);
        assertFalse(thread.isAlive());
    }
}