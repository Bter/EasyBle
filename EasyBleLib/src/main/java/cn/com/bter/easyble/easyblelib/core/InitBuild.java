package cn.com.bter.easyble.easyblelib.core;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 初始化构建器
 */
public class InitBuild {
    private LinkedHashMap<String,String> initNotifyList = new LinkedHashMap<>();
    private InitCallBack initCallBack;
    private Map.Entry<String,String> lastInitEntry;
    private BluetoothDeviceBean deviceBean;

    public InitBuild setInitCallBack(InitCallBack initCallBack) {
        this.initCallBack = initCallBack;
        return this;
    }

    public InitBuild enableNotify(String notifyUUid, String serviceUUid){
        if(notifyUUid != null && serviceUUid != null && !notifyUUid.trim().equals("") && !serviceUUid.trim().equals("")){
            initNotifyList.put(notifyUUid,serviceUUid);
        }
        return this;
    }

    public InitBuild build(){
        return this;
    }

    void setDeviceBean(BluetoothDeviceBean deviceBean){
        this.deviceBean = deviceBean;
    }

    /**
     * 执行初始化
     */
    void doInit(){
        getNextNotifyEntry();
        if(lastInitEntry != null){
            if(!deviceBean.enableNotify(lastInitEntry.getValue(),lastInitEntry.getKey())){
                handResult(false);
            }
        }else{
            if(initCallBack != null){
                initCallBack.onComplete();
            }
            initCallBack = null;
        }
    }

    /**
     * 获取下一个要初始化的特征点
     * @return
     */
    private Map.Entry<String,String> getNextNotifyEntry(){
        lastInitEntry = null;
        Iterator<Map.Entry<String,String>> entryIterator = initNotifyList.entrySet().iterator();
        if(entryIterator.hasNext()){
            lastInitEntry = entryIterator.next();
            entryIterator.remove();
            return lastInitEntry;
        }

        return null;
    }

    /**
     * 处理初始化结果
     * @param isSuccess
     */
    void handResult(boolean isSuccess){
        if(initCallBack != null && lastInitEntry != null){
            initCallBack.onInitResult(lastInitEntry.getKey(),lastInitEntry.getValue(),isSuccess);
        }
        doInit();
    }

    public static interface InitCallBack{
        void onInitResult(String notifyUUid, String serviceUUid,boolean isSuccess);
        void onComplete();
    }
}
