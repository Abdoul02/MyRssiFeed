package com.abdoul.myrssifeed.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.abdoul.myrssifeed.R
import com.abdoul.myrssifeed.databinding.ActivityMainBinding
import com.abdoul.myrssifeed.model.DeviceInfo
import com.abdoul.myrssifeed.model.WifiInformation
import com.abdoul.myrssifeed.other.AppUtility
import com.abdoul.myrssifeed.other.AppUtility.Companion.EXTRA_KEY
import com.abdoul.myrssifeed.other.AppUtility.Companion.WORKER_TAG
import com.abdoul.myrssifeed.service.WifiLogWorker
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var appUtility: AppUtility

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerWorker()
        requestPermission()

        if (!appUtility.isLocationEnabled()) {
            appUtility.showMessage(getString(R.string.turn_on_location))
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        observeResponse()

        binding.btnUploadLog.setOnClickListener {
            uploadRssiLog()
        }
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extra = intent?.extras
        extra?.let {
            if (it.containsKey(EXTRA_KEY)) {
                val deviceInfo = it.get(EXTRA_KEY) as DeviceInfo
                showResponse(deviceInfo)
            }
        }
    }

    private fun observeResponse() {
        lifecycleScope.launchWhenStarted {
            mainViewModel.wifiInfoState.collect {
                when (it) {
                    is MainViewModel.ViewAction.Loading -> showHideProgress(it.showProgress)
                    is MainViewModel.ViewAction.DeviceResponse -> handleResponse(it.deviceInfo)
                    MainViewModel.ViewAction.Empty -> Unit
                }
            }
        }
    }

    private fun handleResponse(deviceInfo: Result<DeviceInfo>) {
        if (deviceInfo.getOrNull() != null) {
            showResponse(deviceInfo.getOrNull()!!)
        } else {
            appUtility.showSnackBarMessage(binding.container, getString(R.string.generic_error))
        }
    }

    private fun showResponse(deviceInfo: DeviceInfo) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val prettyJson = gson.toJson(deviceInfo)
        binding.wifiInfo.text = prettyJson
        binding.wifiInfo.isVisible = true
    }

    private fun showHideProgress(showProgress: Boolean) {
        binding.loading.isVisible = showProgress
    }

    private fun uploadRssiLog() {
        if (appUtility.hasLocationPermission(this)) {

            if (!appUtility.isLocationEnabled()) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } else {
                sendWifiData()
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun sendWifiData() {
        val wifiManager =
            this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        wifiManager?.let {
            val wifiList = it.scanResults
            val infoList = arrayListOf<HashMap<String, String>>()
            val wifiInfoList = mutableListOf<WifiInformation>()

            for (scanResult in wifiList) {
                val map = HashMap<String, String>()
                val level = WifiManager.calculateSignalLevel(scanResult.level, 5)
                map["bssid"] = scanResult.BSSID
                map["ssid"] = scanResult.SSID
                map["level"] = level.toString()
                val wifiInfo = WifiInformation(scanResult.BSSID, scanResult.SSID, level)
                wifiInfoList.add(wifiInfo)
                infoList.add(map)
            }
            val deviceId = appUtility.getDeviceId()
            mainViewModel.uploadWifiInfo(DeviceInfo(deviceId, wifiInfoList.toList()))
        }
    }

    private fun registerWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val myWorker = PeriodicWorkRequest.Builder(WifiLogWorker::class.java, 20, TimeUnit.MINUTES)
            .setInitialDelay(1, TimeUnit.MINUTES) //For testing purposes
            .setConstraints(constraints)
            .addTag(WORKER_TAG)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, myWorker)
    }

    private fun requestPermission() {

        if (appUtility.hasLocationPermission(this)) {
            return
        } else {
            when {
                android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.Q -> {
                    EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.all_permission_required),
                        AppUtility.REQUEST_CODE_LOCATION_PERMISSION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R -> {
                    EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.background_location_permission_message),
                        AppUtility.REQUEST_CODE_LOCATION_PERMISSION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
                else -> {
                    EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.all_permission_required),
                        AppUtility.REQUEST_CODE_LOCATION_PERMISSION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).setThemeResId(R.style.AlertDialogTheme).build().show()
        } else {
            requestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }
}