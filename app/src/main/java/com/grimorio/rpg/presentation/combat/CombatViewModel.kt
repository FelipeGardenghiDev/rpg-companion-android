package com.grimorio.rpg.presentation.combat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grimorio.rpg.core.audio.NoOpSoundManager
import com.grimorio.rpg.core.audio.SoundManager
import com.grimorio.rpg.core.haptic.HapticManager
import com.grimorio.rpg.core.haptic.NoOpHapticManager
import com.grimorio.rpg.data.repository.CombatRepositoryImpl
import com.grimorio.rpg.domain.model.Combatant
import com.grimorio.rpg.domain.repository.CombatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CombatViewModel(
    private val combatRepository: CombatRepository,
    private val hapticManager: HapticManager = NoOpHapticManager(),
    private val soundManager: SoundManager = NoOpSoundManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CombatUiState())
    val uiState: StateFlow<CombatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (combatRepository is CombatRepositoryImpl) {
                combatRepository.seedDefaultsIfEmpty()
            }
            combatRepository.getCombatState().collect { state ->
                _uiState.update { it.copy(combatState = state) }
            }
        }
    }

    fun nextTurn() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            combatRepository.nextTurn()
        }
    }

    fun previousTurn() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            combatRepository.previousTurn()
        }
    }

    fun startCombat() {
        soundManager.playCriticalHitSound()
        hapticManager.vibrateCriticalHit()
        viewModelScope.launch {
            combatRepository.startCombat()
        }
    }

    fun resetCombat() {
        viewModelScope.launch {
            combatRepository.resetCombat()
        }
    }

    fun rollAllInitiatives() {
        soundManager.playRollSound()
        hapticManager.vibrateRoll()
        viewModelScope.launch {
            combatRepository.rollInitiativeForAll()
        }
    }

    fun applyDamage(combatantId: Long, amount: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            combatRepository.applyDamage(combatantId, amount)
        }
    }

    fun applyHeal(combatantId: Long, amount: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            combatRepository.applyHeal(combatantId, amount)
        }
    }

    fun addCombatant(
        name: String,
        initiative: Int,
        modifier: Int,
        maxHp: Int,
        isPlayer: Boolean
    ) {
        viewModelScope.launch {
            val combatant = Combatant(
                name = name,
                initiative = initiative,
                initiativeModifier = modifier,
                currentHp = maxHp,
                maxHp = maxHp,
                isPlayer = isPlayer
            )
            combatRepository.addCombatant(combatant)
            dismissAddCombatantDialog()
        }
    }

    fun removeCombatant(id: Long) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            combatRepository.removeCombatant(id)
        }
    }

    fun openAddCombatantDialog() {
        _uiState.update { it.copy(isAddCombatantDialogOpen = true) }
    }

    fun dismissAddCombatantDialog() {
        _uiState.update { it.copy(isAddCombatantDialogOpen = false) }
    }

    companion object {
        fun provideFactory(
            repository: CombatRepository,
            hapticManager: HapticManager = NoOpHapticManager(),
            soundManager: SoundManager = NoOpSoundManager()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CombatViewModel(
                    combatRepository = repository,
                    hapticManager = hapticManager,
                    soundManager = soundManager
                ) as T
            }
        }
    }
}
