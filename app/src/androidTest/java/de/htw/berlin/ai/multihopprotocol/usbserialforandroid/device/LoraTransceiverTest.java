package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import ai.berlin.htw.de.seriallibrary.driver.UsbSerialDriver;
import ai.berlin.htw.de.seriallibrary.driver.UsbSerialPort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class LoraTransceiverTest {

    LoraTransceiver loraTransceiver;

    UsbManager usbManager;
    UsbSerialPort usbSerialPort;
    UsbSerialDriver usbSerialDriver;
    UsbDevice usbDevice;

    String latestMessage;

    @Before
    public void setUp() throws Exception {
        usbManager = Mockito.mock(UsbManager.class);
        usbSerialPort = Mockito.mock(UsbSerialPort.class);
        usbSerialDriver = Mockito.mock(UsbSerialDriver.class);
        usbDevice = Mockito.mock(UsbDevice.class);

        loraTransceiver = new LoraTransceiver(usbSerialPort, usbManager);
        mockTransceiverStart();
    }

    private void mockTransceiverStart() throws IOException {
        when(usbSerialPort.getDriver()).thenReturn(usbSerialDriver);
        when(usbSerialPort.getDriver().getDevice()).thenReturn(usbDevice);

        when(usbSerialPort.read(any(), anyInt())).thenReturn(0);

        loraTransceiver.start();
    }

    @Test
    public void send() {

    }

    @Test
    public void testSerialMessageListenerWorking() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        SerialMessageListener serialMessageListener = message -> {
            latestMessage = message;
            semaphore.release();
        };

        loraTransceiver.addListener(serialMessageListener);

        loraTransceiver.serialMessageCallback.onMessageReceived("testmessage");

        semaphore.acquire();

        assertNotNull(latestMessage);
    }

    @Test
    public void testNetworkMessageListenerWorking() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        NetworkMessageListener listener = message -> {
            latestMessage = message;
            semaphore.release();
        };

        loraTransceiver.addListener(listener);

        loraTransceiver.serialMessageCallback.onMessageReceived("serialMessage");
        Thread.sleep(500);
        // do not receive serial messages
        assertNull(latestMessage);

        loraTransceiver.serialMessageCallback.onMessageReceived("LR, ...");
        semaphore.acquire();

        // do receive messages starting with LR
        assertNotNull(latestMessage);
    }
}