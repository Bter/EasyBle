package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnCharacteristicChangedCallBack {
    /**
     * 注意：BluetoothGattCharacteristic是引用传值
     * 应该final byte[] data = characteristic.getValue();
     * 而不是final characteristic，因为onCharacteristicChanged()回调在子线程，后面的BluetoothGattCharacteristic都是同一个对象
     * 如果不是final byte[] data = characteristic.getValue();而用Handler抛到主线程去characteristic.getValue()，将有可能获取到的值不正确
     * {@link android.bluetooth.BluetoothGattCallback#onCharacteristicChanged(BluetoothGatt, BluetoothGattCharacteristic)}
     * @param device
     * @param characteristic
     */
    void onCharacteristicChanged(BluetoothDeviceBean device, BluetoothGattCharacteristic characteristic);
}
