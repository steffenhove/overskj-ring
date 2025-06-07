package no.steffenhove.betongkalkulator.ui.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.ThicknessValues
import java.io.InputStreamReader

fun loadOverskjaeringData(context: Context): List<OverskjaeringData> {
    val TAG = "OverskjæringLoaderDebug"
    Log.d(TAG, "Starter loadOverskjaeringData...")

    return try {
        val assetManager = context.assets
        val inputStream = assetManager.open("overskjaering_interpolert.json")
        Log.d(TAG, "Fil funnet.")

        val reader = InputStreamReader(inputStream)
        val gson = Gson()

        // Leser JSON som en Map, som matcher filstrukturen
        val rawDataType = object : TypeToken<Map<String, Map<String, ThicknessValues>>>() {}.type
        val rawData: Map<String, Map<String, ThicknessValues>> = gson.fromJson(reader, rawDataType)
        Log.d(TAG, "Gson parsing ferdig. Fant data for ${rawData.size} bladdiametere.")

        // Konverterer den innleste Map'en til en Liste
        val overskjaeringList = mutableListOf<OverskjaeringData>()

        rawData.forEach { (bladeSizeStr, thicknessDataMapStr) ->
            val bladeSizeInt = bladeSizeStr.toIntOrNull()
            if (bladeSizeInt != null) {
                val finalThicknessDataMap = mutableMapOf<Int, ThicknessValues>()
                thicknessDataMapStr.forEach { (thicknessStr, thicknessValuesObj) ->
                    val thicknessInt = thicknessStr.toIntOrNull()
                    if (thicknessInt != null) {
                        finalThicknessDataMap[thicknessInt] = thicknessValuesObj
                    }
                }
                if (finalThicknessDataMap.isNotEmpty()) {
                    overskjaeringList.add(OverskjaeringData(bladeSizeInt, finalThicknessDataMap))
                }
            }
        }
        Log.d(TAG, "Konvertering ferdig. Endelig listestørrelse: ${overskjaeringList.size}")
        overskjaeringList

    } catch (e: Exception) {
        Log.e(TAG, "En FEIL oppstod under lasting av JSON-data:", e)
        return emptyList()
    }
}