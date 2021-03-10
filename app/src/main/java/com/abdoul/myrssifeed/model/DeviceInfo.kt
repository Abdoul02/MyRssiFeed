package com.abdoul.myrssifeed.model

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("imei")
    val imei: String,
    @SerializedName("wifiInformation")
    var wifiInformation: List<WifiInformation>
)