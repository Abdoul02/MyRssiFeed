package com.abdoul.myrssifeed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.repository.RssiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: RssiRepository) : ViewModel() {

    private val _wifiInfoState = MutableStateFlow<ViewAction>(ViewAction.Empty)
    val wifiInfoState: StateFlow<ViewAction> = _wifiInfoState

    fun uploadWifiInfo(deviceInfo: DeviceInfo) {
        _wifiInfoState.value = ViewAction.Loading(true)
        viewModelScope.launch {
            repository.uploadWifiInfo(deviceInfo)
                .onEach {
                    _wifiInfoState.value = ViewAction.Loading(false)
                }.collect {
                    _wifiInfoState.value = ViewAction.DeviceResponse(it)
                }
        }
    }

    sealed class ViewAction {
        data class Loading(val showProgress: Boolean) : ViewAction()
        data class DeviceResponse(val deviceInfo: Result<DeviceInfo>) : ViewAction()
        object Empty : ViewAction()
    }
}