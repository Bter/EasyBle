package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnCharacteristicReadCallBack{
    /**
     * {@link android.bluetooth.BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)}
     * @param device
     * @param characteristic
     * @param status
     */
    void onCharacteristicRead(BluetoothDeviceBean device, BluetoothGattCharacteristic characteristic, int status);
}
