package com.grimorio.rpg.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SharePayloadType {
    CHARACTER,
    MACRO,
    COMBAT_ENCOUNTER
}

@Serializable
data class ShareablePayload(
    val type: SharePayloadType,
    val version: Int = 1,
    val title: String,
    val character: CharacterProfile? = null,
    val macro: DiceMacro? = null,
    val combatants: List<Combatant>? = null,
    val timestamp: Long = System.currentTimeMillis()
)
