package no.steffenhove.betongkalkulator.ui.model

data class OverskjaeringResult(
    val minSkjaeringCm: Float,    // Tidligere interpolertMin, nå med enhet i navnet
    val maksSkjaeringCm: Float,   // Tidligere interpolertMax, nå med enhet i navnet
    val minBorehullMm: Float      // Nytt felt for beregnet borehullstørrelse
)