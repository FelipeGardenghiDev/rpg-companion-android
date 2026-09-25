package com.grimorio.rpg.domain.repository

import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PMessage
import com.grimorio.rpg.domain.model.P2PRole
import com.grimorio.rpg.domain.model.RollResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de comunicação P2P local sem servidor nem internet via Google Nearby Connections.
 */
interface P2PPartyRepository {
    val connectionState: Flow<P2PConnectionState>
    val connectedEndpoints: Flow<List<P2PEndpoint>>
    val discoveredEndpoints: Flow<List<P2PEndpoint>>
    val incomingMessages: Flow<P2PMessage>
    val currentRole: Flow<P2PRole>

    suspend fun startHosting(tableSessionName: String)
    suspend fun startDiscovering()
    suspend fun connectToTable(endpoint: P2PEndpoint, clientName: String)
    suspend fun disconnect()

    suspend fun broadcastDiceRoll(playerName: String, rollResult: RollResult, macroName: String? = null)
    suspend fun broadcastChatMessage(senderName: String, text: String)
}
