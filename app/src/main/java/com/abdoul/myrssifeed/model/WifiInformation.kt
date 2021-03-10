package com.abdoul.myrssifeed.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WifiInformation(
    @SerializedName("bssid")
    val bssid: String,
    @SerializedName("ssid")
    val ssid: String,
    @SerializedName("level")
    val level: Int
) : Parcelable