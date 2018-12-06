package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.device;

import java.util.ArrayList;
import java.util.List;

import ai.berlin.htw.de.seriallibrary.util.SerialInputOutputManager;
import timber.log.Timber;

public class LoraSerialListener implements SerialInputOutputManager.Listener {

    private final MessageCallback messageCallback;

    private List<Character> receivedDataBuffer = new ArrayList<>();

    public LoraSerialListener(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    @Override
    public void onRunError(Exception e) {
        Timber.e(e, "Runner stopped.");
    }

    @Override
    public void onNewData(final byte[] data) {
        putDataToBuffer(data);

        for (Integer index : getLineFeedPositionsInBuffer()) {
            if (index <= receivedDataBuffer.size()) {
                List<Character> messageCharacterList = receivedDataBuffer.subList(0, index);
                String message = getStringFromCharacterList(messageCharacterList);
                messageCallback.onMessageReceived(message);
                receivedDataBuffer.removeAll(messageCharacterList);
            }
        }
    }

    private void putDataToBuffer(byte[] data) {
        for (byte aByte : data) {
            receivedDataBuffer.add((char) aByte);
        }
    }

    private List<Integer> getLineFeedPositionsInBuffer() {
        List<Integer> indexList = new ArrayList<>();

        for (int i = 1; i < receivedDataBuffer.size(); i++) {
            if (receivedDataBuffer.get(i - 1) == '\r' && receivedDataBuffer.get(i) == '\n') {
                indexList.add(i);
            }
        }
        return indexList;
    }

    private String getStringFromCharacterList(List<Character> messageCharacterList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : messageCharacterList) {
            stringBuilder.append(character);
        }
        return stringBuilder.toString();
    }
}