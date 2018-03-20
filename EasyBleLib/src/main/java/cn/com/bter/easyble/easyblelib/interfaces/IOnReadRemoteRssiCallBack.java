package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnReadRemoteRssiCallBack {
    /**
     * 调用{@link BluetoothDeviceBean#getRssi()},{@link android.bluetooth.BluetoothGattCallback#onReadRemoteRssi(BluetoothGatt, int, int)}
     * @param device
     */
    void onReadRemoteRssi(BluetoothDeviceBean device, int status);
}
