package io.flaterlab.smsbroadcast.data

data class SmsResult(
    val uid: Int,
    val status: Status
) {

    enum class Status {
        DELIVERED,
        FAILED,
        UNSUPPORTED_OPERATOR,
    }
}