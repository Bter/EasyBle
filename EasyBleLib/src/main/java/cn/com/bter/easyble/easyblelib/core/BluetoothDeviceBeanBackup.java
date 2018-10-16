package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import cn.com.bter.easyble.easyblelib.base.BluetoothDeviceBase;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicChangedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicReadCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnDescriptorReadCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnDescriptorWriteCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnMtuChangedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnReadRemoteRssiCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnReliableWriteCompletedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnServicesDiscoveredCallBack;
import cn.com.bter.easyble.easyblelib.utils.LogUtil;

/**
 * BLE设备管理，含连接状态部分
 * http://blog.csdn.net/qingtiantianqing/article/details/52459629?locationNum=13
 * Created by admin on 2017/10/19.
 */

public class BluetoothDeviceBeanBackup /*extends BluetoothDeviceBase*/ {
   /* private static final String TAG = BluetoothDeviceBeanBackup.class.getSimpleName();

    *//**
     * 连接失败
     * 没有成功连接过然后直接返回BluetoothProfile.STATE_DISCONNECTED，会认为是连接失败
     *//*
    public static final int STATE_CONNECT_FAILD = ~BluetoothProfile.STATE_DISCONNECTED;

    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    *//**
     * 连接状态
     *//*
    private int connectStatus = BluetoothProfile.STATE_DISCONNECTED;
    private BluetoothGatt mBluetoothGatt;
    private IConnectStateListener mConnectStateListener;

    private IOnServicesDiscoveredCallBack mOnDiscoverServiceCallBack;
    private IOnCharacteristicWriteCallBack mOnCharacteristicWriteCallBack;
    private IOnCharacteristicReadCallBack mOnCharacteristicReadCallBack;
    private IOnCharacteristicChangedCallBack mOnCharacteristicChangedCallBack;
    private IOnDescriptorReadCallBack mOnDescriptorReadCallBack;
    private IOnDescriptorWriteCallBack mOnDescriptorWriteCallBack;
    private IOnReliableWriteCompletedCallBack mOnReliableWriteCompletedCallBack;
    private IOnReadRemoteRssiCallBack mOnReadRemoteRssiCallBack;
    private IOnMtuChangedCallBack mOnMtuChangedCallBack;

    private boolean isServiceDiscovered = false;
    private Handler mHandler;

    BluetoothDeviceBeanBackup(BluetoothDevice device, int rssi, byte[] scanRecord, Handler mHandler) {
        super(device,rssi,scanRecord);
        this.mHandler = mHandler;
        init();
    }

    *//**
     * 初始化
     *//*
    private void init(){
    }

    *//**
     * 连接
     * 最好在主线程中调用?
     * 如果已经连接直接回调
     * 连接前应该还要判断蓝牙是否打开了
     * 15s未连接上认为连接失败
     * @param autoConnect
     *//*
    void connect(Context context, boolean autoConnect, IConnectStateListener mConnectStateListener){
        if(getBleDevice() != null){
            if(context != null){//是否应该判断已经连接还是连接中还是未连接
                if(!isConnectingOrConnected()) {//还没有连接或还没有正在连接
                    this.mConnectStateListener = mConnectStateListener;
                    connectStatus = BluetoothProfile.STATE_CONNECTING;
                    poastConnectState(this, connectStatus);
                    if(mHandler != null){
                        mHandler.removeCallbacks(connectTimeOut);
                        mHandler.postDelayed(connectTimeOut,1000 * 15);
                    }
                    mBluetoothGatt = getBleDevice().connectGatt(context, autoConnect, mBluetoothGattCallback);
                }
            }else{
                //未初始化上下文
                LogUtil.w(TAG,"EasyBleManager never init success!!!!");
                throw new NullPointerException("this context is null");
            }

        }else{
            //设备为空
            LogUtil.w(TAG,">>>>>>>>mDevice is null<<<<<<<");
        }
    }

    private Runnable connectTimeOut = new Runnable() {
        @Override
        public void run() {
            if(mHandler != null){
                mHandler.removeCallbacks(this);
            }
            connectStatus = BluetoothProfile.STATE_DISCONNECTED;
            poastConnectState(BluetoothDeviceBeanBackup.this, connectStatus);
        }
    };

    *//**
     * 未调用close时重连
     * 最好在主线程中调用?
     * 比较正确的做法是调用connect
     *//*
    private void reConnect(){
        if(null != mBluetoothGatt && !isConnectingOrConnected()){
            connectStatus = BluetoothProfile.STATE_CONNECTING;
            poastConnectState(this, connectStatus);
            mBluetoothGatt.connect();
        }
    }

    *//**
     * 断开连接
     * 最好在主线程中调用?
     *//*
    public void disConnect(){
        if(null != mBluetoothGatt){
            mBluetoothGatt.disconnect();
            refreshDeviceCache();
        }
    }

    *//**
     * 关闭连接
     * 最好在主线程中调用?
     *//*
    private void close(){
        if(null != mBluetoothGatt){
            mBluetoothGatt.close();
            connectStatus = BluetoothProfile.STATE_DISCONNECTED;
        }
    }

    *//**
     * Android手机会对连接过的BLE设备的Services进行缓存，
     * 若设备升级后Services等有改动，则程序会出现通讯失败。
     * 此时就得刷新缓存，反射调用BluetoothGatt类总的refresh()方法
     * 在disconnect之后且在close之前调用
     * @return
     *//*
    public boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                BluetoothGatt callback = mBluetoothGatt;
                if(null != callback) {
                    final boolean success = (Boolean) refresh.invoke(callback);
                    LogUtil.i(TAG,"refreshDeviceCache, is success:  " + success);
                    return success;
                }else{
                    return false;
                }
            }
        } catch (Exception e) {
            LogUtil.w("exception occur while refreshing device: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    *//**
     * 是否已经连接
     * @return
     *//*
    public boolean isConnected(){
        if(mBluetoothGatt != null && connectStatus == BluetoothProfile.STATE_CONNECTED){
            return true;
        }
        return false;
    }

    *//**
     * 是否正在连接或者已经连接
     * @return
     *//*
    public boolean isConnectingOrConnected(){
        return connectStatus == BluetoothProfile.STATE_CONNECTED
                || connectStatus == BluetoothProfile.STATE_CONNECTING;
    }

    *//**
     * 发现服务
     * 是否发现成功的前提是蓝牙已经连接
     * @return
     *//*
    public boolean discoverServices(){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.discoverServices();
        }
        return false;
    }

    public List<BluetoothGattService> getServices(){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.getServices();
        }
        return null;
    }

    public BluetoothGattService getService(UUID uuid){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.getService(uuid);
        }
        return null;
    }


    *//**
     * 获得已经连接的BLE设备
     * @return 如果没有连接会返回null
     *//*
    public BluetoothDevice getConnectDevice(){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.getDevice();
        }
        return null;
    }

    *//**
     * 获取连接状态
     * @return one of {@link BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}
     * or {@link BluetoothProfile#STATE_CONNECTING}
     *//*
    public int getConnectStatus() {
        return connectStatus;
    }

    *//**
     * 抛出连接状态
     * @param device
     * @param currentState
     *//*
    private void poastConnectState(BluetoothDeviceBeanBackup device, int currentState){
        if(mConnectStateListener != null){
            mConnectStateListener.connectStateChange(device,currentState);
        }
    }

    *//***********************************操作*************************************************//*

    private BluetoothGattCharacteristic getCharacteristic(String serviceUUID,String characteristicUUID,String erroMsg){
        if(erroMsg == null){
            erroMsg = "getCharacteristic";
        }
        if(!isConnected()){
            LogUtil.w(TAG, String.format(">>>>>>%s fail,this device is disconected<<<<<",erroMsg));
            return null;
        }
        if(serviceUUID != null){
            if(characteristicUUID != null){
                UUID mServiceUUID = formatUUID(serviceUUID);
                UUID mNotifyUUID = formatUUID(characteristicUUID);
                if(mServiceUUID != null && mNotifyUUID != null ) {
                    BluetoothGattService service = getService(mServiceUUID);
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(mNotifyUUID);
                        if (characteristic != null) {
                            return characteristic;
                        }else{
                            LogUtil.w(TAG, String.format(">>>>>>%s fail,not found this %sUUID(" + characteristicUUID + ")<<<<<",erroMsg,erroMsg));
                        }
                    } else {
                        LogUtil.w(TAG, String.format(">>>>>>%s fail,not found this serviceUUID(" + serviceUUID + ")<<<<<",erroMsg));
                    }
                }else{
                    LogUtil.w(TAG, String.format(">>>>>>%s fail,serviceUUID(" + serviceUUID + ") or %sUUID(" + characteristicUUID + ") format faild<<<<<",erroMsg,erroMsg));
                }
            }else{
                LogUtil.w(TAG,String.format(">>>>>>%s fail, %sUUID is null<<<<<",erroMsg,erroMsg));
            }
        }else{
            LogUtil.w(TAG,String.format(">>>>>>%s fail, serviceUUID is null<<<<<",erroMsg));
        }
        return null;
    }

    *//**
     * enable notify
     * 打开通知监听
     * @param serviceUUID
     * @param notifyUUID
     * @return {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)} {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    public boolean enableNotify(String serviceUUID, String notifyUUID){
        return changeNotify(serviceUUID,notifyUUID,true,"enableNotify");
    }

    public boolean enalbeNotifyByFixNotifyValue(String serviceUUID,String notifyUUID,byte[] FIX_NOTIFICATION_VALUE){
        return setNotification(getCharacteristic(serviceUUID,notifyUUID,"enalbeNotifyByFixNotifyValue"),true,FIX_NOTIFICATION_VALUE);
    }

    *//**
     * disable notify
     * 关闭通知监听
     * @param serviceUUID
     * @param notifyUUID
     * @return {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)} {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    public boolean disableNofify(String serviceUUID,String notifyUUID){
        return changeNotify(serviceUUID,notifyUUID,false,"disableNofify");
    }

    *//**
     * change notify
     * 改变通知
     * @param serviceUUID
     * @param notifyUUID
     * @param isEnable
     * @return {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)} {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    private boolean changeNotify(String serviceUUID, String notifyUUID, boolean isEnable,String descri){
        return setNotification(getCharacteristic(serviceUUID,notifyUUID,descri),isEnable);
    }

    *//**
     * set notify
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     *//*
    public boolean setNotification(BluetoothGattCharacteristic characteristic,boolean enable){
        return setNotification(characteristic,enable,enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    *//**
     * set notify
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     *//*
    private boolean setNotification(BluetoothGattCharacteristic characteristic,boolean enable,byte[] FIX_NOTIFICATION_VALUE){
        if(null != characteristic){
            if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                if(null != mBluetoothGatt){
                    boolean result = false;
                    boolean isSuccess = setCharacteristicNotification(characteristic,enable);

                    if(isSuccess) {
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                formatUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
                        LogUtil.i(TAG, ">>>>>>>>> " + characteristic.getUuid().toString() + " setCharacteristicNotification isSuccess = " + isSuccess);


                        if (descriptor != null) {
                            descriptor.setValue(FIX_NOTIFICATION_VALUE);
                            result = writeDescriptor(descriptor);
                        }
                        LogUtil.d(TAG, ">>>>>>>>> " + characteristic.getUuid().toString() + " notify result = " + result);
                        return result;
                    }
                }
            }else{
                LogUtil.w(TAG, ">>>>>>this notifyUUID(" + characteristic.getUuid().toString() + ") not support notification<<<<<");
            }
        }else{
            LogUtil.w(TAG,">>>>>>setNotification fail,this characteristic is null<<<<<");
        }
        return false;
    }

    *//**
     * write no response
     * 写
     * @param serviceUUID
     * @param writeUUID
     * @param data
     * @return {@link BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicWriteCallBack#onCharacteristicWrite(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic, int)}
     *//*
    public boolean writeNoResponse(String serviceUUID,String writeUUID,byte[] data){
        return write(serviceUUID,writeUUID,data,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    *//**
     * 自动识别特征点类型发送数据
     * 自动识别wirte的类型<br\>
     * 依次顺序为1.PROPERTY_WRITE；2.PROPERTY_WRITE_NO_RESPONSE；PROPERTY_SIGNED_WRITE。<br\>
     * 一旦识别到则以该类型发送数据<br\>
     * @param serviceUUID
     * @param writeUUID
     * @param data
     * @return
     *//*
    public boolean writeAutoIndentifyType(String serviceUUID,String writeUUID,byte[] data){
        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID,writeUUID,"writeAutoIndentifyType");
        return writeAutoIndentifyType(characteristic,data);
    }

    *//**
     * 自动识别特征点类型发送数据
     * 自动识别wirte的类型<br\>
     * 依次顺序为1.PROPERTY_WRITE；2.PROPERTY_WRITE_NO_RESPONSE；PROPERTY_SIGNED_WRITE。<br\>
     * 一旦识别到则以该类型发送数据<br\>
     * @param data
     * @return
     *//*
    public boolean writeAutoIndentifyType(BluetoothGattCharacteristic characteristic,byte[] data){
        int type = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
        if(characteristic != null) {
            int properties = characteristic.getProperties();
            if((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0){
                type = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
            }else if((type = properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0){
                type = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
            }else if((type = properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0){
                type = BluetoothGattCharacteristic.WRITE_TYPE_SIGNED;
            }
        }else{
            LogUtil.w(TAG,"write fail this BluetoothGattCharacteristic is null");
        }
        return writeCharacteristic(characteristic,data,type);
    }

    *//**
     * write
     * 写
     * @param serviceUUID
     * @param writeUUID
     * @param data
     * @param writeType The write type to for this characteristic. Can be one
     *                  of:
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return {@link BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicWriteCallBack#onCharacteristicWrite(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic, int)}
     *//*
    public boolean write(String serviceUUID,String writeUUID,byte[] data,int writeType){
        return writeCharacteristic(getCharacteristic(serviceUUID,writeUUID,"write"),data,writeType);
    }

    *//**
     * 写
     * @param characteristic
     * @param data
     * @param writeType The write type to for this characteristic. Can be one
     *                  of:
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return {@link BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicWriteCallBack#onCharacteristicWrite(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic, int)}
     *//*
    public synchronized boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data,int writeType){
        if(null != data && data.length > 0){
            if(characteristic != null){
                if((characteristic.getProperties()
                        & (BluetoothGattCharacteristic.PROPERTY_WRITE
                            | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                            | BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE)) == 0){
                    LogUtil.w(TAG,"this characteristic(uuid = " + characteristic.getUuid().toString()  +") not support write!");
                    return false;
                }
                characteristic.setValue(data);
                characteristic.setWriteType(writeType);
                LogUtil.d(TAG,">>>>>>>>>>>>device " + characteristic.getService());
                if(mBluetoothGatt != null){
                    boolean result = mBluetoothGatt.writeCharacteristic(characteristic);
                    return result;
                }else{
                    LogUtil.w(TAG,">>>>>>>>>writeCharacteristic fail,because mBluetoothGatt is null<<<<<<<<<");
                }
            }else{
                LogUtil.w(TAG,">>>>>>>>>writeCharacteristic fail,because characteristic is null<<<<<<<<<");
            }
        }
        return false;
    }

    *//**
     * 读
     * @param serviceUUID
     * @param readUUID
     * @return {@link BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicReadCallBack#onCharacteristicRead(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic, int)}
     *//*
    public boolean read(String serviceUUID,String readUUID){
        return readCharacteristic(getCharacteristic(serviceUUID,readUUID,"read"));
    }

    *//**
     * read<br\>
     * 读
     * @param characteristic
     * @return {@link BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicReadCallBack#onCharacteristicRead(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic, int)}
     *//*
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(characteristic != null){
            if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0){
                LogUtil.w(TAG,">>>>>>>>>this characteristic(uuid = " + characteristic.getUuid().toString() + ") not support read!<<<<<<<<<");
                return false;
            }
            if(mBluetoothGatt != null){
                return mBluetoothGatt.readCharacteristic(characteristic);
            }
        }else{
            LogUtil.w(TAG,">>>>>>>>>readCharacteristic fail,because characteristic is null<<<<<<<<<");
        }
        return false;
    }

    *//**
     * 读取当前连接的设备的信号强度
     * @return {@link BluetoothGatt#readRemoteRssi()}
     * data call back {@link IOnReadRemoteRssiCallBack#onReadRemoteRssi(BluetoothDeviceBeanBackup, int)}
     *//*
    public boolean readRemoteRssi(){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.readRemoteRssi();
        }
        return false;
    }

    *//**
     *
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param serviceUUID
     * @param indicateUUID
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    public boolean enableIndicate(String serviceUUID,String indicateUUID){
        return changeIndicate(serviceUUID,indicateUUID,true,"enableIndicate");
    }

    *//**
     *
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param serviceUUID
     * @param indicateUUID
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    public boolean disableIndicate(String serviceUUID,String indicateUUID){
        return changeIndicate(serviceUUID,indicateUUID,false,"disableIndicate");
    }

    *//**
     *
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param serviceUUID
     * @param indicateUUID
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    private boolean changeIndicate(String serviceUUID,String indicateUUID,boolean enable,String descri){
        return setCharacteristicIndication(getCharacteristic(serviceUUID,indicateUUID,descri),enable);
    }

    *//**
     * indicate setting
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBeanBackup, BluetoothGattCharacteristic)}
     *//*
    public boolean setCharacteristicIndication(BluetoothGattCharacteristic characteristic,boolean enable){
        if(characteristic != null){
            if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0){
                LogUtil.w(TAG,">>>>>characteristic(uuid = " + characteristic.getUuid().toString() + ") not supports indication<<<<<<");
                return false;
            }
            *//*if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0){
                //???????is right
                return false;
            }*//*
            if(mBluetoothGatt != null){
                boolean success = setCharacteristicNotification(characteristic,enable);
                LogUtil.d(TAG, "setCharacteristicIndication:" + enable
                        + "\nsuccess:" + success
                        + "\ncharacteristic.getUuid():" + characteristic.getUuid());
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        formatUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
                if(success) {
                    boolean result = false;
                    if (descriptor != null) {
                        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE :
                                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        result = writeDescriptor(descriptor);
                    }
                    LogUtil.i(TAG, "setCharacteristicIndication writeDescriptor result = " + result);
                    return result;
                }
            }
        }else{
            LogUtil.w(TAG,">>>>>>>>>>>>>>>setCharacteristicIndication fail,characteristic is null<<<<<<<<<<<<<<<");
        }
        return false;
    }

    *//**
     *
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     *//*
    private boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enable){
        if(characteristic != null) {
            if (mBluetoothGatt != null) {
                return mBluetoothGatt.setCharacteristicNotification(characteristic,enable);
            }
        }else{
            LogUtil.w(TAG,">>>>>>>>>>>>>>>setCharacteristicNotification fail,characteristic is null<<<<<<<<<<<<<<<");
        }
        return false;
    }

    *//**
     *
     * @param mtu
     * @return {@link BluetoothGatt#requestMtu(int)}
     *//*
    public boolean requestMtu(int mtu){
        if(mBluetoothGatt != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mBluetoothGatt.requestMtu(mtu);
            }
        }
        return false;
    }

    *//**
     *
     * @param descriptor
     * @return {@link BluetoothGatt#readDescriptor(BluetoothGattDescriptor)}
     *//*
    public boolean readDescriptor(BluetoothGattDescriptor descriptor){
        if(null != mBluetoothGatt) {
            return mBluetoothGatt.readDescriptor(descriptor);
        }
        return false;
    }

    *//**
     *
     * @param descriptor
     * @return  {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)}
     *//*
    public boolean writeDescriptor(BluetoothGattDescriptor descriptor){
        if(mBluetoothGatt != null){
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    *//**
     * 开始可靠的characteristic写的请求<br\>
     * 所有characteristic的写请求都会保存在gattServer中的队列中，<br\>
     * 直到有executeReliableWrite()执行，中途可以检验某个之是不是已经正确写入，<br\>
     * 同时可以调用public void abortReliableWrite()来终止这次可靠写characteristic请求，<br\>
     * 所有从开始的数据都不会写入到characteristic中.<br\>
     * @return
     *//*
    public boolean beginReliableWrite(){
        if(mBluetoothGatt != null) {
            return mBluetoothGatt.beginReliableWrite();
        }
        return false;
    }

    *//**
     * 执行可靠写数据的请求
     * @return
     *//*
    public boolean executeReliableWrite(){
        if(mBluetoothGatt != null){
            return mBluetoothGatt.executeReliableWrite();
        }
        return false;
    }

    *//**
     * 放弃可靠写请求
     *//*
    public void abortReliableWrite(){
        if(null != mBluetoothGatt) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBluetoothGatt.abortReliableWrite();
            }
        }
    }

    *//***********************************操作*************************************************//*

    private UUID formatUUID(String uuid){
        if(null != uuid){
            return UUID.fromString(uuid);
        }
        return null;
    }

    public BluetoothDeviceBeanBackup setmOnDiscoverServiceCallBack(IOnServicesDiscoveredCallBack mOnDiscoverServiceCallBack){
        this.mOnDiscoverServiceCallBack = mOnDiscoverServiceCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnCharacteristicWriteCallBack(IOnCharacteristicWriteCallBack mOnCharacteristicWriteCallBack) {
        this.mOnCharacteristicWriteCallBack = mOnCharacteristicWriteCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnCharacteristicReadCallBack(IOnCharacteristicReadCallBack mOnCharacteristicReadCallBack) {
        this.mOnCharacteristicReadCallBack = mOnCharacteristicReadCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnCharacteristicChangedCallBack(IOnCharacteristicChangedCallBack mOnCharacteristicChangedCallBack) {
        this.mOnCharacteristicChangedCallBack = mOnCharacteristicChangedCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnDescriptorReadCallBack(IOnDescriptorReadCallBack mOnDescriptorReadCallBack) {
        this.mOnDescriptorReadCallBack = mOnDescriptorReadCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnDescriptorWriteCallBack(IOnDescriptorWriteCallBack mOnDescriptorWriteCallBack) {
        this.mOnDescriptorWriteCallBack = mOnDescriptorWriteCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnReliableWriteCompletedCallBack(IOnReliableWriteCompletedCallBack mOnReliableWriteCompletedCallBack) {
        this.mOnReliableWriteCompletedCallBack = mOnReliableWriteCompletedCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnReadRemoteRssiCallBack(IOnReadRemoteRssiCallBack mOnReadRemoteRssiCallBack) {
        this.mOnReadRemoteRssiCallBack = mOnReadRemoteRssiCallBack;
        return this;
    }

    public BluetoothDeviceBeanBackup setmOnMtuChangedCallBack(IOnMtuChangedCallBack mOnMtuChangedCallBack) {
        this.mOnMtuChangedCallBack = mOnMtuChangedCallBack;
        return this;
    }

    *//**
     * 核心回调
     * 运行在子线程
     *//*
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        *//**
         * 状态回调
         * 运行在子线程
         * @param gatt
         * @param status
         * @param newState
         *//*
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(mHandler != null){
                mHandler.removeCallbacks(connectTimeOut);
            }

            if(null != gatt){
                BluetoothDevice device = gatt.getDevice();
                if(null != getBleDevice() && getBleDevice().equals(device)) {
                    isServiceDiscovered = false;
                    replaceBleDevice(device);
                    int tempStatus = connectStatus;
                    connectStatus = newState;
                    mBluetoothGatt = gatt;
                    if(newState == BluetoothProfile.STATE_DISCONNECTED
                            && tempStatus != BluetoothProfile.STATE_CONNECTED) {
                        //当前状态为断开连接，但从未连接成功过，所以认为是连接失败
                        poastConnectState(BluetoothDeviceBeanBackup.this, STATE_CONNECT_FAILD);
                    }else{
                        poastConnectState(BluetoothDeviceBeanBackup.this, newState);
                    }
                }else{//应该通知，让其他对象知道该对象关闭了
                    poastConnectState(null,newState);//应该永远不会调用到
                    gatt.close();
                }

                if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    close();//需要这么调用吗？
                }

                if(LogUtil.isEnable()) {
                    if(null != device) {
                        LogUtil.i("device name:" + device.getName()
                                + "\ndevice mac:" + device.getAddress()
                                + "\nsuccessStatus:" + status
                                + "\nconnectState:" + newState);
                    }
                }
            }
        }

        *//**
         * 运行在子线程
         * @param gatt
         * @param status
         *//*
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = getServices();
            if(services != null && services.size() > 0){
                isServiceDiscovered = true;
            }
            if(mOnDiscoverServiceCallBack != null){
                mOnDiscoverServiceCallBack.onServicesDiscovered(BluetoothDeviceBeanBackup.this,services,status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(null != mOnCharacteristicReadCallBack){
                mOnCharacteristicReadCallBack.onCharacteristicRead(BluetoothDeviceBeanBackup.this,characteristic,status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(null != mOnCharacteristicWriteCallBack){
                mOnCharacteristicWriteCallBack.onCharacteristicWrite(BluetoothDeviceBeanBackup.this,characteristic,status);
            }
        }

        *//**
         * 运行在子线程
         * @param gatt
         * @param characteristic
         *//*
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(null != mOnCharacteristicChangedCallBack){
                mOnCharacteristicChangedCallBack.onCharacteristicChanged(BluetoothDeviceBeanBackup.this,characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            if(null != mOnDescriptorReadCallBack){
                mOnDescriptorReadCallBack.onDescriptorRead(BluetoothDeviceBeanBackup.this,descriptor,status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if(null != mOnDescriptorWriteCallBack){
                mOnDescriptorWriteCallBack.onDescriptorWrite(BluetoothDeviceBeanBackup.this,descriptor,status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            if(null != mOnReliableWriteCompletedCallBack){
                mOnReliableWriteCompletedCallBack.onReliableWriteCompleted(BluetoothDeviceBeanBackup.this,status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            setRssi(rssi);
            if(null != mOnReadRemoteRssiCallBack){
                mOnReadRemoteRssiCallBack.onReadRemoteRssi(BluetoothDeviceBeanBackup.this,status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if(null != mOnMtuChangedCallBack){
                mOnMtuChangedCallBack.onMtuChanged(BluetoothDeviceBeanBackup.this,mtu,status);
            }
        }
    };*/
}
