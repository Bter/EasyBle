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
import cn.com.bter.easyble.easyblelib.utils.LogUtil;

/**
 * BLE设备管理，含连接状态部分
 * http://blog.csdn.net/qingtiantianqing/article/details/52459629?locationNum=13
 * Created by admin on 2017/10/19.
 */

public abstract class DeviceConnectBean extends BluetoothDeviceBase {
    private static final String TAG = DeviceConnectBean.class.getSimpleName();

    /**
     * 连接失败
     * 没有成功连接过然后直接返回BluetoothProfile.STATE_DISCONNECTED，会认为是连接失败
     */
    public static final int STATE_CONNECT_FAILD = ~BluetoothProfile.STATE_DISCONNECTED;


    /**
     * 连接状态
     */
    private int connectStatus = BluetoothProfile.STATE_DISCONNECTED;
    private BluetoothGatt mBluetoothGatt;
    private IConnectStateListener mConnectStateListener;


    private boolean isServiceDiscovered = false;
    private Handler mHandler;

    DeviceConnectBean(BluetoothDevice device, int rssi, byte[] scanRecord, Handler mHandler) {
        super(device,rssi,scanRecord);
        this.mHandler = mHandler;
        init();
    }

    /**
     * 初始化
     */
    private void init(){
    }

    /**
     * 连接
     * 最好在主线程中调用?
     * 如果已经连接直接回调
     * 连接前应该还要判断蓝牙是否打开了
     * 15s未连接上认为连接失败
     * @param autoConnect
     */
    final void connect(Context context, boolean autoConnect, IConnectStateListener mConnectStateListener){
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

    protected final BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    private Runnable connectTimeOut = new Runnable() {
        @Override
        public void run() {
            if(mHandler != null){
                mHandler.removeCallbacks(this);
            }
            connectStatus = BluetoothProfile.STATE_DISCONNECTED;
            poastConnectState(DeviceConnectBean.this, connectStatus);
        }
    };

    /**
     * 未调用close时重连
     * 最好在主线程中调用?
     * 比较正确的做法是调用connect
     */
    private void reConnect(){
        if(null != mBluetoothGatt && !isConnectingOrConnected()){
            connectStatus = BluetoothProfile.STATE_CONNECTING;
            poastConnectState(this, connectStatus);
            mBluetoothGatt.connect();
        }
    }

    /**
     * 断开连接
     * 最好在主线程中调用?
     */
    public void disConnect(){
        if(null != mBluetoothGatt){
            mBluetoothGatt.disconnect();
            refreshDeviceCache();
        }
    }

    /**
     * 关闭连接
     * 最好在主线程中调用?
     */
    private void close(){
        if(null != mBluetoothGatt){
            mBluetoothGatt.close();
            connectStatus = BluetoothProfile.STATE_DISCONNECTED;
        }
    }

    /**
     * Android手机会对连接过的BLE设备的Services进行缓存，
     * 若设备升级后Services等有改动，则程序会出现通讯失败。
     * 此时就得刷新缓存，反射调用BluetoothGatt类总的refresh()方法
     * 在disconnect之后且在close之前调用
     * @return
     */
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

    /**
     * 是否已经连接
     * @return
     */
    public boolean isConnected(){
        if(mBluetoothGatt != null && connectStatus == BluetoothProfile.STATE_CONNECTED){
            return true;
        }
        return false;
    }

    /**
     * 是否正在连接或者已经连接
     * @return
     */
    public boolean isConnectingOrConnected(){
        return connectStatus == BluetoothProfile.STATE_CONNECTED
                || connectStatus == BluetoothProfile.STATE_CONNECTING;
    }

    /**
     * 发现服务
     * 是否发现成功的前提是蓝牙已经连接
     * @return
     */
    private boolean discoverServices(){
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


    /**
     * 获得已经连接的BLE设备
     * @return 如果没有连接会返回null
     */
    public BluetoothDevice getConnectDevice(){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.getDevice();
        }
        return null;
    }

    /**
     * 获取连接状态
     * @return one of {@link BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}
     * or {@link BluetoothProfile#STATE_CONNECTING}
     */
    public int getConnectStatus() {
        return connectStatus;
    }

    /**
     * 抛出连接状态
     * @param device
     * @param currentState
     */
    private void poastConnectState(DeviceConnectBean device, int currentState){
        if(mConnectStateListener != null){
            mConnectStateListener.connectStateChange(device,currentState);
        }
    }

    /**
     * 读取当前连接的设备的信号强度
     * @return {@link BluetoothGatt#readRemoteRssi()}
     * data call back {@link #onReadRemoteRssi(DeviceConnectBean, int)}
     */
    public boolean readRemoteRssi(){
        if(isConnected() && mBluetoothGatt != null){
            return mBluetoothGatt.readRemoteRssi();
        }
        return false;
    }


    /**
     *
     * @param mtu
     * @return {@link BluetoothGatt#requestMtu(int)}
     */
    public boolean requestMtu(int mtu){
        if(mBluetoothGatt != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mBluetoothGatt.requestMtu(mtu);
            }
        }
        return false;
    }

