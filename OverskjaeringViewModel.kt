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

    private val _result = MutableStateFlow<OverskjaeringResult?>(null)
    val result: StateFlow<OverskjaeringResult?> = _result

    fun calculate(bladDiameter: Int, betongTykkelseCm: Int) {
        Log.d("OverskjæringDebug", "ViewModel.calculate kalt med Blad: $bladDiameter, Tykkelse: $betongTykkelseCm")
        Log.d("OverskjæringDebug", "Totalt antall blad-datatyper lastet: ${overskjaeringDataList.size}")

        val bladDataMap = overskjaeringDataList.find { it.bladeSize == bladDiameter }?.data
        Log.d("OverskjæringDebug", "Fant data for blad $bladDiameter? ${bladDataMap != null}")

        if (bladDataMap == null) {
            _result.value = null
            return
        }

        val lavereTykkelseKey = bladDataMap.keys.filter { it <= betongTykkelseCm }.maxOrNull()
        val hoyereTykkelseKey = bladDataMap.keys.filter { it >= betongTykkelseCm }.minOrNull()

        if (lavereTykkelseKey == null || hoyereTykkelseKey == null) {
            _result.value = null
            return
        }

        val values1 = bladDataMap[lavereTykkelseKey]!!
        val values2 = bladDataMap[hoyereTykkelseKey]!!

        // Her antar vi at 'overcutCm' er "Maks. skjæring" (A-verdi for borehull)
        // og 'minCutCm' er "Min. skjæring" (B-verdi / potensiell dybde)
        val overkapp1 = values1.overcutCm
        val minSkjaering1 = values1.minCutCm

        val overkapp2 = values2.overcutCm
        val minSkjaering2 = values2.minCutCm

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

        val minBorehullMm = if (interpolertOverkappCm > 0) interpolertOverkappCm * 10f else 0f

        _result.value = OverskjaeringResult(
            minSkjaeringCm = interpolertMinSkjaeringCm,
            maksSkjaeringCm = interpolertOverkappCm,
            minBorehullMm = minBorehullMm
        )
    }
}