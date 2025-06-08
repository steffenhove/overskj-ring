package no.steffenhove.betongkalkulator.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringResult
import no.steffenhove.betongkalkulator.ui.utils.loadOverskjaeringData

class OverskjaeringViewModel(application: Application) : AndroidViewModel(application) {

    private val overskjaeringDataList: List<OverskjaeringData> = loadOverskjaeringData(application)
        .sortedBy { it.bladeSize } // Sorterer listen for å enkelt finne neste større blad

    private val _result = MutableStateFlow<OverskjaeringResult?>(null)
    val result: StateFlow<OverskjaeringResult?> = _result

    // Ny StateFlow for feilmeldinger/info-meldinger
    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage

    fun calculate(bladDiameter: Int, betongTykkelseCm: Int) {
        // Nullstill tidligere meldinger og resultater
        _infoMessage.value = null
        _result.value = null

        val bladDataMap = overskjaeringDataList.find { it.bladeSize == bladDiameter }?.dataPoints

        if (bladDataMap == null) {
            _infoMessage.value = "Fant ikke data for Ø$bladDiameter mm blad."
            return
        }

        // --- NY LOGIKK: Sjekk om bladet er stort nok ---
        val maksTykkelseForBlad = bladDataMap.keys.maxOrNull() ?: 0
        if (betongTykkelseCm > maksTykkelseForBlad) {
            // Finn det minste bladet som er større enn det valgte, og som kan håndtere tykkelsen
            val anbefaltBlad = overskjaeringDataList.find {
                it.bladeSize > bladDiameter && (it.dataPoints.keys.maxOrNull() ?: 0) >= betongTykkelseCm
            }

            val anbefaltBladTekst = anbefaltBlad?.let { "Ø${it.bladeSize} mm eller større." } ?: "et større blad."

            _infoMessage.value = "Ø$bladDiameter mm er for lite for $betongTykkelseCm cm betong.\nAnbefalt blad: $anbefaltBladTekst"
            return
        }
        // ---------------------------------------------

        val lavereTykkelseKey = bladDataMap.keys.filter { it <= betongTykkelseCm }.maxOrNull()
        val hoyereTykkelseKey = bladDataMap.keys.filter { it >= betongTykkelseCm }.minOrNull()

        if (lavereTykkelseKey == null || hoyereTykkelseKey == null) {
            _infoMessage.value = "Ugyldig betongtykkelse for det valgte bladet."
            return
        }

        val (overkapp1_cm, minSkjaering1_cm) = bladDataMap[lavereTykkelseKey]!!
        val (overkapp2_cm, minSkjaering2_cm) = bladDataMap[hoyereTykkelseKey]!!

        val interpolertOverkappCm: Float
        val interpolertMinSkjaeringCm: Float

        if (lavereTykkelseKey == hoyereTykkelseKey) {
            interpolertOverkappCm = overkapp1_cm
            interpolertMinSkjaeringCm = minSkjaering1_cm
        } else {
            val t = (betongTykkelseCm - lavereTykkelseKey).toFloat() / (hoyereTykkelseKey - lavereTykkelseKey).toFloat()
            interpolertOverkappCm = overkapp1_cm + t * (overkapp2_cm - overkapp1_cm)
            interpolertMinSkjaeringCm = minSkjaering1_cm + t * (minSkjaering2_cm - minSkjaering1_cm)
        }

        val minBorehullMm = if (interpolertOverkappCm > 0) interpolertOverkappCm * 10f else 0f

        _result.value = OverskjaeringResult(
            minSkjaeringCm = interpolertMinSkjaeringCm,
            maksSkjaeringCm = interpolertOverkappCm,
            minBorehullMm = minBorehullMm
        )
    }
}