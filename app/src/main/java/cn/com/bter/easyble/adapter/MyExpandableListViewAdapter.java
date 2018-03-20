package cn.com.bter.easyble.adapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import cn.com.bter.easyble.R;
import cn.com.bter.easyble.utils.CommonUtils;

/**
 * Created by admin on 2017/10/26.
 */

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {
    private List<BluetoothGattService> services;
    private Context mContext;

    public MyExpandableListViewAdapter(List<BluetoothGattService> services,Context mContext) {
        this.services = services;
        this.mContext = mContext;
    }

    @Override
    public int getGroupCount() {
        return services.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return services.get(groupPosition).getCharacteristics().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return services.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return services.get(groupPosition).getCharacteristics().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expandable_parent_layout, null, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothGattService service = services.get(groupPosition);
        viewHolder.title.setText("服务：" + groupPosition);
        viewHolder.uuid.setText(service.getUuid().toString());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expandable_parent_layout, null, false);
            convertView.setPadding(CommonUtils.dp2px(mContext,20),0,0,0);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothGattCharacteristic characteristic = services.get(groupPosition).getCharacteristics().get(childPosition);
        viewHolder.title.setText("特征：" + childPosition);
        viewHolder.uuid.setText(characteristic.getUuid().toString());
        viewHolder.descri.setText(getDescri(characteristic.getProperties()));
        return convertView;
    }

    private String getDescri(int properties){
        String str = "";
        if((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0){
            str += ",write";
        }
        if((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0){
            str += ",write no response";
        }
        if((properties & (BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE)) > 0){
            str += ",write signed";
        }
        if((properties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) > 0){
            str += ",notify";
        }
        if((properties & (BluetoothGattCharacteristic.PROPERTY_READ)) > 0){
            str += ",read";
        }
        if((properties & (BluetoothGattCharacteristic.PROPERTY_INDICATE)) > 0){
            str += ",indicate";
        }
        if(str.startsWith(",")){
            str = str.substring(1,str.length());
        }
        return str;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static class ViewHolder{
        private TextView title;
        private TextView uuid;
        private TextView descri;

        public ViewHolder(View view){
            title = (TextView) view.findViewById(R.id.title);
            uuid = (TextView) view.findViewById(R.id.uuid);
            descri = (TextView) view.findViewById(R.id.descri);
        }
    }
}
