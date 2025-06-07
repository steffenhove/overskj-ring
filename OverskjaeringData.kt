package no.steffenhove.betongkalkulator.ui.model // Sjekk at pakkenavnet er riktig

// Denne klassen representerer dataene for én enkelt bladdiameter
data class OverskjaeringData(
    val bladeSize: Int,
    val data: Map<Int, ThicknessValues> // Nøkkel: tykkelse i cm (Int), Verdi: ThicknessValues-objekt
)