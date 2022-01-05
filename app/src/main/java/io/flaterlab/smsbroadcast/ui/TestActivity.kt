package io.flaterlab.smsbroadcast.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.Snackbar
import io.flaterlab.smsbroadcast.R
import io.flaterlab.smsbroadcast.databinding.ActivityTestBinding


class TestActivity : AppCompatActivity() {

    companion object {
        private const val SENT = "SMS_SENT"
        private const val DELIVERED = "SMS_DELIVERED"

        private val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    }

    val sendSms = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                RESULT_OK -> {
                    showSnackBar(getString(R.string.sms_sent))
                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    showSnackBar(getString(R.string.sms_send_failed_try_again))
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    showSnackBar(getString(R.string.no_service_sms_failed))
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    showSnackBar(getString(R.string.no_service_sms_failed))
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    showSnackBar(getString(R.string.no_service_sms_failed))
                }
            }
        }
    }

    val deliverSms = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                RESULT_OK -> showSnackBar(getString(R.string.sms_delivered))
                RESULT_CANCELED -> showSnackBar(getString(R.string.sms_not_delivered))
            }
        }
    }

    private lateinit var binding: ActivityTestBinding

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) sendCurrentSms()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            sendCurrentSms()
        }
        val subscriptionManager = getSystemService<SubscriptionManager>()!!
        val subscriptionList = subscriptionManager.activeSubscriptionInfoList
        val simInfo2 = subscriptionList[0] as SubscriptionInfo
        Log.d(
            "Mylog", "Sims: ${
                subscriptionList.joinToString {
                    "${it.displayName}, ${it.number}, ${it.mnc}"
                }
            }"
        )
    }

    private fun sendCurrentSms() {
        val phone = binding.etDestination.text?.toString()
        val message = binding.etMessage.text?.toString()
        if (phone == null || message == null) {
            Toast.makeText(this, "Invalid fields", Toast.LENGTH_SHORT).show()
            return
        }
        sendSms(phone, message)
        registerReceiver(sendSms, IntentFilter(SENT))
        registerReceiver(deliverSms, IntentFilter(DELIVERED))
    }

    @SuppressLint("MissingPermission")
    private fun sendSms(phone: String, message: String) {
        if (isSmsSendingPermissionsGranted()) {
            val sentPI = PendingIntent
                .getBroadcast(this, 0, Intent(SENT), PendingIntent.FLAG_IMMUTABLE)
            val deliveredPI = PendingIntent
                .getBroadcast(this, 1, Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE)

            val subscriptionManager = getSystemService<SubscriptionManager>()!!
            val smsManager = if (subscriptionManager.activeSubscriptionInfoCount > 1) {
                val subscriptionList = subscriptionManager.activeSubscriptionInfoList
                val simInfo2 = subscriptionList[0] as SubscriptionInfo
                Log.d(
                    "Mylog", "Sims: ${
                        subscriptionList.joinToString {
                            "${it.displayName}, ${it.number}, ${it.mnc}"
                        }
                    }"
                )
                SmsManager.getSmsManagerForSubscriptionId(simInfo2.subscriptionId)
            } else {
                getSystemService()!!
            }
            Log.d("Mylog", "Phone: $phone, Message: $message")
            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI)
        } else {
            permissionRequest.launch(permissions)
        }
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun isSmsSendingPermissionsGranted(): Boolean {
        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(sendSms)
            unregisterReceiver(deliverSms)
        } catch (ignored: Exception) {

        }
    }
}