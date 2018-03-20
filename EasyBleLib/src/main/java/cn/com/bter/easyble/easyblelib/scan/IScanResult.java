package cn.com.bter.easyble.easyblelib.scan;

import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;

/**
 * 扫描结果接口
 * Created by admin on 2017/10/24.
 */

public interface IScanResult {
    /**
     * 没有找到设备
     */
    void notFound(String macOrFuzzyName, boolean isMac);

    /**
     * 找到设备
     * @param device
     */
    void found(BluetoothDeviceBean device);
}
