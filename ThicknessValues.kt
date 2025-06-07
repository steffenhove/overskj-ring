package no.steffenhove.betongkalkulator.ui.model

import com.google.gson.annotations.SerializedName

data class ThicknessValues(
    @SerializedName("minCutCm") val minCutCm: Float,
    @SerializedName("maxCutCm") val maxCutCm: Float,
    @SerializedName("overcutCm") val overcutCm: Float
)