package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import cn.com.bter.easyble.easyblelib.interfaces.IBleDeviceStateListener;

/**
 * BLE连接管理器
 * Created by admin on 2017/10/19.
 */

public class BleConnectManager {
    /**
     * 已经连接的设备
     */
    private HashMap<String,BluetoothDeviceBean> connectedDevices = new HashMap<>();

    /**
     * 已经连接或正在连接的设备
     */
    private HashMap<String,BluetoothDeviceBean> connectedOrConnectingDevices = new HashMap<>();

    private IBleDeviceStateListener mBleDeviceStateListener;

    public BleConnectManager(IBleDeviceStateListener mBleDeviceStateListener){
        setBleDeviceStateListener(mBleDeviceStateListener);
    }

    /**
     * 设置设备连接监听器
     * @param mBleDeviceStateListener
     */
    public BleConnectManager setBleDeviceStateListener(IBleDeviceStateListener mBleDeviceStateListener) {
        this.mBleDeviceStateListener = mBleDeviceStateListener;
        return this;
    }

    /**
     * 是否已经有连接
     * @return
     */
    public boolean hasConnected(){
        return connectedDevices.size() > 0;
    }

    /**
     * 是否已经有连接或正在连接的设备
     * @return
     */
    public boolean hasConnectedOrConnecting(){
        return connectedOrConnectingDevices.size() > 0;
    }

    /**
     * 该设备是否已经连接
     * @param device
     * @return
     */
    public boolean isConnected(BluetoothDeviceBean device){
        if(null != device && device.getBleDevice() != null){
            return isConnected(device.getBleDevice().getAddress());
        }
        return false;
    }

    /**
     * 通过mac地址判断某个设备是否已经连接
     * @param mac
     * @return
     */
    public boolean isConnected(String mac){
        BluetoothDeviceBean device = getConnectedDevice(mac);
        if(device != null){
            return device.isConnected();
        }
        return false;
    }

    /**
     * 该设备是否正在连接或已经连接
     * @param device
     * @return
     */
    public boolean isConnectedOrConnecting(BluetoothDeviceBean device){
        if(null != device && null != device.getBleDevice()){
            return isConnectedOrConnecting(device.getBleDevice().getAddress());
        }
        return false;
    }

    /**
     * 该设备是否正在连接或已经连接
     * @param mac
     * @return
     */
    public boolean isConnectedOrConnecting(String mac){
        BluetoothDeviceBean device = connectedOrConnectingDevices.get(mac);
        if(null != device) {
            return device.isConnectingOrConnected();
        }
        return false;
    }


    /**
     * 断开所有连接
     */
    protected void disConnectAllDevice(){
        Collection<BluetoothDeviceBean> devices = getConnectedDevices();
        if(null != devices && devices.size() > 0){
            Iterator<BluetoothDeviceBean> deviceIterator = devices.iterator();
            while (deviceIterator.hasNext()){
                BluetoothDeviceBean device = deviceIterator.next();
                if(null != device){
                    device.disConnect();
                }
                deviceIterator.remove();
            }
        }
    }

    protected void clearConnectedOrConnectingDevices(){
        connectedOrConnectingDevices.clear();
    }

    protected void clearConnectedDevices(){
        connectedDevices.clear();
    }

    /**
     * 销毁
     */
    public void destroy(){
        disConnectAllDevice();
        clearConnectedOrConnectingDevices();
    }

    /**
     * 获得所有已经连接的设备
     * @return
     */
    public Collection<BluetoothDeviceBean> getConnectedDevices(){
        Collection<BluetoothDeviceBean> values = new HashSet<>();
        synchronized (this) {
            values.addAll(connectedDevices.values());
        }
        return values;
    }

    /**
     * 获得已经连接的设备或正在连接的设备
     * @return
     */
    public Collection<BluetoothDeviceBean> getConnectedOrConnectingDevices(){
        Collection<BluetoothDeviceBean> values = new HashSet<>();
        values.addAll(connectedOrConnectingDevices.values());
        return values;
    }

    /**
     * 获得指定mac地址的已经连接的BLE设备
     * @param mac
     * @return
     */
    public BluetoothDeviceBean getConnectedDevice(String mac){
        if(null != mac) {
            return connectedDevices.get(mac);
        }
        return null;
    }

    /**
     * 连接BLE
     * @param device
     * @param autoConnect
     */
    public void connectBle(BluetoothDeviceBean device, Context context,boolean autoConnect){
        if(device != null && !isConnectedOrConnecting(device)){
            device.connect(context,autoConnect,mConnectStateListener);
        }
    }

    /**
     * BLE设备连接状态监听
     */
    private IConnectStateListener mConnectStateListener = new IConnectStateListener() {
        @Override
        public void connectStateChange(DeviceConnectBean dev, int currentState) {
            BluetoothDeviceBean device = (BluetoothDeviceBean) dev;
            if(currentState == BluetoothProfile.STATE_CONNECTED || currentState == BluetoothProfile.STATE_CONNECTING){
                connectedOrConnectingDevices.put(device.getBleDevice().getAddress(),device);
                if(currentState == BluetoothProfile.STATE_CONNECTED) {
                    synchronized (this) {
                        connectedDevices.put(device.getBleDevice().getAddress(), device);//加入已经连接的设备
                    }
                }
            }else if(currentState == BluetoothProfile.STATE_DISCONNECTED
                    || currentState == BluetoothDeviceBean.STATE_CONNECT_FAILD){
                connectedOrConnectingDevices.remove(device.getBleDevice().getAddress());
                synchronized (this) {
                    connectedDevices.remove(device.getBleDevice().getAddress());//删除已经断开的设备
                }
            }
            //下面应该通知设备状态改变
            poastOutState(device,currentState);
        }
    };

    /**
     * 对外抛出连接状态变化
     * @param device
     * @param currentState one of {@link BluetoothProfile#STATE_DISCONNECTED}
     * or {@link BluetoothProfile#STATE_CONNECTED}
     * or {@link BluetoothProfile#STATE_CONNECTING}
     * or {@link BluetoothDeviceBean#STATE_CONNECT_FAILD}
     */
    private void poastOutState(BluetoothDeviceBean device,int currentState){
        if(null != device) {
            if (mBleDeviceStateListener != null) {
                switch (currentState){
                    case BluetoothProfile.STATE_CONNECTING:
                        mBleDeviceStateListener.connecting(device);
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        mBleDeviceStateListener.connectSuccess(device);
                        break;
                    case BluetoothDeviceBean.STATE_CONNECT_FAILD:
                        mBleDeviceStateListener.connectFaild(device);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        mBleDeviceStateListener.disConnect(device);
                        break;
                }
            }
        }
    }


    /**
     * 获得状态监听器
     * @return
     */
    public IConnectStateListener getConnectStateListener(){
        return mConnectStateListener;
    }
}
