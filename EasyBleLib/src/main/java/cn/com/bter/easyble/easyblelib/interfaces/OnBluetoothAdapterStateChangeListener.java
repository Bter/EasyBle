package cn.com.bter.easyble.easyblelib.interfaces;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by admin on 2017/10/27.
 */

public interface OnBluetoothAdapterStateChangeListener {
    /**
     *
     * @param state one of the
     * {@link BluetoothAdapter#STATE_OFF},
     * {@link BluetoothAdapter#STATE_TURNING_ON},
     * {@link BluetoothAdapter#STATE_ON},
     * {@link BluetoothAdapter#STATE_TURNING_OFF}
     * @param oldState one of the
     * {@link BluetoothAdapter#STATE_OFF},
     * {@link BluetoothAdapter#STATE_TURNING_ON},
     * {@link BluetoothAdapter#STATE_ON},
     * {@link BluetoothAdapter#STATE_TURNING_OFF}
     */
    void onStateChange(int state, int oldState);
}
