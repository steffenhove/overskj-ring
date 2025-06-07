package no.steffenhove.betongkalkulator.data

val skjæredybdeTabell = mapOf(
    600 to 220.0,
    700 to 270.0,
    750 to 300.0,
    800 to 320.0,
    900 to 370.0,
    1000 to 420.0,
    1200 to 520.0,
    1500 to 650.0,
    1600 to 730.0
)

fun beregnOverskjaeringFraSkjaeredybde(bladdiameter: Int, tykkelse: Double): Triple<Double, Double, Double>? {
    val dybde = skjæredybdeTabell[bladdiameter] ?: return null
    val maks = (dybde * 2) - tykkelse
    val min = dybde - tykkelse
    val anbefaltKjerne = maks
    return Triple(maks, min, anbefaltKjerne)
}
