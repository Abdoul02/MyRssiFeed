package com.abdoul.myrssifeed.repository

import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.service.RssiApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RssiRepository @Inject constructor(private val service: RssiApiService) {

    suspend fun uploadWifiInfo(deviceInfo: DeviceInfo): Flow<Result<DeviceInfo>> =
        service.uploadWifiInfo(deviceInfo).map {
            if (it.isSuccess)
                Result.success(it.getOrNull()!!)
            else
                Result.failure(it.exceptionOrNull()!!)
        }
}