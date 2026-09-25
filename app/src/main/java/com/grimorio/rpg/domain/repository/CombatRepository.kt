package com.grimorio.rpg.domain.repository

import com.grimorio.rpg.domain.model.Combatant
import com.grimorio.rpg.domain.model.CombatState
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de gerenciamento e persistência da fila de combate e iniciativas.
 */
interface CombatRepository {
    fun getCombatState(): Flow<CombatState>
    suspend fun addCombatant(combatant: Combatant): Long
    suspend fun updateCombatant(combatant: Combatant)
    suspend fun removeCombatant(id: Long)
    suspend fun nextTurn()
    suspend fun previousTurn()
    suspend fun startCombat()
    suspend fun resetCombat()
    suspend fun clearCombatants()
    suspend fun applyDamage(combatantId: Long, amount: Int)
    suspend fun applyHeal(combatantId: Long, amount: Int)
    suspend fun rollInitiativeForAll()
}
