# EasyBle
Android BLE封装，支持多连接BLE，方便BLE开发者快速集成。

所有API已经封装在EasyBleManager中。

1.EasyBleManager.isEnabled();//蓝牙是否已经打开

2.EasyBleManager.isSupportBle();//是否支持BLE

3.EasyBleManager.enable(boolean force);//打开蓝牙

4.EasyBleManager.hasScaning();//是否正在扫描

5.EasyBleManager.startSacn(EasyBleLeScanCallback easyBleLeScanCallback);//扫描BLE

6.EasyBleManager.stopScan(EasyBleLeScanCallback easyBleLeScanCallback);//停止当前回调扫描

7.EasyBleManager.cancelAllScan();//取消扫描，停止所有扫描

8.EasyBleManager.hasConnected();//是否已经有连接

9.EasyBleManager.hasConnectedOrConnecting();//是否已经有连接或正在连接的设备

10.EasyBleManager.isConnected(String mac);//通过mac地址判断某个设备是否已经连接

11.EasyBleManager.autoConnectBle(String deviceNameOrMac, int timeOut, boolean isAddress,boolean autoConnect);//自动连接指定MAC或名称的BLE

12.EasyBleManager.tryStopAutoConnectBle(boolean isCanCallBackResult);//尝试停止自动连接

13.EasyBleManager.connectBle(BluetoothDeviceBean device, boolean autoConnect);//连接BLE

14.EasyBleManager.setBleDeviceStateListener(IBleDeviceStateListener mBleDeviceStateListener);//设置设备连接监听器

15.EasyBleManager.getConnectedDevices();//获得已经连接的设备

16.BluetoothDeviceBean.disConnect();//断开连接
