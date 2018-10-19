package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnCharacteristicWriteCallBack {
    /**
     * {@link android.bluetooth.BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)}
     * @param device
     * @param data
     * @param status
     */
    void onCharacteristicWrite(BluetoothDeviceBean device, byte[] data, int status);
}
