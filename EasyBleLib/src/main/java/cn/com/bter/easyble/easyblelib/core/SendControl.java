package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;


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
            if(!deviceBean.writeCharacteristicInner(currentDataUnit.getNextData(deviceBean.getMtu()))){
                //发送失败
                notifyResult(currentDataUnit,BluetoothGatt.GATT_FAILURE);
                writeNext();
            }
        }
    }

    private void writeNext(){
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

    private void notifyResult(DataUnit currentDataUnit,int result){
        IOnCharacteristicWriteCallBack callBack;
        if(currentDataUnit != null && (callBack = currentDataUnit.getCallBack()) != null) {
            callBack.onCharacteristicWrite(deviceBean, currentDataUnit.getDataBuf(), result);
        }
    }
}
