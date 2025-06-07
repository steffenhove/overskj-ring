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

class OverskjaeringViewModel(application: Application) : AndroidViewModel(application) {

    private val overskjaeringData: List<OverskjaeringData> = loadOverskjaeringData(application)

    private val _result = MutableStateFlow<OverskjaeringResult?>(null)
    val result: StateFlow<OverskjaeringResult?> = _result

    fun calculate(bladdiameter: Int, tykkelseCm: Int) {
        Log.d("Overskjæring", "Input: blad=$bladdiameter, tykkelse=$tykkelseCm")

        val bladeEntry = overskjaeringData.find { it.bladeSize == bladdiameter }
        if (bladeEntry == null) {
            Log.e("Overskjæring", "Fant ikke blad $bladdiameter")
            _result.value = null
            return
        }

        val dataMap = bladeEntry.data
        if (dataMap == null) {
            Log.e("Overskjæring", "Data for blad $bladdiameter er NULL")
            _result.value = null
            return
        }

        if (dataMap.isEmpty()) {
            Log.e("Overskjæring", "DataMap for blad $bladdiameter er tomt")
            _result.value = null
            return
        }

        Log.d("Overskjæring", "Tilgjengelige tykkelser: ${dataMap.keys.sorted()}")

        val exact = dataMap[tykkelseCm]
        if (exact != null) {
            Log.d("Overskjæring", "Eksakt match for tykkelse $tykkelseCm")
            _result.value = OverskjaeringResult(
                minSkjaeringCm = exact.minCutCm,
                maksSkjaeringCm = exact.maxCutCm,
                minBorehullMm = exact.overcutCm * 10
            )
            return
        }

        val sorted = dataMap.entries.sortedBy { it.key }
        val lavere = sorted.lastOrNull { it.key < tykkelseCm }
        val hoyere = sorted.firstOrNull { it.key > tykkelseCm }

        if (lavere == null || hoyere == null) {
            Log.w("Overskjæring", "Kan ikke interpolere tykkelse $tykkelseCm")
            _result.value = null
            return
        }

        val interpolert = interpolateValues(
            tykkelseCm,
            lavere.key, lavere.value,
            hoyere.key, hoyere.value
        )

        Log.d("Overskjæring", "Interpolert verdi generert")

        _result.value = OverskjaeringResult(
            minSkjaeringCm = interpolert.minCutCm,
            maksSkjaeringCm = interpolert.maxCutCm,
            minBorehullMm = interpolert.overcutCm * 10
        )
    }

    private fun interpolateValues(
        target: Int,
        lowKey: Int, lowVal: ThicknessValues,
        highKey: Int, highVal: ThicknessValues
    ): ThicknessValues {
        val fraction = (target - lowKey).toFloat() / (highKey - lowKey)
        return ThicknessValues(
            minCutCm = lowVal.minCutCm + fraction * (highVal.minCutCm - lowVal.minCutCm),
            maxCutCm = lowVal.maxCutCm + fraction * (highVal.maxCutCm - lowVal.maxCutCm),
            overcutCm = lowVal.overcutCm + fraction * (highVal.overcutCm - lowVal.overcutCm)
        )
    }
}