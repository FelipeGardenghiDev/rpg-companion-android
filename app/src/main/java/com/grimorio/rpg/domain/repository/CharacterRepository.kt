package com.grimorio.rpg.domain.repository

import com.grimorio.rpg.domain.model.CharacterProfile
import com.grimorio.rpg.domain.model.ConditionType
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistência e gerenciamento dos recursos e condições do personagem.
 */
interface CharacterRepository {
    fun getCharacter(): Flow<CharacterProfile>
    suspend fun saveCharacter(profile: CharacterProfile)
    suspend fun applyDamage(amount: Int)
    suspend fun applyHeal(amount: Int)
    suspend fun setTempHp(amount: Int)
    suspend fun updateMaxHp(newMax: Int)
    suspend fun useSpellSlot(level: Int)
    suspend fun recoverSpellSlot(level: Int)
    suspend fun shortRest()
    suspend fun longRest()
    suspend fun toggleCondition(conditionType: ConditionType, durationTurns: Int? = null)
    suspend fun removeCondition(conditionType: ConditionType)
}
