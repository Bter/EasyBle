package cn.com.bter.easyble;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.com.bter.easyble.adapter.BaseRecycleViewAdapter;
import cn.com.bter.easyble.adapter.BleDevicesItemAdapter;
import cn.com.bter.easyble.easyblelib.core.BluetoothDeviceBean;
import cn.com.bter.easyble.easyblelib.core.EasyBleManager;
import cn.com.bter.easyble.easyblelib.interfaces.IBleDeviceStateListener;
import cn.com.bter.easyble.easyblelib.interfaces.IOnReadRemoteRssiCallBack;
import cn.com.bter.easyble.easyblelib.scan.FilterBleLeScanCallback;
import cn.com.bter.easyble.easyblelib.utils.LogUtil;
import cn.com.bter.easyble.fragment.OptionsFragment;
import cn.com.bter.easyble.utils.CommonUtils;
import cn.com.bter.easyble.view.RecycleViewDivider;

public class MainActivity extends AppCompatActivity{
    private EasyBleManager manager;
    private RecyclerView recycleView;
    private BleDevicesItemAdapter mBleDevicesItemAdapter;
    private View progressBar;

    private List<BluetoothDeviceBean> mData = new ArrayList();

    private Handler mHandler = new Handler();

    private IBleDeviceStateListener outIBleDeviceStateListener;

    private HandlerThread refreshRemoteRssiThrad = new HandlerThread("refreshRemoteRssiThrad");
    private Handler refreshRemoteRssiHandler;

