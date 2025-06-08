package no.steffenhove.betongkalkulator.ui.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringEntry
import java.io.InputStreamReader

fun loadOverskjaeringData(context: Context): List<OverskjaeringData> {
    val TAG = "OverskjæringLoaderDebug"
    Log.d(TAG, "Starter loadOverskjaeringData...")

    return try {
        val jsonText = context.assets.open("overskjaering_data.json") // Bruker det nye filnavnet
            .bufferedReader()
            .use { it.readText() }
        Log.d(TAG, "Fil lest inn OK.")

        val gson = Gson()
        // Forteller Gson at den skal lese en liste av OverskjaeringEntry-objekter
        val entryListType = object : TypeToken<List<OverskjaeringEntry>>() {}.type
        val entries: List<OverskjaeringEntry> = gson.fromJson(jsonText, entryListType)
        Log.d(TAG, "Gson parsing ferdig. Fant ${entries.size} rader med data.")

        // Grupperer alle radene etter bladdiameter
        val groupedByBladeSize = entries.groupBy { it.bladeSize }

        // Konverterer den grupperte map'en til den endelige listen
        val overskjaeringList = groupedByBladeSize.map { (bladeSize, entryList) ->
            val dataMap = entryList.associate { entry ->
                entry.thicknessCm to Pair(entry.maxOvercutCm, entry.minCuttingDepthCm)
            }
            OverskjaeringData(bladeSize, dataMap)
        }

        Log.d(TAG, "Konvertering til gruppert liste ferdig. Listestørrelse: ${overskjaeringList.size}")
        return overskjaeringList

    } catch (e: Exception) {
        Log.e(TAG, "En FEIL oppstod under lasting av JSON-data:", e)
        return emptyList()
    }
}