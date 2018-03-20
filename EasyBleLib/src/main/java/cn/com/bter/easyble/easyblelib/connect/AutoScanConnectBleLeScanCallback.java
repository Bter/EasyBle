package cn.com.bter.easyble.easyblelib.connect;

import cn.com.bter.easyble.easyblelib.core.EasyBleManager;
import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;
import cn.com.bter.easyble.easyblelib.scan.FilterBleLeScanCallback;
import cn.com.bter.easyble.easyblelib.scan.IScanResult;

/**
 * 自动连接扫描器
 * Created by admin on 2017/10/24.
 */

public class AutoScanConnectBleLeScanCallback extends FilterBleLeScanCallback {
    private EasyBleManager manager;
    private boolean isFound = false;
    private BluetoothDeviceBean mDevice;
    private boolean autoConnect;

    /**
     * 扫描结果接口
     */
    private IScanResult mScanResult;

    private boolean isCanCallBackResult = true;

    public AutoScanConnectBleLeScanCallback(String name, EasyBleManager manager, String deviceNameOrMac, int timeOut, boolean isAddressFilter
            , boolean autoConnect) {
        this(name,manager,deviceNameOrMac, timeOut, isAddressFilter, true,null);
    }

    public AutoScanConnectBleLeScanCallback(String name, EasyBleManager manager, String deviceNameOrMac, int timeOut, boolean isAddressFilter
            , boolean autoConnect, IScanResult mScanResult) {
        super(name,deviceNameOrMac, timeOut, isAddressFilter, true);
        this.manager = manager;
        this.autoConnect = autoConnect;
        setBleDeviceStateListener(mScanResult);
    }

    public void setBleDeviceStateListener(IScanResult mScanResult){
        this.mScanResult = mScanResult;
    }

    /**
     *
     * @param isCanCallBackResult
     */
    public void setIsCanCallBackResult(boolean isCanCallBackResult){
        this.isCanCallBackResult = isCanCallBackResult;
    }

    /**
     * 开始扫描通知
     */
    @Override
    protected void notifyStartScan() {
        isCanCallBackResult = true;
        isFound = false;
        mDevice = null;
    }

    @Override
    protected void onScanTimeOut() {
        scanResult();
        clearDevices();
    }

    @Override
    protected void onScanCancel() {
        scanResult();
        clearDevices();
    }

    /**
     * 扫描结果
     */
    private void scanResult(){
        if(isCanCallBackResult) {
            if (isFound) {//未找到设备
                if (null != mScanResult) {
                    mScanResult.found(mDevice);
                }
                manager.connectBle(mDevice, autoConnect);
            } else {
                if (null != mScanResult) {
                    mScanResult.notFound(getFilterArg(), isAddressFilter());
                }
            }
        }
    }

    /**
     * 扫描到设备
     * @param device
     */
    @Override
    protected void onLeScanFound(BluetoothDeviceBean device) {
        isFound = true;
        if(null == mDevice) {
            this.mDevice = device;
        }
        manager.tryStopAutoConnectBle(true);//已经找到设备，停止扫描后连接
    }

    @Override
    protected void refresh(BluetoothDeviceBean device) {

    }
}
