# EasyBle
Android BLE封装，支持多连接BLE，方便BLE开发者快速集成。

主要API已经封装在EasyBleManager中。
以下只列举常用方法。

一、Base 基础部分<br/>
关键类：EasyBleManager

1.EasyBleManager.isEnabled();//蓝牙是否已经打开

2.EasyBleManager.isSupportBle();//是否支持BLE

3.EasyBleManager.enable(boolean force);//打开蓝牙

4.EasyBleManager. disable();//关闭蓝牙

5.EasyBleManager. destroy();//

二、Scan 扫描部分<br/>
关键类：EasyBleManager、EasyBleLeScanCallback

1.EasyBleManager.hasScaning();//是否有扫描正在进行

2.EasyBleLeScanCallback.isScaning();//是否正在扫描

3.EasyBleManager.startScan(EasyBleLeScanCallback easyBleLeScanCallback);//扫描BLE

4.EasyBleManager.stopScan(EasyBleLeScanCallback easyBleLeScanCallback);//停止当前指定扫描

5.EasyBleManager.cancelAllScan();//取消扫描，停止所有扫描

6.EasyBleManager.enableAutoManagerDevices();//实验功能（experiment），开启自动维护设备池任务

7.EasyBleManager.cancelAutoManagerDevices();//实验功能（experiment），取消自动维护设备池任务

三、Connect 连接部分

1.EasyBleManager.hasConnected();//是否已经有连接

2.EasyBleManager.hasConnectedOrConnecting();//是否已经有连接或正在连接的设备

3.EasyBleManager.isConnected(String mac);//通过mac地址判断某个设备是否已经连接

4.1 EasyBleManager.isConnectedOrConnecting(String mac);//是否已经连接或正在连接

5.EasyBleManager.autoConnectBle(String deviceNameOrMac, int timeOut, boolean isAddress,boolean autoConnect);//自动连接指定MAC或名称的BLE

6.EasyBleManager.tryStopAutoConnectBle(boolean isCanCallBackResult);//尝试停止自动连接

7.EasyBleManager.connectBle(BluetoothDeviceBean device, boolean autoConnect);//连接BLE

8.EasyBleManager.setBleDeviceStateListener(IBleDeviceStateListener mBleDeviceStateListener);//设置设备连接监听器

9.EasyBleManager.getConnectedDevices();//获得已经连接的设备

10.EasyBleManager.getConnectedDevice(String mac);//获得指定mac地址的已经连接的BLE设备

11.BluetoothDeviceBean.disConnect();//断开连接

12.BluetoothDeviceBean.isConnected();//该设备是否已经连接

13.BluetoothDeviceBean.getConnectStatus();//获取连接状态

四、Options 操作部分<br/>
关键类：BluetoothDeviceBean

1.发现服务(注意以下所有操作均在发现服务后才能操作)<br/>
BluetoothDeviceBean.discoverServices();

2.设置发现服务回调<br/>
BluetoothDeviceBean.setmOnDiscoverServiceCallBack(CallBack)

3.刷新缓存,刷新BLE设备的Services缓存<br/>
BluetoothDeviceBean.refreshDeviceCache();

4.enable notify,打开通知监听<br/>
BluetoothDeviceBean.enableNotify(String serviceUUID, String notifyUUID);

5.listenter notify CallBack,监听通知（类似于接收数据）<br/>
BluetoothDeviceBean.setmOnCharacteristicChangedCallBack(CallBack);

6.write no response,发送数据<br/>
BluetoothDeviceBean.writeNoResponse(String serviceUUID,String writeUUID,byte[] data);

7.write data assign type，指定发送模式来发送数据<br/>
BluetoothDeviceBean.write(String serviceUUID,String writeUUID,byte[] data,int writeType);


8.listenter write CallBack,监听发送回调（注意并非接收数据）<br/>
BluetoothDeviceBean.setmOnCharacteristicWriteCallBack(CallBack);


9.readCharacteristic，主动读取数据<br/>
BluetoothDeviceBean.read(String serviceUUID,String readUUID);

10.listenter read CallBack，监听读取到的数据<br/>
BluetoothDeviceBean.setmOnCharacteristicReadCallBack(CallBack);


11.读取当前连接的设备的信号强度<br/>
BluetoothDeviceBean.readRemoteRssi()；

12.listenter Rssi CallBack，监听读取到的信号强度<br/>
BluetoothDeviceBean.setmOnReadRemoteRssiCallBack（CallBack）；


13.Other,其它（还有其它几个操作，因为不常用且篇幅过长，所以不再列举）。


