package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import android.arch.lifecycle.LiveData;

public interface TransceiverDevice {

    void send(String message);

    void setMessageCallback(MessageCallback messageCallback);

    void start();

    void stop();

    LiveData<ConnectionStatus> getConnectionStatus();
}
