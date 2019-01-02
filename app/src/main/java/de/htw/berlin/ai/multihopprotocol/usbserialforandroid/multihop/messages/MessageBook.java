package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.messages;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class MessageBook {

    private Set<MultihopMessage> messages;


    public MessageBook() {
        messages = new HashSet<>();
    }

    public boolean hasMessage(MultihopMessage message) {
        return messages.contains(message);
    }

    public boolean addMessage(MultihopMessage message) {
        return messages.add(message);
    }

    public boolean removeMessage(MultihopMessage message) {
        return messages.remove(message);
    }

    public void clearAllMessagees() {
        messages.clear();
    }

    public Collection<MultihopMessage> getAllMessagees() {
        return messages;
    }
}
