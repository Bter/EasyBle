package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnReliableWriteCompletedCallBack {
    /**
     * {@link BluetoothGattCallback#onReliableWriteCompleted(BluetoothGatt, int)}
     * @param device
     * @param status
     */
    void onReliableWriteCompleted(BluetoothDeviceBean device, int status);
}
