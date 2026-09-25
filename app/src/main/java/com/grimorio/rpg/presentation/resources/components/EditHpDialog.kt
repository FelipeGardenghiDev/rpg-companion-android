package com.grimorio.rpg.presentation.resources.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.grimorio.rpg.core.designsystem.theme.DarkBackground
import com.grimorio.rpg.core.designsystem.theme.DarkSurface
import com.grimorio.rpg.core.designsystem.theme.GoldPrimary

@Composable
fun EditHpDialog(
    currentMaxHp: Int,
    currentTempHp: Int,
    onDismiss: () -> Unit,
    onConfirm: (newMaxHp: Int, newTempHp: Int) -> Unit
) {
    var maxHpText by remember { mutableStateOf(currentMaxHp.toString()) }
    var tempHpText by remember { mutableStateOf(currentTempHp.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Ajustar Pontos de Vida (HP)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GoldPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = maxHpText,
                    onValueChange = { maxHpText = it },
                    label = { Text("HP Máximo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderRunic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempHpText,
                    onValueChange = { tempHpText = it },
                    label = { Text("HP Temporário (Escudo)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderRunic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val max = maxHpText.toIntOrNull() ?: currentMaxHp
                    val temp = tempHpText.toIntOrNull() ?: currentTempHp
                    onConfirm(max, temp)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
