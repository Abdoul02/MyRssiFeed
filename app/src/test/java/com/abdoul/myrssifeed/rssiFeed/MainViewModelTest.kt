package com.abdoul.myrssifeed.rssiFeed

import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.repository.RssiRepository
import com.abdoul.myrssifeed.ui.MainViewModel
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
class MainViewModelTest : BaseUnitTest() {

    private val repository: RssiRepository = mock()
    private val deviceInfo: DeviceInfo = mock()
    private val exception = RuntimeException("Something went wrong")
    private val expectedDeviceInfo = Result.success(deviceInfo)

    private val deviceInfoViewAction = MainViewModel.ViewAction.DeviceResponse(expectedDeviceInfo)
    private val deviceInfoErrorViewAction =
        MainViewModel.ViewAction.DeviceResponse(Result.failure(exception))

    @Test
    fun `GIVEN uploadWifiInfo is called, THEN verify MainViewModel upload data via repository`() =
        runBlockingTest {
            val viewModel = mockNetworkResponse()

            viewModel.uploadWifiInfo(deviceInfo)

            verify(repository, times(1)).uploadWifiInfo(deviceInfo)
        }

    @Test
    fun `GIVEN uploadWifiInfo is called, WHEN result is successful THEN emit deviceInfo via ViewAction`() =
        runBlockingTest {
            val viewModel = mockNetworkResponse()

            viewModel.uploadWifiInfo(deviceInfo)
            viewModel.wifiInfoState.first()

            assertEquals(deviceInfoViewAction, viewModel.wifiInfoState.first())
        }

    @Test
    fun `GIVEN uploadWifiInfo is called, WHEN result is failure THEN emit exception via ViewAction`() =
        runBlockingTest {
            val viewModel = mockNetworkResponse(false)

            viewModel.uploadWifiInfo(deviceInfo)

            assertEquals(deviceInfoErrorViewAction, viewModel.wifiInfoState.first())
        }

    private suspend fun mockNetworkResponse(success: Boolean = true): MainViewModel {
        whenever(repository.uploadWifiInfo(deviceInfo)).thenReturn(
            flow {
                if (success)
                    emit(Result.success(deviceInfo))
                else
                    emit(Result.failure<DeviceInfo>(exception))
            }
        )

        return MainViewModel(repository)
    }
}