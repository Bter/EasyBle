package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * 服务发现监听
 * Created by admin on 2017/10/25.
 */

public interface IOnServicesDiscoveredCallBack {
    /**
     * {@link android.bluetooth.BluetoothGattCallback#onServicesDiscovered(BluetoothGatt, int)}
     * @param device
     * @param services 可能为空
     * @param status
     */
    void onServicesDiscovered(BluetoothDeviceBean device, List<BluetoothGattService> services, int status);
}
