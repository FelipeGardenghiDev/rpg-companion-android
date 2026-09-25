package com.grimorio.rpg.presentation.dice

import com.grimorio.rpg.domain.model.DiceMacro
import com.grimorio.rpg.domain.model.DiceType
import com.grimorio.rpg.domain.model.RollResult

enum class AdvantageMode(val label: String) {
    NORMAL("Normal"),
    ADVANTAGE("Vantagem"),
    DISADVANTAGE("Desvantagem")
}

data class DiceUiState(
    val formula: String = "",
    val modifier: Int = 0,
    val advantageMode: AdvantageMode = AdvantageMode.NORMAL,
    val selectedDice: Map<DiceType, Int> = emptyMap(),
    val isExploding: Boolean = false,
    val lastResult: RollResult? = null,
    val lastRolledMacroName: String? = null,
    val rollHistory: List<RollResult> = emptyList(),
    val macros: List<DiceMacro> = emptyList(),
    val isCreateMacroDialogOpen: Boolean = false,
    val errorMessage: String? = null
)
