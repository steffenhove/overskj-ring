package no.steffenhove.betongkalkulator.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // Import for rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue // Import for TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.steffenhove.betongkalkulator.ui.components.AppDropdown
import no.steffenhove.betongkalkulator.ui.utils.SharedPrefsUtils
import no.steffenhove.betongkalkulator.ui.utils.convertToMeters
import no.steffenhove.betongkalkulator.ui.viewmodel.OverskjaeringViewModel

@Composable
fun OverskjaeringScreen(viewModel: OverskjaeringViewModel = viewModel()) {
    val context = LocalContext.current
    val result by viewModel.result.collectAsState() // Denne overlever via ViewModel

    // Bruker rememberSaveable for UI-tilstand
    var thicknessInputTfv by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var selectedBlade by rememberSaveable { mutableStateOf("800") }

    val blades = listOf("600", "700", "750", "800", "900", "1000", "1200", "1500", "1600")

    val unitSystem = SharedPrefsUtils.getUnitSystem(context)
    val unitOptions = if (unitSystem == "Imperialsk") listOf("inch", "foot") else listOf("mm", "cm", "m")
    // For selectedUnit er det viktig hvordan unitOptions initialiseres etter rotasjon.
    // Hvis unitOptions alltid er den samme listen basert på en stabil SharedPrefsUtils.getUnitSystem,
    // vil dette fungere.
    var selectedUnit by rememberSaveable { mutableStateOf(unitOptions.firstOrNull() ?: "cm") }


    fun performCalculationAndUpdateViewModel() {
        val tykkelseTrimmed = thicknessInputTfv.text.trim() // Bruker .text fra TextFieldValue
        if (tykkelseTrimmed.isBlank()) {
            Toast.makeText(context, "Tykkelse kan ikke være tom", Toast.LENGTH_SHORT).show()
            return
        }

        val tykkelseDouble = tykkelseTrimmed.toDoubleOrNull()
        if (tykkelseDouble == null) {
            Toast.makeText(context, "Ugyldig tall for tykkelse", Toast.LENGTH_SHORT).show()
            return
        }

        val tykkelseMeter = convertToMeters(tykkelseTrimmed, selectedUnit)
        val tykkelseCm = tykkelseMeter?.let { (it * 100).toInt() }

        if (tykkelseCm == null) {
            Toast.makeText(context, "Ugyldig tykkelse eller enhet", Toast.LENGTH_SHORT).show()
            return
        }

        val bladDiameterInt = selectedBlade.toIntOrNull()
        if (bladDiameterInt == null) {
            Toast.makeText(context, "Ugyldig bladdiameter", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.calculate(bladDiameterInt, tykkelseCm) // Antar 'calculate' er navnet i ViewModel
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AppDropdown( // Antar at AppDropdown tar en obligatorisk 'label'
            label = "Bladdiameter",
            options = blades,
            selectedOption = selectedBlade,
            onOptionSelected = { selectedBlade = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = thicknessInputTfv, // Bruker TextFieldValue her
            onValueChange = { thicknessInputTfv = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text("Betongtykkelse (f.eks. 25)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppDropdown( // Antar at AppDropdown tar en obligatorisk 'label'
            label = "Enhet for tykkelse",
            options = unitOptions,
            selectedOption = selectedUnit,
            onOptionSelected = { selectedUnit = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { performCalculationAndUpdateViewModel() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Beregn")
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let { res ->
            // Antar at res (OverskjaeringResult) har feltene minSkjaeringCm, maksSkjaeringCm, minBorehullMm
            Text("Min. skjæring: ${"%.1f".format(res.minSkjaeringCm)} cm")
            Text("Maks. skjæring: ${"%.1f".format(res.maksSkjaeringCm)} cm")
            Text("Min. borehull: ${"%.0f".format(res.minBorehullMm)} mm")
        }
    }
}