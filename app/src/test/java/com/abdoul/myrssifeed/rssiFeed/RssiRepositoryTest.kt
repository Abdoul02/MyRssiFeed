package com.abdoul.myrssifeed.rssiFeed

import com.abdoul.myrssifeed.model.DeviceInfo
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
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
class RssiRepositoryTest : BaseUnitTest() {

    private val service: RssiApiService = mock()
    private val deviceInfo: DeviceInfo = mock()
    private val exception = RuntimeException("Something went wrong")

    @Test
    fun `GIVEN uploadWifiInfo is called, THEN verify repository upload data via service `() =
        runBlockingTest {
            val repository = mockNetworkResponse()

            repository.uploadWifiInfo(deviceInfo)

            verify(service, times(1)).uploadWifiInfo(deviceInfo)
        }

    @Test
    fun `GIVEN uploadWifiInfo returns successful response, THEN deviceInfo is emitted`() =
        runBlockingTest {
            val repository = mockNetworkResponse()

            assertEquals(deviceInfo, repository.uploadWifiInfo(deviceInfo).first().getOrNull())
        }

    @Test
    fun `GIVEN uploadWifiInfo returns failure response, THEN exception is emitted`() =
        runBlockingTest {
            val repository = mockNetworkResponse(false)

            assertEquals(exception, repository.uploadWifiInfo(deviceInfo).first().exceptionOrNull())
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

        return RssiRepository(service)
    }
}