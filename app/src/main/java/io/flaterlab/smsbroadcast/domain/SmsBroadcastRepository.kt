package io.flaterlab.smsbroadcast.domain

import io.flaterlab.smsbroadcast.data.SmsBroadcasterApi
import io.flaterlab.smsbroadcast.data.SmsMessage
import io.flaterlab.smsbroadcast.data.SmsResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton


interface SmsBroadcastRepository {

    fun serviceStatus(): Flow<ServiceStatus>

    fun message(): ReceiveChannel<SmsMessage>

    fun sendResult(result: SmsResult): Boolean

    suspend fun startService()

    suspend fun stopService()
}

@Singleton
class SmsBroadcastRepositoryImpl @Inject constructor(
    private val api: SmsBroadcasterApi
) : SmsBroadcastRepository {

    private val serviceStatus = MutableStateFlow(ServiceStatus.OFF)

    override fun serviceStatus(): Flow<ServiceStatus> {
        return serviceStatus
    }

    override fun message(): ReceiveChannel<SmsMessage> {
        return api.messages()
    }

    override fun sendResult(result: SmsResult): Boolean {
        return api.sendResult(result)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun startService() {
        serviceStatus.value = ServiceStatus.ON
    }

    override suspend fun stopService() {
        serviceStatus.value = ServiceStatus.OFF
    }
}