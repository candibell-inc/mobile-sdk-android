package com.candibell.sdk

// BLE Request
enum class BleServiceRequestType {
    CONNECT,
    DISCONNECT,
    F2_COMMAND,
    RESET
}

// BLE Service Event
enum class BleRequestDataType {
    BLE_REQUEST_TYPE,
    BLE_REQUEST_PARAMETER
}

enum class DeviceType {
    HUB,
    SENSOR
}

enum class BleOperation {
    CONNECT,
    DISCOVER_SERVICE,
    DISCONNECT,
    GET_CHARACTERISTIC_SERVICE,
    READ_CHARACTERISTIC_FOR_ENCRYPTION,
    GET_CHARACTERISTIC_DESCRIPTOR,
    WRITE_CHARACTER,
    CLOSE_GATT,
    RESET
}

enum class TagColor {
    RED,
    GREEN,
    BLUE,
    YELLOW
}

enum class HubState {
    SCANNED,
    CONNECTED,
    DISCONNECTED,
    CONFIGURED
}

enum class CandibellError {
    HUB_ERROR,
    SENSOR_ERROR
}

data class HubData(
    var macAddress: String = "",
    var macAddressLastFour: String = "",
    var id: String = ""
)

data class SensorData(
    var macAddress: String = "",
    var macAddressLastFour: String = "",
    var rawBytes: String = "",
    var rssi: Int = 0,
    var id: String = ""
)

data class FoundDevice(
    var macAddress: String = "",
    var rssi: Int = 0,
    var deviceType: DeviceType = DeviceType.SENSOR,
    var tagColor: TagColor = TagColor.RED,
    var rawBytes: String = "",
    var macAddressLastFour: String = "",
    var id: String = ""
)

data class SetupHubResponse(
    val responseType: SetupHubResponseType,
    val wifiErrorResponseType: WifiErrorResponseType
)

enum class SetupHubResponseType(var responseCode: Int) {

    OK(0),
    CONFIGURE_TIMEOUT(1),
    INVALID_WIFI_SSID_LENGTH(2),
    INVALID_WIFI_PASSWORD_LENGTH(3),
    INVALID_WIFI_EMAIL_LENGTH(4),
    INVALID_BLE_TIMEOUT(5),
    WIFI_ERROR(6),
    UNKNOWN(255)

}

enum class WifiErrorResponseType(var responseCode: Int) {

    OK(0),
    WIFI_REASON_UNSPECIFIED(1),
    WIFI_REASON_AUTH_EXPIRE(2),
    WIFI_REASON_AUTH_LEAVE(3),
    WIFI_REASON_ASSOC_EXPIRE(4),
    WIFI_REASON_ASSOC_TOOMANY(5),
    WIFI_REASON_NOT_AUTHED(6),
    WIFI_REASON_NOT_ASSOCED(7),
    WIFI_REASON_ASSOC_LEAVE(8),
    WIFI_REASON_ASSOC_NOT_AUTHED(9),
    WIFI_REASON_DISASSOC_PWRCAP_BAD(10),
    WIFI_REASON_DISASSOC_SUPCHAN_BAD(11),
    WIFI_REASON_IE_INVALID(13),
    WIFI_REASON_MIC_FAILURE(14),
    WIFI_REASON_4WAY_HANDSHAKE_TIMEOUT(15),
    WIFI_REASON_GROUP_KEY_UPDATE_TIMEOUT(16),
    WIFI_REASON_IE_IN_4WAY_DIFFERS(17),
    WIFI_REASON_GROUP_CIPHER_INVALID(18),
    WIFI_REASON_PAIRWISE_CIPHER_INVALID(19),
    WIFI_REASON_AKMP_INVALID(20),
    WIFI_REASON_UNSUPP_RSN_IE_VERSION(21),
    WIFI_REASON_INVALID_RSN_IE_CAP(22),
    WIFI_REASON_802_1X_AUTH_FAILED(23),
    WIFI_REASON_CIPHER_SUITE_REJECTED(24),
    WIFI_REASON_BEACON_TIMEOUT(200),
    WIFI_REASON_NO_AP_FOUND(201),
    WIFI_REASON_AUTH_FAIL(202),
    WIFI_REASON_ASSOC_FAIL(203),
    WIFI_REASON_HANDSHAKE_TIMEOUT(204),
    UNKNOWN(255)

}

enum class ScanBleExceptionType {
    BLUETOOTH_NOT_AVAILABLE,
    LOCATION_PERMISSION_NOT_GRANTED,
    BLUETOOTH_NOT_ENABLED,
    LOCATION_SERVICES_NOT_ENABLED
}

data class RegisterDeviceResp(
    val id: String,
    val userId: String,
    val deviceType: String,
    val deviceName: String? = null,
    val deviceColorInHex: String? = null,
    val warningPercentage: Double? = null
)

data class PostFactoryInfoReq(
    val id: String,
    val deviceType: String,
    val batchID: String? = null,
    val manufactureDate: Long? = null,
    val notes: String? = null
)

data class PostFactoryInfoResp(
    val id: String,
    val deviceType: String,
    val batchID: String? = null,
    val manufactureDate: Long? = null,
    val notes: String? = null
)

data class RegisterDeviceReq(
    val id: String,
    val userId: String,
    val deviceType: String,
    val deviceName: String? = null,
    val deviceColorInHex: Int? = null,
    val orderPercentage: Double? = null,
    val warningIdleTime: Long? = null,
    var notification: Boolean? = null,
    var perConsumptionNotification: Boolean? = null,
    var idleNotification: Boolean? = null,
    var product_upc_uuid: String? = null,
    val prodMeta: String? = null
)

data class ProviderResp(
    val provider_id: String,
    val provider_name: String,
    val provider_description: String
)

data class BindProductReq(
    val product_uuid: String,
    val userId: String,
    val id: String
)

data class BindProductResp(
    val product_upc_uuid: String
)

data class GetDeviceListItem(
    val id: String,
    val deviceType: DeviceType,
    val deviceName: String?,
    val userId: String,
    val createdTime: String,
    val lastUpdatedTime: String,
    val deviceColorInHex: String?,
    val orderPercentage: Double?,
    val warningIdleTime: Long?,
    val warningBatteryLow: Long?,
    val warningTempLow: Long?,
    val warningTempHigh: Long?,
    val notification: Boolean?,
    val perConsumptionNotification: Boolean?,
    val idleNotification: Boolean?,
    val tempNotification: Boolean?,
    var product_upc_uuid: String?,
    val battery: String?,
    val shakeCounter: Int?,
    val temperature: Int?,
    val prodMeta: String?,
    val seenSensors: MutableList<HubSeenSensorItem>
)

data class HubSeenSensorItem(
    var sensorId: String = "",
    var advertisingPower: String = "",
    var lastSeenTimestamp: String = ""
)




