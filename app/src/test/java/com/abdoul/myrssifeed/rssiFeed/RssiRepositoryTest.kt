package com.abdoul.myrssifeed.rssiFeed

import android.content.Context
import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.model.WifiInformation
import com.abdoul.myrssifeed.repository.RssiRepository
import com.abdoul.myrssifeed.service.RssiApiService
import com.abdoul.myrssifeed.utils.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RssiRepositoryTest : BaseUnitTest() {

    private val service: RssiApiService = mock()
    private val deviceInfo: DeviceInfo = mockDeviceInfo()
    private val exception = RuntimeException("Something went wrong")
    private val context = mock<@dagger.hilt.android.qualifiers.ApplicationContext Context>()

    @Test
    fun `GIVEN uploadWifiInfo is called, THEN verify repository upload data via service `() =
        runBlockingTest {
            val repository = mockNetworkResponse()

            repository.uploadWifiInfo()

            verify(service, times(1)).uploadWifiInfo(deviceInfo)
        }

    @Test
    fun `GIVEN uploadWifiInfo returns successful response, THEN deviceInfo is emitted`() =
        runBlockingTest {
            val repository = mockNetworkResponse()

            assertEquals(deviceInfo, repository.uploadWifiInfo().first().getOrNull())
        }

    @Test
    fun `GIVEN uploadWifiInfo returns failure response, THEN exception is emitted`() =
        runBlockingTest {
            val repository = mockNetworkResponse(false)

            assertEquals(exception, repository.uploadWifiInfo().first().exceptionOrNull())
        }


    private suspend fun mockNetworkResponse(success: Boolean = true): RssiRepository {
        whenever(service.uploadWifiInfo(deviceInfo)).thenReturn(
            flow {
                if (success)
                    emit(Result.success(deviceInfo))
                else
                    emit(Result.failure<DeviceInfo>(exception))
            }
        )

        return RssiRepository(service, context)
    }

    private fun mockDeviceInfo(): DeviceInfo {
        val wifiInfoList = mutableListOf<WifiInformation>()
        val deviceId = "N/A"
        return DeviceInfo(deviceId, wifiInfoList)
    }
}