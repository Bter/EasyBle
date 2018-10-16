package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.util.Collection;

import cn.com.bter.easyble.easyblelib.interfaces.IBleDeviceStateListener;
import cn.com.bter.easyble.easyblelib.connect.AutoScanConnectBleLeScanCallback;
import cn.com.bter.easyble.easyblelib.interfaces.OnBluetoothAdapterStateChangeListener;
import cn.com.bter.easyble.easyblelib.receiver.BluetoothAdapterStateChangeReceiver;

/**
 * Created by admin on 2017/10/19.
 */

public class EasyBleManager {
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BleConnectManager mBleConnectManager;
    private BleScanManager mBleScanManager;
    private IBleDeviceStateListener mBleDeviceStateListener;
    private BluetoothAdapterStateChangeReceiver mBluetoothAdapterStateChangeReceiver;

    private AutoScanConnectBleLeScanCallback mAutoScanConnectBleLeScanCallback;

    public EasyBleManager(Context mContext){
        this(mContext,null);
    }

    public EasyBleManager(Context mContext, IBleDeviceStateListener mBleDeviceStateListener){
        mBleConnectManager = new BleConnectManager(mBleDeviceStateListener);
        mBleScanManager = new BleScanManager();
        mAutoScanConnectBleLeScanCallback = new AutoScanConnectBleLeScanCallback("EasyBleManager auto scanner",this,null
                ,EasyBleLeScanCallback.DEFAULT_TIME_OUT,true,false,mBleDeviceStateListener);
        init(mContext);
    }

