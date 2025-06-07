// Fil: AppDropdown.kt (DENNE FILEN FORBLIR UENDRET HVIS DU FØLGER DENNE LØSNINGEN)
package no.steffenhove.betongkalkulator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp // Denne var i din originale, kan være nødvendig for padding etc. internt.
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class) // Hvis du bruker TextFieldDefaults.colors
@Composable
fun AppDropdown(
    label: String, // Obligatorisk
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
    // modifier: Modifier = Modifier // Den gamle hadde ikke denne, men det er lurt å ha med
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) { // Du kan legge til mottatt modifier her hvis du legger den til i signaturen
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = { /* Gjør ingenting, da verdien settes av dropdown */ },
                label = { Text(label) }, // Label vises alltid
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown Icon",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                // Farger fra din opprinnelige kode
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant, // Ikke i bruk
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = true }),
                enabled = false, // Din opprinnelige måte for å hindre fokus
                singleLine = true
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
                properties = PopupProperties(focusable = true)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}