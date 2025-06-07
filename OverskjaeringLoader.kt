package no.steffenhove.betongkalkulator.ui.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringEntry
import no.steffenhove.betongkalkulator.ui.model.ThicknessValues
import java.io.InputStreamReader

fun loadOverskjaeringData(context: Context): List<OverskjaeringData> {
    val TAG = "OverskjæringLoaderDebug"
    Log.d(TAG, "Starter loadOverskjaeringData (for JSON Array)...")

    return try {
        val jsonText = context.assets.open("overskjaering_interpolert.json").bufferedReader().use { it.readText() }
        Log.d(TAG, "Fil lest inn OK.")

        val gson = Gson()

        // 1. Les JSON inn som en liste av OverskjaeringEntry-objekter
        val entryListType = object : TypeToken<List<OverskjaeringEntry>>() {}.type
        val entries: List<OverskjaeringEntry> = gson.fromJson(jsonText, entryListType)
        Log.d(TAG, "Gson parsing ferdig. Fant ${entries.size} rader med data.")

        // 2. Grupper listen etter bladdiameter og konverter til den strukturen ViewModelen forventer
        val groupedByBladeSize = entries.groupBy { it.bladeSize }

        val overskjaeringList = groupedByBladeSize.map { (bladeSize, entryList) ->
            val dataMap = entryList.associate { entry ->
                // Her må du velge hvilke verdier fra JSON som skal brukes.
                // Anta at `overcutCm` er A-verdi (overkapp) og `minSkjaeringCm` er B-verdi (dybde)
                val thicknessValues = ThicknessValues(
                    minCutCm = entry.minSkjaeringCm,
                    maxCutCm = entry.maksSkjaeringCm,
                    overcutCm = entry.overcutCm ?: 0f // Bruker 0f hvis overcutCm er null
                )
                entry.betongtykkelse_cm to thicknessValues
            }
            OverskjaeringData(bladeSize, dataMap)
        }

        Log.d(TAG, "Konvertering til gruppert liste ferdig. Endelig listestørrelse: ${overskjaeringList.size}")
        return overskjaeringList

    } catch (e: Exception) {
        Log.e(TAG, "En FEIL oppstod under lasting av JSON-data (Array-versjon):", e)
        return emptyList()
    }
}