package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.io.ByteArrayInputStream;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;

public class SendControl {
    private BluetoothDeviceBean deviceBean;
    private BluetoothGattCharacteristic characteristic;
    private ByteArrayInputStream inputStream;

    private IOnCharacteristicWriteCallBack writeCallBackOut;
    private IOnCharacteristicWriteCallBack writeCallBack = new IOnCharacteristicWriteCallBack() {
        @Override
        public void onCharacteristicWrite(BluetoothDeviceBean device, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){

            }else{

            }
        }
    };

    public SendControl(BluetoothDeviceBean deviceBean, BluetoothGattCharacteristic characteristic) {
        this.deviceBean = deviceBean;
        this.characteristic = characteristic;
    }

    IOnCharacteristicWriteCallBack getWriteCallBack(){
        return writeCallBack;
    }

    void setWriteCallBackOut(IOnCharacteristicWriteCallBack writeCallBackOut){
        this.writeCallBackOut = writeCallBackOut;
    }

    void writeNext(){
//        characteristic.setValue();
        deviceBean.writeCharacteristicInner(characteristic);
    }
}
