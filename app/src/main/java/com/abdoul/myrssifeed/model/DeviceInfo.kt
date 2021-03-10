package com.abdoul.myrssifeed.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceInfo(
    @SerializedName("imei")
    val imei: String,
    @SerializedName("wifiInformation")
    var wifiInformation: List<WifiInformation>
):Parcelable