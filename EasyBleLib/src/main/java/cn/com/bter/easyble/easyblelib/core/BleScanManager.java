package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import cn.com.bter.easyble.easyblelib.utils.LogUtil;


/**
 * BLE扫描管理器
 * 可以多个同时扫描？
 * 扫描和连接互不影响？
 * 扫描是耗时的，应放在子线程中进行
 * Created by admin on 2017/10/19.
 */

public class BleScanManager {
    private static final String TAG = BleScanManager.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    /**
     * 扫描池
     */
    private HashSet<EasyBleLeScanCallback> scanCallbacks = new HashSet<>();

    /**
     * 设备池
     */
    private HashMap<String,BluetoothDeviceBean> allFoundDevices = new HashMap<>();

    private boolean autoManagerDevices = true;

    /**
     * 扫描线粗
     */
    private HandlerThread scanThread = new HandlerThread("scanThread");
    private Handler scanHandler;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private final int deviceInvalidTime = 1000 * 60 * 5;//5min
    private int clearInvalidDevicesCycleTime = 10000;//10s
    private boolean isclearInvalidDeviceTaskRun = false;

    {
        scanThread.start();
    }

    public BleScanManager(){
        scanHandler = new Handler(scanThread.getLooper());
    }

    /**
     * 开启自动维护设备池任务
     * 默认开启<br\>
     */
    public void enableAutoManagerDevices(){
        autoManagerDevices = true;
        scanHandler.removeCallbacks(clearInvalidDeviceTask);
        if(autoManagerDevices) {
            scanHandler.postDelayed(clearInvalidDeviceTask, clearInvalidDevicesCycleTime);
        }
    }

    /**
     * 取消自动维护设备池任务<br\>
     * 默认开启<br\>
     * 设备管理更为精准精确，<br\>
     * 但会导致内存使用的增加，<br\>
     * 直到destroy被调用
     */
    public void cancelAutoManagerDevices(){
        autoManagerDevices = false;
        isclearInvalidDeviceTaskRun = false;
        scanHandler.removeCallbacks(clearInvalidDeviceTask);
    }

    /**
     *清除无效设备任务
     */
    private Runnable clearInvalidDeviceTask = new Runnable() {
        @Override
        public void run() {
            scanHandler.removeCallbacks(this);
            clearInvalidDevice();
            if(autoManagerDevices) {
                scanHandler.postDelayed(this, clearInvalidDevicesCycleTime);
            }
        }
    };

    /**
     * 清除无效设备<br\>
     * 未连接且上次被扫描到的时间距离现在超过5分钟
     */
    private void clearInvalidDevice(){
        if (allFoundDevices.size() > 0) {
            synchronized (this){
                Iterator<BluetoothDeviceBean> iterator = allFoundDevices.values().iterator();
                long time = System.currentTimeMillis();
                while (iterator.hasNext()){
                    BluetoothDeviceBean device = iterator.next();
                    if(null != device && device.getConnectStatus() == BluetoothProfile.STATE_DISCONNECTED
                            && (time - device.getLastRefreshRssiTime() > deviceInvalidTime)){
                        //未连接且上次被扫描到的时间距离现在超过5分钟
                        iterator.remove();
                    }
                }
            }
        }
    }

    protected void clearAllDevicesBluttoothAdapterClose(){
        allFoundDevices.clear();
    }

    /**
     * 获取新设备
     * @param device
     * @param rssi
     * @param scanRecord
     * @return
     */
    BluetoothDeviceBean getBluetoothDeviceBeanInstance(BluetoothDevice device, int rssi, byte[] scanRecord){
        if(null != device){
            BluetoothDeviceBean mDevice = null;
            synchronized (this) {
                mDevice = allFoundDevices.get(device.getAddress());
            }

            if (mDevice == null) {
                mDevice = new BluetoothDeviceBean(device, rssi, scanRecord,mHandler);
                synchronized (this) {
                    allFoundDevices.put(device.getAddress(), mDevice);
                }
            } else {
                mDevice.replaceBleDevice(device).setRssi(rssi).setScanRecord(scanRecord);
            }
            return mDevice;
        }
        return null;
    }

