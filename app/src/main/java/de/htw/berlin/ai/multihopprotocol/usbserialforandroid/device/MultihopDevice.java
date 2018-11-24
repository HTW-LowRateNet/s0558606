package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

public interface MultihopDevice {

    void send(String message);

    void setMessageCallback(MessageCallback messageCallback);

    void start();

    void stop();
}
