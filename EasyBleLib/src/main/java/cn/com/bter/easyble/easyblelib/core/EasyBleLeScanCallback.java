package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import cn.com.bter.easyble.easyblelib.utils.LogUtil;

/**
 * 可能会运行在子线程中<br\>
 * 加入超时的扫描监听类
 * Created by admin on 2017/10/23.
 */

public abstract class EasyBleLeScanCallback implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = EasyBleLeScanCallback.class.getSimpleName();

    public static final int DEFAULT_TIME_OUT = 10000;
    private final String name;
    private int timeOut = DEFAULT_TIME_OUT;//扫描超时时间
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private BleScanManager mBleScanManager;
    private boolean isTimeOut = false;

    private boolean isScaning = false;

    public EasyBleLeScanCallback(String name){
        this(name,DEFAULT_TIME_OUT);
    }

    public EasyBleLeScanCallback(String name,int timeOut){
        this.name = name;
        setTimeOut(timeOut);
    }

    public String getName() {
        return name;
    }

    public final EasyBleLeScanCallback setTimeOut(int timeOut) {
        if(timeOut > 0) {
            this.timeOut = timeOut;
        }
        return this;
    }

    /**
     * 开始扫描通知
     * 运行在子线程
     */
    final void nofifyStartScan(BleScanManager mBleScanManager){
        LogUtil.i(TAG,">>>>>>>>>>>>>>>>>>start scan (" + (name == null ? "unkonw" : name) + ")");
        isScaning = true;
        isTimeOut = false;
        this.mBleScanManager = mBleScanManager;
        mHandler.removeCallbacks(timeOutTask);
        mHandler.postDelayed(timeOutTask,timeOut);
        onStartScan();
    }

    private Runnable timeOutTask = new Runnable() {
        @Override
        public void run() {
            if(null != mBleScanManager){
                isTimeOut = true;
                mHandler.removeCallbacks(this);
                mBleScanManager.stopScan(EasyBleLeScanCallback.this);
            }
        }
    };

    /**
     * 扫描到
     * @param device
     * @param rssi
     * @param scanRecord
     */
    @Override
    public final void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(device != null){
            if(!isHad(device)) {
                if(null != mBleScanManager) {
                    BluetoothDeviceBean mDevice = mBleScanManager.getBluetoothDeviceBeanInstance(device, rssi, scanRecord);
                    if(null != mDevice) {
                        onLeScan(mDevice);
                    }
                }
            }else{
                if(null != mBleScanManager) {
                    mBleScanManager.refreshDevice(device, rssi, scanRecord);
                }
                refresh(device,rssi,scanRecord);
            }
        }
    }

    public final boolean isScaning(){
        return isScaning;
    }

    /**
     * 停止扫描通知
     */
    final void nofifyStopScan(){
        isScaning = false;
        mHandler.removeCallbacks(timeOutTask);
//        this.mBleScanManager = null;

        if(isTimeOut){
            onScanTimeOut();
        }else{
            onScanCancel();
        }
    }

    /**
     * 开始扫描<br\>
     * 运行在子线程
     */
    protected abstract void onStartScan();

    /**
     * 扫描到，转化后的对象<br\>
     * 运行在UI线程
     * @param device
     */
    protected abstract void onLeScan(BluetoothDeviceBean device);

    /**
     * 刷新列表中已经存在的设备<br\>
     * 运行在UI线程
     * @param device
     * @param rssi
     * @param scanRecord
     */
    protected abstract void refresh(BluetoothDevice device, int rssi, byte[] scanRecord);

    /**
     * 扫描超时<br\>
     * 运行主线程
     */
    protected abstract void onScanTimeOut();

    /**
     * 主动取消扫描<br\>
     * 运行在调用的线程
     */
    protected abstract void onScanCancel();

    /**
     * 列表中是否已经存在<br\>
     * 如果已经存在则扫描到设备是会调用refresh，实现过滤效果<br\>
     * 否则创建新的BluetoothDeviceBean实例<br\>
     * 运行在UI线程
     * @param device
     * @return
     * 返回true将会调用refresh(BluetoothDevice device, int rssi, byte[] scanRecord)方法;<br\>
     * 返回false将会调用onLeScan(BluetoothDeviceBean device)方法<br\>
     */
    protected abstract boolean isHad(BluetoothDevice device);
}
