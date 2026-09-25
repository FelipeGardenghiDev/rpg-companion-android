package com.grimorio.rpg.data.repository

import com.grimorio.rpg.data.local.dao.CharacterDao
import com.grimorio.rpg.data.local.entity.CharacterEntity
import com.grimorio.rpg.domain.model.CharacterCondition
import com.grimorio.rpg.domain.model.CharacterHealth
import com.grimorio.rpg.domain.model.CharacterProfile
import com.grimorio.rpg.domain.model.ConditionType
import com.grimorio.rpg.domain.model.SpellSlot
import com.grimorio.rpg.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CharacterRepositoryImpl(
    private val characterDao: CharacterDao,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : CharacterRepository {

    override fun getCharacter(): Flow<CharacterProfile> {
        return characterDao.getCharacter().map { entity ->
            if (entity == null) {
                CharacterProfile()
            } else {
                val spellSlots = runCatching {
                    json.decodeFromString<List<SpellSlot>>(entity.spellSlotsJson)
                }.getOrDefault(emptyList())

                val conditions = runCatching {
                    json.decodeFromString<List<CharacterCondition>>(entity.activeConditionsJson)
                }.getOrDefault(emptyList())

                CharacterProfile(
                    id = entity.id,
                    name = entity.name,
                    characterClass = entity.characterClass,
                    health = CharacterHealth(
                        currentHp = entity.currentHp,
                        maxHp = entity.maxHp,
                        tempHp = entity.tempHp
                    ),
                    spellSlots = spellSlots,
                    activeConditions = conditions,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override suspend fun saveCharacter(profile: CharacterProfile) {
        val entity = CharacterEntity(
            id = profile.id,
            name = profile.name,
            characterClass = profile.characterClass,
            currentHp = profile.health.currentHp,
            maxHp = profile.health.maxHp,
            tempHp = profile.health.tempHp,
            spellSlotsJson = json.encodeToString(profile.spellSlots),
            activeConditionsJson = json.encodeToString(profile.activeConditions),
            updatedAt = System.currentTimeMillis()
        )
        characterDao.upsertCharacter(entity)
    }

    override suspend fun applyDamage(amount: Int) {
        val current = getCharacter().first()
        val updatedHealth = current.health.applyDamage(amount)
        saveCharacter(current.copy(health = updatedHealth))
    }

    override suspend fun applyHeal(amount: Int) {
        val current = getCharacter().first()
        val updatedHealth = current.health.applyHeal(amount)
        saveCharacter(current.copy(health = updatedHealth))
    }

    override suspend fun setTempHp(amount: Int) {
        val current = getCharacter().first()
        val updatedHealth = current.health.setTemp(amount)
        saveCharacter(current.copy(health = updatedHealth))
    }

    override suspend fun updateMaxHp(newMax: Int) {
        val current = getCharacter().first()
        val updatedHealth = current.health.updateMaxHp(newMax)
        saveCharacter(current.copy(health = updatedHealth))
    }

    override suspend fun useSpellSlot(level: Int) {
        val current = getCharacter().first()
        val updatedSlots = current.spellSlots.map { slot ->
            if (slot.level == level) slot.use() else slot
        }
        saveCharacter(current.copy(spellSlots = updatedSlots))
    }

    override suspend fun recoverSpellSlot(level: Int) {
        val current = getCharacter().first()
        val updatedSlots = current.spellSlots.map { slot ->
            if (slot.level == level) slot.recover() else slot
        }
        saveCharacter(current.copy(spellSlots = updatedSlots))
    }

    override suspend fun shortRest() {
        val current = getCharacter().first()
        // No descanso curto restaura slots de bruxo/habilidades ou pode ser customizado
        saveCharacter(current)
    }

    override suspend fun longRest() {
        val current = getCharacter().first()
        val restoredHealth = current.health.copy(
            currentHp = current.health.maxHp,
            tempHp = 0
        )
        val restoredSlots = current.spellSlots.map { it.restoreAll() }
        saveCharacter(current.copy(health = restoredHealth, spellSlots = restoredSlots))
    }

    override suspend fun toggleCondition(conditionType: ConditionType, durationTurns: Int?) {
        val current = getCharacter().first()
        val exists = current.activeConditions.any { it.type == conditionType }
        val updatedConditions = if (exists) {
            current.activeConditions.filterNot { it.type == conditionType }
        } else {
            current.activeConditions + CharacterCondition(conditionType, durationTurns)
        }
        saveCharacter(current.copy(activeConditions = updatedConditions))
    }

    override suspend fun removeCondition(conditionType: ConditionType) {
        val current = getCharacter().first()
        val updatedConditions = current.activeConditions.filterNot { it.type == conditionType }
        saveCharacter(current.copy(activeConditions = updatedConditions))
    }

    suspend fun seedDefaultsIfEmpty() {
        if (characterDao.countCharacters() == 0) {
            saveCharacter(CharacterProfile())
        }
    }
}
