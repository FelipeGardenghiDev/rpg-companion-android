package com.grimorio.rpg.presentation.resources

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grimorio.rpg.core.designsystem.theme.ArcanePurple
import com.grimorio.rpg.core.designsystem.theme.BorderRunic
import com.grimorio.rpg.core.designsystem.theme.CriticalFailure
import com.grimorio.rpg.core.designsystem.theme.CriticalSuccess
import com.grimorio.rpg.core.designsystem.theme.DarkBackground
import com.grimorio.rpg.core.designsystem.theme.DarkSurface
import com.grimorio.rpg.core.designsystem.theme.DarkSurfaceCard
import com.grimorio.rpg.core.designsystem.theme.DarkSurfaceVariant
import com.grimorio.rpg.core.designsystem.theme.GoldPrimary
import com.grimorio.rpg.domain.model.CharacterHealth
import com.grimorio.rpg.domain.model.SharePayloadType
import com.grimorio.rpg.domain.model.ShareablePayload
import com.grimorio.rpg.domain.model.SpellSlot
import com.grimorio.rpg.presentation.resources.components.ConditionCatalogDialog
import com.grimorio.rpg.presentation.resources.components.EditHpDialog
import com.grimorio.rpg.presentation.share.components.ShowQrDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResourcesScreen(
    modifier: Modifier = Modifier,
    viewModel: ResourcesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val character = uiState.character
    var showShareQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vaccines,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = character.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GoldPrimary
                            )
                            Text(
                                text = character.characterClass,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showShareQrDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Compartilhar Ficha via QR Code",
                            tint = GoldPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.openLongRestDialog() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = ArcanePurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Descanso",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. CARD DE VIDA (HP TRACKER)
            item {
                HealthTrackerCard(
                    health = character.health,
                    onDamage = { viewModel.applyDamage(it) },
                    onHeal = { viewModel.applyHeal(it) },
                    onEditClick = { viewModel.openEditHpDialog() }
                )
            }

            // 2. CONDIÇÕES & STATUS ATIVOS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderRunic, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Condições & Status (${character.activeConditions.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = { viewModel.openConditionCatalog() }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar Condição",
                                    tint = GoldPrimary
                                )
                            }
                        }

                        if (character.activeConditions.isEmpty()) {
                            Text(
                                text = "Nenhuma condição ativa no momento. O personagem está são e salvo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                character.activeConditions.forEach { condition ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CriticalFailure.copy(alpha = 0.15f))
                                            .border(1.dp, CriticalFailure.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${condition.type.emoji} ${condition.type.displayName}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = CriticalFailure
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remover",
                                                tint = CriticalFailure,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable { viewModel.removeCondition(condition.type) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. GRADE DE SLOTS DE MAGIA (SPELL SLOTS)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderRunic, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Espaços de Magia (Spell Slots)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toque para gastar • Segure para recuperar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            character.spellSlots.forEach { slot ->
                                SpellSlotRow(
                                    slot = slot,
                                    onUse = { viewModel.useSpellSlot(slot.level) },
                                    onRecover = { viewModel.recoverSpellSlot(slot.level) }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Modais
    if (uiState.isEditHpDialogOpen) {
        EditHpDialog(
            currentMaxHp = character.health.maxHp,
            currentTempHp = character.health.tempHp,
            onDismiss = { viewModel.dismissEditHpDialog() },
            onConfirm = { maxHp, tempHp ->
                viewModel.updateMaxHp(maxHp)
                viewModel.setTempHp(tempHp)
                viewModel.dismissEditHpDialog()
            }
        )
    }

    if (uiState.isConditionCatalogOpen) {
        ConditionCatalogDialog(
            activeConditions = character.activeConditions,
            onToggleCondition = { viewModel.toggleCondition(it) },
            onDismiss = { viewModel.dismissConditionCatalog() }
        )
    }

    if (uiState.isLongRestConfirmDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLongRestDialog() },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Descanso Longo (8 Horas)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GoldPrimary
                )
            },
            text = {
                Text(
                    text = "Um descanso longo restaura todos os seus Pontos de Vida ao máximo e recupera todos os Espaços de Magia gastos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmLongRest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Descansar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLongRestDialog() }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showShareQrDialog) {
        ShowQrDialog(
            payload = ShareablePayload(
                type = SharePayloadType.CHARACTER,
                title = "Ficha: ${character.name}",
                character = character
            ),
            onDismiss = { showShareQrDialog = false }
        )
    }
}

@Composable
private fun HealthTrackerCard(
    health: CharacterHealth,
    onDamage: (Int) -> Unit,
    onHeal: (Int) -> Unit,
    onEditClick: () -> Unit
) {
    val hpPercentage by animateFloatAsState(
        targetValue = health.percentage,
        animationSpec = tween(durationMillis = 300),
        label = "hpPercentage"
    )

    val hpBarColor by animateColorAsState(
        targetValue = when {
            health.percentage > 0.5f -> CriticalSuccess
            health.percentage > 0.25f -> GoldPrimary
            else -> CriticalFailure
        },
        animationSpec = tween(durationMillis = 300),
        label = "hpColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, BorderRunic, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = CriticalFailure,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pontos de Vida",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar HP",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display Numérico de HP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${health.currentHp}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = hpBarColor
                    )
                    Text(
                        text = " / ${health.maxHp} HP",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                if (health.tempHp > 0) {
                    Box(
                        modifier = Modifier
                            .background(ArcanePurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, ArcanePurple, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ArcanePurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${health.tempHp} Temp",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ArcanePurple
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de Progresso
            LinearProgressIndicator(
                progress = { hpPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = hpBarColor,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de Ação Rápida (Dano / Cura)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dano
                OutlinedButton(
                    onClick = { onDamage(10) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("-10", color = CriticalFailure, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = { onDamage(5) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("-5", color = CriticalFailure, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = { onDamage(1) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("-1", color = CriticalFailure, fontWeight = FontWeight.Bold) }

                // Cura
                OutlinedButton(
                    onClick = { onHeal(1) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("+1", color = CriticalSuccess, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = { onHeal(5) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("+5", color = CriticalSuccess, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = { onHeal(10) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("+10", color = CriticalSuccess, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpellSlotRow(
    slot: SpellSlot,
    onUse: () -> Unit,
    onRecover: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${slot.level}º Círculo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${slot.availableSlots} de ${slot.totalSlots} disponíveis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until slot.totalSlots) {
                val isAvailable = i < slot.availableSlots

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (isAvailable) ArcanePurple else BorderRunic,
                            shape = CircleShape
                        )
                        .background(if (isAvailable) ArcanePurple else Color.Transparent)
                        .combinedClickable(
                            onClick = onUse,
                            onLongClick = onRecover
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isAvailable) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BorderRunic)
                        )
                    }
                }
            }
        }
    }
}
