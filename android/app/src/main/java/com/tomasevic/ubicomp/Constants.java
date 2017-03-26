package com.tomasevic.ubicomp;

import java.util.UUID;

/**
 * Created by simon on 30.10.16..
 */

public class Constants {
    public final static String ARDUINO_BLUETOOTH_NAME = "blutut";
    public final static String EXTRA_BL_NAME = "bluetooth_name";
    public final static String EXTRA_BL_ADDRESS = "bluetooth_address";
    public final static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public enum ArduinoModeEnum {
        OFF_MODE, AUTOMATIC_MODE, MANUAL_MODE
    }
}
