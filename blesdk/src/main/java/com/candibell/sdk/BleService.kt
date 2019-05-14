package com.candibell.sdk

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.*
import com.candibell.sdk.rxbus.RxBus
import com.candibell.sdk.rxbus.SetupHubType
import java.util.*

class BleService : Service() {
    
    private val mBleServiceThread = HandlerThread("bleServiceThread")
    @Volatile
    private var mBluetoothGatt: BluetoothGatt? = null
    @Volatile
    private var mWriteCharacterService: BluetoothGattCharacteristic? = null
    @Volatile
    private var mNotifyCharacterService: BluetoothGattCharacteristic? = null

    //Handler and All Ble Operations
    private lateinit var mBleHandler: BleHandler
    private lateinit var mBleDeviceAddress: String
    private var mBleDataBuffer: ByteArray? = null
    private var mIsF2CmdSent = false
    private var mIsDisconnectFromClient = false
    private var mBleCommandDataCache: ByteArray? = null
    private var mBleRetryTime = 3

    inner class BleHandler(looper: Looper) : Handler() {
        init {
            Handler(looper)
        }

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                BleOperation.CONNECT.ordinal -> bleOperationConnect()
                BleOperation.DISCOVER_SERVICE.ordinal -> bleOperationDiscoverService()
                BleOperation.DISCONNECT.ordinal -> bleOperationDisconnect()
                BleOperation.GET_CHARACTERISTIC_SERVICE.ordinal -> bleOperationGetCharacteristicService()
                BleOperation.READ_CHARACTERISTIC_FOR_ENCRYPTION.ordinal -> bleOperationReadCharacteristicForEncryption()
                BleOperation.GET_CHARACTERISTIC_DESCRIPTOR.ordinal -> bleOperationGetCharacteristicDescriptor()
                BleOperation.CLOSE_GATT.ordinal -> bleOperationCloseGatt()
                BleOperation.RESET.ordinal -> bleOperationReset()
            }
        }
    }

    private fun bleOperationCloseGatt() {
        d( "bleOperationCloseGatt")
        mBleHandler.removeCallbacksAndMessages(null)
        mBluetoothGatt?.close()
        mBluetoothGatt = null
    }

    private fun bleOperationReset() {
        d( "bleOperationReset")
        mBleHandler.removeCallbacksAndMessages(null)
        mBleDeviceAddress = ""
        mWriteCharacterService = null
        mNotifyCharacterService = null
    }

    private fun bleOperationReadCharacteristicForEncryption() {
        d( "bleOperationReadCharacteristicForEncryption")
        mNotifyCharacterService?.let {
            d( "readCharacteristic")
            mBluetoothGatt?.readCharacteristic(it)
        }
    }

    private fun writeCharacteristic(data: ByteArray): Boolean {
        d( "writeCharacteristic: ${DeviceUtils.bytesToHex(data)}")
        mWriteCharacterService?.let {
            it.value = data
            it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            return mBluetoothGatt?.writeCharacteristic(it) ?: false

        }
        return false
    }

    private fun bleOperationGetCharacteristicDescriptor() {
        d( "bleOperationGetCharacteristicDescriptor")

        mBluetoothGatt?.let {
            val isSuccess = it.setCharacteristicNotification(mNotifyCharacterService, true)
            if (isSuccess) {
                val descriptorList = mNotifyCharacterService?.descriptors
                descriptorList?.forEach { descriptor ->
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    it.writeDescriptor(descriptor)
                }
            } else {
                d( "mNotifyCharacterService setCharacteristicNotification fail")
            }
        }
    }

    private fun bleOperationGetCharacteristicService() {
        d( "bleOperationGetCharacteristicService")
        mBluetoothGatt?.let {
            val gattService = it.getService(UUID.fromString(BaseConstant.BLE_UUID_SERVICE))
            if (gattService != null) {
                mNotifyCharacterService =
                    gattService.getCharacteristic(UUID.fromString(BaseConstant.BLE_UUID_READ_CHARACTERISTIC))
                mWriteCharacterService =
                    gattService.getCharacteristic(UUID.fromString(BaseConstant.BLE_UUID_WRITE_CHARACTERISTIC))
                if (mNotifyCharacterService != null) {
                    mBleHandler.sendEmptyMessage(BleOperation.GET_CHARACTERISTIC_DESCRIPTOR.ordinal)
                } else {
                    d( "bleOperationGetCharacteristicService fail")
                }
            } else {
                d( "bleOperationGetCharacteristicService fail")
            }
        }
    }

    private fun bleOperationDisconnect() {
        d( "bleOperationDisconnect")
        if (mBluetoothGatt != null) {
            mBluetoothGatt?.disconnect()
        }
    }

    private fun bleOperationConnect() {
        d( "bleOperationConnect")
        if (mBleDeviceAddress.isNotBlank()) {
            val device = getBluetoothAdapter().getRemoteDevice(mBleDeviceAddress)
            mBluetoothGatt = device.connectGatt(applicationContext, false, mBleGattCallback)
        }
    }

    private fun bleOperationDiscoverService() {
        d( "bleOperationDiscoverService")
        mBluetoothGatt?.let {
            if (!it.discoverServices()) {
                d( "discoverServices fail")
                mBleHandler.sendEmptyMessageDelayed(BleOperation.DISCONNECT.ordinal, 0)
            } else {
                d( "discoverServices success")
            }
        }
    }

    //Ble Callback from System
    private val mBleGattCallback = TpdBleGattCallback()

    inner class TpdBleGattCallback : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            d( "gatt?.device?.address ===" + gatt?.device?.address)

            mBluetoothGatt = gatt
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    onBleConnected(gatt, status)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    onBleDisconnected(status)
                }
                else -> {
                    d( "onConnectionStateChange other State, no need to handle")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            d( "onServicesDiscovered:")

            fun handleSuccess() {
                e( "Ble handle service discovery Success")
                mBleHandler.sendEmptyMessageDelayed(
                    BleOperation.GET_CHARACTERISTIC_SERVICE.ordinal,
                    100
                )
            }

            fun handleDiscoverFail() {
                e( "Ble handle service discovery Fail")
                mBleHandler.sendEmptyMessage(BleOperation.DISCONNECT.ordinal)
            }

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> handleSuccess()
                else -> handleDiscoverFail()
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            d( "onDescriptorWrite: ")
            fun handleSuccess() {
                d( "handleSuccess, now we can start sending package.")
                RxBus.instance.postRxEvent(SetupHubType.SETUP_HUB_CONNECTED)
            }
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> handleSuccess()
                else -> {
                    d( "onDescriptorWrite: fail")
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            d( "onCharacteristicChanged: ")


            if (gatt?.device?.address == mBleDeviceAddress) {
                characteristic?.let {
                    val data = it.value
                    val dataHexString = DeviceUtils.bytesToHex(data)
                    d( "onCharacteristicChanged: $dataHexString")
                    d( "responseHexString: $dataHexString")
                    val response = DeviceMapper.getSetupHubResponse(data)
                    if (response.responseType == SetupHubResponseType.OK) {
                        d( "isResponseSuccess Notify UI to continue")
                        RxBus.instance.postRxEvent(SetupHubType.SETUP_HUB_SUCCESS)
                    } else {
                        d( "isResponseSuccess fail")
                        if (response.responseType == SetupHubResponseType.WIFI_ERROR && DeviceMapper.isWifiErrorDueToAuthFail(
                                response.wifiErrorResponseType
                            ).not()
                        ) {
                            d( "isResponseSuccess wifi logic error, need check retry")
                            if (mBleRetryTime > 0) {
                                d(
                                    "isResponseSuccess wifi logic error, mBleRetryTime: $mBleRetryTime"
                                )
                                mBleCommandDataCache?.let { byteArray ->
                                    mBleRetryTime--
                                    startUserCommand(byteArray)
                                }
                            } else {
                                RxBus.instance.postRxEvent(SetupHubType.SETUP_HUB_FAIL, response)
                            }

                        } else {
                            d(
                                "isResponseSuccess wifi Auth error or some other error, Notify UI to continue"
                            )
                            RxBus.instance.postRxEvent(SetupHubType.SETUP_HUB_FAIL, response)
                        }

                    }
                }
            } else {
                d( "no need to handle if the characteristic is not from same address")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            d( "onCharacteristicWrite: ${DeviceUtils.bytesToHex(characteristic?.value)}")
            mBleDataBuffer?.let {
                mBleHandler.postDelayed({ startUserCommand(it) }, 300)
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            d( "onReadRemoteRssi:  rssi == $rssi")
        }
    }

    private fun onBleConnected(gatt: BluetoothGatt?, status: Int) {
        d( "onBleConnected")
        fun handleSuccess() {
            d( "handleSuccess")
            mBleRetryTime = 3
            mWriteCharacterService = null
            mNotifyCharacterService = null
            mBleHandler.sendEmptyMessageDelayed(BleOperation.DISCOVER_SERVICE.ordinal, 200)
        }
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> handleSuccess()
            else -> {
                d( "onBleConnected: fail")
            }
        }

    }

    private fun onBleDisconnected(status: Int) {
        d( "onBleDisconnected: $status")

        if (status == 133) {
            if (mBleRetryTime > 0) {
                d(
                    "onBleDisconnected 133, retry, mBleRetryTime: $mBleRetryTime"
                )
                mBleRetryTime--
                connect(mBleDeviceAddress)
                return
            }
        }

        if (mIsF2CmdSent.not()) {
            RxBus.instance.postRxEvent(SetupHubType.SETUP_HUB_DISCONNECTED)
        }
    }

    //Service Life Cycle Logic
    init {
        d( "init")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        d( "onCreate")
        mBleServiceThread.start()
        mBleHandler = BleHandler(mBleServiceThread.looper)
    }

    override fun onDestroy() {
        super.onDestroy()
        d( "onDestroy")
        mBluetoothGatt?.close()
        mBluetoothGatt = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        d( "onStartCommand")
        intent?.let {
            val requestType = it.getSerializableExtra(BleRequestDataType.BLE_REQUEST_TYPE.name)
            when (requestType) {
                BleServiceRequestType.CONNECT -> {
                    mBleRetryTime = 3
                    connect(
                        it.getStringExtra(
                            BleRequestDataType.BLE_REQUEST_PARAMETER.name
                        )
                    )
                }
                BleServiceRequestType.DISCONNECT -> {
                    disconnect()
                }
                BleServiceRequestType.F2_COMMAND -> {
                    mBleRetryTime = 3
                    mBleCommandDataCache = it.getByteArrayExtra(
                        BleRequestDataType.BLE_REQUEST_PARAMETER.name
                    )

                    startUserCommand(
                        mBleCommandDataCache!!
                    )
                }
                BleServiceRequestType.RESET -> {
                    reset()
                }
            }
        }

        return Service.START_STICKY
    }

    //Ble Service Function

    private fun connect(address: String) {
        //removeBond(bluetoothDevice)
        mIsF2CmdSent = false
        mBleDeviceAddress = address
        d( "onStartCommand connect: $mBleDeviceAddress")
        mBleHandler.sendEmptyMessageDelayed(BleOperation.CONNECT.ordinal, 500)
    }

    private fun disconnect() {
        d( "onStartCommand disconnect:")
        mIsDisconnectFromClient = true
        mBluetoothGatt?.disconnect()
        mBleHandler.sendEmptyMessage(BleOperation.CLOSE_GATT.ordinal)
    }

    private fun reset() {
        d( "onStartCommand reset:")
        mBluetoothGatt?.disconnect()
        mBleHandler.sendEmptyMessage(BleOperation.CLOSE_GATT.ordinal)
        mBleHandler.sendEmptyMessage(BleOperation.RESET.ordinal)
    }

    private fun startUserCommand(data: ByteArray) {
        d( "startUserCommand data: ${DeviceUtils.bytesToHex(data)}")
        mIsF2CmdSent = true
        if (data.size > 20) {
            val sendDataBytes = ByteArray(20)
            System.arraycopy(data, 0, sendDataBytes, 0, 20)
            val remainBytes = ByteArray(data.size - 20)
            System.arraycopy(data, 20, remainBytes, 0, data.size - 20)

            mBleDataBuffer = remainBytes
            writeCharacteristic(sendDataBytes)

        } else {
            writeCharacteristic(data)
            mBleDataBuffer = null
        }
    }

    private fun getBluetoothAdapter(): BluetoothAdapter = (applicationContext.getSystemService(
        Context.BLUETOOTH_SERVICE
    ) as BluetoothManager).adapter

}
