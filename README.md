# EasyBle
Android BLE封装，支持多连接BLE，方便BLE开发者快速集成。

所有API已经封装在EasyBleManager中。关键类EasyBleManager、BluetoothDeviceBean、EasyBleLeScanCallback

一、Base 基础部分

1.EasyBleManager.isEnabled();//蓝牙是否已经打开

2.EasyBleManager.isSupportBle();//是否支持BLE

3.EasyBleManager.enable(boolean force);//打开蓝牙

4.EasyBleManager. disable();//关闭蓝牙

5.EasyBleManager. destroy();//

二、Scan 扫描部分

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

四、Options 操作部分


