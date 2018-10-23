package cn.com.bter.easyble.easyblelib.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Handler;

import java.util.List;
import java.util.UUID;

import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicChangedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicReadCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnCharacteristicWriteCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnDescriptorReadCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnDescriptorWriteCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnMtuChangedCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnReadRemoteRssiCallBack;
import cn.com.bter.easyble.easyblelib.interfaces.IOnReliableWriteCompletedCallBack;
import cn.com.bter.easyble.easyblelib.utils.LogUtil;

/**
 * BLE设备管理，含连接状态部分
 * http://blog.csdn.net/qingtiantianqing/article/details/52459629?locationNum=13
 * Created by admin on 2017/10/19.
 */

public class BluetoothDeviceBean extends DeviceConnectBean {
    private static final String TAG = BluetoothDeviceBean.class.getSimpleName();

    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private IOnCharacteristicWriteCallBack callBack;
    private IOnCharacteristicReadCallBack mOnCharacteristicReadCallBack;
    private IOnCharacteristicChangedCallBack mOnCharacteristicChangedCallBack;
    private IOnDescriptorReadCallBack mOnDescriptorReadCallBack;
    private IOnDescriptorWriteCallBack mOnDescriptorWriteCallBack;
    private IOnReliableWriteCompletedCallBack mOnReliableWriteCompletedCallBack;
    private IOnReadRemoteRssiCallBack mOnReadRemoteRssiCallBack;
    private IOnMtuChangedCallBack mOnMtuChangedCallBack;

    private SendControl sendControl;
    /**
     * 是否进行发送控制
     */
    private boolean isControlSend = true;


    BluetoothDeviceBean(BluetoothDevice device, int rssi, byte[] scanRecord,Handler mHandler) {
        super(device,rssi,scanRecord,mHandler);
        sendControl = new SendControl(this);
    }

    /***********************************操作*************************************************/

