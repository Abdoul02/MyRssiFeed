package com.abdoul.myrssifeed.service

import com.abdoul.myrssifeed.model.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RssiApiService @Inject constructor(private val api: RssiApi) {

    suspend fun uploadWifiInfo(deviceInfo: DeviceInfo): Flow<Result<DeviceInfo>> {

        return flow {
            emit(Result.success(api.sendWifiData(deviceInfo)))
        }.catch {
            emit(Result.failure(RuntimeException("Something went wrong")))
        }
    }
}