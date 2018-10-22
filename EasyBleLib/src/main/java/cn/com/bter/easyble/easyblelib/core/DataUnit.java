package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;

public class DataUnit {
    private BluetoothGattCharacteristic characteristic;
    private IOnCharacteristicWriteCallBack callBack;
    private int writeType;
    private byte[] dataBuf;
    private ByteArrayInputStream bais;
    private int count = 0;
    private Lock lock = new ReentrantLock(true);

    public DataUnit(IOnCharacteristicWriteCallBack callBack, byte[] dataBuf) {
        this.callBack = callBack;
        this.dataBuf = dataBuf;
    }

    public DataUnit(BluetoothGattCharacteristic characteristic, IOnCharacteristicWriteCallBack callBack, int writeType, byte[] dataBuf) {
        this.characteristic = characteristic;
        this.callBack = callBack;
        this.writeType = writeType;
        this.dataBuf = dataBuf;
        bais = new ByteArrayInputStream(dataBuf);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj == null || !(obj instanceof DataUnit)){
            return false;
        }
        DataUnit item = (DataUnit) obj;
        if((callBack != null && callBack.equals(item.callBack)) || (callBack == null && item.callBack == null)){
           return Arrays.equals(dataBuf,item.dataBuf);
        }else{
            return false;
        }
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
        lock.lock();
        boolean result = true;
        if(dataBuf != null && dataBuf.length != count){
            result = false;
        }
        lock.unlock();
        return result;
    }

    BluetoothGattCharacteristic getNextData(int mtu){
        if(bais == null || characteristic == null)return null;

        lock.lock();

        byte[] buf = new byte[mtu];
        if(mtu > 0){
            try {
                int len = bais.read(buf);
                if(len == -1){
                    lock.unlock();
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
                lock.unlock();
                return characteristic;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lock.unlock();
        return null;
    }
}
