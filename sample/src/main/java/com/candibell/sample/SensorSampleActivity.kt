package com.candibell.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.candibell.sdk.*

class SensorSampleActivity : AppCompatActivity(), Candibell.CandibelSensorListener {

    private lateinit var candibell: Candibell

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. getInstance
        candibell = Candibell.getInstance(this)

        // 2. setListener
        candibell.setSensorListener(this)

        // 3. scanSensor
        candibell.scanSensor()
    }

    // 4. Get Callback
    override fun onSensorFound(sensorData: SensorData) {
    }

    override fun onError(exception: CandibellException) {
    }

    override fun onDestroy() {
        super.onDestroy()

        // 5. release candibell
        candibell.release()
    }
}