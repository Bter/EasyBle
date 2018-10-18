package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGattCharacteristic;

class DataUnit {
    private BluetoothGattCharacteristic characteristic;
    private int writeType;
    private byte[] dataBuf;
}
