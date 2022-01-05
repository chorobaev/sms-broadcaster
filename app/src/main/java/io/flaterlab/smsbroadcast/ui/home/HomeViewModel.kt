package io.flaterlab.smsbroadcast.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.flaterlab.smsbroadcast.domain.ServiceStatus
import io.flaterlab.smsbroadcast.domain.SmsBroadcastRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val smsBroadcastRepository: SmsBroadcastRepository,
) : ViewModel() {

    val serviceStatus = MutableLiveData(ServiceStatus.OFF)

    init {
        viewModelScope.launch {
            smsBroadcastRepository
                .serviceStatus()
                .collectLatest(serviceStatus::setValue)
        }
    }

    fun onToggleClicked() {
        viewModelScope.launch {
            if (serviceStatus.value == ServiceStatus.ON) {
                smsBroadcastRepository.stopService()
            } else {
                smsBroadcastRepository.startService()
            }
        }
    }
}