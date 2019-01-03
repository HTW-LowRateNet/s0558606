package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address;

import java.util.Collection;
import java.util.Random;

public class AddressProvider {

    public static final int COORDINATOR_ADDRESS = 0x0000;

    public static final int TEMP_ADDRESS_LOWER_BOUND = 0x0011;
    public static final int TEMP_ADDRESS_UPPER_BOUND = 0x00FF;

    public static final int FIXED_ADDRESS_LOWER_BOUND = 0x0100;
    public static final int FIXED_ADDRESS_UPPER_BOUND = 0xFFFE;

    public static final int BROADCAST_ADDRESS = 0xFFFF;

    private AddressBook temporaryAddresses;
    private AddressBook fixedAddresses;

    private Address selfAddress;

    public AddressProvider() {
        temporaryAddresses = new AddressBook();
        fixedAddresses = new AddressBook();
    }

    public Address getNewTemporaryAddress() {
        Random random = new Random();
        Address newAddress;
        do {
            newAddress = new Address(TEMP_ADDRESS_LOWER_BOUND + random.nextInt(TEMP_ADDRESS_UPPER_BOUND - TEMP_ADDRESS_LOWER_BOUND));
        } while (temporaryAddresses.hasAddress(newAddress));
        temporaryAddresses.addAddress(newAddress);
        return newAddress;
    }

    public Address getNewFixedAddress() {
        Random random = new Random();
        Address newAddress;
        do {
            newAddress = new Address(FIXED_ADDRESS_LOWER_BOUND + random.nextInt(FIXED_ADDRESS_UPPER_BOUND));
        } while (fixedAddresses.hasAddress(newAddress));
        fixedAddresses.addAddress(newAddress);

        return newAddress;
    }

    public boolean addFixedAddress(Address address) {
        return fixedAddresses.addAddress(address);
    }

    public boolean addTempAddress(Address address) {
        return temporaryAddresses.addAddress(address);
    }

    public Collection<Address> getFixedAddresses() {
        return fixedAddresses.getAllAddresses();
    }

    public Collection<Address> getTempAddresses() {
        return temporaryAddresses.getAllAddresses();
    }

    public Address getCoordinatorAddress() {
        return new Address(COORDINATOR_ADDRESS);
    }

    public Address getBroadcastAddress() {
        return new Address(BROADCAST_ADDRESS);
    }

    public Address getSelfAddress() {
        return selfAddress;
    }

    public void setSelfAddress(Address selfAddress) {
        this.selfAddress = selfAddress;
    }
}
