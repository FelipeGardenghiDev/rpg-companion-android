package com.grimorio.rpg.presentation.party

import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PMessage
import com.grimorio.rpg.domain.model.P2PRole

data class PartyUiState(
    val role: P2PRole = P2PRole.DISCONNECTED,
    val connectionState: P2PConnectionState = P2PConnectionState.DISCONNECTED,
    val myName: String = "Jogador",
    val tableSessionName: String = "Mesa do Grimório",
    val connectedPeers: List<P2PEndpoint> = emptyList(),
    val discoveredTables: List<P2PEndpoint> = emptyList(),
    val tableFeed: List<P2PMessage> = emptyList(),
    val isHostDialogOpen: Boolean = false
)
