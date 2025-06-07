package no.steffenhove.betongkalkulator.ui.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.steffenhove.betongkalkulator.ui.model.OverskjaeringData
import java.io.InputStreamReader

fun loadOverskjaeringData(context: Context): List<OverskjaeringData> {
    return try {
        val assetManager = context.assets
        val inputStream = assetManager.open("overskjaering_interpolert.json")
        val reader = InputStreamReader(inputStream)
        val gson = Gson()

        val type = object : TypeToken<List<OverskjaeringData>>() {}.type
        gson.fromJson(reader, type)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