    /***********************************操作*************************************************/

    protected final UUID formatUUID(String uuid){
        if(null != uuid){
            return UUID.fromString(uuid);
        }
        return null;
    }

    /**
     * 核心回调
     * 运行在子线程
     */
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * 状态回调
         * 运行在子线程
         * @param gatt
         * @param status
         * @param newState
         */
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
                        poastConnectState(DeviceConnectBean.this, STATE_CONNECT_FAILD);
                    }else{
                        if(newState == BluetoothProfile.STATE_CONNECTED){
                            discoverServices();
                        }else {
                            poastConnectState(DeviceConnectBean.this, newState);
                        }
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

        /**
         * 运行在子线程
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = getServices();
            if(services != null && services.size() > 0){
                isServiceDiscovered = true;
                poastConnectState(DeviceConnectBean.this, BluetoothProfile.STATE_CONNECTED);
            }else{
                disConnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            DeviceConnectBean.this.onCharacteristicRead(DeviceConnectBean.this,characteristic,status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            DeviceConnectBean.this.onCharacteristicWrite(DeviceConnectBean.this,characteristic,status);
        }

        /**
         * 运行在子线程
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            DeviceConnectBean.this.onCharacteristicChanged(DeviceConnectBean.this,characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            DeviceConnectBean.this.onDescriptorRead(DeviceConnectBean.this,descriptor,status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            DeviceConnectBean.this.onDescriptorWrite(DeviceConnectBean.this,descriptor,status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            DeviceConnectBean.this.onReliableWriteCompleted(DeviceConnectBean.this,status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            setRssi(rssi);
            DeviceConnectBean.this.onReadRemoteRssi(DeviceConnectBean.this,status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            DeviceConnectBean.this.onMtuChanged(DeviceConnectBean.this,mtu,status);
        }
    };

    protected abstract void onMtuChanged(DeviceConnectBean deviceConnectBean, int mtu, int status);
    protected abstract void onReadRemoteRssi(DeviceConnectBean deviceConnectBean, int status);
    protected abstract void onReliableWriteCompleted(DeviceConnectBean deviceConnectBean, int status);
    protected abstract void onDescriptorWrite(DeviceConnectBean deviceConnectBean, BluetoothGattDescriptor descriptor, int status);
    protected abstract void onDescriptorRead(DeviceConnectBean deviceConnectBean, BluetoothGattDescriptor descriptor, int status);
    protected abstract void onCharacteristicChanged(DeviceConnectBean deviceConnectBean, BluetoothGattCharacteristic characteristic);
    protected abstract void onCharacteristicWrite(DeviceConnectBean deviceConnectBean, BluetoothGattCharacteristic characteristic, int status);
    protected abstract void onCharacteristicRead(DeviceConnectBean deviceConnectBean, BluetoothGattCharacteristic characteristic, int status);
}
