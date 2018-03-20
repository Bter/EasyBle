package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * Created by admin on 2017/10/25.
 */

public interface IOnMtuChangedCallBack {
    /**
     * {@link android.bluetooth.BluetoothGattCallback#onMtuChanged(BluetoothGatt, int, int)}
     * @param device
     * @param mtu
     * @param status
     */
    void onMtuChanged(BluetoothDeviceBean device, int mtu, int status);
}
