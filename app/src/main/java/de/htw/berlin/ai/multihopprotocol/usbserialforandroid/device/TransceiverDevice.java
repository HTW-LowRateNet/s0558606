package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import android.arch.lifecycle.LiveData;

public interface TransceiverDevice {

    void send(String message);

    void addListener(NetworkMessageListener listener);

    void removeListener(NetworkMessageListener listener);

    void start();

    void stop();

    LiveData<ConnectionStatus> getConnectionStatus();
}
