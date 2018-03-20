package cn.com.bter.easyble.easyblelib.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.com.bter.easyble.easyblelib.interfaces.OnBluetoothAdapterStateChangeListener;

/**
 * 蓝牙适配器监听
 * Created by admin on 2017/10/27.
 */

public class BluetoothAdapterStateChangeReceiver extends BroadcastReceiver {
    private OnBluetoothAdapterStateChangeListener mOnBluetoothAdapterStateChangeListener;

    public BluetoothAdapterStateChangeReceiver(OnBluetoothAdapterStateChangeListener mOnBluetoothAdapterStateChangeListener) {
        this.mOnBluetoothAdapterStateChangeListener = mOnBluetoothAdapterStateChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
            //蓝牙适配器状态改变
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.STATE_OFF);
            int oldState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE,BluetoothAdapter.STATE_OFF);

            if(null != mOnBluetoothAdapterStateChangeListener){
                mOnBluetoothAdapterStateChangeListener.onStateChange(state,oldState);
            }
        }
    }
}
