package io.flaterlab.smsbroadcast.data

import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.channels.ReceiveChannel

interface SmsBroadcasterApi {

    @Receive
    fun messages(): ReceiveChannel<SmsMessage>

    @Send
    fun sendResult(result: SmsResult): Boolean
}