    /**
     * 刷新设备
     * @param device
     * @param rssi
     * @param scanRecord
     * @return
     */
    BluetoothDeviceBean refreshDevice(BluetoothDevice device, int rssi, byte[] scanRecord){
        if(null != device){
            BluetoothDeviceBean mDevice = null;
            synchronized (this) {
                mDevice = allFoundDevices.get(device.getAddress());
            }

            if (mDevice != null) {
                mDevice.replaceBleDevice(device).setRssi(rssi).setScanRecord(scanRecord);
            }
            return mDevice;
        }
        return null;
    }

    /**
     * 开始扫描
     * 默认超时时间为10s
     * @param easyBleLeScanCallback
     */
    public void startSacn(final EasyBleLeScanCallback easyBleLeScanCallback){
        if(!easyBleLeScanCallback.isScaning()) {
            scanHandler.post(new Runnable() {
                @Override
                public void run() {
                    startScanInner(easyBleLeScanCallback);
                }
            });
        }
    }

    /**
     * 开始扫描
     * 默认超时时间为10s
     * mBluetoothAdapter.startLeScan属于耗时操作
     * @param easyBleLeScanCallback
     */
    private boolean startScanInner(EasyBleLeScanCallback easyBleLeScanCallback){
        if(null != mBluetoothAdapter && mBluetoothAdapter.isEnabled() && null != easyBleLeScanCallback) {
            if(!easyBleLeScanCallback.isScaning()) {
//                easyBleLeScanCallback.nofifyStartScan(this);
                //返回值是否在onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)之前返回？
                //也就是要确认是不是同步的，要是异步的那么最好提前调用easyBleLeScanCallback.nofifyStartScan()
                if (mBluetoothAdapter.startLeScan(easyBleLeScanCallback)) {
                    scanCallbacks.add(easyBleLeScanCallback);
                    easyBleLeScanCallback.nofifyStartScan(this);

                    if(!isclearInvalidDeviceTaskRun) {
                        scanHandler.removeCallbacks(clearInvalidDeviceTask);
                        if(autoManagerDevices) {
                            isclearInvalidDeviceTaskRun = true;
                            scanHandler.postDelayed(clearInvalidDeviceTask, clearInvalidDevicesCycleTime);
                        }
                    }
//                easyBleLeScanCallback.onStartScan();
                    return true;
                } else {
                    stopScan(easyBleLeScanCallback);
                    LogUtil.w(TAG,">>>>>>>>>>>>>>>>>>>startSacn faild<<<<<<<<<<<<<<<<<<");
                }
            }else{
                LogUtil.w(TAG,"this easyBleLeScanCallback is scaning");
            }
        }
        return false;
    }

    /**
     * 停止扫描
     * @param easyBleLeScanCallback
     */
    public void stopScan(EasyBleLeScanCallback easyBleLeScanCallback){
        if(null != mBluetoothAdapter) {
            mBluetoothAdapter.stopLeScan(easyBleLeScanCallback);
            if(null != easyBleLeScanCallback && easyBleLeScanCallback.isScaning()) {
                easyBleLeScanCallback.nofifyStopScan();
                scanCallbacks.remove(easyBleLeScanCallback);
            }
        }
    }

    /**
     * 是否有扫描正在进行
     * @return
     */
    public boolean hasScaning(){
        return scanCallbacks.size() > 0;
    }

    /**
     * 取消所有扫描
     */
    public void cancelAllScan(){
        HashSet<EasyBleLeScanCallback> callbacks = new HashSet<>();
        callbacks.addAll(scanCallbacks);
        if(callbacks.size() > 0){
            Iterator<EasyBleLeScanCallback> callbackIterator = callbacks.iterator();
            while (callbackIterator.hasNext()){
                EasyBleLeScanCallback callback = callbackIterator.next();
                stopScan(callback);
            }
        }
    }

    /**
     * 清空创建的扫描任务
     */
    private void clearTask(){
        if(scanHandler != null){
            scanHandler.removeCallbacks(clearInvalidDeviceTask);
            scanHandler.removeCallbacksAndMessages(null);
            isclearInvalidDeviceTaskRun = false;
            clearInvalidDevice();
        }
    }

    /**
     * 销毁
     */
    public void destroy(){
        clearTask();
        scanThread.quit();
        scanThread.interrupt();
        cancelAllScan();
        allFoundDevices.clear();
    }
}
