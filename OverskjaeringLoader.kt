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

        // ENDRING HER: Vi leser JSON-teksten først og bruker en annen fromJson-metode
        val jsonText = reader.readText()

        // Definerer typen på samme måte
        val typeToken = object : TypeToken<Map<String, Map<String, ThicknessValues>>>() {}.type

        // Bruker fromJson som tar en String som input
        val rawData: Map<String, Map<String, ThicknessValues>> = gson.fromJson(jsonText, typeToken)
        Log.d(TAG, "Gson parsing ferdig. Fant data for ${rawData.size} bladdiametere.")

        // Konverterer den innleste Map'en til en Liste
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