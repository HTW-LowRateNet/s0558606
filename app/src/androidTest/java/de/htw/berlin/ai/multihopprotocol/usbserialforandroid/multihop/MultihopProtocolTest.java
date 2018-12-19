package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.Semaphore;

import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.LoraTransceiver;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device.TransceiverDevice;
import de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address.AddressProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MultihopProtocolTest {

    private MultihopProtocol multihopProtocol;
    private TransceiverDevice loraTransceiver;

    private MultihopProtocol.ProtocolState protocolState;

    @Before
    public void setUp() throws Exception {
        loraTransceiver = Mockito.mock(LoraTransceiver.class);

        multihopProtocol = new MultihopProtocol(loraTransceiver);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testStartWithoutNearNodes() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                multihopProtocol.start();

            }
        });

        Semaphore semaphore = new Semaphore(0);
        multihopProtocol.getProtocolState().observeForever(new Observer<MultihopProtocol.ProtocolState>() {
            @Override
            public void onChanged(@Nullable MultihopProtocol.ProtocolState newProtocolState) {
                protocolState = newProtocolState;
                semaphore.release();
            }
        });

        thread.start();
        semaphore.acquire();

        assertEquals(MultihopProtocol.ProtocolState.COORDINATOR_DISCOVERY, protocolState);

        semaphore.acquire();

        assertEquals(MultihopProtocol.ProtocolState.SELF_COORDINATOR, protocolState);

        assertEquals(AddressProvider.COORDINATOR_ADDRESS, multihopProtocol.addressProvider.getSelfAddress().getAddress());
    }

    @Test
    public void testStartWithNearNodes() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                multihopProtocol.start();
            }
        });

        Semaphore semaphore = new Semaphore(0);
        multihopProtocol.getProtocolState().observeForever(new Observer<MultihopProtocol.ProtocolState>() {
            @Override
            public void onChanged(@Nullable MultihopProtocol.ProtocolState newProtocolState) {
                protocolState = newProtocolState;
                semaphore.release();
            }
        });

        thread.start();

        semaphore.acquire();

        assertEquals(MultihopProtocol.ProtocolState.COORDINATOR_DISCOVERY, protocolState);

        multihopProtocol.currentNetworkMessageListener.onMessageReceived("LR,0000,10,ALIV,21,5,1,0000,FFFF,");

        semaphore.acquire();

        assertNotEquals(0, multihopProtocol.addressProvider.getFixedAddresses().size());
        assertEquals(MultihopProtocol.ProtocolState.COORDINATOR_KNOWN, protocolState);

        assertTrue(multihopProtocol.addressProvider.getSelfAddress().getAddress() <= AddressProvider.TEMP_ADDRESS_UPPER_BOUND
                && multihopProtocol.addressProvider.getSelfAddress().getAddress() >= AddressProvider.TEMP_ADDRESS_LOWER_BOUND);
    }

}