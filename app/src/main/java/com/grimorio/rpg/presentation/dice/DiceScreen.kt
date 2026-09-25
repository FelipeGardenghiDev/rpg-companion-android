package com.grimorio.rpg.presentation.dice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.grimorio.rpg.core.designsystem.theme.DroppedDice
import com.grimorio.rpg.core.designsystem.theme.GoldPrimary
import com.grimorio.rpg.domain.model.DiceType
import com.grimorio.rpg.domain.model.RollResult
import com.grimorio.rpg.presentation.components.CreateMacroDialog
import com.grimorio.rpg.presentation.components.DiceButton
import com.grimorio.rpg.presentation.components.MacroCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiceScreen(
    modifier: Modifier = Modifier,
    viewModel: DiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GRIMÓRIO RPG",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GoldPrimary
                            )
                            Text(
                                text = "Mesa de Rolagem Arcana",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.formula.isNotEmpty() || uiState.lastResult != null) {
                        IconButton(onClick = { viewModel.clear() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar Mesa",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

            // 1. HERO CARD: RESULTADO PRINCIPAL
            item {
                RollResultHeroCard(
                    result = uiState.lastResult,
                    macroName = uiState.lastRolledMacroName
                )
            }

            // Mensagem de erro, se houver
            item {
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = CriticalFailure,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // 2. ATALHOS & MACROS PERSONALIZADOS
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Atalhos & Macros (${uiState.macros.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { viewModel.openCreateMacroDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Novo Atalho",
                                tint = GoldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(uiState.macros, key = { it.id }) { macro ->
                            MacroCard(
                                macro = macro,
                                onClick = { viewModel.rollMacro(macro) },
                                onDelete = { viewModel.deleteMacro(macro.id) }
                            )
                        }

                        // Botão adicionar rápido
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderRunic, RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                                    .clickable { viewModel.openCreateMacroDialog() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Novo Atalho",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. INPUT DE FÓRMULA & LIMPAR
            item {
                OutlinedTextField(
                    value = uiState.formula,
                    onValueChange = { viewModel.onFormulaChanged(it) },
                    label = { Text("Fórmula de Dados") },
                    placeholder = { Text("ex: 2d20kh1 + 5, 3d6!") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (uiState.formula.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clear() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderRunic,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = GoldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 4. SELETOR DE VANTAGEM / DESVANTAGEM E DADOS EXPLOSIVOS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdvantageMode.entries.forEach { mode ->
                        val isSelected = uiState.advantageMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAdvantageMode(mode) },
                            label = { Text(mode.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (mode == AdvantageMode.ADVANTAGE) CriticalSuccess.copy(alpha = 0.25f)
                                else if (mode == AdvantageMode.DISADVANTAGE) CriticalFailure.copy(alpha = 0.25f)
                                else GoldPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = if (mode == AdvantageMode.ADVANTAGE) CriticalSuccess
                                else if (mode == AdvantageMode.DISADVANTAGE) CriticalFailure
                                else GoldPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BorderRunic,
                                selectedBorderColor = if (mode == AdvantageMode.ADVANTAGE) CriticalSuccess
                                else if (mode == AdvantageMode.DISADVANTAGE) CriticalFailure
                                else GoldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    FilterChip(
                        selected = uiState.isExploding,
                        onClick = { viewModel.toggleExploding() },
                        label = { Text("Explosivo (!)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ArcanePurple.copy(alpha = 0.25f),
                            selectedLabelColor = ArcanePurple,
                            selectedLeadingIconColor = ArcanePurple
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.isExploding,
                            borderColor = BorderRunic,
                            selectedBorderColor = ArcanePurple
                        )
                    )
                }
            }

            // 5. MESA DE DADOS POLIÉDRICOS
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dados Poliédricos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Segure para rolar direto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiceType.entries.forEach { dice ->
                            val count = uiState.selectedDice[dice] ?: 0
                            DiceButton(
                                diceType = dice,
                                count = count,
                                onClick = { viewModel.addDie(dice) },
                                onLongClick = { viewModel.quickRoll(dice) },
                                modifier = Modifier.width(96.dp)
                            )
                        }
                    }
                }
            }

            // 6. MODIFICADORES RÁPIDOS (+ / -)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Modificador:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { viewModel.adjustModifier(-5) },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("-5") }

                    OutlinedButton(
                        onClick = { viewModel.adjustModifier(-1) },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("-1") }

                    Text(
                        text = if (uiState.modifier >= 0) "+${uiState.modifier}" else "${uiState.modifier}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    OutlinedButton(
                        onClick = { viewModel.adjustModifier(+1) },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("+1") }

                    OutlinedButton(
                        onClick = { viewModel.adjustModifier(+5) },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("+5") }
                }
            }

            // 7. BOTÃO PRINCIPAL DE ROLAGEM
            item {
                Button(
                    onClick = { viewModel.rollDice() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ROLAR DADOS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // 8. HISTÓRICO RECENTE
            if (uiState.rollHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Histórico da Sessão (${uiState.rollHistory.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar Histórico",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                items(uiState.rollHistory) { historyItem ->
                    HistoryItemCard(result = historyItem)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Modal para criar novo atalho
    if (uiState.isCreateMacroDialogOpen) {
        CreateMacroDialog(
            initialFormula = uiState.formula,
            onDismiss = { viewModel.dismissCreateMacroDialog() },
            onConfirm = { name, formula, category ->
                viewModel.saveMacro(name, formula, category)
            }
        )
    }
}

@Composable
private fun RollResultHeroCard(
    result: RollResult?,
    macroName: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = when {
                    result?.isCriticalHit == true -> CriticalSuccess
                    result?.isCriticalFail == true -> CriticalFailure
                    result != null -> GoldPrimary
                    else -> BorderRunic
                },
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (result == null) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = BorderRunic,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A mesa está pronta",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Selecione os dados ou atalhos acima para rolar!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Tag de Macro
                if (macroName != null) {
                    Box(
                        modifier = Modifier
                            .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = macroName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Banner de Crítico
                if (result.isCriticalHit) {
                    Box(
                        modifier = Modifier
                            .background(CriticalSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚔️ ACERTO CRÍTICO! (20 NATURAL)",
                            color = CriticalSuccess,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (result.isCriticalFail) {
                    Box(
                        modifier = Modifier
                            .background(CriticalFailure.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "💀 FALHA CRÍTICA! (1 NATURAL)",
                            color = CriticalFailure,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Total Gigante
                Text(
                    text = result.total.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = when {
                        result.isCriticalHit -> CriticalSuccess
                        result.isCriticalFail -> CriticalFailure
                        else -> GoldPrimary
                    }
                )

                Text(
                    text = "Fórmula: ${result.formula}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detalhamento dos dados individuais
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    result.groups.forEach { group ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${group.count}d${group.sides}${if (group.isExploding) "!" else ""}:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                group.keptValues.forEach { v ->
                                    Text(
                                        text = v.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (group.sides == 20 && v == 20) CriticalSuccess
                                        else if (group.sides == 20 && v == 1) CriticalFailure
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                group.droppedValues.forEach { v ->
                                    Text(
                                        text = v.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = DroppedDice
                                    )
                                }
                            }
                        }
                    }

                    if (result.modifier != 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Modificador:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (result.modifier > 0) "+${result.modifier}" else "${result.modifier}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(result: RollResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = result.formula,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (result.isCriticalHit) {
                    Text(
                        text = "Acerto Crítico",
                        style = MaterialTheme.typography.labelSmall,
                        color = CriticalSuccess
                    )
                } else if (result.isCriticalFail) {
                    Text(
                        text = "Falha Crítica",
                        style = MaterialTheme.typography.labelSmall,
                        color = CriticalFailure
                    )
                }
            }

            Text(
                text = result.total.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = when {
                    result.isCriticalHit -> CriticalSuccess
                    result.isCriticalFail -> CriticalFailure
                    else -> GoldPrimary
                }
            )
        }
    }
}
