package com.grimorio.rpg.presentation.combat

import com.grimorio.rpg.domain.model.CombatState

data class CombatUiState(
    val combatState: CombatState = CombatState(),
    val isAddCombatantDialogOpen: Boolean = false
)
