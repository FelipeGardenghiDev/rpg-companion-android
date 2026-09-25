package com.grimorio.rpg.presentation.resources

import com.grimorio.rpg.domain.model.CharacterProfile

data class ResourcesUiState(
    val character: CharacterProfile = CharacterProfile(),
    val isEditHpDialogOpen: Boolean = false,
    val isConditionCatalogOpen: Boolean = false,
    val isLongRestConfirmDialogOpen: Boolean = false
)
