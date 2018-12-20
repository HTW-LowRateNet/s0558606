package de.htw.berlin.ai.multihopprotocol.usbserialforandroid.multihop.address;

import java.util.Objects;

import timber.log.Timber;

public class Address {

    private int address = -1;

    public Address(int address) {
        this.address = address;
    }

    public Address(String integerHexString) {
        try {
            address = Integer.parseInt(integerHexString.trim(), 16);
        } catch (NumberFormatException e) {
            Timber.e(e);

            try {
                address = Integer.parseInt(integerHexString.trim());
            } catch (NumberFormatException e2) {
                Timber.e(e);
            }
        }

    }

    public int getAddress() {
        return address;
    }

    public String getFourLetterHexAddress() {
        StringBuilder hexAddress = new StringBuilder(Integer.toHexString(address));
        while (hexAddress.length() < 4) {
            hexAddress.insert(0, "0");
        }
        return hexAddress.toString().toUpperCase();
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address1 = (Address) o;
        return address == address1.address;
    }

    @Override
    public int hashCode() {

        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "Address{" +
                "address=" + address +
                '}';
    }
}
