package com.abdoul.myrssifeed.rssiFeed

import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.service.RssiApi
import com.abdoul.myrssifeed.service.RssiApiService
import com.abdoul.myrssifeed.utils.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RssiApiServiceTest : BaseUnitTest() {

    private lateinit var service: RssiApiService
    private var api: RssiApi = mock()
    private val deviceInfo: DeviceInfo = mock()

    @Test
    fun `GIVEN uploadInfo is called, THEN verify that service upload device info to api `() =
        runBlockingTest {
            service = RssiApiService(api)

            service.uploadWifiInfo(deviceInfo).first()

            verify(api, times(1)).sendWifiData(deviceInfo)
        }

    @Test
    fun `GIVEN upload is called, WHEN result is successful THEN response is converted to Flow`() =
        runBlockingTest {
            whenever(api.sendWifiData(deviceInfo)).thenReturn(deviceInfo)

            service = RssiApiService(api)

            assertEquals(Result.success(deviceInfo), service.uploadWifiInfo(deviceInfo).first())
        }

    @Test
    fun `GIVEN upload is called, WHEN result is failure THEN response should throw error`() =
        runBlockingTest {
            whenever(api.sendWifiData(deviceInfo)).thenThrow(RuntimeException("Network error"))

            service = RssiApiService(api)

            assertEquals(
                "Something went wrong",
                service.uploadWifiInfo(deviceInfo).first().exceptionOrNull()?.message
            )
        }
}