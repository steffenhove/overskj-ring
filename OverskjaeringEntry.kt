package no.steffenhove.betongkalkulator.ui.model

import com.google.gson.annotations.SerializedName

// Denne klassen representerer én enkelt linje i den nye JSON-filen din
data class OverskjaeringEntry(
    @SerializedName("bladeSize") val bladeSize: Int,
    @SerializedName("betongtykkelse_cm") val betongtykkelse_cm: Int,
    @SerializedName("minSkjaeringCm") val minSkjaeringCm: Float,
    @SerializedName("maksSkjaeringCm") val maksSkjaeringCm: Float,
    @SerializedName("overcutCm") val overcutCm: Float? // Gjør denne valgfri hvis den ikke alltid er der
)