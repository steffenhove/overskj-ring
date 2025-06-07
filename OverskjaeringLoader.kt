package no.steffenhove.betongkalkulator.ui.utils // Sjekk at pakkenavnet er riktig

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import no.steffenhove.betongkalkulator.ui.model.ThicknessValues
import java.io.InputStreamReader

fun loadOverskjaeringData(context: Context): List<OverskjaeringData> {
    return try {
        val assetManager = context.assets
        // Sørg for at filen ligger i app/src/main/assets/
        val inputStream = assetManager.open("overskjaering_interpolert.json")
        val reader = InputStreamReader(inputStream)
        val gson = Gson()

        // 1. Parse JSON til den faktiske strukturen: En Map hvor nøkkelen er bladdiameter (String)
        // og verdien er en ny Map hvor nøkkelen er tykkelse (String) til et ThicknessValues-objekt.
        val rawDataType = object : TypeToken<Map<String, Map<String, ThicknessValues>>>() {}.type
        val rawData: Map<String, Map<String, ThicknessValues>> = gson.fromJson(reader, rawDataType)

        // 2. Konverter den rå data-mappen til den ønskede List<OverskjaeringData>
        val overskjaeringList = mutableListOf<OverskjaeringData>()

        rawData.forEach { (bladeSizeStr, thicknessDataMapStr) ->
            // Konverter bladdiameter-strengen til Int
            val bladeSizeInt = bladeSizeStr.toIntOrNull()
            if (bladeSizeInt != null) {

                // Konverter den indre map'en fra <String, ...> til <Int, ...>
                val finalThicknessDataMap = mutableMapOf<Int, ThicknessValues>()
                thicknessDataMapStr.forEach { (thicknessStr, thicknessValuesObj) ->
                    val thicknessInt = thicknessStr.toIntOrNull()
                    if (thicknessInt != null) {
                        finalThicknessDataMap[thicknessInt] = thicknessValuesObj
                    }
                }

                // Legg til det konverterte objektet i listen
                if (finalThicknessDataMap.isNotEmpty()) {
                    overskjaeringList.add(OverskjaeringData(bladeSizeInt, finalThicknessDataMap))
                }
            }
        }
        return overskjaeringList

    } catch (e: Exception) {
        // Denne vil fange opp feil i JSON-formatet eller filnavnet. Sjekk Logcat!
        e.printStackTrace()
        return emptyList()
    }
}