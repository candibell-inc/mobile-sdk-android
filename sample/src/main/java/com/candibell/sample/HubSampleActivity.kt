package com.candibell.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.candibell.sdk.Candibell
import com.candibell.sdk.CandibellException
import com.candibell.sdk.HubData
import com.candibell.sdk.HubState

class HubSampleActivity : AppCompatActivity(), Candibell.CandibellHubListener {

    private lateinit var candibell: Candibell

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. getInstance
        candibell = Candibell.getInstance(this)

        // 2. setListener
        candibell.setHubListener(this)

        // 3. setUpHub
        candibell.configHub(
            wifiSSID = "wifiSSIDSampleString",
            wifiPassword = "wifiPasswordSampleString",
            userEmail = "userEmailSampleString",
            isTargetTestServer = true
        )
    }

    // 4. Get Callback
    override fun onHubStateChanged(hubData: HubData, hubState: HubState) {
    }

    override fun onError(exception: CandibellException) {
    }

    override fun onDestroy() {
        super.onDestroy()

        // 5. release candibell
        candibell.release()
    }
}