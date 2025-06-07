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
        val jsonText = context.assets.open("overskjaering_interpolert.json").bufferedReader().use { it.readText() }
        Log.d(TAG, "Fil lest inn OK.")

        val gson = Gson()

        // 1. Les JSON inn som den faktiske strukturen: En Map hvor nøkkel er String (bladdiameter)
        val typeToken = object : TypeToken<Map<String, Map<String, ThicknessValues>>>() {}.type
        val rawData: Map<String, Map<String, ThicknessValues>> = gson.fromJson(jsonText, typeToken)
        Log.d(TAG, "Gson parsing til Map ferdig. Fant data for ${rawData.size} bladdiametere.")

        // 2. Bygg den endelige listen ved å konvertere Map'en
        val overskjaeringList = rawData.map { (bladeSizeStr, thicknessMap) ->
            OverskjaeringData(
                bladeSize = bladeSizeStr.toInt(),
                data = thicknessMap.mapKeys { (thicknessStr, _) ->
                    thicknessStr.toInt()
                }
            )
        }

        Log.d(TAG, "Konvertering til Liste ferdig. Endelig listestørrelse: ${overskjaeringList.size}")
        return overskjaeringList

    } catch (e: Exception) {
        Log.e(TAG, "En FEIL oppstod under lasting av JSON-data:", e)
        return emptyList()
    }
}