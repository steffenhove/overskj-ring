package no.steffenhove.betongkalkulator.ui.model // Sjekk at pakkenavnet er riktig

import com.google.gson.annotations.SerializedName

data class ThicknessValues(
    @SerializedName("minCutCm") val minCutCm: Float,
    @SerializedName("maxCutCm") val maxCutCm: Float,
    @SerializedName("overcutCm") val overcutCm: Float
)