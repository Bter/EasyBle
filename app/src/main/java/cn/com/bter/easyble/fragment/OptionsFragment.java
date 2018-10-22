package cn.com.bter.easyble.fragment;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.com.bter.easyble.MainActivity;
import cn.com.bter.easyble.R;
import cn.com.bter.easyble.adapter.MyExpandableListViewAdapter;
import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;
import cn.com.bter.easyble.easyblelib.interfaces.IBleDeviceStateListener;
import cn.com.bter.easyble.easyblelib.utils.LogUtil;
import cn.com.bter.easyble.interfaces.OptionsCallBack;
import cn.com.bter.easyble.utils.CommonUtils;

/**
 * 操作界面
 * Created by admin on 2017/10/26.
 */

public class OptionsFragment extends Fragment {
    private final String PREFERENCE_NAME = "preferences";
    private View contentView;
    private BluetoothDeviceBean device;

    private CheckBox checkboxNoRespone;
    private TextView deviceName;
    private TextView connectState;
    private View selectWriteService;
    private CheckBox checkbox1;
    private EditText editTextContent1;
    private View writeBtn1;
    private CheckBox checkbox2;
    private EditText editTextContent2;
    private View writeBtn2;
    private CheckBox checkbox3;
    private EditText editTextContent3;
    private View writeBtn3;
    private CheckBox checkboxReceiver;
    private View clearBtn;
    private View readBtn;
    private View selectReceiveService;
    private TextView textViewReciverContent;

    private StringBuilder builder = new StringBuilder();
    private BluetoothGattCharacteristic writeBluetoothGattCharacteristic;
    private BluetoothGattCharacteristic readBluetoothGattCharacteristic;

    private MainActivity mainActivity;

    private Handler mHandler = new Handler();

    private int selectType = 1;

