package com.candibell.sdk

import android.annotation.SuppressLint
import android.content.Context
import com.candibell.sdk.rxbus.RxBus
import com.candibell.sdk.rxbus.SetupHubType
import com.candibell.sdk.rxbus.UISetupHubEvent
import com.polidea.rxandroidble2.exceptions.BleScanException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class Candibell private constructor(context: Context) {

    private val deviceService = DeviceService(context)
    private var scanDisposable: Disposable? = null
    private var hubListener: CandibellHubListener? = null
    private var sensorListener: CandibelSensorListener? = null

    private var isTargetTestServer: Boolean = true
    private var wifiSSID: String = ""
    private var wifiPassword: String = ""
    private var userEmail: String = ""

    companion object {
        @Volatile
        private var INSTANCE: Candibell? = null

        fun getInstance(context: Context): Candibell =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildCandibell(context).also { INSTANCE = it }
            }

        private fun buildCandibell(context: Context) =
            Candibell(context.applicationContext)
    }

    fun setHubListener(listener: CandibellHubListener) {
        this.hubListener = listener
    }

    fun setSensorListener(listener: CandibelSensorListener) {
        this.sensorListener = listener
    }

    fun configHub(
        wifiSSID: String,
        wifiPassword: String,
        userEmail: String,
        isTargetTestServer: Boolean = true
    ) {
        if (scanDisposable != null && scanDisposable!!.isDisposed.not()) {
            d("Already start scan, dispose first, then start scan again.")
            scanDisposable!!.dispose()
        }

        this.wifiSSID = wifiSSID
        this.wifiPassword = wifiPassword
        this.userEmail = userEmail
        this.isTargetTestServer = isTargetTestServer

        scanDisposable =
            deviceService.scanHub().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe({ foundDevice ->
                    hubListener?.onHubStateChanged(
                        HubData(foundDevice.macAddress),
                        HubState.SCANNED
                    )
                    stopScan()
                    deviceService.connect(foundDevice.macAddress)

                }, { throwable ->
                    //TODO
                    d("scanHub: $throwable")
                    hubListener?.onError(CandibellException(CandibellError.HUB_ERROR, ""))
                    if (throwable is BleScanException) {
//                    mView.onScanTagError(DeviceMapper.mapBleScanExceptionToError(throwable.reason))
                    } else {
//                    mView.onError(throwable.message ?: "", 0)
                    }
                }, {})
    }

    fun scanSensor() {

        if (scanDisposable != null && scanDisposable!!.isDisposed.not()) {
            d("Already start scan, dispose first, then start scan again.")
            scanDisposable!!.dispose()
        }

        scanDisposable =
            deviceService.scanTag(null).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe({
                    //TODO
                    sensorListener?.onSensorFound(SensorData(it.macAddress, rssi = it.rssi))
                }, { throwable ->
                    d("scanTag: $throwable")
                    if (throwable is BleScanException) {
//                    mView.onScanTagError(DeviceMapper.mapBleScanExceptionToError(throwable.reason))
                    } else {
//                    mView.onError(throwable.message ?: "", 0)
                    }
                    sensorListener?.onError(CandibellException(CandibellError.SENSOR_ERROR, ""))
                }, {})
    }

    fun release() {
        stopScan()
        deviceService.reset()
        hubListener = null
        sensorListener = null
    }

    private fun stopScan() {
        if (scanDisposable != null && !scanDisposable!!.isDisposed)
            scanDisposable!!.dispose()
    }

    @SuppressLint("CheckResult")
    private fun registerRxEvent() {
        RxBus.instance.register(UISetupHubEvent::class.java)
            .subscribe {
                d("registerRxEvent: ${it.setupHubType}")
                when (it.setupHubType) {
                    SetupHubType.SETUP_HUB_CONNECTED -> {
                        hubListener?.onHubStateChanged(
                            HubData(it.parameter as String),
                            HubState.CONNECTED
                        )
                        deviceService.sendMessage(
                            isTargetTestServer,
                            wifiSSID,
                            wifiPassword,
                            userEmail
                        )
                    }
                    SetupHubType.SETUP_HUB_SUCCESS -> {
                        hubListener?.onHubStateChanged(
                            HubData(it.parameter as String),
                            HubState.CONFIGURED
                        )
                    }
                    // TODO error message
                    SetupHubType.SETUP_HUB_DISCONNECTED -> {
                        hubListener?.onError(CandibellException(CandibellError.HUB_ERROR, ""))
                    }
                    SetupHubType.SETUP_HUB_FAIL -> {
                        val response = it.parameter as SetupHubResponse
                        hubListener?.onError(CandibellException(CandibellError.HUB_ERROR, ""))
                    }
                }

            }
    }

    interface CandibellHubListener {
        fun onHubStateChanged(hubData: HubData, hubState: HubState)

        fun onError(exception: CandibellException)
    }

    interface CandibelSensorListener {
        fun onSensorFound(sensorData: SensorData)

        fun onError(exception: CandibellException)
    }
}