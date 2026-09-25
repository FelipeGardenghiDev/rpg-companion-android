package com.grimorio.rpg.data.repository

import com.grimorio.rpg.data.nearby.NearbyClient
import com.grimorio.rpg.domain.model.P2PConnectionState
import com.grimorio.rpg.domain.model.P2PEndpoint
import com.grimorio.rpg.domain.model.P2PMessage
import com.grimorio.rpg.domain.model.P2PMessageType
import com.grimorio.rpg.domain.model.P2PRole
import com.grimorio.rpg.domain.model.RollResult
import com.grimorio.rpg.domain.repository.P2PPartyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class P2PPartyRepositoryImpl(
    private val client: NearbyClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : P2PPartyRepository {

    override val connectionState: Flow<P2PConnectionState> = client.connectionState
    override val connectedEndpoints: Flow<List<P2PEndpoint>> = client.connectedEndpoints
    override val discoveredEndpoints: Flow<List<P2PEndpoint>> = client.discoveredEndpoints
    override val currentRole: Flow<P2PRole> = client.currentRole

    override val incomingMessages: Flow<P2PMessage> = client.incomingPayloads.mapNotNull { bytes ->
        runCatching {
            val jsonString = String(bytes, Charsets.UTF_8)
            json.decodeFromString<P2PMessage>(jsonString)
        }.getOrNull()
    }

    override suspend fun startHosting(tableSessionName: String) {
        client.startAdvertising(tableSessionName)
    }

    override suspend fun startDiscovering() {
        client.startDiscovery()
    }

    override suspend fun connectToTable(endpoint: P2PEndpoint, clientName: String) {
        client.connectToEndpoint(endpoint.id, clientName)
    }

    override suspend fun disconnect() {
        client.stopAll()
    }

    override suspend fun broadcastDiceRoll(playerName: String, rollResult: RollResult, macroName: String?) {
        val msg = P2PMessage(
            type = P2PMessageType.DICE_ROLL,
            senderName = playerName,
            rollResult = rollResult,
            macroName = macroName
        )
        val bytes = json.encodeToString(msg).toByteArray(Charsets.UTF_8)
        client.sendPayloadToAll(bytes)
    }

    override suspend fun broadcastChatMessage(senderName: String, text: String) {
        val msg = P2PMessage(
            type = P2PMessageType.CHAT_MESSAGE,
            senderName = senderName,
            text = text
        )
        val bytes = json.encodeToString(msg).toByteArray(Charsets.UTF_8)
        client.sendPayloadToAll(bytes)
    }
}
