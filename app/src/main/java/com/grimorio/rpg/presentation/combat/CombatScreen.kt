package com.grimorio.rpg.presentation.combat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import com.grimorio.rpg.domain.model.CombatState
import com.grimorio.rpg.domain.model.Combatant
import com.grimorio.rpg.domain.model.SharePayloadType
import com.grimorio.rpg.domain.model.ShareablePayload
import com.grimorio.rpg.presentation.combat.components.AddCombatantDialog
import com.grimorio.rpg.presentation.share.components.ShowQrDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatScreen(
    modifier: Modifier = Modifier,
    viewModel: CombatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val combatState = uiState.combatState
    var showShareQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsKabaddi,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "COMBAT TRACKER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GoldPrimary
                            )
                            Text(
                                text = "Gestão de Iniciativa & Turnos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showShareQrDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Exportar Encontro via QR Code",
                            tint = GoldPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.rollAllInitiatives() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Rolar Iniciativas",
                            tint = GoldPrimary
                        )
                    }

                    IconButton(onClick = { viewModel.openAddCombatantDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar Combatente",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. HERO CARD: RODADA & CONTROLE DE TURNOS
            item {
                CombatRoundHeroCard(
                    state = combatState,
                    onNextTurn = { viewModel.nextTurn() },
                    onPrevTurn = { viewModel.previousTurn() },
                    onReset = { viewModel.resetCombat() }
                )
            }

            // 2. CABEÇALHO DA FILA DE INICIATIVA
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ordem de Iniciativa (${combatState.combatants.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedButton(
                        onClick = { viewModel.openAddCombatantDialog() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Novo",
                            color = GoldPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // 3. CARDS DE COMBATENTES
            itemsIndexed(combatState.combatants, key = { _, c -> c.id }) { index, combatant ->
                val isCurrentTurn = index == combatState.currentTurnIndex

                CombatantCard(
                    combatant = combatant,
                    isCurrentTurn = isCurrentTurn,
                    onDamage = { viewModel.applyDamage(combatant.id, it) },
                    onHeal = { viewModel.applyHeal(combatant.id, it) },
                    onDelete = { viewModel.removeCombatant(combatant.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (uiState.isAddCombatantDialogOpen) {
        AddCombatantDialog(
            onDismiss = { viewModel.dismissAddCombatantDialog() },
            onConfirm = { name, init, mod, hp, isPlayer ->
                viewModel.addCombatant(name, init, mod, hp, isPlayer)
            }
        )
    }

    if (showShareQrDialog) {
        ShowQrDialog(
            payload = ShareablePayload(
                type = SharePayloadType.COMBAT_ENCOUNTER,
                title = "Encontro com ${combatState.combatants.size} Combatentes",
                combatants = combatState.combatants
            ),
            onDismiss = { showShareQrDialog = false }
        )
    }
}

@Composable
private fun CombatRoundHeroCard(
    state: CombatState,
    onNextTurn: () -> Unit,
    onPrevTurn: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RODADA ${state.round}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                }

                IconButton(onClick = onReset) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reiniciar Combate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nome do combatente do turno
            val activeName = state.currentCombatant?.name ?: "Nenhum combatente ativo"
            Text(
                text = "Turno Atual:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = activeName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de Avanço de Turno
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevTurn,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anterior")
                }

                Button(
                    onClick = onNextTurn,
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground
                    )
                ) {
                    Text("Próximo Turno", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CombatantCard(
    combatant: Combatant,
    isCurrentTurn: Boolean,
    onDamage: (Int) -> Unit,
    onHeal: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isCurrentTurn) GoldPrimary else BorderRunic,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    val hpBarColor by animateColorAsState(
        targetValue = when {
            combatant.hpPercentage > 0.5f -> CriticalSuccess
            combatant.hpPercentage > 0.25f -> GoldPrimary
            else -> CriticalFailure
        },
        animationSpec = tween(durationMillis = 200),
        label = "hpBarColor"
    )

    val hpPercentage by animateFloatAsState(
        targetValue = combatant.hpPercentage,
        animationSpec = tween(durationMillis = 250),
        label = "hpPercentage"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isCurrentTurn) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTurn) DarkSurfaceVariant else DarkSurfaceCard
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge de Iniciativa
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (combatant.isPlayer) ArcanePurple else CriticalFailure.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = combatant.initiative.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = combatant.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (combatant.isDefeated) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (combatant.isDefeated) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )

                            if (isCurrentTurn) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(GoldPrimary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "TURNO ATUAL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DarkBackground,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (combatant.isPlayer) "Jogador / Aliado" else "Inimigo / Monstro",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (combatant.isPlayer) CriticalSuccess else CriticalFailure
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir Combatente",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de HP & Indicador numérico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (combatant.isDefeated) "DERROTADO" else "${combatant.currentHp} / ${combatant.maxHp} HP",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (combatant.isDefeated) CriticalFailure else hpBarColor
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { onDamage(5) },
                        shape = RoundedCornerShape(6.dp)
                    ) { Text("-5", color = CriticalFailure, fontSize = 12.sp) }

                    OutlinedButton(
                        onClick = { onDamage(1) },
                        shape = RoundedCornerShape(6.dp)
                    ) { Text("-1", color = CriticalFailure, fontSize = 12.sp) }

                    OutlinedButton(
                        onClick = { onHeal(1) },
                        shape = RoundedCornerShape(6.dp)
                    ) { Text("+1", color = CriticalSuccess, fontSize = 12.sp) }

                    OutlinedButton(
                        onClick = { onHeal(5) },
                        shape = RoundedCornerShape(6.dp)
                    ) { Text("+5", color = CriticalSuccess, fontSize = 12.sp) }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { hpPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = hpBarColor,
                trackColor = DarkSurfaceVariant
            )
        }
    }
}
