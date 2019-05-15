package com.candibell.sdk

import android.content.Context
import android.content.Intent
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable


class DeviceService(val context: Context) {

    private val rxBleClient = RxBleClient.create(context)
    private lateinit var macAddress: String
    private val minRssi = -55

    fun scanHub(): Observable<FoundDevice> {
        val rssi = minRssi
        d("scanHub: $rssi")

        val scanSettings = ScanSettings.Builder().build()
        return rxBleClient.scanBleDevices(scanSettings)
            .filter {
                it.rssi >= rssi
            }
            .filter { DeviceMapper.isCandibellHub(it.scanRecord.bytes) }
            .flatMap {
                d("scanHub: macAddress:${it.bleDevice.macAddress}")
                macAddress = it.bleDevice.macAddress
                Observable.just(DeviceMapper.mapScanResultToFoundDevice(it))
            }
    }

    fun scanTag(rssiInput: Int?): Observable<FoundDevice> {

        var rssi = minRssi

        if (rssiInput != null) {
            d("scanTag: using input rssi")
            rssi = rssiInput
        }

        d("scanTag: $rssi")

        val scanSettings = ScanSettings.Builder().build()
        return rxBleClient.scanBleDevices(scanSettings)
            .filter { it.rssi >= rssi }
            .filter { DeviceMapper.isCandibellTag(it.scanRecord.bytes) }
            .flatMap { Observable.just(DeviceMapper.mapScanResultToFoundDevice(it)) }
    }

    fun connect(deviceAddress: String) {
        d("connect")
        startCommand(BleServiceRequestType.CONNECT, deviceAddress)
    }

    fun disconnect() {
        d("disconnect")
        startCommand(BleServiceRequestType.DISCONNECT)
    }

    fun reset() {
        d("reset")
        startCommand(BleServiceRequestType.RESET)
    }

    fun sendMessage(
        isTargetTestServer: Boolean,
        wifiSSID: String,
        wifiPassword: String,
        userEmail: String
    ) {
        d("sendMessage")
        startCommand(
            BleServiceRequestType.F2_COMMAND,
            requestParameterBytes = DeviceMapper.prepareSetupHubRequest(
                isTargetTestServer,
                wifiSSID,
                wifiPassword,
                userEmail
            )
        )
    }

    fun startCommand(
        bleServiceRequestType: BleServiceRequestType,
        requestParameterString: String? = null,
        requestParameterBytes: ByteArray? = null
    ) {
        d("startCommand : mContext.startService")
        if (requestParameterString != null) {
            Intent(context, BleService::class.java).also { intent ->
                intent.putExtra(BleRequestDataType.BLE_REQUEST_TYPE.name, bleServiceRequestType)
                intent.putExtra(
                    BleRequestDataType.BLE_REQUEST_PARAMETER.name,
                    requestParameterString
                )
                context.startService(intent)
            }
        } else if (requestParameterBytes != null) {
            Intent(context, BleService::class.java).also { intent ->
                intent.putExtra(BleRequestDataType.BLE_REQUEST_TYPE.name, bleServiceRequestType)
                intent.putExtra(
                    BleRequestDataType.BLE_REQUEST_PARAMETER.name,
                    requestParameterBytes
                )
                context.startService(intent)
            }
        } else {
            Intent(context, BleService::class.java).also { intent ->
                intent.putExtra(BleRequestDataType.BLE_REQUEST_TYPE.name, bleServiceRequestType)
                context.startService(intent)
            }
        }
    }
}