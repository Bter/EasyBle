package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;


import java.util.concurrent.ConcurrentLinkedQueue;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;

class SendControl {
    private BluetoothDeviceBean deviceBean;
    private ConcurrentLinkedQueue<DataUnit> linkedQueue = new ConcurrentLinkedQueue();
    private DataUnit currentDataUnit;
    private boolean isWriteing = false;

    private IOnCharacteristicWriteCallBack writeCallBack = new IOnCharacteristicWriteCallBack() {
        @Override
        public void onCharacteristicWrite(BluetoothDeviceBean device, byte[] data, int status) {
            if(status != BluetoothGatt.GATT_SUCCESS){
                notifyResult(BluetoothGatt.GATT_FAILURE);
            }
            doWrite();
        }
    };

    public SendControl(BluetoothDeviceBean deviceBean, BluetoothGattCharacteristic characteristic) {
        this.deviceBean = deviceBean;
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

    private void doWrite(){
        if(currentDataUnit == null){
            writeNext();
        }else if(currentDataUnit.isEnd()){
            notifyResult(BluetoothGatt.GATT_SUCCESS);
            writeNext();
        }else{
            if(deviceBean.writeCharacteristicInner(currentDataUnit.getNextData(deviceBean.getMtu()))){
                //发送失败
                notifyResult(BluetoothGatt.GATT_FAILURE);
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
        if(deviceBean.writeCharacteristicInner(currentDataUnit.getNextData(deviceBean.getMtu()))){
            //发送失败
            notifyResult(BluetoothGatt.GATT_FAILURE);
        }
    }

    private void notifyResult(int result){
        IOnCharacteristicWriteCallBack callBack = currentDataUnit.getCallBack();
        if(currentDataUnit != null && (callBack = currentDataUnit.getCallBack()) != null) {
            callBack.onCharacteristicWrite(deviceBean, currentDataUnit.getDataBuf(), result);
        }
    }
}
