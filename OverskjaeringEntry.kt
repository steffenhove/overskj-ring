package no.steffenhove.betongkalkulator.ui.model

import com.google.gson.annotations.SerializedName

data class OverskjaeringEntry(
    @SerializedName("bladeSize") val bladeSize: Int,
    @SerializedName("thicknessCm") val thicknessCm: Int,
    @SerializedName("maxOvercutCm") val maxOvercutCm: Float,
    @SerializedName("minCuttingDepthCm") val minCuttingDepthCm: Float
)