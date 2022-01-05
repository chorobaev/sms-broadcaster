package io.flaterlab.smsbroadcast.data

data class SmsMessage(
    val type: String?,
    val data: Sms?
)

data class Sms(
    val sms_id: Int,
    val number: String?,
    val text: String?
)