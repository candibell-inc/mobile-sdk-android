package com.candibell.sdk

import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanResult

// TODO id and lastFour
object DeviceMapper {

    fun mapScanResultToFoundDevice(scanResult: ScanResult): FoundDevice {
        return if (isCandibellHub(scanResult.scanRecord.bytes)) {
            FoundDevice(scanResult.bleDevice.macAddress, scanResult.rssi, DeviceType.HUB)
        } else {
            FoundDevice(
                scanResult.bleDevice.macAddress,
                scanResult.rssi,
                DeviceType.SENSOR,
                getTagColorType(scanResult.scanRecord.bytes),
                rawBytes = DeviceUtils.bytesToHex(scanResult.scanRecord.bytes)
            )
        }
    }

    fun isCandibellHub(rawBytes: ByteArray): Boolean {
        return isCandibellDevice(rawBytes) && DeviceType.HUB == getDeviceType(rawBytes)
    }

    fun isCandibellTag(rawBytes: ByteArray): Boolean {
        return isCandibellDevice(rawBytes) && DeviceType.SENSOR == getDeviceType(rawBytes)
    }

    fun isCandibellDevice(rawBytes: ByteArray): Boolean {
        val companyIdHigh =
            String.format(
                "%02x",
                rawBytes[BaseConstant.COMPANY_ID_HIGH_BYTE_INDEX].toInt() and 0xff
            )
        val companyIdLow =
            String.format("%02x", rawBytes[BaseConstant.COMPANY_ID_LOW_BYTE_INDEX].toInt() and 0xff)

        val companyId = companyIdHigh + companyIdLow
        return companyId.equals(BaseConstant.COMPANY_ID, true)
    }

    fun getDeviceType(rawBytes: ByteArray): DeviceType? {
        val protocolId =
            String.format("%02x", rawBytes[BaseConstant.PROTOCOL_ID_INDEX].toInt() and 0xff)
        return when (protocolId) {
            BaseConstant.PROTOCOL_ID_HUB -> DeviceType.HUB
            BaseConstant.PROTOCOL_ID_TAG -> DeviceType.SENSOR
            else -> null
        }
    }

    fun getTagColorType(rawBytes: ByteArray): TagColor {
        val deviceColorInHex = rawBytes[BaseConstant.TAG_COLOR_INDEX].toInt() and 0xff
        return when (deviceColorInHex) {
            TagColor.RED.ordinal -> TagColor.RED
            TagColor.GREEN.ordinal -> TagColor.GREEN
            TagColor.BLUE.ordinal -> TagColor.BLUE
            TagColor.YELLOW.ordinal -> TagColor.YELLOW
            else -> TagColor.RED
        }
    }

