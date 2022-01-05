package io.flaterlab.smsbroadcast

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import io.flaterlab.smsbroadcast.data.Operator
import io.flaterlab.smsbroadcast.data.SmsResult
import io.flaterlab.smsbroadcast.domain.ServiceStatus
import io.flaterlab.smsbroadcast.domain.SmsBroadcastRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastService : Service() {

    companion object {
        private const val SENT = "SMS_SENT"
        private const val DELIVERED = "SMS_DELIVERED"
        private const val MESSAGE_UID = "MESSAGE_UID"

        private const val FOREGROUND_ID = 101

        fun start(context: Context) {
            context.startForegroundService(
                Intent(context, SmsBroadcastService::class.java)
            )
        }
    }

    private val sentBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    // sms_sent
                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    // sms_send_failed_try_again
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    // no_service_sms_failed
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    // getString(R.string.no_service_sms_failed
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    // no_service_sms_failed
                }
            }
        }
    }

    private val deliveryBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val uid = intent?.getIntExtra(MESSAGE_UID, -1) ?: return
            if (uid == -1) return
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> repository.sendResult(
                    SmsResult(uid, SmsResult.Status.DELIVERED)
                )
                AppCompatActivity.RESULT_CANCELED -> repository.sendResult(
                    SmsResult(uid, SmsResult.Status.FAILED)
                )
            }
        }
    }

    @Inject
    lateinit var repository: SmsBroadcastRepository

    private var coroutineScope: CoroutineScope? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        observeStatus()
        observeMessages()
        registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))
        registerReceiver(deliveryBroadcastReceiver, IntentFilter(DELIVERED))
    }

    private fun startForeground() {
        val notificationBuilder =
            NotificationCompat.Builder(this, SmsApp.NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(FOREGROUND_ID, notification)
    }

    private fun observeStatus() {
        coroutineScope?.launch {
            repository.serviceStatus()
                .collectLatest { status ->
                    if (status == ServiceStatus.OFF) stopSelf()
                }
        }
    }

    private fun observeMessages() {
        coroutineScope?.launch {
            repository.message()
                .consumeEach { smsMessage ->
                    val uid = smsMessage.data?.sms_id
                    val phone = smsMessage.data?.number
                    val message = smsMessage.data?.text
                    if (phone != null && message != null && uid != null) {
                        try {
                            sendSms(
                                phone = phone,
                                message = message,
                                targetMnc = Operator
                                    .values()
                                    .find { it.codes.contains(Operator.getCode(phone)) }
                                    ?.networkCode ?: -1,
                                deliveryExtras = bundleOf(
                                    MESSAGE_UID to uid,
                                )
                            )
                        } catch (ex: UnsupportedOperatorException) {
                            repository.sendResult(
                                SmsResult(
                                    uid = uid,
                                    status = SmsResult.Status.UNSUPPORTED_OPERATOR
                                )
                            )
                        }
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendSms(
        phone: String,
        message: String,
        targetMnc: Int = Operator.O.networkCode,
        sendingExtras: Bundle = bundleOf(),
        deliveryExtras: Bundle = bundleOf()
    ) {
        val sentPI = PendingIntent
            .getBroadcast(this, 0, Intent(SENT).apply {
                putExtras(sendingExtras)
            }, PendingIntent.FLAG_IMMUTABLE)
        val deliveredPI = PendingIntent
            .getBroadcast(this, 1, Intent(DELIVERED).apply {
                putExtras(deliveryExtras)
            }, PendingIntent.FLAG_IMMUTABLE)

        val subscriptionManager = getSystemService<SubscriptionManager>()!!
        val smsManager = if (subscriptionManager.activeSubscriptionInfoCount > 1) {
            val subscriptionList = subscriptionManager.activeSubscriptionInfoList
            val simInfo = subscriptionList.filterIsInstance<SubscriptionInfo>()
                .find { it.mnc == targetMnc }
            val subscriptionId = simInfo?.subscriptionId ?: throw UnsupportedOperatorException()
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        } else {
            getSystemService()!!
        }
        smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(sentBroadcastReceiver)
            unregisterReceiver(deliveryBroadcastReceiver)
        } catch (ignored: Exception) {

        }
        coroutineScope?.coroutineContext?.cancelChildren()
        coroutineScope = null
    }
}

class UnsupportedOperatorException : IllegalArgumentException()