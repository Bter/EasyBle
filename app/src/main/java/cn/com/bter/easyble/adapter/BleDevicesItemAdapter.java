package cn.com.bter.easyble.adapter;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;

import cn.com.bter.easyble.R;
import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;
import cn.com.bter.easyble.utils.CommonUtils;


/**
 * 蓝牙设备列表
 * Created by admin on 2017/7/16.
 */

public class BleDevicesItemAdapter extends BaseRecycleViewAdapter<BleDevicesItemAdapter.ViewHolder> {

    private Context mContext;
    private List<BluetoothDeviceBean> mData;
    private HashMap<TextView,BluetoothDeviceBean> viewBean;


    public BleDevicesItemAdapter(Context mContext, RecyclerView recyclerView, List<BluetoothDeviceBean> mData, HashMap<TextView,BluetoothDeviceBean> viewBean) {
        super(recyclerView);
        this.mContext = mContext;
        this.mData = mData;
        this.viewBean = viewBean;
    }

    @Override
    public ViewHolder onCreateViewHolder(int viewType, ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.activity_ble_device_item_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(int position, ViewHolder holder) {
        BluetoothDeviceBean bleDeviceBean = mData.get(position);
        if(viewBean != null) {
            viewBean.put(holder.textViewBleAddress, null);
        }
        if (null != bleDeviceBean){
            if(bleDeviceBean.getBleDevice() != null) {
                String name = bleDeviceBean.getBleDevice().getName();
                if((null == name || name.trim().equals(""))){
                    name = "Unknow";
                }
                holder.textViewBleName.setText(name + " " + bleDeviceBean.getBleDevice().getAddress());
                //rssi 计算方式：d = 10^((abs(RSSI) - A) / (10 * n))
                //其中：d - 计算所得距离; RSSI - 接收信号强度（负值); A - 发射端和接收端相隔1米时的信号强度(取59) ; n - 环境衰减因子（取2.0）

                holder.textViewBleAddress.setText(getDistanceDescri(mContext,bleDeviceBean.getRssi()));//BleContacts.SERVICE_UUID
                if(viewBean != null) {
                    viewBean.put(holder.textViewBleAddress, bleDeviceBean);
                }

                String stateId = "未连接";
                switch (bleDeviceBean.getConnectStatus()){
                    case BluetoothProfile.STATE_CONNECTING:
                        stateId = "正在连接";  //R.string.ble_descri_connect_start;
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        stateId = "已连接";
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        stateId = "未连接";
                        break;
                }
                holder.textViewConnectState.setText(stateId);
            }
        }
    }

    /**
     * 获取信号强度描述
     * @return
     */
    public static String getDistanceDescri(Context mContext,int rssi){
        float distance = (float) Math.pow(10, (int)((Math.abs(rssi) - 59) / (10 * 2.0)));
        return String.format("信号强度：%s，距离：%s m",
                ("" + rssi),("" + CommonUtils.formatDecima(distance,1)));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewBleName;
        private TextView textViewBleAddress;
        private TextView textViewConnectState;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewBleName = (TextView) itemView.findViewById(R.id.textViewBleName);
            textViewBleAddress = (TextView) itemView.findViewById(R.id.textViewBleAddress);
            textViewConnectState = (TextView) itemView.findViewById(R.id.textViewConnectState);
        }
    }
}
