package cn.com.bter.easyble.interfaces;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicChangedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicReadCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;

/**
 * Created by admin on 2017/10/25.
 */

public interface OptionsCallBack extends IOnCharacteristicChangedCallBack,IOnCharacteristicReadCallBack,IOnCharacteristicWriteCallBack {
}
