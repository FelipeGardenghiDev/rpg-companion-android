package com.grimorio.rpg.presentation.dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grimorio.rpg.core.audio.NoOpSoundManager
import com.grimorio.rpg.core.audio.SoundManager
import com.grimorio.rpg.core.haptic.HapticManager
import com.grimorio.rpg.core.haptic.NoOpHapticManager
import com.grimorio.rpg.data.repository.MacroRepositoryImpl
import com.grimorio.rpg.domain.model.DiceMacro
import com.grimorio.rpg.domain.model.DiceType
import com.grimorio.rpg.domain.model.MacroCategory
import com.grimorio.rpg.domain.model.RollResult
import com.grimorio.rpg.domain.repository.DiceRepository
import com.grimorio.rpg.domain.repository.MacroRepository
import com.grimorio.rpg.domain.repository.P2PPartyRepository
import com.grimorio.rpg.domain.usecase.RollDiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiceViewModel(
    private val rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
    private val diceRepository: DiceRepository? = null,
    private val macroRepository: MacroRepository? = null,
    private val partyRepository: P2PPartyRepository? = null,
    private val hapticManager: HapticManager = NoOpHapticManager(),
    private val soundManager: SoundManager = NoOpSoundManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    init {
        diceRepository?.let { repo ->
            viewModelScope.launch {
                repo.getRecentRolls().collect { history ->
                    _uiState.update { it.copy(rollHistory = history) }
                }
            }
        }

        macroRepository?.let { repo ->
            viewModelScope.launch {
                if (repo is MacroRepositoryImpl) {
                    repo.seedDefaultsIfEmpty()
                }
                repo.getAllMacros().collect { macroList ->
                    _uiState.update { it.copy(macros = macroList) }
                }
            }
        }
    }

    fun addDie(diceType: DiceType) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()

        _uiState.update { current ->
            val updatedMap = current.selectedDice.toMutableMap()
            val currentCount = updatedMap[diceType] ?: 0
            updatedMap[diceType] = currentCount + 1

            val newFormula = buildFormula(
                diceMap = updatedMap,
                mod = current.modifier,
                adv = current.advantageMode,
                exploding = current.isExploding
            )

            current.copy(
                selectedDice = updatedMap,
                formula = newFormula,
                lastRolledMacroName = null,
                errorMessage = null
            )
        }
    }

    fun removeDie(diceType: DiceType) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()

        _uiState.update { current ->
            val updatedMap = current.selectedDice.toMutableMap()
            val currentCount = updatedMap[diceType] ?: 0
            if (currentCount <= 1) {
                updatedMap.remove(diceType)
            } else {
                updatedMap[diceType] = currentCount - 1
            }

            val newFormula = buildFormula(
                diceMap = updatedMap,
                mod = current.modifier,
                adv = current.advantageMode,
                exploding = current.isExploding
            )

            current.copy(
                selectedDice = updatedMap,
                formula = newFormula,
                lastRolledMacroName = null,
                errorMessage = null
            )
        }
    }

    fun adjustModifier(delta: Int) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()

        _uiState.update { current ->
            val newModifier = current.modifier + delta
            val newFormula = buildFormula(
                diceMap = current.selectedDice,
                mod = newModifier,
                adv = current.advantageMode,
                exploding = current.isExploding
            )

            current.copy(
                modifier = newModifier,
                formula = newFormula,
                lastRolledMacroName = null,
                errorMessage = null
            )
        }
    }

    fun setAdvantageMode(mode: AdvantageMode) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()

        _uiState.update { current ->
            val newMode = if (current.advantageMode == mode) AdvantageMode.NORMAL else mode
            val newFormula = buildFormula(
                diceMap = current.selectedDice,
                mod = current.modifier,
                adv = newMode,
                exploding = current.isExploding
            )

            current.copy(
                advantageMode = newMode,
                formula = newFormula,
                errorMessage = null
            )
        }
    }

    fun toggleExploding() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()

        _uiState.update { current ->
            val newExploding = !current.isExploding
            val newFormula = buildFormula(
                diceMap = current.selectedDice,
                mod = current.modifier,
                adv = current.advantageMode,
                exploding = newExploding
            )

            current.copy(
                isExploding = newExploding,
                formula = newFormula,
                errorMessage = null
            )
        }
    }

    fun onFormulaChanged(rawText: String) {
        _uiState.update { current ->
            current.copy(
                formula = rawText,
                lastRolledMacroName = null,
                errorMessage = null
            )
        }
    }

    fun rollDice() {
        val formula = _uiState.value.formula.trim()
        if (formula.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Selecione ao menos um dado ou digite uma fórmula.") }
            return
        }

        viewModelScope.launch {
            val result = rollDiceUseCase(formula)
            result.onSuccess { rollResult ->
                triggerSensoryFeedback(rollResult)

                _uiState.update { current ->
                    val updatedHistory = if (diceRepository == null) {
                        listOf(rollResult) + current.rollHistory.take(19)
                    } else {
                        current.rollHistory
                    }
                    current.copy(
                        lastResult = rollResult,
                        rollHistory = updatedHistory,
                        errorMessage = null
                    )
                }
                diceRepository?.saveRoll(rollResult)
                partyRepository?.broadcastDiceRoll("Jogador", rollResult, null)
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(errorMessage = error.localizedMessage ?: "Erro ao calcular rolagem.")
                }
            }
        }
    }

    fun quickRoll(diceType: DiceType) {
        viewModelScope.launch {
            val formula = when (diceType) {
                DiceType.D20 -> when (_uiState.value.advantageMode) {
                    AdvantageMode.ADVANTAGE -> "2d20kh1"
                    AdvantageMode.DISADVANTAGE -> "2d20kl1"
                    AdvantageMode.NORMAL -> "1d20"
                }
                else -> "1${diceType.displayName}"
            }

            val finalFormula = if (_uiState.value.modifier != 0) {
                val mod = _uiState.value.modifier
                if (mod > 0) "$formula + $mod" else "$formula - ${-mod}"
            } else {
                formula
            }

            val result = rollDiceUseCase(finalFormula)
            result.onSuccess { rollResult ->
                triggerSensoryFeedback(rollResult)

                _uiState.update { current ->
                    val updatedHistory = if (diceRepository == null) {
                        listOf(rollResult) + current.rollHistory.take(19)
                    } else {
                        current.rollHistory
                    }
                    current.copy(
                        lastResult = rollResult,
                        lastRolledMacroName = null,
                        rollHistory = updatedHistory,
                        errorMessage = null
                    )
                }
                diceRepository?.saveRoll(rollResult)
                partyRepository?.broadcastDiceRoll("Jogador", rollResult, null)
            }
        }
    }

    fun rollMacro(macro: DiceMacro) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    formula = macro.formula,
                    lastRolledMacroName = "${macro.category.iconEmoji} ${macro.name}"
                )
            }

            val result = rollDiceUseCase(macro.formula)
            result.onSuccess { rollResult ->
                triggerSensoryFeedback(rollResult)

                _uiState.update { current ->
                    val updatedHistory = if (diceRepository == null) {
                        listOf(rollResult) + current.rollHistory.take(19)
                    } else {
                        current.rollHistory
                    }
                    current.copy(
                        lastResult = rollResult,
                        rollHistory = updatedHistory,
                        errorMessage = null
                    )
                }
                diceRepository?.saveRoll(rollResult)
                partyRepository?.broadcastDiceRoll("Jogador", rollResult, macro.name)
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(errorMessage = error.localizedMessage ?: "Erro ao executar macro.")
                }
            }
        }
    }

    fun openCreateMacroDialog() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        _uiState.update { it.copy(isCreateMacroDialogOpen = true) }
    }

    fun dismissCreateMacroDialog() {
        _uiState.update { it.copy(isCreateMacroDialogOpen = false) }
    }

    fun saveMacro(name: String, formula: String, category: MacroCategory) {
        viewModelScope.launch {
            val newMacro = DiceMacro(
                name = name,
                formula = formula,
                category = category
            )
            macroRepository?.saveMacro(newMacro)
            dismissCreateMacroDialog()
        }
    }

    fun deleteMacro(id: Long) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            macroRepository?.deleteMacro(id)
        }
    }

    fun clearHistory() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            diceRepository?.clearHistory()
            _uiState.update { it.copy(rollHistory = emptyList()) }
        }
    }

    fun clear() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        _uiState.update { current ->
            current.copy(
                formula = "",
                modifier = 0,
                selectedDice = emptyMap(),
                advantageMode = AdvantageMode.NORMAL,
                isExploding = false,
                lastRolledMacroName = null,
                errorMessage = null
            )
        }
    }

    private fun triggerSensoryFeedback(rollResult: RollResult) {
        if (rollResult.isCriticalHit) {
            soundManager.playCriticalHitSound()
            hapticManager.vibrateCriticalHit()
        } else if (rollResult.isCriticalFail) {
            soundManager.playCriticalFailSound()
            hapticManager.vibrateCriticalFail()
        } else {
            soundManager.playRollSound()
            hapticManager.vibrateRoll()
        }
    }

    private fun buildFormula(
        diceMap: Map<DiceType, Int>,
        mod: Int,
        adv: AdvantageMode,
        exploding: Boolean
    ): String {
        if (diceMap.isEmpty() && mod == 0) return ""

        val parts = mutableListOf<String>()
        val explodeSuffix = if (exploding) "!" else ""

        for ((type, count) in diceMap) {
            if (count <= 0) continue
            val part = when {
                type == DiceType.D20 && adv == AdvantageMode.ADVANTAGE -> "2d20kh1$explodeSuffix"
                type == DiceType.D20 && adv == AdvantageMode.DISADVANTAGE -> "2d20kl1$explodeSuffix"
                else -> "${count}${type.displayName}$explodeSuffix"
            }
            parts.add(part)
        }

        var result = parts.joinToString(" + ")

        if (mod != 0) {
            val modSign = if (mod > 0) "+ $mod" else "- ${-mod}"
            result = if (result.isEmpty()) mod.toString() else "$result $modSign"
        }

        return result
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    companion object {
        fun provideFactory(
            repository: DiceRepository,
            macroRepository: MacroRepository? = null,
            partyRepository: P2PPartyRepository? = null,
            rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
            hapticManager: HapticManager = NoOpHapticManager(),
            soundManager: SoundManager = NoOpSoundManager()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiceViewModel(
                    rollDiceUseCase = rollDiceUseCase,
                    diceRepository = repository,
                    macroRepository = macroRepository,
                    partyRepository = partyRepository,
                    hapticManager = hapticManager,
                    soundManager = soundManager
                ) as T
            }
        }
    }
}