    {
        refreshRemoteRssiThrad.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycleView = (RecyclerView) findViewById(R.id.recycleView);
        progressBar = findViewById(R.id.progressBar);

        mBleDevicesItemAdapter = new BleDevicesItemAdapter(this,recycleView,mData,viewBean);
        recycleView.setAdapter(mBleDevicesItemAdapter);
        recycleView.hasFixedSize();
        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.addItemDecoration(new RecycleViewDivider(this,LinearLayoutManager.HORIZONTAL ,
                CommonUtils.dp2px(this,10), Color.parseColor("#FFFFFF")));

        mBleDevicesItemAdapter.setOnItemClickLitener(onItemClickLitener);

        clearData();
        reFresh();

        refreshRemoteRssiHandler = new Handler(refreshRemoteRssiThrad.getLooper());

        manager = new EasyBleManager(this,mBleDeviceStateListener);
        manager.startSacn(scanCallback);
        /*manager.autoConnectBle("00:1B:10:A2:02:C5",1000 * 10,true,false);
        manager.autoConnectBle("00:1B:10:A0:2C:1A",1000 * 10,true,false);*/

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.startSacn(scanCallback);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.stopScan(scanCallback);
            }
        });
    }

    public void addIBleDeviceStateListener(IBleDeviceStateListener mIBleDeviceStateListener) {
        this.outIBleDeviceStateListener = mIBleDeviceStateListener;
    }

    /**
     * 运行在子线程
     */
    private IBleDeviceStateListener mBleDeviceStateListener = new IBleDeviceStateListener() {
        @Override
        public void connecting(BluetoothDeviceBean device) {
            addDevice(device,true);
            if(null != outIBleDeviceStateListener){
                outIBleDeviceStateListener.connecting(device);
            }
        }

        @Override
        public void connectSuccess(BluetoothDeviceBean device) {
            addDevice(device,true);
            device.setmOnReadRemoteRssiCallBack(mOnReadRemoteRssiCallBack);
            if(null != outIBleDeviceStateListener){
                outIBleDeviceStateListener.connectSuccess(device);
            }
        }

        @Override
        public void connectFaild(BluetoothDeviceBean device) {
            addDevice(device,true);
            device.setmOnReadRemoteRssiCallBack(null);
            if(null != outIBleDeviceStateListener){
                outIBleDeviceStateListener.connectFaild(device);
            }
        }

        @Override
        public void disConnect(BluetoothDeviceBean device) {
            addDevice(device,true);
            device.setmOnReadRemoteRssiCallBack(null);
            if(null != outIBleDeviceStateListener){
                outIBleDeviceStateListener.disConnect(device);
            }
        }

        @Override
        public void notFound(String macOrFuzzyName, boolean isMac) {

        }

        @Override
        public void found(BluetoothDeviceBean device) {

        }
    };

    private Runnable refreshRemoteRssiTask = new Runnable() {
        @Override
        public void run() {
            refreshRemoteRssiHandler.removeCallbacks(this);
            readRemoteDevicesRssi();
            refreshRemoteRssiHandler.postDelayed(this,1000);
        }
    };

    private void readRemoteDevicesRssi(){
        Collection<BluetoothDeviceBean> devices = manager.getConnectedDevices();
        if(devices.size() > 0){
            Iterator<BluetoothDeviceBean> iterator = devices.iterator();
            while (iterator.hasNext()){
                BluetoothDeviceBean device = iterator.next();
                if(device != null){
                    device.readRemoteRssi();
                }
            }
        }
    }

    private IOnReadRemoteRssiCallBack mOnReadRemoteRssiCallBack = new IOnReadRemoteRssiCallBack() {
        @Override
        public void onReadRemoteRssi(BluetoothDeviceBean device, int status) {
            reFreshRssiTask.setDevice(device);
            mHandler.post(reFreshRssiTask);
        }
    };

    /**
     * 刷新界面
     */
    private void reFresh(){
        mHandler.post(reFreshTask);
    }

    private Runnable reFreshTask = new Runnable() {
        @Override
        public void run() {
            if(null != mBleDevicesItemAdapter) {
                mBleDevicesItemAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * 点击蓝牙设备
     * click item listener
     */
    private BaseRecycleViewAdapter.OnItemClickLitener onItemClickLitener = new BaseRecycleViewAdapter.OnItemClickLitener() {

        @Override
        public void onItemClick(View view, int position) {
            LogUtil.i("click item");
            final BluetoothDeviceBean device = mData.get(position);
            if(!device.isConnectingOrConnected()) {
                connectBle(device);
            }else if(device.isConnected()){
                new AlertDialog.Builder(MainActivity.this).setTitle("温馨提示")
                        .setMessage("是否要断开蓝牙?")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                device.disConnect();
                            }
                        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
            }
        }

        @Override
        public boolean onItemLongClick(View view, int position) {
            LogUtil.i("onItemLongClick");
            BluetoothDeviceBean device = mData.get(position);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            OptionsFragment optionsFragment = (OptionsFragment) Fragment.instantiate(MainActivity.this,OptionsFragment.class.getName());
            transaction.replace(R.id.fragmentContent,optionsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            optionsFragment.setDevice(device);
            return true;
        }
    };

    public void connectBle(BluetoothDeviceBean device){
        if(null != device) {
            manager.connectBle(device, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshRemoteRssiHandler.removeCallbacks(refreshRemoteRssiTask);
        refreshRemoteRssiHandler.removeCallbacksAndMessages(null);
        refreshRemoteRssiThrad.quit();
        refreshRemoteRssiThrad.interrupt();
        manager.destroy();
        clearData();
    }

    /**
     * 过滤类型的扫描器
     * 但不进行过滤
     */
    private FilterBleLeScanCallback scanCallback = new FilterBleLeScanCallback("MainActivity scanCallback","",1000 * 60 * 2,false,false) {
        @Override
        public void onLeScanFound(BluetoothDeviceBean device) {
            LogUtil.i("onLeScanFound","onLeScanFound mac = " + device.getBleDevice().getAddress());
            addDevice(device,true);
        }

        @Override
        public void refresh(BluetoothDeviceBean device) {
            reFreshRssiTask.setDevice(device);
            mHandler.post(reFreshRssiTask);
        }

        @Override
        protected void notifyStartScan() {
            clearData();
            reFresh();
            addDevices(manager.getConnectedDevices());
            refreshRemoteRssiHandler.removeCallbacks(refreshRemoteRssiTask);
            refreshRemoteRssiHandler.post(refreshRemoteRssiTask);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected void onScanTimeOut() {
            refreshRemoteRssiHandler.removeCallbacks(refreshRemoteRssiTask);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        protected void onScanCancel() {
            refreshRemoteRssiHandler.removeCallbacks(refreshRemoteRssiTask);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    };

    private void addDevices(Collection<BluetoothDeviceBean> devices){
        if(devices != null && devices.size() > 0){
            Iterator<BluetoothDeviceBean> iterator = devices.iterator();
            BluetoothDeviceBean device = null;
            while (iterator.hasNext()){
                device = iterator.next();
                addDevice(device,false);
            }
            addDevice(device,true);
        }
    }

    /**
     * 添加设备
     * @param device
     */
    private void addDevice(BluetoothDeviceBean device,boolean isNeedRefresh){
        if(null != device){
            boolean isExits = false;//是否已经存在
            int index = -1;//存在的位置
            for (int i = 0; i < mData.size(); i++) {
                BluetoothDeviceBean mDevice = mData.get(i);
                if(mDevice.getBleDevice().equals(device.getBleDevice())){
                    isExits = true;
                    break;
                }
            }
            if(!isExits) {
                mData.add(device);
            }

            if(isNeedRefresh) {
                reFresh();
            }

        }
    }

    private void clearData(){
        mData.clear();
        viewBean.clear();
    }

    /************因频繁notifyDataSetChanged导致要点击item两次才能连接的bug的解决方案*******************/
    private HashMap<TextView,BluetoothDeviceBean> viewBean = new HashMap<>();


    /**
     * 刷新信号强度
     * 注意要在UI线程中刷新
     */
    private void reFreshViewBean(BluetoothDeviceBean deviceBean){
        if(null == deviceBean)return;

        if(null != viewBean && viewBean.size() > 0){
            Set<TextView> textViews = viewBean.keySet();
            Iterator<TextView> textViewIterator = textViews.iterator();
            TextView textView = null;
            BluetoothDeviceBean tempBean = null;
            if(null != textViewIterator) {
                while (textViewIterator.hasNext()) {
                    tempBean = null;
                    textView = null;

                    textView = textViewIterator.next();
                    if (null != textView) {
                        tempBean = viewBean.get(textView);
                        if(tempBean != null
                                && tempBean.getBleDevice() != null
                                && tempBean.getBleDevice().equals(deviceBean.getBleDevice())){
                            textView.setText(BleDevicesItemAdapter.getDistanceDescri(this,deviceBean.getRssi()));
                        }
                    }
                }
            }
        }
    }

    private RefreshRssiTask reFreshRssiTask = new RefreshRssiTask();
    /**
     * 刷新信号强度任务
     */
    private class RefreshRssiTask implements Runnable{
        private BluetoothDeviceBean device;

        public void setDevice(BluetoothDeviceBean device) {
            this.device = device;
        }

        @Override
        public void run() {
            reFreshViewBean(device);
        }
    }


    /************因频繁notifyDataSetChanged导致要点击item两次才能连接的bug的解决方案*******************/
}
