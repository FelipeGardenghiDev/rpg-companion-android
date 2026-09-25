package com.grimorio.rpg.presentation.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grimorio.rpg.core.audio.NoOpSoundManager
import com.grimorio.rpg.core.audio.SoundManager
import com.grimorio.rpg.core.haptic.HapticManager
import com.grimorio.rpg.core.haptic.NoOpHapticManager
import com.grimorio.rpg.data.repository.CharacterRepositoryImpl
import com.grimorio.rpg.domain.model.ConditionType
import com.grimorio.rpg.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResourcesViewModel(
    private val characterRepository: CharacterRepository,
    private val hapticManager: HapticManager = NoOpHapticManager(),
    private val soundManager: SoundManager = NoOpSoundManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourcesUiState())
    val uiState: StateFlow<ResourcesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (characterRepository is CharacterRepositoryImpl) {
                characterRepository.seedDefaultsIfEmpty()
            }
            characterRepository.getCharacter().collect { profile ->
                _uiState.update { it.copy(character = profile) }
            }
        }
    }

    fun applyDamage(amount: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            characterRepository.applyDamage(amount)
        }
    }

    fun applyHeal(amount: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            characterRepository.applyHeal(amount)
        }
    }

    fun setTempHp(amount: Int) {
        viewModelScope.launch {
            characterRepository.setTempHp(amount)
        }
    }

    fun updateMaxHp(newMax: Int) {
        viewModelScope.launch {
            characterRepository.updateMaxHp(newMax)
        }
    }

    fun useSpellSlot(level: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            characterRepository.useSpellSlot(level)
        }
    }

    fun recoverSpellSlot(level: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            characterRepository.recoverSpellSlot(level)
        }
    }

    fun toggleCondition(type: ConditionType) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            characterRepository.toggleCondition(type)
        }
    }

    fun removeCondition(type: ConditionType) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            characterRepository.removeCondition(type)
        }
    }

    fun confirmLongRest() {
        soundManager.playCriticalHitSound()
        hapticManager.vibrateCriticalHit()
        viewModelScope.launch {
            characterRepository.longRest()
            dismissLongRestDialog()
        }
    }

    fun openEditHpDialog() {
        _uiState.update { it.copy(isEditHpDialogOpen = true) }
    }

    fun dismissEditHpDialog() {
        _uiState.update { it.copy(isEditHpDialogOpen = false) }
    }

    fun openConditionCatalog() {
        _uiState.update { it.copy(isConditionCatalogOpen = true) }
    }

    fun dismissConditionCatalog() {
        _uiState.update { it.copy(isConditionCatalogOpen = false) }
    }

    fun openLongRestDialog() {
        _uiState.update { it.copy(isLongRestConfirmDialogOpen = true) }
    }

    fun dismissLongRestDialog() {
        _uiState.update { it.copy(isLongRestConfirmDialogOpen = false) }
    }

    companion object {
        fun provideFactory(
            repository: CharacterRepository,
            hapticManager: HapticManager = NoOpHapticManager(),
            soundManager: SoundManager = NoOpSoundManager()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ResourcesViewModel(
                    characterRepository = repository,
                    hapticManager = hapticManager,
                    soundManager = soundManager
                ) as T
            }
        }
    }
}
