package com.grimorio.rpg.presentation.resources.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grimorio.rpg.core.designsystem.theme.BorderRunic
import com.grimorio.rpg.core.designsystem.theme.CriticalSuccess
import com.grimorio.rpg.core.designsystem.theme.DarkBackground
import com.grimorio.rpg.core.designsystem.theme.DarkSurface
import com.grimorio.rpg.core.designsystem.theme.DarkSurfaceCard
import com.grimorio.rpg.core.designsystem.theme.GoldPrimary
import com.grimorio.rpg.domain.model.CharacterCondition
import com.grimorio.rpg.domain.model.ConditionType

@Composable
fun ConditionCatalogDialog(
    activeConditions: List<CharacterCondition>,
    onToggleCondition: (ConditionType) -> Unit,
    onDismiss: () -> Unit
) {
    val activeTypes = activeConditions.map { it.type }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Catálogo de Condições & Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GoldPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ConditionType.entries) { condition ->
                    val isActive = activeTypes.contains(condition)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) CriticalSuccess else BorderRunic,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (isActive) CriticalSuccess.copy(alpha = 0.12f) else DarkSurfaceCard)
                            .clickable { onToggleCondition(condition) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = condition.emoji,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = condition.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isActive) CriticalSuccess else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = condition.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(CriticalSuccess, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = DarkBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Concluído")
            }
        }
    )
}
