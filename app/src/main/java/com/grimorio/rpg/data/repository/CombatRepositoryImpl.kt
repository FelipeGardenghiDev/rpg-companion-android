package com.grimorio.rpg.data.repository

import com.grimorio.rpg.data.local.dao.CombatDao
import com.grimorio.rpg.data.local.entity.CombatSessionEntity
import com.grimorio.rpg.data.local.entity.CombatantEntity
import com.grimorio.rpg.domain.model.CombatState
import com.grimorio.rpg.domain.model.Combatant
import com.grimorio.rpg.domain.repository.CombatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

class CombatRepositoryImpl(
    private val combatDao: CombatDao,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val random: Random = Random.Default
) : CombatRepository {

    override fun getCombatState(): Flow<CombatState> {
        return combine(
            combatDao.getAllCombatants(),
            combatDao.getSession()
        ) { entities, sessionEntity ->
            val combatants = entities.map { entity ->
                val conditions = runCatching {
                    json.decodeFromString<List<String>>(entity.conditionsJson)
                }.getOrDefault(emptyList())

                Combatant(
                    id = entity.id,
                    name = entity.name,
                    initiative = entity.initiative,
                    initiativeModifier = entity.initiativeModifier,
                    currentHp = entity.currentHp,
                    maxHp = entity.maxHp,
                    isPlayer = entity.isPlayer,
                    conditions = conditions
                )
            }

            val session = sessionEntity ?: CombatSessionEntity()
            CombatState(
                round = session.round,
                currentTurnIndex = session.currentTurnIndex.coerceIn(0, (combatants.size - 1).coerceAtLeast(0)),
                combatants = combatants,
                isActive = session.isActive
            )
        }
    }

    override suspend fun addCombatant(combatant: Combatant): Long {
        val entity = CombatantEntity(
            id = combatant.id,
            name = combatant.name,
            initiative = combatant.initiative,
            initiativeModifier = combatant.initiativeModifier,
            currentHp = combatant.currentHp,
            maxHp = combatant.maxHp,
            isPlayer = combatant.isPlayer,
            conditionsJson = json.encodeToString(combatant.conditions)
        )
        return combatDao.insertCombatant(entity)
    }

    override suspend fun updateCombatant(combatant: Combatant) {
        val entity = CombatantEntity(
            id = combatant.id,
            name = combatant.name,
            initiative = combatant.initiative,
            initiativeModifier = combatant.initiativeModifier,
            currentHp = combatant.currentHp,
            maxHp = combatant.maxHp,
            isPlayer = combatant.isPlayer,
            conditionsJson = json.encodeToString(combatant.conditions)
        )
        combatDao.updateCombatant(entity)
    }

    override suspend fun removeCombatant(id: Long) {
        combatDao.deleteCombatant(id)
    }

    override suspend fun nextTurn() {
        val current = getCombatState().first()
        val next = current.nextTurn()
        combatDao.upsertSession(
            CombatSessionEntity(
                round = next.round,
                currentTurnIndex = next.currentTurnIndex,
                isActive = true
            )
        )
    }

    override suspend fun previousTurn() {
        val current = getCombatState().first()
        val prev = current.previousTurn()
        combatDao.upsertSession(
            CombatSessionEntity(
                round = prev.round,
                currentTurnIndex = prev.currentTurnIndex,
                isActive = prev.isActive
            )
        )
    }

    override suspend fun startCombat() {
        combatDao.upsertSession(
            CombatSessionEntity(
                round = 1,
                currentTurnIndex = 0,
                isActive = true
            )
        )
    }

    override suspend fun resetCombat() {
        combatDao.upsertSession(
            CombatSessionEntity(
                round = 1,
                currentTurnIndex = 0,
                isActive = false
            )
        )
    }

    override suspend fun clearCombatants() {
        combatDao.clearAllCombatants()
        resetCombat()
    }

    override suspend fun applyDamage(combatantId: Long, amount: Int) {
        val current = getCombatState().first()
        val combatant = current.combatants.find { it.id == combatantId } ?: return
        updateCombatant(combatant.applyDamage(amount))
    }

    override suspend fun applyHeal(combatantId: Long, amount: Int) {
        val current = getCombatState().first()
        val combatant = current.combatants.find { it.id == combatantId } ?: return
        updateCombatant(combatant.applyHeal(amount))
    }

    override suspend fun rollInitiativeForAll() {
        val current = getCombatState().first()
        for (c in current.combatants) {
            val d20Roll = random.nextInt(1, 21)
            val totalInitiative = d20Roll + c.initiativeModifier
            updateCombatant(c.copy(initiative = totalInitiative))
        }
    }

    suspend fun seedDefaultsIfEmpty() {
        if (combatDao.countCombatants() == 0) {
            val defaultCombatants = listOf(
                Combatant(name = "🛡️ Valeros (Guerreiro)", initiative = 18, initiativeModifier = 2, currentHp = 28, maxHp = 28, isPlayer = true),
                Combatant(name = "🔮 Ezren (Mago)", initiative = 15, initiativeModifier = 1, currentHp = 18, maxHp = 18, isPlayer = true),
                Combatant(name = "🏹 Goblin Arqueiro 1", initiative = 12, initiativeModifier = 2, currentHp = 7, maxHp = 7, isPlayer = false),
                Combatant(name = "🗡️ Goblin Espadachim 2", initiative = 9, initiativeModifier = 2, currentHp = 9, maxHp = 9, isPlayer = false)
            )
            for (c in defaultCombatants) {
                addCombatant(c)
            }
        }
    }
}
