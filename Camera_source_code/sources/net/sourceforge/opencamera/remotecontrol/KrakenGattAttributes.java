package net.sourceforge.opencamera.remotecontrol;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class KrakenGattAttributes {
    static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    static final UUID KRAKEN_BUTTONS_CHARACTERISTIC = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
    static final UUID KRAKEN_SENSORS_CHARACTERISTIC = UUID.fromString("00001625-1212-efde-1523-785feabcd123");

    KrakenGattAttributes() {
    }

    static List<UUID> getDesiredCharacteristics() {
        return Arrays.asList(new UUID[]{KRAKEN_BUTTONS_CHARACTERISTIC, KRAKEN_SENSORS_CHARACTERISTIC});
    }
}
