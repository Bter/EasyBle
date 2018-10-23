package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;


import java.util.concurrent.ConcurrentLinkedQueue;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;

/**
 * 发送控制
 */
class SendControl {
    private BluetoothDeviceBean deviceBean;
    private ConcurrentLinkedQueue<DataUnit> linkedQueue = new ConcurrentLinkedQueue();
    private DataUnit currentDataUnit;
    private boolean isWriteing = false;
    private long lastWriteTime = -1;
    /**
     * 延迟时间
     * 发完上一条数据与下一条数据的最小间隔
     * 负数表示不做延迟
     */
    private int delayTime = -1;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private IOnCharacteristicWriteCallBack writeCallBack = new IOnCharacteristicWriteCallBack() {
        @Override
        public void onCharacteristicWrite(BluetoothDeviceBean device, byte[] data, int status) {
            if(status != BluetoothGatt.GATT_SUCCESS){
                notifyResult(currentDataUnit,BluetoothGatt.GATT_FAILURE);
                writeNext();
                return;
            }
            doWrite();
        }
    };

    public SendControl(BluetoothDeviceBean deviceBean) {
        this.deviceBean = deviceBean;
    }

    boolean isWriteing() {
        return isWriteing;
    }

    /**
     * 蓝牙断开
     */
    void bleDisconnect(){
        isWriteing = false;
        linkedQueue.clear();
    }

    /**
     * 设置写间隔<br/>
     * 作用：可用于调节带宽，防止数据带宽过大导致对端下位机处理不过来，<br/>
     * 主要是由于下位机性能低导致的。<br/>
     *
     * <br/>
     * 一般如果要设置都是至少30ms<br/>
     * 负数表示不进行间隔<br/>
     * @param delayTime ms
     */
    void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    IOnCharacteristicWriteCallBack getWriteCallBack(){
        return writeCallBack;
    }

    synchronized void write(BluetoothGattCharacteristic characteristic,IOnCharacteristicWriteCallBack callBack,byte[] dataBuf,int writeType){
        linkedQueue.add(new DataUnit(characteristic, callBack, writeType, dataBuf));
        if(!isWriteing) {
            isWriteing = true;
            doWrite();
        }
    }

    /**
     * 取消发送
     * @param callBack
     * @param dataBuf
     */
    public boolean cancleSend(IOnCharacteristicWriteCallBack callBack,byte[] dataBuf){
        return linkedQueue.remove(new DataUnit(callBack,dataBuf));
    }

    private void doWrite(){
        if(currentDataUnit == null){
            writeNext();
        }else if(currentDataUnit.isEnd()){
            notifyResult(currentDataUnit,BluetoothGatt.GATT_SUCCESS);
            writeNext();
        }else{
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastWriteTime < delayTime){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doWrite();
                    }
                },(delayTime - (currentTime - lastWriteTime)) );
                return;
            }
            lastWriteTime = currentTime;

            if(!deviceBean.writeCharacteristicInner(currentDataUnit.getNextData(deviceBean.getMtu()))){
                //发送失败
                notifyResult(currentDataUnit,BluetoothGatt.GATT_FAILURE);
                writeNext();
            }
        }
    }

    private void writeNext(){
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastWriteTime < delayTime){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    writeNext();
                }
            },(delayTime - (currentTime - lastWriteTime)) );
            return;
        }
        lastWriteTime = currentTime;

        currentDataUnit = linkedQueue.poll();
        if(currentDataUnit == null){
            //队列中没有再可发的数据
            isWriteing = false;
            return;
        }
        if(!deviceBean.writeCharacteristicInner(currentDataUnit.getNextData(deviceBean.getMtu()))){
            //发送失败
            notifyResult(currentDataUnit,BluetoothGatt.GATT_FAILURE);
            writeNext();
        }
    }

    private void notifyResult(final DataUnit currentDataUnit, final int result){
        WriteCallThread.getInstance().excuteTask(new Runnable() {
            @Override
            public void run() {
                try {
                    IOnCharacteristicWriteCallBack callBack;
                    if(currentDataUnit != null && (callBack = currentDataUnit.getCallBack()) != null) {
                        callBack.onCharacteristicWrite(deviceBean, currentDataUnit.getDataBuf(), result);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
