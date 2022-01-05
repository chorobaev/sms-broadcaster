package io.flaterlab.smsbroadcast.data

data class SmsMessage(
    val type: String?,
    val data: Sms?
)

data class Sms(
    val uid: String?,
    val number: String?,
    val text: String?
)