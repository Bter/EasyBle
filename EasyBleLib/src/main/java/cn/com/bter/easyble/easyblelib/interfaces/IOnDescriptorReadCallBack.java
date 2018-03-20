package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnDescriptorReadCallBack {
    /**
     * {@link android.bluetooth.BluetoothGattCallback#onDescriptorRead(BluetoothGatt, BluetoothGattDescriptor, int)}
     * @param device
     * @param descriptor
     * @param status
     */
    void onDescriptorRead(BluetoothDeviceBean device, BluetoothGattDescriptor descriptor, int status);
}