    fun prepareSetupHubRequest(
        isTargetTestServer: Boolean,
        wifiSSID: String,
        wifiPassword: String,
        userEmail: String
    ): ByteArray {

        d(
            "prepareSetupHubRequest: isTestServer=$isTargetTestServer,wifiSSID=$wifiSSID, " +
                    "wifiPassword=$wifiPassword, userEmail=$userEmail"
        )

        val userEmailHexString = DeviceUtils.bytesToHex(userEmail.toByteArray()).toLowerCase()

        val serverTargetPre = if (isTargetTestServer) {
            BaseConstant.DEV_SERVER_CONFIG
        } else {
            BaseConstant.PROD_SERVER_CONFIG
        }

        val userEmailString = "$serverTargetPre/$userEmailHexString"

        d("prepareSetupHubRequest: userString: ${BuildConfig.FLAVOR}: $userEmailString")

        val commandIdByte = BaseConstant.CMD_ID.toByte()
        val reserveByte = BaseConstant.CMD_RESERVE_BYTE

        val wifiSSIDBytes = wifiSSID.toByteArray()
        val wifiPasswordBytes = wifiPassword.toByteArray()
        val userEmailBytes = userEmailString.toByteArray()

        val wifiSSIDLength = wifiSSIDBytes.size
        val wifiPasswordLength = wifiPasswordBytes.size
        val userEmailLength = userEmailBytes.size

        val cmd =
            ByteArray(1 + 1 + 2 + wifiSSIDLength + 2 + wifiPasswordLength + 2 + userEmailLength)

        var i = 0
        cmd[i++] = commandIdByte
        cmd[i++] = reserveByte.toByte()

        cmd[i++] = (wifiSSIDLength shr 8 and 0xFF).toByte()
        cmd[i++] = (wifiSSIDLength and 0xFF).toByte()
        System.arraycopy(wifiSSIDBytes, 0, cmd, i, wifiSSIDLength)
        i += wifiSSIDLength

        cmd[i++] = (wifiPasswordLength shr 8 and 0xFF).toByte()
        cmd[i++] = (wifiPasswordLength and 0xFF).toByte()
        System.arraycopy(wifiPasswordBytes, 0, cmd, i, wifiPasswordLength)
        i += wifiPasswordLength

        cmd[i++] = (userEmailLength shr 8 and 0xFF).toByte()
        cmd[i++] = (userEmailLength and 0xFF).toByte()
        System.arraycopy(userEmailBytes, 0, cmd, i, userEmailLength)

        return cmd
    }

    fun getSetupHubResponse(bytes: ByteArray): SetupHubResponse {
        var result = SetupHubResponseType.UNKNOWN
        var wifiResult = WifiErrorResponseType.OK
        val responseHexString = DeviceUtils.bytesToHex(bytes)

        if (responseHexString.startsWith(BaseConstant.CMD_PREFIX, true)) {
            if (bytes.size > 2) {
                val responseCode = bytes[2].toInt() and 0xFF
                result = SetupHubResponseType.values().first { it.responseCode == responseCode }

                if (result == SetupHubResponseType.WIFI_ERROR) {
                    e("isResponseSuccess: WIFI_ERROR, need to parse extra wifi code.")
                    val wifiResponseCode = bytes[3].toInt() and 0xFF
                    wifiResult =
                        WifiErrorResponseType.values()
                            .first { it.responseCode == wifiResponseCode }
                }

            }
        } else {
            result = SetupHubResponseType.UNKNOWN
        }
        val response = SetupHubResponse(result, wifiResult)

        d("isResponseSuccess: $response")
        return response
    }

    fun isWifiErrorDueToAuthFail(wifiErrorResponseType: WifiErrorResponseType): Boolean {
        return when (wifiErrorResponseType) {
            WifiErrorResponseType.WIFI_REASON_802_1X_AUTH_FAILED,
            WifiErrorResponseType.WIFI_REASON_ASSOC_NOT_AUTHED,
            WifiErrorResponseType.WIFI_REASON_AUTH_EXPIRE,
            WifiErrorResponseType.WIFI_REASON_AUTH_FAIL,
            WifiErrorResponseType.WIFI_REASON_AUTH_LEAVE,
            WifiErrorResponseType.WIFI_REASON_NOT_AUTHED,
            WifiErrorResponseType.WIFI_REASON_4WAY_HANDSHAKE_TIMEOUT -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun mapBleScanExceptionToError(reason: Int): ScanBleExceptionType {
        return when (reason) {
            BleScanException.BLUETOOTH_DISABLED -> ScanBleExceptionType.BLUETOOTH_NOT_ENABLED
            BleScanException.LOCATION_PERMISSION_MISSING -> ScanBleExceptionType.LOCATION_PERMISSION_NOT_GRANTED
            BleScanException.LOCATION_SERVICES_DISABLED -> ScanBleExceptionType.LOCATION_SERVICES_NOT_ENABLED
            else -> {
                ScanBleExceptionType.BLUETOOTH_NOT_AVAILABLE
            }
        }
    }

    fun convertMacAddressToDeviceId(macAddress: String): String {
        return macAddress.replace(":", "").toUpperCase()
    }

}