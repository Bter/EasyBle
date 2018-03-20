package cn.com.bter.easyble.interfaces;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicChangedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicReadCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnServicesDiscoveredCallBack;

/**
 * Created by admin on 2017/10/25.
 */

public interface OptionsCallBack extends IOnServicesDiscoveredCallBack,IOnCharacteristicChangedCallBack,IOnCharacteristicReadCallBack,IOnCharacteristicWriteCallBack {
}
