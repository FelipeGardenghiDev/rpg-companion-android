package com.grimorio.rpg.domain.repository

import com.grimorio.rpg.domain.model.RollResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistência e recuperação do histórico de rolagens de dados.
 */
interface DiceRepository {
    fun getRecentRolls(limit: Int = 30): Flow<List<RollResult>>
    suspend fun saveRoll(roll: RollResult)
    suspend fun clearHistory()
}
