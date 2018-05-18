package com.bter.easyble.peripheral.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.bter.easyble.peripheral.utils.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PeripheralManager {
    private static final String TAG = PeripheralManager.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private boolean isInit = false;

    short mMajor = 0xF1;
    short mMinor = 0xF2;
    byte mTxPower = (byte) 0xF3;
    static String SERVICE_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void init(Context mContext){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mContext != null && !isInit) {
                isInit = true;
                this.mContext = mContext.getApplicationContext();
                this.mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            } else {
                LogUtil.w(TAG,"mContext is null");
            }
        } else {
            LogUtil.w(TAG,"Android version must no less than " + Build.VERSION_CODES.LOLLIPOP);
        }
    }

    private boolean isInit(){
        if(!isInit){
            LogUtil.w(TAG,"Never call init method!");
        }
        return isInit;
    }

    /**
     * is support ble
     */
    public boolean isSupportBle() {
        if(isInit()) {
            return mContext.getApplicationContext()
                    .getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }
        return false;
    }

    /**
     * 开启广播
     */
    public void startAdvertiser(){
        if(isInit()){
            if(mBluetoothLeAdvertiser != null){
                mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true,0),createIBeaconAdvertiseData(UUID.fromString(SERVICE_UUID),
                        mMajor, mMinor, mTxPower),createScanAdvertiseData(mMajor, mMinor, mTxPower),mAdvCallback);
            }
        }
    }

    public void stopAdvertising(){
        if(isInit()){
            if(mBluetoothLeAdvertiser != null){
                mBluetoothLeAdvertiser.stopAdvertising(mAdvCallback);
            }
        }
    }

    /**
     * 创建广播设置
     * @param connectable
     * @param timeoutMillis 广播时长单位秒，但为0时，代表无时间限制会一直广播
     * @return
     */
    private AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        //设置广播的模式, 功耗相关
        /**
         * 设置广播的模式，低功耗，平衡和低延迟三种模式；
            对应  AdvertiseSettings.ADVERTISE_MODE_LOW_POWER  ,ADVERTISE_MODE_BALANCED ,ADVERTISE_MODE_LOW_LATENCY
            从左右到右，广播的间隔会越来越短
         */
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setConnectable(connectable);
        builder.setTimeout(timeoutMillis * 1000);
        /**
         * 设置广播的信号强度
         * 常量有AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW, ADVERTISE_TX_POWER_LOW, ADVERTISE_TX_POWER_MEDIUM, ADVERTISE_TX_POWER_HIGH
         从左到右分别表示强度越来越强.
         举例：当设置为ADVERTISE_TX_POWER_ULTRA_LOW时，
         手机1和手机2放在一起，手机2扫描到的rssi信号强度为-56左右，
         当设置为ADVERTISE_TX_POWER_HIGH  时， 扫描到的信号强度为-33左右，
         信号强度越大，表示手机和设备靠的越近
         */
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = builder.build();
        if (mAdvertiseSettings == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }

    //设置scan广播数据
    private static AdvertiseData createScanAdvertiseData(short major, short minor, byte txPower) {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);

        byte[] serverData = new byte[5];
        ByteBuffer bb = ByteBuffer.wrap(serverData);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(major);
        bb.putShort(minor);
        bb.put(txPower);
        builder.addServiceData(ParcelUuid.fromString(UUID.fromString(SERVICE_UUID).toString())
                , serverData);

        AdvertiseData adv = builder.build();
        return adv;
    }

    /**
      * create AdvertiseDate for iBeacon
    */
    private static AdvertiseData createIBeaconAdvertiseData(UUID proximityUuid, short major, short minor, byte txPower) {
        String[] uuidstr = proximityUuid.toString().replaceAll("-", "").toLowerCase().split("");
        byte[] uuidBytes = new byte[16];
        for (int i = 1, x = 0; i < uuidstr.length; x++) {
            uuidBytes[x] = (byte) ((Integer.parseInt(uuidstr[i++], 16) << 4) | Integer.parseInt(uuidstr[i++], 16));
        }
        byte[] majorBytes = {(byte) (major >> 8), (byte) (major & 0xff)};
        byte[] minorBytes = {(byte) (minor >> 8), (byte) (minor & 0xff)};
        byte[] mPowerBytes = {txPower};
        byte[] manufacturerData = new byte[0x17];
        byte[] flagibeacon = {0x02, 0x15};

        System.arraycopy(flagibeacon, 0x0, manufacturerData, 0x0, 0x2);
        System.arraycopy(uuidBytes, 0x0, manufacturerData, 0x2, 0x10);
        System.arraycopy(majorBytes, 0x0, manufacturerData, 0x12, 0x2);
        System.arraycopy(minorBytes, 0x0, manufacturerData, 0x14, 0x2);
        System.arraycopy(mPowerBytes, 0x0, manufacturerData, 0x16, 0x1);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addManufacturerData(0x004c, manufacturerData);

        AdvertiseData adv = builder.build();
        return adv;
    }

    //发送广播的回调，onStartSuccess/onStartFailure很明显的两个Callback
     private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
         public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
             super.onStartSuccess(settingsInEffect);
             if (settingsInEffect != null) {
                 Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode() + " timeout=" + settingsInEffect.getTimeout());
             } else {
                 Log.d(TAG, "onStartSuccess, settingInEffect is null");
             }
         }

        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "onStartFailure errorCode=" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                Toast.makeText(mContext, "advertise_failed_data_too_large", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                Toast.makeText(mContext, "advertise_failed_too_many_advertises", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising because no advertising instance is available.");

            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                Toast.makeText(mContext, "advertise_failed_already_started", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising as the advertising is already started");

            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                Toast.makeText(mContext, "advertise_failed_internal_error", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Operation failed due to an internal error");

            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                Toast.makeText(mContext, "advertise_failed_feature_unsupported", Toast.LENGTH_LONG).show();
                Log.e(TAG, "This feature is not supported on this platform");

            }
        }
     };
}
