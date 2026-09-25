package com.grimorio.rpg.presentation.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grimorio.rpg.core.audio.NoOpSoundManager
import com.grimorio.rpg.core.audio.SoundManager
import com.grimorio.rpg.core.haptic.HapticManager
import com.grimorio.rpg.core.haptic.NoOpHapticManager
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PMessageType
import com.grimorio.rpg.domain.repository.P2PPartyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PartyViewModel(
    private val partyRepository: P2PPartyRepository,
    private val hapticManager: HapticManager = NoOpHapticManager(),
    private val soundManager: SoundManager = NoOpSoundManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            partyRepository.connectionState.collect { connState ->
                _uiState.update { it.copy(connectionState = connState) }
            }
        }

        viewModelScope.launch {
            partyRepository.currentRole.collect { role ->
                _uiState.update { it.copy(role = role) }
            }
        }

        viewModelScope.launch {
            partyRepository.connectedEndpoints.collect { peers ->
                _uiState.update { it.copy(connectedPeers = peers) }
            }
        }

        viewModelScope.launch {
            partyRepository.discoveredEndpoints.collect { tables ->
                _uiState.update { it.copy(discoveredTables = tables) }
            }
        }

        viewModelScope.launch {
            partyRepository.incomingMessages.collect { msg ->
                _uiState.update { current ->
                    current.copy(tableFeed = listOf(msg) + current.tableFeed.take(49))
                }

                when (msg.type) {
                    P2PMessageType.DICE_ROLL -> {
                        val roll = msg.rollResult
                        if (roll?.isCriticalHit == true) {
                            soundManager.playCriticalHitSound()
                            hapticManager.vibrateCriticalHit()
                        } else if (roll?.isCriticalFail == true) {
                            soundManager.playCriticalFailSound()
                            hapticManager.vibrateCriticalFail()
                        } else {
                            soundManager.playRollSound()
                            hapticManager.vibrateRoll()
                        }
                    }
                    else -> {
                        soundManager.playClickSound()
                        hapticManager.vibrateClick()
                    }
                }
            }
        }
    }

    fun startHosting(tableName: String) {
        soundManager.playCriticalHitSound()
        hapticManager.vibrateCriticalHit()
        _uiState.update { it.copy(tableSessionName = tableName, isHostDialogOpen = false) }
        viewModelScope.launch {
            partyRepository.startHosting(tableName)
        }
    }

    fun startDiscovering() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            partyRepository.startDiscovering()
        }
    }

    fun joinTable(endpoint: P2PEndpoint) {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            partyRepository.connectToTable(endpoint, _uiState.value.myName)
        }
    }

    fun disconnect() {
        soundManager.playClickSound()
        hapticManager.vibrateClick()
        viewModelScope.launch {
            partyRepository.disconnect()
        }
    }

    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        soundManager.playClickSound()
        viewModelScope.launch {
            partyRepository.broadcastChatMessage(_uiState.value.myName, trimmed)
        }
    }

    fun openHostDialog() {
        _uiState.update { it.copy(isHostDialogOpen = true) }
    }

    fun dismissHostDialog() {
        _uiState.update { it.copy(isHostDialogOpen = false) }
    }

    companion object {
        fun provideFactory(
            repository: P2PPartyRepository,
            hapticManager: HapticManager = NoOpHapticManager(),
            soundManager: SoundManager = NoOpSoundManager()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PartyViewModel(
                    partyRepository = repository,
                    hapticManager = hapticManager,
                    soundManager = soundManager
                ) as T
            }
        }
    }
}
