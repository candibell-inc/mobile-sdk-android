package com.candibell.sdk

import android.content.Context
import com.polidea.rxandroidble2.exceptions.BleScanException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class Candibell private constructor(context: Context) {
    private val deviceService = DeviceService(context)
    private var scanDisposable: Disposable? = null

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

    fun scanTag() {

        //mView.showLoading()

        if (scanDisposable != null && scanDisposable!!.isDisposed.not()) {
            d("Already start scan tag, dispose first, then start scan again.")
            scanDisposable!!.dispose()
        }

        scanDisposable =
            deviceService.scanTag(null).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe({

                    //                mView.onScanTagResult(it.macAddress, it.rssi)
                }, { throwable ->
                    d("scanTag: $throwable")
                    if (throwable is BleScanException) {
//                    mView.onScanTagError(DeviceMapper.mapBleScanExceptionToError(throwable.reason))
                    } else {
//                    mView.onError(throwable.message ?: "", 0)
                    }
                }, {})
    }


    fun stopScan() {
        if (scanDisposable != null && !scanDisposable!!.isDisposed)
            scanDisposable!!.dispose()
    }
}