    private HandlerThread handlerThread = new HandlerThread("back thread");
    private Handler backHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handlerThread.start();
        backHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        mainActivity.addIBleDeviceStateListener(mBleDeviceStateListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.activity_options_layout,null,false);
        initView();
        refreshReceiverContent();
        return contentView;
    }

    private void initView() {
        checkboxNoRespone = (CheckBox) F(R.id.checkboxNoRespone);
        deviceName = (TextView) F(R.id.deviceName);
        connectState = (TextView) F(R.id.connectState);
        selectWriteService = F(R.id.selectWriteService);
        checkbox1 = (CheckBox) F(R.id.checkbox1);
        editTextContent1 = (EditText) F(R.id.editTextContent1);
        writeBtn1 = F(R.id.writeBtn1);
        checkbox2 = (CheckBox) F(R.id.checkbox2);
        editTextContent2 = (EditText) F(R.id.editTextContent2);
        writeBtn2 = F(R.id.writeBtn2);
        checkbox3 = (CheckBox) F(R.id.checkbox3);
        editTextContent3 = (EditText) F(R.id.editTextContent3);
        writeBtn3 = F(R.id.writeBtn3);
        checkboxReceiver = (CheckBox) F(R.id.checkboxReceiver);
        clearBtn = F(R.id.clearBtn);
        readBtn = F(R.id.readBtn);
        selectReceiveService = F(R.id.selectReceiveService);
        textViewReciverContent = (TextView) F(R.id.textViewReciverContent);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_NAME,0);
        editTextContent1.setText(sharedPreferences.getString("editTextContent1",""));
        editTextContent2.setText(sharedPreferences.getString("editTextContent2",""));
        editTextContent3.setText(sharedPreferences.getString("editTextContent3",""));

        connectState.setOnClickListener(viewClick);
        selectWriteService.setOnClickListener(viewClick);
        writeBtn1.setOnClickListener(viewClick);
        writeBtn2.setOnClickListener(viewClick);
        writeBtn3.setOnClickListener(viewClick);
        clearBtn.setOnClickListener(viewClick);
        readBtn.setOnClickListener(viewClick);
        selectReceiveService.setOnClickListener(viewClick);

        editTextContent1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveStr("editTextContent1",s.toString());
            }
        });
        editTextContent2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveStr("editTextContent2",s.toString());
            }
        });
        editTextContent3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveStr("editTextContent3",s.toString());
            }
        });
    }

    private void saveStr(String key,String str){
        if(key == null){
            return;
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_NAME,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,str);
        editor.apply();
    }

    private View F(int id){
        return contentView.findViewById(id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearBuild();
        writeBluetoothGattCharacteristic = null;
        readBluetoothGattCharacteristic = null;
        device.setmOnCharacteristicChangedCallBack(null)
                .setmOnCharacteristicReadCallBack(null);

        if(backHandler != null) {
            backHandler.removeCallbacksAndMessages(null);
        }
        if(handlerThread != null) {
            handlerThread.quit();
            handlerThread.interrupt();
        }
    }

    /**
     * 清除接收区
     */
    private void clearBuild(){
        if(builder.length() > 0){
            builder.delete(0,builder.length());
        }
    }

    /**
     * 增加数据至接收区
     * @param str
     */
    private void addReceiverContent(String str){
        builder.append(str).append("\n");
        refreshReceiverContent();
    }

    /**
     * 刷新接收区
     */
    private void refreshReceiverContent(){
        if(null != textViewReciverContent){
            textViewReciverContent.setText(builder.toString());
        }
    }

    private View.OnClickListener viewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.connectState:
                    clickStateView();
                    break;
                case R.id.selectWriteService:
                    selectService(true,1);
                    break;
                case R.id.writeBtn1:
                    write(checkbox1,editTextContent1);
                    break;
                case R.id.writeBtn2:
//                    write(checkbox2,editTextContent2);

                    if(!isTestTaskRun) {
                        backHandler.postDelayed(testTask, delayTime);
                    }else{
                        backHandler.removeCallbacks(testTask);
                    }

                    isTestTaskRun = true;
                    break;
                case R.id.writeBtn3:
                    write(checkbox3,editTextContent3);
                    break;
                case R.id.clearBtn:
                    clearBuild();
                    refreshReceiverContent();
                    break;
                case R.id.readBtn:
//                    showToast("unrealized(未实现)");
                    if(device != null) {
                        if(device.isConnected()){
                            read();
                        }else{
                            clickStateView();
                        }
                    }
                    break;
                case R.id.selectReceiveService:
                    selectService(true,2);
                    break;
            }
        }
    };

    /***
     * 循环发送测试开始
     ***/
    private int delayTime = 5;
    private boolean isTestTaskRun = false;
    private Runnable testTask = new Runnable() {
        int i = 0;
        @Override
        public void run() {
            backHandler.removeCallbacks(this);
            write(false,"" + i);
            i++;
            backHandler.postDelayed(this,delayTime);
        }
    };
    /***
     * 循环发送测试结束
     ***/

    /**
     * 点击连接状态
     */
    private void clickStateView(){
        if(device != null) {
            String str = "是否要连接蓝牙?";
            if(device.isConnected()){
                str = "是否要断开蓝牙?";
            }
            new AlertDialog.Builder(getActivity()).setTitle("温馨提示")
                    .setMessage(str)
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(device.isConnected()) {
                                device.disConnect();
                            }else{
                                mainActivity.connectBle(device);
                            }
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(true).create().show();
        }else{
            showToast("没有选择设备，请到主界面选择设备");
        }
    }

    /**
     * 选择服务
     * @param isSelectCharacteristic
     * @param selectType
     */
    private void selectService(boolean isSelectCharacteristic,int selectType){
        if(device != null){
            if(device.isConnected()){
                this.selectType = selectType;
                showService(device.getServices());
            }else{
                clickStateView();
            }
        }else{
            showToast("没有选择设备，请到主界面选择设备");
        }
    }

    /**
     * write
     * @param checkBox
     * @param cmdEditText
     * @return
     */
    private void write(CheckBox checkBox,EditText cmdEditText){
        boolean isHex = false;
        if(checkBox != null) {
            isHex = checkBox.isChecked();
        }
        if(cmdEditText != null && cmdEditText.getText() != null
                && cmdEditText.getText().toString() != null){
            String str = cmdEditText.getText().toString();

            write(isHex,str);
            return;
        }
        return;
    }

    private void write(boolean isHex,String dataStr){

        if(dataStr != null){
            if(!isHex){
                write(dataStr.getBytes());
                return;
            }else{
                try {
                    byte[] data = CommonUtils.hexAsciiStrs2Bytes(dataStr);
                    if(data != null) {
                        write(data);
                        return;
                    }else{
                        showToast("输入的16进制不正确");
                    }
                }catch (NumberFormatException e){
                    showToast("输入的16进制不正确");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return;
    }

    /**
     * write
     * @param data
     * @return
     */
    private void write(byte[] data){
        if(device != null) {
            if(device.isConnected()) {
                LogUtil.i("write", "data = " + CommonUtils.bytes2HexString(data,true));
                write(writeBluetoothGattCharacteristic, data);
                return;
            }else {
                showToast("设备未连接");
            }
        }else {
            showToast("没有选择设备，请到主界面选择设备");
        }
        return;
    }

    /**
     * write
     * @param characteristic
     * @param data
     * @return
     */
    private void write(BluetoothGattCharacteristic characteristic,byte[] data){
        if(characteristic != null) {
            if (device != null && data != null && data.length > 0) {
                if (!checkWrite(characteristic.getProperties())) {
                    return;
                }
                if(!checkboxNoRespone.isChecked()) {
                    device.writeAutoIndentifyType(characteristic, data,mOptionsCallBack);
                    return;
                }else{
                    device.writeCharacteristic(characteristic, data,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,mOptionsCallBack);
                    return;
                }
            }
        }else{
            showToast("请选择要写的服务");
        }
        return;
    }

    private boolean read(){
        if(device != null && device.isConnected()){
            if(null != readBluetoothGattCharacteristic) {
                return device.readCharacteristic(readBluetoothGattCharacteristic);
            }else{
                showToast("请选择要读取的特征值");
            }
        }
        return false;
    }

    /**
     * 检查特征点是否可写
     * @param properties
     * @return
     */
    private boolean checkWrite(int properties){
        if ((properties
                & (BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                | BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE)) == 0) {
            showToast("not support write!");
            return false;
        }
        return true;
    }

    /**
     * 检查特征点是否可通知或可读
     * @param properties
     * @return
     */
    private boolean checkNotifyOrRead(int properties){
        if ((properties
                & (BluetoothGattCharacteristic.PROPERTY_NOTIFY
                | BluetoothGattCharacteristic.PROPERTY_INDICATE
                | BluetoothGattCharacteristic.PROPERTY_READ)) == 0) {
            showToast("not support read or notify!");
            return false;
        }
        return true;
    }

    private void showToast(String msg){
        if(msg != null && !msg.trim().equals(""))
            Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置设备
     * @param device
     */
    public void setDevice(BluetoothDeviceBean device) {
        if(this.device == null && device != null) {
            this.device = device;
            reFreshStateView();
        }else{
            if(device != null && this.device.getBleDevice().equals(device.getBleDevice())){
                this.device = device;
                reFreshStateView();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reFreshStateView();
    }

    /**
     * 刷新界面连接状态
     */
    private void reFreshStateView(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(null != connectState && device != null){

                    device.setmOnCharacteristicChangedCallBack(mOptionsCallBack)
                            .setmOnCharacteristicReadCallBack(mOptionsCallBack);

                    String str = "未连接";
                    switch (device.getConnectStatus()){
                        case BluetoothProfile.STATE_CONNECTED:
                            str = "已连接";
                            break;
                        case BluetoothProfile.STATE_CONNECTING:
                            str = "正在连接";
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            str = "未连接";
                            break;
                    }
                    String name = device.getBleDevice().getName();
                    if(name == null || name.trim().equals("")){
                        name = "Unknow";
                    }
                    deviceName.setText(name);
                    connectState.setText(str);
                }
            }
        });
    }

    /**
     * 连接状态回调
     */
    private IBleDeviceStateListener mBleDeviceStateListener = new IBleDeviceStateListener() {
        @Override
        public void connecting(BluetoothDeviceBean device) {
            LogUtil.i("connecting mBleDeviceStateListener thread name = " + Thread.currentThread().getName());
            setDevice(device);
        }

        @Override
        public void connectSuccess(BluetoothDeviceBean device) {
            LogUtil.i("connectSuccess mBleDeviceStateListener thread name = " + Thread.currentThread().getName());
            setDevice(device);
        }

        @Override
        public void connectFaild(BluetoothDeviceBean device) {
            LogUtil.i("connectFaild mBleDeviceStateListener thread name = " + Thread.currentThread().getName());
            writeBluetoothGattCharacteristic = null;
            readBluetoothGattCharacteristic = null;
            setDevice(device);
        }

        @Override
        public void disConnect(BluetoothDeviceBean device) {
            LogUtil.i("disConnect mBleDeviceStateListener thread name = " + Thread.currentThread().getName());
            writeBluetoothGattCharacteristic = null;
            readBluetoothGattCharacteristic = null;
            setDevice(device);
        }

        @Override
        public void notFound(String macOrFuzzyName, boolean isMac) {

        }

        @Override
        public void found(BluetoothDeviceBean device) {

        }
    };


    /**
     * 读写操作回调
     */
    private OptionsCallBack mOptionsCallBack= new OptionsCallBack() {
        @Override
        public void onCharacteristicWrite(BluetoothDeviceBean device, byte[] data, int status) {
            if(status == 0) {
                LogUtil.w("onCharacteristicWrite", Thread.currentThread().getName() + " status = " + status);
            }else{
                LogUtil.e("onCharacteristicWrite", Thread.currentThread().getName() + "write fail status = " + status);
            }
            if(data != null){
                LogUtil.d(CommonUtils.bytes2HexString(data,true));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothDeviceBean device, BluetoothGattCharacteristic characteristic, int status) {
            final byte[] data = characteristic.getValue();
            handleValue(data);
            if(null != data && data.length > 0) {
                LogUtil.i("onCharacteristicRead", "value = " + CommonUtils.bytes2HexString(data, true) + " status = " + (status == BluetoothGatt.GATT_SUCCESS));
            }else{
                LogUtil.i("onCharacteristicRead", "value is null , status = " + (status == BluetoothGatt.GATT_SUCCESS));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothDeviceBean device, BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            handleValue(data);
            LogUtil.i("onCharacteristicChanged",Thread.currentThread().getName() + " >>>>>>>>>>>>>>>>>>>>>>> " + CommonUtils.bytes2HexString(data, true));
        }

        private void handleValue(final byte[] data){
            if(data != null && data.length > 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkboxReceiver != null) {
                            if (checkboxReceiver.isChecked()) {
                                addReceiverContent(CommonUtils.bytes2HexString(data, true));
                            } else {
                                addReceiverContent(new String(data));
                            }
                        }
                    }
                });
            }
        }
    };

    /**
     * 选择服务
     * @param services
     */
    private void showService(final List<BluetoothGattService> services) {
        if(services != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setCancelable(true).create();
                    alertDialog.show();
                    View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ble_service_layout,null,false);
                    TextView textView = (TextView) view.findViewById(R.id.deviceName);
                    textView.setText(device.getBleDevice().getName());
                    ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
                    expandableListView.setAdapter(new MyExpandableListViewAdapter(services,getActivity()));
                    expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                            BluetoothGattCharacteristic characteristic = services.get(groupPosition).getCharacteristics().get(childPosition);
                            if(selectType == 1){
                                writeBluetoothGattCharacteristic = characteristic;
                                if(writeBluetoothGattCharacteristic != null){
                                    if(checkWrite(writeBluetoothGattCharacteristic.getProperties())){
                                        alertDialog.dismiss();
                                    }
                                }
                            }else{
                                if(null != characteristic){
                                    if(checkNotifyOrRead(characteristic.getProperties())){
                                        alertDialog.dismiss();
                                        if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0){
                                            device.readCharacteristic(characteristic);
                                            readBluetoothGattCharacteristic = characteristic;
                                        }else if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                                            if(device.setNotification(characteristic, true)){
                                                showToast("success");
                                            }else{
                                                showToast("fail");
                                            }
                                        }else{
                                            if(device.setCharacteristicIndication(characteristic,true)){
                                                showToast("success");
                                            }else{
                                                showToast("fail");
                                            }
                                        }
                                    }
                                }
                            }

                            return true;
                        }
                    });
                    alertDialog.setContentView(view);
                }
            });
        }
    }
}
