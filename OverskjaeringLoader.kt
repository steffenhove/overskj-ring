package no.steffenhove.betongkalkulator.ui.utils

import android.content.Context
import android.util.Log
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.ThicknessValues
import org.json.JSONObject // Importer Androids innebygde JSON-parser
import java.io.InputStreamReader

fun loadOverskjaeringData(context: Context): List<OverskjaeringData> {
    val TAG = "OverskjæringLoaderDebug"
    Log.d(TAG, "Starter loadOverskjaeringData (JSONObject-versjon)...")

    return try {
        // Leser hele filen inn i en streng
        val jsonText = context.assets.open("overskjaering_interpolert.json").bufferedReader().use { it.readText() }
        Log.d(TAG, "Fil lest inn OK.")

        // Parser hele strengen som ett stort JSON-objekt
        val rootObject = JSONObject(jsonText)
        Log.d(TAG, "Parsing til JSONObject ferdig. Fant ${rootObject.length()} bladdiametere.")

        val overskjaeringList = mutableListOf<OverskjaeringData>()

        // Går gjennom hver nøkkel i rot-objektet (f.eks. "600", "700", etc.)
        for (bladeSizeStr in rootObject.keys()) {
            val bladeSizeInt = bladeSizeStr.toIntOrNull()
            if (bladeSizeInt != null) {

                val thicknessDataMap = mutableMapOf<Int, ThicknessValues>()
                val thicknessObject = rootObject.getJSONObject(bladeSizeStr)

                // Går gjennom hver nøkkel i det indre objektet (f.eks. "1", "2", etc.)
                for (thicknessStr in thicknessObject.keys()) {
                    val thicknessInt = thicknessStr.toIntOrNull()
                    if (thicknessInt != null) {

                        val valuesObject = thicknessObject.getJSONObject(thicknessStr)
                        // Henter ut verdiene for minCutCm, maxCutCm, og overcutCm
                        val minCut = valuesObject.getDouble("minCutCm").toFloat()
                        val maxCut = valuesObject.getDouble("maxCutCm").toFloat()
                        val overcut = valuesObject.getDouble("overcutCm").toFloat()

                        thicknessDataMap[thicknessInt] = ThicknessValues(minCut, maxCut, overcut)
                    }
                }

                if (thicknessDataMap.isNotEmpty()) {
                    overskjaeringList.add(OverskjaeringData(bladeSizeInt, thicknessDataMap))
                }
            }
        }

        Log.d(TAG, "Manuell konvertering ferdig. Endelig listestørrelse: ${overskjaeringList.size}")
        return overskjaeringList

    } catch (e: Exception) {
        Log.e(TAG, "En FEIL oppstod under lasting av JSON-data (JSONObject-versjon):", e)
        return emptyList()
    }
}