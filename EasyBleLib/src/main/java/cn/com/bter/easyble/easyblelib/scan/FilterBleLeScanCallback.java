package cn.com.bter.easyble.easyblelib.scan;

import android.bluetooth.BluetoothDevice;

import java.util.HashMap;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;
import cn.com.bter.easyble.easyblelib.core.EasyBleLeScanCallback;
import cn.com.bter.easyble.easyblelib.utils.LogUtil;

/**
 * 过滤扫描器<br\>
 * 可实现模糊地址过滤或模糊名称过滤<br\>
 * 默认为MAC地址过滤<br\>
 * 默认可以进行过滤
 * Created by admin on 2017/10/24.
 */

public abstract class FilterBleLeScanCallback extends EasyBleLeScanCallback {
    private static final String TAG = FilterBleLeScanCallback.class.getSimpleName();
    /**
     * 存储扫描到的设备
     */
    private HashMap<String,BluetoothDeviceBean> devices = new HashMap<>();

    /**
     * 是否是地址过滤
     */
    private boolean isAddressFilter;

    /**
     * 是否可以进行过滤
     */
    private final boolean isEnableFilter;

    /**
     * 过滤的字段
     */
    private String filterArg;

    /**
     * 不过滤
     */
    public FilterBleLeScanCallback(String name){
        this(name,EasyBleLeScanCallback.DEFAULT_TIME_OUT);
    }

    /**
     * 不过滤<br\>
     * 但指定超时时间
     * @param timeOut 超时时间
     */
    public FilterBleLeScanCallback(String name,int timeOut){
        this(name,"",timeOut);
    }

    /**
     * 过滤<br\>
     * 且为mac过滤<br\>
     * 且指定超时时间
     *
     * @param deviceNameOrMac MAC地址
     * @param timeOut 超时时间
     */
    public FilterBleLeScanCallback(String name,String deviceNameOrMac,int timeOut) {
        this(name,deviceNameOrMac,timeOut,true,true);
    }

    /**
     * 指定是否过滤<br\>
     * 指定过滤类型<br\>
     * 指定超时时间
     * @param deviceNameOrMac MAC或模糊名称
     * @param timeOut 超时时间
     * @param isAddressFilter 是否是MAC
     * @param isEnableFilter 是否要进行过滤
     */
    public FilterBleLeScanCallback(String name,String deviceNameOrMac,int timeOut,boolean isAddressFilter,boolean isEnableFilter) {
        super(name,timeOut);
        this.filterArg = deviceNameOrMac;
        this.isAddressFilter = isAddressFilter;
        this.isEnableFilter = isEnableFilter;
    }

    public void changeFilterArg(String deviceNameOrMac,int timeOut,boolean isAddressFilter){
        if(!isScaning()) {
            setTimeOut(timeOut);
            this.filterArg = deviceNameOrMac;
            this.isAddressFilter = isAddressFilter;
        }else{
            LogUtil.w(TAG,"changeFilterArg faild,because this object isScaning,please stop scan then changeFilterArg");
        }
    }

    protected final String getFilterArg(){
        return filterArg;
    }

    protected final boolean isAddressFilter(){
        return isAddressFilter;
    }

    @Override
    protected final void onLeScan(BluetoothDeviceBean device) {
        if(isEnableFilter) {
            String deviceNameOrMac = device.getBleDevice().getAddress();
            if (!isAddressFilter) {
                deviceNameOrMac = device.getBleDevice().getName();
            }
            if (null != deviceNameOrMac && deviceNameOrMac.contains(filterArg)) {//利用包含来实现模糊过滤
                devices.put(device.getBleDevice().getAddress(), device);
                onLeScanFound(device);
            }
        }else{
            devices.put(device.getBleDevice().getAddress(), device);
            onLeScanFound(device);
        }
    }

    @Override
    protected final void refresh(BluetoothDevice device, int rssi, byte[] scanRecord) {
        BluetoothDeviceBean mDevice = devices.get(device.getAddress());
        if(null != mDevice){
            mDevice.replaceBleDevice(device)
                    .setRssi(rssi)
                    .setScanRecord(scanRecord);
            refresh(mDevice);
        }
    }

    /**
     * 发现新的设备<br\>
     * 运行在UI线程
     * @param device
     */
    protected abstract void onLeScanFound(BluetoothDeviceBean device);

    /**
     * 刷新列表中已经存在的设备<br\>
     * 运行在UI线程
     * @param device
     */
    protected abstract void refresh(BluetoothDeviceBean device);

    @Override
    public final boolean isHad(BluetoothDevice device) {
        if(isEnableFilter) {
            String deviceNameOrMac = device.getAddress();
            if (!isAddressFilter) {
                deviceNameOrMac = device.getName();
            }
            boolean isContains = false;
            if (null != deviceNameOrMac) {
                isContains = deviceNameOrMac.contains(filterArg);
            }
            return devices.get(device.getAddress()) != null || !isContains;//欺骗手法，利用!isContains来减少BluetoothDeviceBean对象的创建
        }else{
            return devices.get(device.getAddress()) != null;
        }
    }

    @Override
    protected final void onStartScan() {
        clearDevices();
        notifyStartScan();
    }

    /**
     * 通知开始扫描运行在子线程
     */
    protected abstract void notifyStartScan();

    /**
     * 清除扫描到的设备
     */
    protected final void clearDevices(){
        devices.clear();
    }

    private final void removeDevice(String mac){
        devices.remove(mac);
    }
}
