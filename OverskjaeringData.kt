package no.steffenhove.betongkalkulator.ui.model

// Denne representerer alle datapunktene for én bladdiameter
data class OverskjaeringData(
    val bladeSize: Int,
    // Map fra Betongtykkelse (Int) til et par med (Overkapp, Skjæredybde)
    val dataPoints: Map<Int, Pair<Float, Float>>
)