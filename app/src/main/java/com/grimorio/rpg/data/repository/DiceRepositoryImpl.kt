package com.grimorio.rpg.data.repository

import com.grimorio.rpg.data.local.dao.RollHistoryDao
import com.grimorio.rpg.data.local.entity.RollHistoryEntity
import com.grimorio.rpg.domain.model.DiceGroupResult
import com.grimorio.rpg.domain.model.RollResult
import com.grimorio.rpg.domain.repository.DiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DiceRepositoryImpl(
    private val rollHistoryDao: RollHistoryDao,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : DiceRepository {

    override fun getRecentRolls(limit: Int): Flow<List<RollResult>> {
        return rollHistoryDao.getRecentRolls(limit).map { entities ->
            entities.map { entity ->
                val groups = runCatching {
                    json.decodeFromString<List<DiceGroupResult>>(entity.groupsJson)
                }.getOrDefault(emptyList())

                RollResult(
                    formula = entity.formula,
                    total = entity.total,
                    groups = groups,
                    modifier = entity.modifier,
                    isCriticalHit = entity.isCriticalHit,
                    isCriticalFail = entity.isCriticalFail,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    override suspend fun saveRoll(roll: RollResult) {
        val groupsJson = json.encodeToString(roll.groups)
        val entity = RollHistoryEntity(
            formula = roll.formula,
            total = roll.total,
            modifier = roll.modifier,
            isCriticalHit = roll.isCriticalHit,
            isCriticalFail = roll.isCriticalFail,
            groupsJson = groupsJson,
            timestamp = roll.timestamp
        )
        rollHistoryDao.insertRoll(entity)
    }

    override suspend fun clearHistory() {
        rollHistoryDao.clearAll()
    }
}
