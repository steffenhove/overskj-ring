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

        val bladDataMap = overskjaeringDataList.find { it.bladeSize == bladDiameter }?.dataPoints
        Log.d("OverskjæringDebug", "Fant data for blad $bladDiameter? ${bladDataMap != null}")

        if (bladDataMap == null) {
            _result.value = null; return
        }

        val lavereTykkelseKey = bladDataMap.keys.filter { it <= betongTykkelseCm }.maxOrNull()
        val hoyereTykkelseKey = bladDataMap.keys.filter { it >= betongTykkelseCm }.minOrNull()

        if (lavereTykkelseKey == null || hoyereTykkelseKey == null) {
            _result.value = null; return
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