package cn.com.bter.easyble.easyblelib.base;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.CallSuper;

/**
 * BLE基本设备管理
 * Created by admin on 2017/10/23.
 */

public class BluetoothDeviceBase {
    private BluetoothDevice mDevice;
    /**
     * 信号强度
     */
    private int rssi;
    private byte[] scanRecord;

    private Long lastRefreshRssiTime = System.currentTimeMillis();

    public BluetoothDeviceBase(BluetoothDevice mDevice, int rssi, byte[] scanRecord) {
        this.mDevice = mDevice;
        this.rssi = rssi;
        lastRefreshRssiTime = System.currentTimeMillis();
        this.scanRecord = scanRecord;
    }

    /**
     * 获得BLE设备
     * @return
     */
    public BluetoothDevice getBleDevice() {
        return mDevice;
    }

    /**
     * 设置新的BLE设备
     * @param mDevice
     */
    public BluetoothDeviceBase replaceBleDevice(BluetoothDevice mDevice){
        if(null != mDevice && mDevice.equals(this.mDevice)){
            this.mDevice = mDevice;
            lastRefreshRssiTime = System.currentTimeMillis();
        }else if(this.mDevice == null){
            this.mDevice = mDevice;
            lastRefreshRssiTime = System.currentTimeMillis();
        }
        return this;
    }

    /**
     * 设置信号强度
     * @param rssi
     */
    @CallSuper
    public BluetoothDeviceBase setRssi(int rssi) {
        this.rssi = rssi;
        lastRefreshRssiTime = System.currentTimeMillis();
        return this;
    }

    public Long getLastRefreshRssiTime() {
        return lastRefreshRssiTime;
    }

    /**
     * 获得信号强度
     * @return
     */
    public int getRssi() {
        return rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public BluetoothDeviceBase setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
        return this;
    }
}
