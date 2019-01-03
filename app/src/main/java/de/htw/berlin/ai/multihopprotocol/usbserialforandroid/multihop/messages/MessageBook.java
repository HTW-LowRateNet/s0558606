package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class MessageBook {

    private Set<MultihopMessage> messages;

    public MessageBook() {
        messages = new HashSet<>();
    }

    public boolean hasMessage(MultihopMessage newMessage) {
        for (MultihopMessage message : messages) {
            if (message.getMessageID().equals(newMessage.getMessageID())
                    && message.getOriginalSourceAddress().equals(newMessage.getOriginalSourceAddress())
                    && message.getTargetAddress().equals(newMessage.getTargetAddress())) {
                return true;
            }
        }
        return false;
    }

    public boolean addMessage(MultihopMessage message) {
        if (!hasMessage(message)) {
            return messages.add(message);
        } else {
            return false;
        }
    }

    public boolean removeMessage(MultihopMessage message) {
        return messages.remove(message);
    }

    public void clearAllMessages() {
        messages.clear();
    }

    public Collection<MultihopMessage> getAllMessages() {
        return messages;
    }
}