    private BluetoothGattCharacteristic getCharacteristic(String serviceUUID,String characteristicUUID,String erroMsg){
        if(erroMsg == null){
            erroMsg = "getCharacteristic";
        }
        if(!isConnected()){
            LogUtil.w(TAG, String.format(">>>>>>%s fail,this device is disconected<<<<<",erroMsg));
            return null;
        }
        if(serviceUUID != null){
            if(characteristicUUID != null){
                UUID mServiceUUID = formatUUID(serviceUUID);
                UUID mNotifyUUID = formatUUID(characteristicUUID);
                if(mServiceUUID != null && mNotifyUUID != null ) {
                    BluetoothGattService service = getService(mServiceUUID);
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(mNotifyUUID);
                        if (characteristic != null) {
                            return characteristic;
                        }else{
                            LogUtil.w(TAG, String.format(">>>>>>%s fail,not found this %sUUID(" + characteristicUUID + ")<<<<<",erroMsg,erroMsg));
                        }
                    } else {
                        LogUtil.w(TAG, String.format(">>>>>>%s fail,not found this serviceUUID(" + serviceUUID + ")<<<<<",erroMsg));
                    }
                }else{
                    LogUtil.w(TAG, String.format(">>>>>>%s fail,serviceUUID(" + serviceUUID + ") or %sUUID(" + characteristicUUID + ") format faild<<<<<",erroMsg,erroMsg));
                }
            }else{
                LogUtil.w(TAG,String.format(">>>>>>%s fail, %sUUID is null<<<<<",erroMsg,erroMsg));
            }
        }else{
            LogUtil.w(TAG,String.format(">>>>>>%s fail, serviceUUID is null<<<<<",erroMsg));
        }
        return null;
    }

    public void initNotifys(){

    }

    /**
     * enable notify
     * 打开通知监听
     * @param serviceUUID
     * @param notifyUUID
     * @return {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)} {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    public boolean enableNotify(String serviceUUID, String notifyUUID){
        return changeNotify(serviceUUID,notifyUUID,true,"enableNotify");
    }

    public boolean enalbeNotifyByFixNotifyValue(String serviceUUID,String notifyUUID,byte[] FIX_NOTIFICATION_VALUE){
        return setNotification(getCharacteristic(serviceUUID,notifyUUID,"enalbeNotifyByFixNotifyValue"),true,FIX_NOTIFICATION_VALUE);
    }

    /**
     * disable notify
     * 关闭通知监听
     * @param serviceUUID
     * @param notifyUUID
     * @return {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)} {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    public boolean disableNofify(String serviceUUID,String notifyUUID){
        return changeNotify(serviceUUID,notifyUUID,false,"disableNofify");
    }

    /**
     * change notify
     * 改变通知
     * @param serviceUUID
     * @param notifyUUID
     * @param isEnable
     * @return {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)} {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    private boolean changeNotify(String serviceUUID, String notifyUUID, boolean isEnable,String descri){
        return setNotification(getCharacteristic(serviceUUID,notifyUUID,descri),isEnable);
    }

    /**
     * set notify
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     */
    public boolean setNotification(BluetoothGattCharacteristic characteristic,boolean enable){
        return setNotification(characteristic,enable,enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    /**
     * set notify
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     */
    private boolean setNotification(BluetoothGattCharacteristic characteristic,boolean enable,byte[] FIX_NOTIFICATION_VALUE){
        if(null != characteristic){
            if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                BluetoothGatt mBluetoothGatt = getBluetoothGatt();
                if(null != mBluetoothGatt){
                    boolean result = false;
                    boolean isSuccess = setCharacteristicNotification(characteristic,enable);

                    if(isSuccess) {
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                formatUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
                        LogUtil.i(TAG, ">>>>>>>>> " + characteristic.getUuid().toString() + " setCharacteristicNotification isSuccess = " + isSuccess);


                        if (descriptor != null) {
                            descriptor.setValue(FIX_NOTIFICATION_VALUE);
                            result = writeDescriptor(descriptor);
                        }
                        LogUtil.d(TAG, ">>>>>>>>> " + characteristic.getUuid().toString() + " notify result = " + result);
                        return result;
                    }
                }
            }else{
                LogUtil.w(TAG, ">>>>>>this notifyUUID(" + characteristic.getUuid().toString() + ") not support notification<<<<<");
            }
        }else{
            LogUtil.w(TAG,">>>>>>setNotification fail,this characteristic is null<<<<<");
        }
        return false;
    }

    public boolean isControlSend() {
        return isControlSend;
    }

    /**
     * 是否要进行发送控制
     * @param controlSend
     */
    public void setControlSend(boolean controlSend) {
        isControlSend = controlSend;
    }

    /**
     * write no response
     * 写
     * @param serviceUUID
     * @param writeUUID
     * @param data
     * @return {@link BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicWriteCallBack#onCharacteristicWrite(BluetoothDeviceBean, byte[], int)}
     */
    public void writeNoResponse(String serviceUUID,String writeUUID,byte[] data,IOnCharacteristicWriteCallBack callBack){
        write(serviceUUID,writeUUID,data,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,callBack);
    }

    /**
     * 自动识别特征点类型发送数据
     * 自动识别wirte的类型<br\>
     * 依次顺序为1.PROPERTY_WRITE；2.PROPERTY_WRITE_NO_RESPONSE；PROPERTY_SIGNED_WRITE。<br\>
     * 一旦识别到则以该类型发送数据<br\>
     * @param serviceUUID
     * @param writeUUID
     * @param data
     * @return
     */
    public void writeAutoIndentifyType(String serviceUUID,String writeUUID,byte[] data,IOnCharacteristicWriteCallBack callBack){
        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID,writeUUID,"writeAutoIndentifyType");
        writeAutoIndentifyType(characteristic,data,callBack);
    }

    /**
     * 自动识别特征点类型发送数据
     * 自动识别wirte的类型<br\>
     * 依次顺序为1.PROPERTY_WRITE；2.PROPERTY_WRITE_NO_RESPONSE；PROPERTY_SIGNED_WRITE。<br\>
     * 一旦识别到则以该类型发送数据<br\>
     * @param data
     * @return
     */
    public void writeAutoIndentifyType(BluetoothGattCharacteristic characteristic,byte[] data,IOnCharacteristicWriteCallBack callBack){
        int type = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
        if(characteristic != null) {
            int properties = characteristic.getProperties();
            if((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0){
                type = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
            }else if((type = properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0){
                type = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
            }else if((type = properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0){
                type = BluetoothGattCharacteristic.WRITE_TYPE_SIGNED;
            }
        }else{
            LogUtil.w(TAG,"write fail this BluetoothGattCharacteristic is null");
        }
        writeCharacteristic(characteristic,data,type,callBack);
    }

    /**
     * write
     * 写
     * @param serviceUUID
     * @param writeUUID
     * @param data
     * @param writeType The write type to for this characteristic. Can be one
     *                  of:
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return {@link BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicWriteCallBack#onCharacteristicWrite(BluetoothDeviceBean, byte[], int)}
     */
    public void write(String serviceUUID,String writeUUID,byte[] data,int writeType,IOnCharacteristicWriteCallBack callBack){
        writeCharacteristic(getCharacteristic(serviceUUID,writeUUID,"write"),data,writeType,callBack);
    }

    /**
     * 写
     * @param characteristic
     * @param data
     * @param writeType The write type to for this characteristic. Can be one
     *                  of:
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or
     *                  {@link BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
     * @return {@link BluetoothGatt#writeCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicWriteCallBack#onCharacteristicWrite(BluetoothDeviceBean, byte[], int)}
     */
    public synchronized void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data,int writeType,IOnCharacteristicWriteCallBack callBack){
        if(null != data && data.length > 0){
            if(characteristic != null){
                if((characteristic.getProperties()
                        & (BluetoothGattCharacteristic.PROPERTY_WRITE
                            | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                            | BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE)) == 0){
                    LogUtil.w(TAG,"this characteristic(uuid = " + characteristic.getUuid().toString()  +") not support write!");
                    notifyWriteFail(callBack,data);
                    return;
                }

                LogUtil.d(TAG,">>>>>>>>>>>>device " + characteristic.getService());
                if(isControlSend) {
                    sendControl.write(characteristic, callBack, data, writeType);
                }else {
                    characteristic.setValue(data);
                    characteristic.setWriteType(writeType);
                    boolean result = writeCharacteristicInner(characteristic);
                    if(!result){
                        notifyWriteFail(callBack,data);
                    }else{
                        this.callBack = callBack;
                    }
                }
                return;
            }else{
                LogUtil.w(TAG,">>>>>>>>>writeCharacteristic fail,because characteristic is null<<<<<<<<<");
            }
        }
        notifyWriteFail(callBack,data);
        return;
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
    public void setWriteDelayTime(int delayTime) {
        sendControl.setDelayTime(delayTime);
    }

    /**
     * 取消发送
     * 如果还未发送可以取消发送
     * @param callBack
     * @param dataBuf
     * @return
     */
    public boolean cancleWrite(IOnCharacteristicWriteCallBack callBack,byte[] dataBuf){
        return sendControl.cancleSend(callBack,dataBuf);
    }

    private void notifyWriteFail(IOnCharacteristicWriteCallBack callBack,byte[] data){
        if(callBack != null){
            callBack.onCharacteristicWrite(this,data,BluetoothGatt.GATT_FAILURE);
        }
    }

    synchronized boolean writeCharacteristicInner(BluetoothGattCharacteristic characteristic){
        if(characteristic != null && isConnected()){
            BluetoothGatt mBluetoothGatt = getBluetoothGatt();
            if(mBluetoothGatt != null){
                boolean result = mBluetoothGatt.writeCharacteristic(characteristic);
                return result;
            }else{
                LogUtil.w(TAG,">>>>>>>>>writeCharacteristic fail,because mBluetoothGatt is null<<<<<<<<<");
            }
        }
        return false;
    }

    /**
     * 读
     * @param serviceUUID
     * @param readUUID
     * @return {@link BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicReadCallBack#onCharacteristicRead(BluetoothDeviceBean, BluetoothGattCharacteristic, int)}
     */
    public boolean read(String serviceUUID,String readUUID){
        return readCharacteristic(getCharacteristic(serviceUUID,readUUID,"read"));
    }

    /**
     * read<br\>
     * 读
     * @param characteristic
     * @return {@link BluetoothGatt#readCharacteristic(BluetoothGattCharacteristic)}
     * data call back {@link IOnCharacteristicReadCallBack#onCharacteristicRead(BluetoothDeviceBean, BluetoothGattCharacteristic, int)}
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic){
        if(characteristic != null){
            if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0){
                LogUtil.w(TAG,">>>>>>>>>this characteristic(uuid = " + characteristic.getUuid().toString() + ") not support read!<<<<<<<<<");
                return false;
            }
            BluetoothGatt mBluetoothGatt = getBluetoothGatt();
            if(mBluetoothGatt != null){
                return mBluetoothGatt.readCharacteristic(characteristic);
            }
        }else{
            LogUtil.w(TAG,">>>>>>>>>readCharacteristic fail,because characteristic is null<<<<<<<<<");
        }
        return false;
    }

    /**
     *
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param serviceUUID
     * @param indicateUUID
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    public boolean enableIndicate(String serviceUUID,String indicateUUID){
        return changeIndicate(serviceUUID,indicateUUID,true,"enableIndicate");
    }

    /**
     *
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param serviceUUID
     * @param indicateUUID
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    public boolean disableIndicate(String serviceUUID,String indicateUUID){
        return changeIndicate(serviceUUID,indicateUUID,false,"disableIndicate");
    }

    /**
     *
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param serviceUUID
     * @param indicateUUID
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    private boolean changeIndicate(String serviceUUID,String indicateUUID,boolean enable,String descri){
        return setCharacteristicIndication(getCharacteristic(serviceUUID,indicateUUID,descri),enable);
    }

    /**
     * indicate setting
     * 当设备为 Indication 模式时，设备的值有变化时会主动返回给App，App在 onCharacteristicChanged() 方法中能收到返回的值。<br\>
     * Indication： 从机会先向主机发送一条通知，主机接收到通知后去读取从机数据 <br\>
     * Notification：从机直接发送给主机数据<br\>
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     * data call back {@link IOnCharacteristicChangedCallBack#onCharacteristicChanged(BluetoothDeviceBean, BluetoothGattCharacteristic)}
     */
    public boolean setCharacteristicIndication(BluetoothGattCharacteristic characteristic,boolean enable){
        if(characteristic != null){
            if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0){
                LogUtil.w(TAG,">>>>>characteristic(uuid = " + characteristic.getUuid().toString() + ") not supports indication<<<<<<");
                return false;
            }
            /*if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0){
                //???????is right
                return false;
            }*/
            BluetoothGatt mBluetoothGatt = getBluetoothGatt();
            if(mBluetoothGatt != null){
                boolean success = setCharacteristicNotification(characteristic,enable);
                LogUtil.d(TAG, "setCharacteristicIndication:" + enable
                        + "\nsuccess:" + success
                        + "\ncharacteristic.getUuid():" + characteristic.getUuid());
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        formatUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
                if(success) {
                    boolean result = false;
                    if (descriptor != null) {
                        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE :
                                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        result = writeDescriptor(descriptor);
                    }
                    LogUtil.i(TAG, "setCharacteristicIndication writeDescriptor result = " + result);
                    return result;
                }
            }
        }else{
            LogUtil.w(TAG,">>>>>>>>>>>>>>>setCharacteristicIndication fail,characteristic is null<<<<<<<<<<<<<<<");
        }
        return false;
    }

    /**
     *
     * @param characteristic
     * @param enable
     * @return {@link BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)}
     */
    private boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enable){
        if(characteristic != null) {
            BluetoothGatt mBluetoothGatt = getBluetoothGatt();
            if (mBluetoothGatt != null) {
                return mBluetoothGatt.setCharacteristicNotification(characteristic,enable);
            }
        }else{
            LogUtil.w(TAG,">>>>>>>>>>>>>>>setCharacteristicNotification fail,characteristic is null<<<<<<<<<<<<<<<");
        }
        return false;
    }

    /**
     *
     * @param descriptor
     * @return {@link BluetoothGatt#readDescriptor(BluetoothGattDescriptor)}
     */
    public boolean readDescriptor(BluetoothGattDescriptor descriptor){
        BluetoothGatt mBluetoothGatt = getBluetoothGatt();
        if(null != mBluetoothGatt) {
            return mBluetoothGatt.readDescriptor(descriptor);
        }
        return false;
    }

    /**
     *
     * @param descriptor
     * @return  {@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)}
     */
    private boolean writeDescriptor(BluetoothGattDescriptor descriptor){
        BluetoothGatt mBluetoothGatt = getBluetoothGatt();
        if(mBluetoothGatt != null){
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    /**
     * 开始可靠的characteristic写的请求<br\>
     * 所有characteristic的写请求都会保存在gattServer中的队列中，<br\>
     * 直到有executeReliableWrite()执行，中途可以检验某个之是不是已经正确写入，<br\>
     * 同时可以调用public void abortReliableWrite()来终止这次可靠写characteristic请求，<br\>
     * 所有从开始的数据都不会写入到characteristic中.<br\>
     * @return
     */
    public boolean beginReliableWrite(){
        BluetoothGatt mBluetoothGatt = getBluetoothGatt();
        if(mBluetoothGatt != null) {
            return mBluetoothGatt.beginReliableWrite();
        }
        return false;
    }

    /**
     * 执行可靠写数据的请求
     * @return
     */
    public boolean executeReliableWrite(){
        BluetoothGatt mBluetoothGatt = getBluetoothGatt();
        if(mBluetoothGatt != null){
            return mBluetoothGatt.executeReliableWrite();
        }
        return false;
    }

    /**
     * 放弃可靠写请求
     */
    public void abortReliableWrite(){
        BluetoothGatt mBluetoothGatt = getBluetoothGatt();
        if(null != mBluetoothGatt) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBluetoothGatt.abortReliableWrite();
            }
        }
    }

    /***********************************操作*************************************************/

    @Override
    protected void onMtuChanged(DeviceConnectBean deviceConnectBean, int mtu, int status) {
        if(null != mOnMtuChangedCallBack){
            mOnMtuChangedCallBack.onMtuChanged(BluetoothDeviceBean.this,mtu,status);
        }
    }

    @Override
    protected void onReadRemoteRssi(DeviceConnectBean deviceConnectBean, int status) {
        if(null != mOnReadRemoteRssiCallBack){
            mOnReadRemoteRssiCallBack.onReadRemoteRssi(BluetoothDeviceBean.this,status);
        }
    }

    @Override
    protected void onReliableWriteCompleted(DeviceConnectBean deviceConnectBean, int status) {
        if(null != mOnReliableWriteCompletedCallBack){
            mOnReliableWriteCompletedCallBack.onReliableWriteCompleted(BluetoothDeviceBean.this,status);
        }
    }

    @Override
    protected void onDescriptorWrite(DeviceConnectBean deviceConnectBean, BluetoothGattDescriptor descriptor, int status) {
        /*if(null != mOnDescriptorWriteCallBack){
            mOnDescriptorWriteCallBack.onDescriptorWrite(BluetoothDeviceBean.this,descriptor,status);
        }*/
    }

    @Override
    protected void onDescriptorRead(DeviceConnectBean deviceConnectBean, BluetoothGattDescriptor descriptor, int status) {
        if(null != mOnDescriptorReadCallBack){
            mOnDescriptorReadCallBack.onDescriptorRead(BluetoothDeviceBean.this,descriptor,status);
        }
    }

    /**
     * 运行在子线程
     * @param
     * @param characteristic
     */
    @Override
    protected void onCharacteristicChanged(DeviceConnectBean deviceConnectBean, BluetoothGattCharacteristic characteristic) {
        if(null != mOnCharacteristicChangedCallBack){
            mOnCharacteristicChangedCallBack.onCharacteristicChanged(BluetoothDeviceBean.this,characteristic);
        }
    }

    @Override
    protected void onCharacteristicWrite(DeviceConnectBean deviceConnectBean, BluetoothGattCharacteristic characteristic, int status) {
        if(isControlSend && sendControl.isWriteing()) {
            sendControl.getWriteCallBack().onCharacteristicWrite(BluetoothDeviceBean.this, characteristic.getValue(), status);
        }else{
            if(callBack != null){
                callBack.onCharacteristicWrite(this,characteristic.getValue(),status);
            }
        }
    }

    @Override
    protected void onCharacteristicRead(DeviceConnectBean deviceConnectBean, BluetoothGattCharacteristic characteristic, int status) {
        if(null != mOnCharacteristicReadCallBack){
            mOnCharacteristicReadCallBack.onCharacteristicRead(BluetoothDeviceBean.this,characteristic,status);
        }
    }

    public BluetoothDeviceBean setmOnCharacteristicReadCallBack(IOnCharacteristicReadCallBack mOnCharacteristicReadCallBack) {
        this.mOnCharacteristicReadCallBack = mOnCharacteristicReadCallBack;
        return this;
    }

    public BluetoothDeviceBean setmOnCharacteristicChangedCallBack(IOnCharacteristicChangedCallBack mOnCharacteristicChangedCallBack) {
        this.mOnCharacteristicChangedCallBack = mOnCharacteristicChangedCallBack;
        return this;
    }

    public BluetoothDeviceBean setmOnDescriptorReadCallBack(IOnDescriptorReadCallBack mOnDescriptorReadCallBack) {
        this.mOnDescriptorReadCallBack = mOnDescriptorReadCallBack;
        return this;
    }

    public BluetoothDeviceBean setmOnDescriptorWriteCallBack(IOnDescriptorWriteCallBack mOnDescriptorWriteCallBack) {
        this.mOnDescriptorWriteCallBack = mOnDescriptorWriteCallBack;
        return this;
    }

    public BluetoothDeviceBean setmOnReliableWriteCompletedCallBack(IOnReliableWriteCompletedCallBack mOnReliableWriteCompletedCallBack) {
        this.mOnReliableWriteCompletedCallBack = mOnReliableWriteCompletedCallBack;
        return this;
    }

    public BluetoothDeviceBean setmOnReadRemoteRssiCallBack(IOnReadRemoteRssiCallBack mOnReadRemoteRssiCallBack) {
        this.mOnReadRemoteRssiCallBack = mOnReadRemoteRssiCallBack;
        return this;
    }

    public BluetoothDeviceBean setmOnMtuChangedCallBack(IOnMtuChangedCallBack mOnMtuChangedCallBack) {
        this.mOnMtuChangedCallBack = mOnMtuChangedCallBack;
        return this;
    }

    @Override
    protected void onDeviceDisConnected() {
        super.onDeviceDisConnected();
        sendControl.bleDisconnect();
    }
}
