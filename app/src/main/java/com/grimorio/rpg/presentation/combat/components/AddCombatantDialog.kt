package com.grimorio.rpg.presentation.combat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grimorio.rpg.core.designsystem.theme.BorderRunic
import com.grimorio.rpg.core.designsystem.theme.CriticalFailure
import com.grimorio.rpg.core.designsystem.theme.CriticalSuccess
import com.grimorio.rpg.core.designsystem.theme.DarkBackground
import com.grimorio.rpg.core.designsystem.theme.DarkSurface
import com.grimorio.rpg.core.designsystem.theme.GoldPrimary

@Composable
fun AddCombatantDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, initiative: Int, modifier: Int, maxHp: Int, isPlayer: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var initiativeText by remember { mutableStateOf("10") }
    var modifierText by remember { mutableStateOf("0") }
    var hpText by remember { mutableStateOf("15") }
    var isPlayer by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Adicionar Combatente",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GoldPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Nome do Combatente") },
                    placeholder = { Text("ex: Orc Chefe, Paladino") },
                    isError = nameError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderRunic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = initiativeText,
                        onValueChange = { initiativeText = it },
                        label = { Text("Iniciativa") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderRunic
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = modifierText,
                        onValueChange = { modifierText = it },
                        label = { Text("Bônus Dex") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderRunic
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = hpText,
                    onValueChange = { hpText = it },
                    label = { Text("Pontos de Vida (HP)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderRunic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Tipo de Participante:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isPlayer,
                        onClick = { isPlayer = true },
                        label = { Text("🛡️ Jogador / Aliado") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CriticalSuccess.copy(alpha = 0.25f),
                            selectedLabelColor = CriticalSuccess
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isPlayer,
                            borderColor = BorderRunic,
                            selectedBorderColor = CriticalSuccess
                        )
                    )

                    FilterChip(
                        selected = !isPlayer,
                        onClick = { isPlayer = false },
                        label = { Text("👹 Inimigo / Monstro") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CriticalFailure.copy(alpha = 0.25f),
                            selectedLabelColor = CriticalFailure
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = !isPlayer,
                            borderColor = BorderRunic,
                            selectedBorderColor = CriticalFailure
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        nameError = true
                        return@Button
                    }
                    val init = initiativeText.toIntOrNull() ?: 10
                    val mod = modifierText.toIntOrNull() ?: 0
                    val hp = hpText.toIntOrNull() ?: 15
                    onConfirm(trimmedName, init, mod, hp, isPlayer)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
