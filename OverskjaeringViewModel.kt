package no.steffenhove.betongkalkulator.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringResult
import no.steffenhove.betongkalkulator.ui.model.ThicknessValues
import no.steffenhove.betongkalkulator.ui.utils.loadOverskjaeringData

// ... (importer er viktige, spesielt for de nye dataklassene)
// ...

class OverskjaeringViewModel(application: Application) : AndroidViewModel(application) {

    private val overskjaeringDataList: List<OverskjaeringData> = loadOverskjaeringData(application)

    private val _result = MutableStateFlow<OverskjaeringResult?>(null)
    val result: StateFlow<OverskjaeringResult?> = _result

    fun calculate(bladDiameter: Int, betongTykkelseCm: Int) {
        // Finner riktig sett med data for valgt bladdiameter
        val bladDataMap = overskjaeringDataList.find { it.bladeSize == bladDiameter }?.data

        if (bladDataMap == null) {
            _result.value = null // Dette skjer når bladdata ikke finnes
            return
        }

        // Finner nærmeste lavere og høyere betongtykkelse i tabellen
        val lavereTykkelseKey = bladDataMap.keys.filter { it <= betongTykkelseCm }.maxOrNull()
        val hoyereTykkelseKey = bladDataMap.keys.filter { it >= betongTykkelseCm }.minOrNull()

        if (lavereTykkelseKey == null || hoyereTykkelseKey == null) {
            _result.value = null // Ugyldig tykkelse ift. tabellen
            return
        }

        // Henter ThicknessValues-objekter for interpolasjon
        val values1 = bladDataMap[lavereTykkelseKey]!!
        val values2 = bladDataMap[hoyereTykkelseKey]!!

        // Her må DU bestemme hvilke verdier fra JSON som skal brukes.
        // La oss anta at "overcutCm" er det du bruker for "Maks. skjæring" (og borehull)
        // og "minCutCm" er det du bruker for "Min. skjæring".
        val overkapp1 = values1.overcutCm // "A-verdi"
        val minSkjaering1 = values1.minCutCm // "B-verdi"

        val overkapp2 = values2.overcutCm // "A-verdi"
        val minSkjaering2 = values2.minCutCm // "B-verdi"

        val interpolertOverkappCm: Float
        val interpolertMinSkjaeringCm: Float

        if (lavereTykkelseKey == hoyereTykkelseKey) {
            interpolertOverkappCm = overkapp1
            interpolertMinSkjaeringCm = minSkjaering1
        } else {
            val t = (betongTykkelseCm - lavereTykkelseKey).toFloat() / (hoyereTykkelseKey - lavereTykkelseKey).toFloat()
            interpolertOverkappCm = overkapp1 + t * (overkapp2 - overkapp1)
            interpolertMinSkjaeringCm = minSkjaering1 + t * (minSkjaering2 - minSkjaering1)
        }

        // Beregn minste borehull basert på overkapp-lengden.
        // Hvis overkapp kan være negativt, må du håndtere det her.
        val minBorehullMm = if (interpolertOverkappCm > 0) interpolertOverkappCm * 10f else 0f

        _result.value = OverskjaeringResult(
            minSkjaeringCm = interpolertMinSkjaeringCm,
            maksSkjaeringCm = interpolertOverkappCm,
            minBorehullMm = minBorehullMm
        )
    }
}