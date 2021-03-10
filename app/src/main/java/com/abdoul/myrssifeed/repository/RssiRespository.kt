package com.abdoul.myrssifeed.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.provider.Settings
import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.model.WifiInformation
import com.abdoul.myrssifeed.service.RssiApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RssiRepository @Inject constructor(
    private val service: RssiApiService,
    @ApplicationContext private val context: Context
) {

    private val wifiInfoList = mutableListOf<WifiInformation>()
    private var deviceId = "N/A"

    @SuppressLint("HardwareIds", "WifiManagerPotentialLeak")
    suspend fun uploadWifiInfo(): Flow<Result<DeviceInfo>> {

        val wifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        wifiManager?.let {
            val wifiList = it.scanResults

            for (scanResult in wifiList) {
                val map = HashMap<String, String>()
                val level = WifiManager.calculateSignalLevel(scanResult.level, 5)
                map["bssid"] = scanResult.BSSID
                map["ssid"] = scanResult.SSID
                map["level"] = level.toString()
                val wifiInfo = WifiInformation(scanResult.BSSID, scanResult.SSID, level)
                wifiInfoList.add(wifiInfo)
            }
            deviceId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        }

        return service.uploadWifiInfo(DeviceInfo(deviceId, wifiInfoList.toList())).map {
            if (it.isSuccess)
                Result.success(it.getOrNull()!!)
            else
                Result.failure(it.exceptionOrNull()!!)
        }
    }
}