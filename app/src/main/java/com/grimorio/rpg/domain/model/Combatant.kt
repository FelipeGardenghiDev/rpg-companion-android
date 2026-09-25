package com.grimorio.rpg.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Combatant(
    val id: Long = 0,
    val name: String,
    val initiative: Int = 10,
    val initiativeModifier: Int = 0,
    val currentHp: Int = 20,
    val maxHp: Int = 20,
    val isPlayer: Boolean = false,
    val conditions: List<String> = emptyList(),
    val orderIndex: Int = 0
) {
    val hpPercentage: Float
        get() = if (maxHp > 0) (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f

    val isDefeated: Boolean
        get() = currentHp <= 0

    fun applyDamage(amount: Int): Combatant {
        val newHp = (currentHp - amount.coerceAtLeast(0)).coerceAtLeast(0)
        return copy(currentHp = newHp)
    }

    fun applyHeal(amount: Int): Combatant {
        val newHp = (currentHp + amount.coerceAtLeast(0)).coerceAtMost(maxHp)
        return copy(currentHp = newHp)
    }
}

@Serializable
data class CombatState(
    val round: Int = 1,
    val currentTurnIndex: Int = 0,
    val combatants: List<Combatant> = emptyList(),
    val isActive: Boolean = false
) {
    val currentCombatant: Combatant?
        get() = if (combatants.isNotEmpty() && currentTurnIndex in combatants.indices) {
            combatants[currentTurnIndex]
        } else {
            null
        }

    fun nextTurn(): CombatState {
        if (combatants.isEmpty()) return this
        val nextIndex = (currentTurnIndex + 1) % combatants.size
        val nextRound = if (nextIndex == 0) round + 1 else round
        return copy(
            currentTurnIndex = nextIndex,
            round = nextRound,
            isActive = true
        )
    }

    fun previousTurn(): CombatState {
        if (combatants.isEmpty()) return this
        val prevIndex = if (currentTurnIndex > 0) currentTurnIndex - 1 else combatants.size - 1
        val prevRound = if (currentTurnIndex == 0 && round > 1) round - 1 else round
        return copy(
            currentTurnIndex = prevIndex,
            round = prevRound
        )
    }

    fun reset(): CombatState {
        return copy(
            round = 1,
            currentTurnIndex = 0,
            isActive = false
        )
    }
}
