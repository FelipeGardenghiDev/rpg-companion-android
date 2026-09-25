package com.grimorio.rpg.data.repository

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.local.dao.CombatDao
import com.grimorio.rpg.data.local.entity.CombatSessionEntity
import com.grimorio.rpg.data.local.entity.CombatantEntity
import com.grimorio.rpg.domain.model.Combatant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeCombatDao : CombatDao {
    private val combatants = mutableListOf<CombatantEntity>()
    private val combatantsFlow = MutableStateFlow<List<CombatantEntity>>(emptyList())
    private var session: CombatSessionEntity? = null
    private val sessionFlow = MutableStateFlow<CombatSessionEntity?>(null)
    private var nextId = 1L

    override fun getAllCombatants(): Flow<List<CombatantEntity>> = combatantsFlow

    override suspend fun insertCombatant(combatant: CombatantEntity): Long {
        val assignedId = if (combatant.id == 0L) nextId++ else combatant.id
        val newEntity = combatant.copy(id = assignedId)
        combatants.removeAll { it.id == assignedId }
        combatants.add(newEntity)
        // Ordena por iniciativa decrescente
        combatants.sortWith(compareByDescending<CombatantEntity> { it.initiative }.thenByDescending { it.initiativeModifier })
        combatantsFlow.value = combatants.toList()
        return assignedId
    }

    override suspend fun updateCombatant(combatant: CombatantEntity) {
        val idx = combatants.indexOfFirst { it.id == combatant.id }
        if (idx >= 0) {
            combatants[idx] = combatant
            combatants.sortWith(compareByDescending<CombatantEntity> { it.initiative }.thenByDescending { it.initiativeModifier })
            combatantsFlow.value = combatants.toList()
        }
    }

    override suspend fun deleteCombatant(id: Long) {
        combatants.removeAll { it.id == id }
        combatantsFlow.value = combatants.toList()
    }

    override suspend fun clearAllCombatants() {
        combatants.clear()
        combatantsFlow.value = emptyList()
    }

    override suspend fun countCombatants(): Int = combatants.size

    override fun getSession(): Flow<CombatSessionEntity?> = sessionFlow

    override suspend fun upsertSession(session: CombatSessionEntity) {
        this.session = session
        sessionFlow.value = session
    }
}

class CombatRepositoryImplTest {

    @Test
    fun `addCombatant insere combatentes e mantem ordenacao por iniciativa decrescente`() = runTest {
        val fakeDao = FakeCombatDao()
        val repository = CombatRepositoryImpl(combatDao = fakeDao)

        repository.addCombatant(Combatant(name = "Goblin", initiative = 12))
        repository.addCombatant(Combatant(name = "Guerreiro", initiative = 19))
        repository.addCombatant(Combatant(name = "Mago", initiative = 15))

        val state = repository.getCombatState().first()
        assertThat(state.combatants).hasSize(3)
        assertThat(state.combatants.map { it.name }).containsExactly("Guerreiro", "Mago", "Goblin").inOrder()
    }

    @Test
    fun `nextTurn avanca o turno e incrementa a rodada ao completar ciclo`() = runTest {
        val fakeDao = FakeCombatDao()
        val repository = CombatRepositoryImpl(combatDao = fakeDao)

        repository.addCombatant(Combatant(name = "A", initiative = 20))
        repository.addCombatant(Combatant(name = "B", initiative = 10))

        // Inicia na Rodada 1, Turno 0 (A)
        var state = repository.getCombatState().first()
        assertThat(state.round).isEqualTo(1)
        assertThat(state.currentTurnIndex).isEqualTo(0)
        assertThat(state.currentCombatant?.name).isEqualTo("A")

        // Próximo turno -> Rodada 1, Turno 1 (B)
        repository.nextTurn()
        state = repository.getCombatState().first()
        assertThat(state.round).isEqualTo(1)
        assertThat(state.currentTurnIndex).isEqualTo(1)
        assertThat(state.currentCombatant?.name).isEqualTo("B")

        // Próximo turno -> Wrap-around -> Rodada 2, Turno 0 (A)
        repository.nextTurn()
        state = repository.getCombatState().first()
        assertThat(state.round).isEqualTo(2)
        assertThat(state.currentTurnIndex).isEqualTo(0)
        assertThat(state.currentCombatant?.name).isEqualTo("A")
    }

    @Test
    fun `applyDamage aplica dano e pode derrotar combatente`() = runTest {
        val fakeDao = FakeCombatDao()
        val repository = CombatRepositoryImpl(combatDao = fakeDao)

        val id = repository.addCombatant(Combatant(name = "Goblin", initiative = 10, currentHp = 8, maxHp = 8))

        repository.applyDamage(id, 5)
        var state = repository.getCombatState().first()
        assertThat(state.combatants[0].currentHp).isEqualTo(3)
        assertThat(state.combatants[0].isDefeated).isFalse()

        repository.applyDamage(id, 10)
        state = repository.getCombatState().first()
        assertThat(state.combatants[0].currentHp).isEqualTo(0)
        assertThat(state.combatants[0].isDefeated).isTrue()
    }
}
