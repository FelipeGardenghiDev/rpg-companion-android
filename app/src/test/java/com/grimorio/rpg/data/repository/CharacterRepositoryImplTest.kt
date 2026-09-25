package com.grimorio.rpg.data.repository

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.local.dao.CharacterDao
import com.grimorio.rpg.data.local.entity.CharacterEntity
import com.grimorio.rpg.domain.model.CharacterHealth
import com.grimorio.rpg.domain.model.CharacterProfile
import com.grimorio.rpg.domain.model.ConditionType
import com.grimorio.rpg.domain.model.SpellSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeCharacterDao : CharacterDao {
    private var entity: CharacterEntity? = null
    private val flow = MutableStateFlow<CharacterEntity?>(null)

    override fun getCharacter(): Flow<CharacterEntity?> = flow

    override suspend fun upsertCharacter(entity: CharacterEntity) {
        this.entity = entity
        flow.value = entity
    }

    override suspend fun countMacros(): Int = if (entity != null) 1 else 0
    override suspend fun countCharacters(): Int = if (entity != null) 1 else 0
}

class CharacterRepositoryImplTest {

    @Test
    fun `applyDamage consome primeiro HP Temporario e depois HP Atual sem negativar`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(characterDao = fakeDao)

        repository.saveCharacter(
            CharacterProfile(
                health = CharacterHealth(currentHp = 20, maxHp = 30, tempHp = 5)
            )
        )

        // Dano de 8: 5 de Temp HP e 3 de Current HP -> sobra 17 HP e 0 Temp
        repository.applyDamage(8)

        val updated = repository.getCharacter().first()
        assertThat(updated.health.tempHp).isEqualTo(0)
        assertThat(updated.health.currentHp).isEqualTo(17)

        // Dano letal de 50: não deve ficar negativo, trava em 0
        repository.applyDamage(50)
        val lethal = repository.getCharacter().first()
        assertThat(lethal.health.currentHp).isEqualTo(0)
    }

    @Test
    fun `applyHeal cura ate o limite de HP Maximo`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(characterDao = fakeDao)

        repository.saveCharacter(
            CharacterProfile(
                health = CharacterHealth(currentHp = 10, maxHp = 25, tempHp = 0)
            )
        )

        repository.applyHeal(8)
        assertThat(repository.getCharacter().first().health.currentHp).isEqualTo(18)

        // Tenta curar além do máximo
        repository.applyHeal(20)
        assertThat(repository.getCharacter().first().health.currentHp).isEqualTo(25)
    }

    @Test
    fun `useSpellSlot e recoverSpellSlot manipulam contagem de slots corretamente`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(characterDao = fakeDao)

        repository.saveCharacter(
            CharacterProfile(
                spellSlots = listOf(SpellSlot(level = 1, totalSlots = 3, usedSlots = 0))
            )
        )

        repository.useSpellSlot(1)
        var slot = repository.getCharacter().first().spellSlots.first { it.level == 1 }
        assertThat(slot.availableSlots).isEqualTo(2)
        assertThat(slot.usedSlots).isEqualTo(1)

        repository.recoverSpellSlot(1)
        slot = repository.getCharacter().first().spellSlots.first { it.level == 1 }
        assertThat(slot.availableSlots).isEqualTo(3)
        assertThat(slot.usedSlots).isEqualTo(0)
    }

    @Test
    fun `toggleCondition adiciona e remove condicoes`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(characterDao = fakeDao)

        repository.saveCharacter(CharacterProfile())

        // Adiciona Envenenado
        repository.toggleCondition(ConditionType.POISONED)
        var conditions = repository.getCharacter().first().activeConditions
        assertThat(conditions).hasSize(1)
        assertThat(conditions[0].type).isEqualTo(ConditionType.POISONED)

        // Remove ao alternar de novo
        repository.toggleCondition(ConditionType.POISONED)
        conditions = repository.getCharacter().first().activeConditions
        assertThat(conditions).isEmpty()
    }

    @Test
    fun `longRest restaura HP total e recupera todos os spell slots`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repository = CharacterRepositoryImpl(characterDao = fakeDao)

        repository.saveCharacter(
            CharacterProfile(
                health = CharacterHealth(currentHp = 5, maxHp = 30, tempHp = 4),
                spellSlots = listOf(
                    SpellSlot(level = 1, totalSlots = 4, usedSlots = 3),
                    SpellSlot(level = 2, totalSlots = 2, usedSlots = 2)
                )
            )
        )

        repository.longRest()

        val afterRest = repository.getCharacter().first()
        assertThat(afterRest.health.currentHp).isEqualTo(30)
        assertThat(afterRest.health.tempHp).isEqualTo(0)
        assertThat(afterRest.spellSlots[0].availableSlots).isEqualTo(4)
        assertThat(afterRest.spellSlots[1].availableSlots).isEqualTo(2)
    }
}
