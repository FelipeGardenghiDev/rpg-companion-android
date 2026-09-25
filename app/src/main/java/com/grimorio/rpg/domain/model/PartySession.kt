package com.grimorio.rpg.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class P2PRole {
    DISCONNECTED,
    HOST,   // Mestre da mesa (anuncia a sessão)
    CLIENT  // Jogador (descobre e conecta à mesa)
}

@Serializable
enum class P2PConnectionState {
    DISCONNECTED,
    ADVERTISING,
    DISCOVERING,
    CONNECTED
}

@Serializable
data class P2PEndpoint(
    val id: String,
    val name: String,
    val isConnected: Boolean = false
)

@Serializable
enum class P2PMessageType {
    DICE_ROLL,
    COMBAT_SYNC,
    CHAT_MESSAGE
}

@Serializable
data class P2PMessage(
    val type: P2PMessageType,
    val senderName: String,
    val rollResult: RollResult? = null,
    val macroName: String? = null,
    val text: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
