package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;

class DataUnit {
    private BluetoothGattCharacteristic characteristic;
    private IOnCharacteristicWriteCallBack callBack;
    private int writeType;
    private byte[] dataBuf;
    private ByteArrayInputStream bais;
    private int count = 0;

    public DataUnit(BluetoothGattCharacteristic characteristic, IOnCharacteristicWriteCallBack callBack, int writeType, byte[] dataBuf) {
        this.characteristic = characteristic;
        this.callBack = callBack;
        this.writeType = writeType;
        this.dataBuf = dataBuf;
        bais = new ByteArrayInputStream(dataBuf);
    }

    IOnCharacteristicWriteCallBack getCallBack() {
        return callBack;
    }

    byte[] getDataBuf() {
        return dataBuf;
    }

    /**
     * 是否已经发送完毕
     * @return
     */
    boolean isEnd(){
        if(dataBuf != null && dataBuf.length != count){
            return false;
        }
        return true;
    }

    synchronized BluetoothGattCharacteristic getNextData(int mtu){
        if(bais == null || characteristic == null)return null;

        byte[] buf = new byte[mtu];
        if(mtu > 0){
            try {
                int len = bais.read(buf);
                if(len == -1){
                    return null;
                }
                count += len;

                byte[] value;
                if(mtu == len){
                    value = buf;
                }else{
                    value = new byte[len];
                    System.arraycopy(buf,0,value,0,len);
                }
                characteristic.setValue(value);
                characteristic.setWriteType(writeType);
                return characteristic;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
