package com.abdoul.myrssifeed.model

import com.google.gson.annotations.SerializedName

data class WifiInformation (
    @SerializedName("bssid")
    val bssid : String,
    @SerializedName("ssid")
    val ssid : String,
    @SerializedName("level")
    val level : Int
)