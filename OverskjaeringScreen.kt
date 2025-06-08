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
// Nye importer for å håndtere livssyklus-hendelser
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
// ---------------------------------------------
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

    // --- FIKS: Gjør unitSystem til en "State" som kan oppdateres ---
    var unitSystem by remember { mutableStateOf(SharedPrefsUtils.getUnitSystem(context)) }
    
    // Observerer livssyklusen til skjermen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Hver gang skjermen blir aktiv igjen (f.eks. tilbake fra innstillinger)
            if (event == Lifecycle.Event.ON_RESUME) {
                // ... leser vi innstillingen på nytt.
                unitSystem = SharedPrefsUtils.getUnitSystem(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Rydder opp observeren når skjermen lukkes
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // ----------------------------------------------------------------

    val unitOptions = if (unitSystem == "Imperialsk") listOf("inch", "foot") else listOf("mm", "cm", "m")
    var selectedUnit by rememberSaveable { mutableStateOf("cm") }

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
        // UI-delen er uendret, den vil nå automatisk oppdatere seg
        // når 'unitSystem' og 'unitOptions' endres.
        
        AppDropdown(label = "Bladdiameter", options = blades, selectedOption = selectedBlade, onOptionSelected = { selectedBlade = it })
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = thicknessInputTfv, onValueChange = { thicknessInputTfv = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            label = { Text("Betongtykkelse (f.eks. 25)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppDropdown(label = "Enhet for tykkelse", options = unitOptions, selectedOption = selectedUnit, onOptionSelected = { selectedUnit = it })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { performCalculationAndUpdateViewModel() },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Beregn") }
        Spacer(modifier = Modifier.height(24.dp))

        if (infoMessage != null) {
            Text(
                text = infoMessage!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            result?.let { res ->
                if (unitSystem == "Imperialsk") {
                    val minSkjaeringInch = res.minSkjaeringCm / 2.54f
                    val maksSkjaeringInch = res.maksSkjaeringCm / 2.54f
                    val minBorehullInch = res.minBorehullMm / 25.4f
                    Text("Min. skjæring: ${"%.1f".format(minSkjaeringInch)} inch")
                    Text("Maks. skjæring: ${"%.1f".format(maksSkjaeringInch)} inch")
                    Text("Min. borehull: ${"%.1f".format(minBorehullInch)} inch")
                } else {
                    Text("Min. skjæring: ${"%.1f".format(res.minSkjaeringCm)} cm")
                    Text("Maks. skjæring: ${"%.1f".format(res.maksSkjaeringCm)} cm")
                    Text("Min. borehull: ${"%.0f".format(res.minBorehullMm)} mm")
                }
            }
        }
    }
}
