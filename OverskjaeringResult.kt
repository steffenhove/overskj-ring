package no.steffenhove.betongkalkulator.ui.model

data class OverskjaeringResult(
    val minSkjaeringCm: Float,
    val maksSkjaeringCm: Float, // Dette representerer overkappet
    val minBorehullMm: Float
)