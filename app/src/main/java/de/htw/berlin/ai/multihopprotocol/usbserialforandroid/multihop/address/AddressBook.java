package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AddressBook {

    private Set<Address> addresses;


    public AddressBook() {
        addresses = new HashSet<>();
    }

    public boolean hasAddress(Address address) {
        return addresses.contains(address);
    }

    public boolean addAddress(Address address) {
        return addresses.add(address);
    }

    public boolean removeAddress(Address address) {
        return addresses.remove(address);
    }

    public void clearAllAddresses() {
        addresses.clear();
    }

    public Collection<Address> getAllAddresses() {
        return addresses;
    }
}
