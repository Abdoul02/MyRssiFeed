package com.abdoul.myrssifeed.service

import com.abdoul.myrssifeed.model.DeviceInfo
import retrofit2.http.Body
import retrofit2.http.POST

interface RssiApi {

    @POST("/api/rssiInfo.php")
    suspend fun sendWifiData(@Body deviceInfo: DeviceInfo): DeviceInfo
}