    /**
     * 初始化
     * @param mContext
     */
    private void init(Context mContext){
        this.mContext = mContext.getApplicationContext();
        mBluetoothAdapterStateChangeReceiver = new BluetoothAdapterStateChangeReceiver(mOnBluetoothAdapterStateChangeListener);
        this.mContext.registerReceiver(mBluetoothAdapterStateChangeReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    /**************************************基础部分********************************************/

    private OnBluetoothAdapterStateChangeListener mOnBluetoothAdapterStateChangeListener = new OnBluetoothAdapterStateChangeListener() {
        @Override
        public void onStateChange(int state, int oldState) {
            if(state == BluetoothAdapter.STATE_OFF) {
                cancelAllScan();
                mBleScanManager.clearAllDevicesBluttoothAdapterClose();
                mBleConnectManager.clearConnectedOrConnectingDevices();
                mBleConnectManager.disConnectAllDevice();
                mBleConnectManager.clearConnectedDevices();
            }
        }
    };

    /**
     * 获得连接管理器
     * @return
     */
    public BleConnectManager getBleConnectManager() {
        return mBleConnectManager;
    }

    /**
     * 蓝牙是否打开
     * @return
     */
    public boolean isEnabled(){
        if(null != mBluetoothAdapter) {
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }

    /**
     * is support ble?
     */
    public boolean isSupportBle() {
        return mContext.getApplicationContext()
                .getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 打开蓝牙
     * @param force 是否强制，强制打开的话有可能因没有权限打开而导致失败
     * @return 实际打开结果以监听到的蓝牙状态改变广播为准
     */
    public boolean enable(boolean force){
        if(null != mBluetoothAdapter) {
            if(force) {
                return mBluetoothAdapter.enable();
            }else{
                Intent requestBluetoothOn = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mContext.startActivity(requestBluetoothOn);
                return true;
            }
        }
        return false;
    }

    /**
     * 关闭蓝牙
     * @return 实际关闭结果以监听到的蓝牙状态改变广播为准
     */
    public boolean disable(){
        if(null != mBluetoothAdapter) {
            return mBluetoothAdapter.disable();
        }
        return false;
    }

    /**************************************基础部分********************************************/

    /**************************************扫描管理器部分********************************************/

    /**
     * 是否有扫描正在进行
     * @return
     */
    public boolean hasScaning(){
        return mBleScanManager.hasScaning();
    }

    /**
     * 开启自动维护设备池任务<br\>
     * {@link BleScanManager#enableAutoManagerDevices()}
     */
    public void enableAutoManagerDevices(){
        mBleScanManager.enableAutoManagerDevices();
    }

    /**
     * 取消自动维护设备池任务<br\>
     * {@link BleScanManager#cancelAutoManagerDevices()}
     */
    public void cancelAutoManagerDevices(){
        mBleScanManager.cancelAutoManagerDevices();
    }

    /**
     * 开始扫描
     * 默认超时时间为10s
     * @param easyBleLeScanCallback
     */
    public void startScan(EasyBleLeScanCallback easyBleLeScanCallback){
        mBleScanManager.startScan(easyBleLeScanCallback);
    }

    /**
     * 停止扫描
     * @param easyBleLeScanCallback
     */
    public void stopScan(EasyBleLeScanCallback easyBleLeScanCallback){
        mBleScanManager.stopScan(easyBleLeScanCallback);
    }

    /**
     * 取消所有扫描
     */
    public void cancelAllScan(){
        mBleScanManager.cancelAllScan();
    }

    /**************************************扫描管理器部分********************************************/

    /**************************************连接管理器部分********************************************/

    /**
     * 是否已经有连接
     * @return
     */
    public boolean hasConnected(){
        return mBleConnectManager.hasConnected();
    }

    /**
     * 是否已经有连接或正在连接的设备
     * @return
     */
    public boolean hasConnectedOrConnecting(){
        return mBleConnectManager.hasConnectedOrConnecting();
    }

    /**
     * 该设备是否已经连接
     * @param device
     * @return
     */
    public boolean isConnected(BluetoothDeviceBean device){
        return mBleConnectManager.isConnected(device);
    }

    /**
     * 通过mac地址判断某个设备是否已经连接
     * @param mac
     * @return
     */
    public boolean isConnected(String mac){
        return mBleConnectManager.isConnected(mac);
    }

    /**
     * 该设备是否正在连接或已经连接
     * @param device
     * @return
     */
    public boolean isConnectedOrConnecting(BluetoothDeviceBean device){
        return mBleConnectManager.isConnectedOrConnecting(device);
    }

    /**
     * 该设备是否正在连接或已经连接
     * @param mac
     * @return
     */
    public boolean isConnectedOrConnecting(String mac){
        return mBleConnectManager.isConnectedOrConnecting(mac);
    }

    /**
     * 自动扫描连接
     * 同一时刻只支持一台设备自动扫描连接
     * @param deviceNameOrMac
     * @param timeOut
     * @param isAddress
     * @param autoConnect
     */
    public void autoConnectBle(String deviceNameOrMac, int timeOut, boolean isAddress,boolean autoConnect){
        if(mAutoScanConnectBleLeScanCallback.isScaning()) {
            tryStopAutoConnectBle(false);
        }
        mAutoScanConnectBleLeScanCallback.setAutoConnect(autoConnect);
        mAutoScanConnectBleLeScanCallback.changeFilterArg(deviceNameOrMac,timeOut,isAddress);
        mBleScanManager.startScan(mAutoScanConnectBleLeScanCallback);
    }

    /**
     * 尝试停止自动扫描连接
     */
    public void tryStopAutoConnectBle(boolean isCanCallBackResult){
        mAutoScanConnectBleLeScanCallback.setIsCanCallBackResult(isCanCallBackResult);
        stopScan(mAutoScanConnectBleLeScanCallback);
    }

    /**
     * 连接指定BLE设备
     * @param device
     * @param autoConnect
     */
    public void connectBle(BluetoothDeviceBean device, boolean autoConnect){
        if(isEnabled()){
            if(null != mContext) {
                mBleConnectManager.connectBle(device, mContext, autoConnect);
            }else{
                throw new NullPointerException("mContext is null,please invoke EasyBleManager.init(Context mContext)");
            }
        }
    }

    /**
     * 设置设备连接监听器
     * @param mBleDeviceStateListener
     */
    public void setBleDeviceStateListener(IBleDeviceStateListener mBleDeviceStateListener){
        this.mBleDeviceStateListener = mBleDeviceStateListener;
        mAutoScanConnectBleLeScanCallback.setBleDeviceStateListener(mBleDeviceStateListener);
        mBleConnectManager.setBleDeviceStateListener(mBleDeviceStateListener);
    }

    /**
     * 获得已经连接的设备<br\>
     * 不要多次调用,在一个方法中调用一次即可
     * @return
     */
    public Collection<BluetoothDeviceBean> getConnectedDevices(){
        return mBleConnectManager.getConnectedDevices();
    }

    /**
     * 获得已经连接的设备或正在连接的设备
     * @return
     */
    public Collection<BluetoothDeviceBean> getConnectedOrConnectingDevices(){
        return mBleConnectManager.getConnectedOrConnectingDevices();
    }

    /**
     * 获得指定mac地址的已经连接的BLE设备
     * @param mac
     * @return
     */
    public BluetoothDeviceBean getConnectedDevice(String mac){
        return mBleConnectManager.getConnectedDevice(mac);
    }

    /**************************************连接管理器部分********************************************/

    public Context getContext(){
        return mContext;
    }

    public void destroy(){
        try {
            mContext.unregisterReceiver(mBluetoothAdapterStateChangeReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        mBleConnectManager.destroy();
        mBleScanManager.destroy();
    }
}
