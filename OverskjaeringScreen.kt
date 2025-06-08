package no.steffenhove.betongkalkulator.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.steffenhove.betongkalkulator.ui.components.AppDropdown
import no.steffenhove.betongkalkulator.ui.utils.SharedPrefsUtils
import no.steffenhove.betongkalkulator.ui.utils.convertToMeters
import no.steffenhove.betongkalkulator.ui.viewmodel.OverskjaeringViewModel

@Composable
fun OverskjaeringScreen(viewModel: OverskjaeringViewModel = viewModel()) {
    val context = LocalContext.current
    val result by viewModel.result.collectAsState()
    val infoMessage by viewModel.infoMessage.collectAsState()

    var thicknessInputTfv by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var selectedBlade by rememberSaveable { mutableStateOf("800") }

    val blades = listOf("600", "700", "750", "800", "900", "1000", "1200", "1500", "1600")

    val unitSystem = SharedPrefsUtils.getUnitSystem(context)
    val unitOptions = if (unitSystem == "Imperialsk") listOf("inch", "foot") else listOf("mm", "cm", "m")
    var selectedUnit by rememberSaveable { mutableStateOf("cm") } // Setter "cm" som en stabil default

    // Sørger for at selectedUnit er gyldig hvis unitSystem endres
    LaunchedEffect(unitOptions) {
        if (selectedUnit !in unitOptions) {
            selectedUnit = unitOptions.first()
        }
    }

    fun performCalculationAndUpdateViewModel() {
        val thicknessNormalized = thicknessInputTfv.text.trim().replace(',', '.')
        if (thicknessNormalized.isBlank()) {
            Toast.makeText(context, "Tykkelse kan ikke være tom", Toast.LENGTH_SHORT).show()
            return
        }
        // ... (resten av valideringslogikken er uendret) ...
        val tykkelseMeter = convertToMeters(thicknessNormalized, selectedUnit)
        val tykkelseCm = tykkelseMeter?.let { (it * 100).toInt() }
        if (tykkelseCm == null) {
            Toast.makeText(context, "Ugyldig tykkelse eller enhet", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.calculate(selectedBlade.toInt(), tykkelseCm)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // --- Input-seksjonen er uendret ---
        AppDropdown(label = "Bladdiameter", options = blades, selectedOption = selectedBlade, onOptionSelected = { selectedBlade = it })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = thicknessInputTfv,
            onValueChange = { thicknessInputTfv = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text("Betongtykkelse (f.eks. 25)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppDropdown(label = "Enhet for tykkelse", options = unitOptions, selectedOption = selectedUnit, onOptionSelected = { selectedUnit = it })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { performCalculationAndUpdateViewModel() },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Beregn") }
        Spacer(modifier = Modifier.height(24.dp))

        // --- Start på den nye logikken for visning av resultat ---
        if (infoMessage != null) {
            Text(
                text = infoMessage!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            result?.let { res ->
                val minSkjaeringCm = res.minSkjaeringCm
                val maksSkjaeringCm = res.maksSkjaeringCm // Dette er overkapp-lengden

                // Viser Min/Maks skjæring i samme enhet som brukeren tastet inn
                when (selectedUnit) {
                    "mm" -> {
                        Text("Min. skjæring: ${"%.0f".format(minSkjaeringCm * 10)} mm")
                        Text("Maks. skjæring: ${"%.0f".format(maksSkjaeringCm * 10)} mm")
                    }
                    "cm" -> {
                        Text("Min. skjæring: ${"%.1f".format(minSkjaeringCm)} cm")
                        Text("Maks. skjæring: ${"%.1f".format(maksSkjaeringCm)} cm")
                    }
                    "m" -> {
                        Text("Min. skjæring: ${"%.2f".format(minSkjaeringCm / 100)} m")
                        Text("Maks. skjæring: ${"%.2f".format(maksSkjaeringCm / 100)} m")
                    }
                    "inch" -> {
                        Text("Min. skjæring: ${"%.2f".format(minSkjaeringCm / 2.54f)} inch")
                        Text("Maks. skjæring: ${"%.2f".format(maksSkjaeringCm / 2.54f)} inch")
                    }
                    "foot" -> {
                        Text("Min. skjæring: ${"%.2f".format(minSkjaeringCm / 30.48f)} foot")
                        Text("Maks. skjæring: ${"%.2f".format(maksSkjaeringCm / 30.48f)} foot")
                    }
                }

                // Viser Min. borehull alltid i mm for metrisk, og tommer for imperialsk
                if (unitSystem == "Imperialsk") {
                    val minBorehullInch = res.minBorehullMm / 25.4f
                    Text("Min. borehull: ${"%.1f".format(minBorehullInch)} inch")
                } else {
                    Text("Min. borehull: ${"%.0f".format(res.minBorehullMm)} mm")
                }
            }
        }
        // --- Slutt på den nye logikken ---
    }
}