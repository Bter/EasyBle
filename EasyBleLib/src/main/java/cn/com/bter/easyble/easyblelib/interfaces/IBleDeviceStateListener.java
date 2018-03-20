package cn.com.bter.easyble.easyblelib.interfaces;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;
import cn.com.bter.easyble.easyblelib.scan.IScanResult;

/**
 * 对外连接状态接口
 * Created by admin on 2017/10/23.
 */

public interface IBleDeviceStateListener extends IScanResult {
    /**
     * 正在连接
     * @param device
     */
    void connecting(BluetoothDeviceBean device);
    /**
     * 连接成功
     */
    void connectSuccess(BluetoothDeviceBean device);
    /**
     * 正在连接
     */
    void connectFaild(BluetoothDeviceBean device);
    /**
     * 连接断开
     */
    void disConnect(BluetoothDeviceBean device);
}
