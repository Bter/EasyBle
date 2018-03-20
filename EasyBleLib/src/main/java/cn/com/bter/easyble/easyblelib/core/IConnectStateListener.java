package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothProfile;

/**
 * 连接状态接口
 * Created by admin on 2017/10/23.
 */

interface IConnectStateListener {
    /**
     *
     * @param device
     * @param currentState {@link BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}
     * or {@link BluetoothProfile#STATE_CONNECTING}
     * or {@link BluetoothDeviceBean#STATE_CONNECT_FAILD}当从未连接成功，由{@link BluetoothProfile#STATE_DISCONNECTED}触发
     */
    void connectStateChange(BluetoothDeviceBean device, int currentState);
}
