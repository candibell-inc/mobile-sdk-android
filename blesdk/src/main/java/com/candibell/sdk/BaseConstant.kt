package com.candibell.sdk

object BaseConstant {

    const val CODE_BAD_REQUEST = 400
    const val CODE_UNAUTHORIZED = 401
    const val CODE_FORBIDDEN = 403
    const val CODE_NOT_FOUND = 404
    const val CODE_INTERNAL_ERROR = 501

    const val MESSAGE_BAD_REQUEST = "Something is not quite right about your request."
    const val MESSAGE_UNAUTHORIZED = "Please resign in."
    const val MESSAGE_FORBIDDEN = "Your request cannot be made."
    const val MESSAGE_NOT_FOUND = "Cannot find the item you are looking for."
    const val MESSAGE_INTERNAL_SERVER_ERROR =
        "There is something wrong with our server. Try again later."

    const val URL_ENCODING = "UTF-8"

    const val TEST_PROVIDER = "cron"

    const val TEST_PRODICT_UUID = "00000000-0000-0000-0000-000000000000"

    const val TABLE_PREFS: String = "Candibell_Android"
    const val KEY_SP_TOKEN = "token"
    const val LOG_TAG_CANDIBELL_ANDROID = "candibell"

    const val CANDY_TERM_SERVICE_URL =
        "/terms_and_conditions.html"
    const val CANDY_PRIVACY_POLICY_URL =
        "/privacy.html"
    const val CANDY_FEEDBACK_EMAIL = "support@candibell.com"
    const val CANDY_FEEDBACK_TITLE = "BUG REPORT"


    const val DEFAULT_WARNING_PERCENTAGE = 0.15
    const val DEFAULT_WARNING_IDLE_TIME = 43200.toLong()
    const val DEFAULT_WARNING_BATTERY_LOW = 2700.toLong()
    const val DEFAULT_WARNING_TEMP_LOW = (-10).toLong()
    const val DEFAULT_WARNING_TEMP_HIGH = 55.toLong()
    const val DEFAULT_WARNING_NOTIFICATION = true


    const val PROVIDER_CANDIBELL = "candibell"
    const val SP_KEY_USER_EMAIL = "user_email"
    const val SP_KEY_MIN_RSSI = "min_rssi"
    const val SP_KEY_WIFI_SSID = "wifi_ssid"
    const val SP_KEY_WIFI_PASSWORD = "wifi_password"
    const val SP_KEY_BATCH_ID = "batch_id"
    const val SP_KEY_BATCH_ID_TAG = "batch_id_tag"
    const val SP_KEY_FACTORY_NOTES = "factory_notes"

    const val COMPANY_ID = "BF00"

    const val PROTOCOL_ID_HUB = "10"
    const val PROTOCOL_ID_TAG = "00"

    const val COMPANY_ID_HIGH_BYTE_INDEX = 5
    const val COMPANY_ID_LOW_BYTE_INDEX = 6
    const val PROTOCOL_ID_INDEX = 9
    const val TAG_SESSION_COUNTER_FIRST_INDEX = 16
    const val TAG_BATTERY_FIRST_INDEX = 18
    const val TAG_TEMP_INDEX = 20
    const val TAG_CONSUMPTION_EVENT_FIRST_INDEX = 21
    const val TAG_MEASURED_POWER_INDEX = 23
    const val TAG_SHAKE_COUNTER_FIRST_INDEX = 24
    const val TAG_TIMER_FIRST_INDEX = 26
    const val TAG_COLOR_INDEX = 30

    const val CMD_PREFIX = "F200"
    const val CMD_ID = 242
    const val CMD_RESERVE_BYTE = 0x00

    const val DEV_SERVER_CONFIG = "dev"
    const val PROD_SERVER_CONFIG = "prod"

    const val BLE_UUID_NOTIFY_CHAR = "00002902-0000-1000-8000-00805f9b34fb"
    const val BLE_UUID_SERVICE = "B0F50200-C788-5791-8533-0D22318D5C56"
    const val BLE_UUID_WRITE_CHARACTERISTIC = "B0F50201-C788-5791-8533-0D22318D5C56"
    const val BLE_UUID_READ_CHARACTERISTIC = "B0F50202-C788-5791-8533-0D22318D5C56"
}