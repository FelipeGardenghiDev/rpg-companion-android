package com.grimorio.rpg.presentation.combat

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.repository.CombatRepositoryImpl
import com.grimorio.rpg.data.repository.FakeCombatDao
import com.grimorio.rpg.domain.model.Combatant
import com.grimorio.rpg.presentation.dice.FakeHapticManager
import com.grimorio.rpg.presentation.dice.FakeSoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CombatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `nextTurn avanca o combatente e aciona som e haptico`() = runTest {
        val fakeDao = FakeCombatDao()
        val repo = CombatRepositoryImpl(combatDao = fakeDao)
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()

        repo.addCombatant(Combatant(name = "A", initiative = 20))
        repo.addCombatant(Combatant(name = "B", initiative = 10))

        val viewModel = CombatViewModel(
            combatRepository = repo,
            hapticManager = fakeHaptic,
            soundManager = fakeSound
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.combatState.currentTurnIndex).isEqualTo(0)

        viewModel.nextTurn()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.combatState.currentTurnIndex).isEqualTo(1)
        assertThat(fakeHaptic.clickCount).isEqualTo(1)
        assertThat(fakeSound.clickCount).isEqualTo(1)
    }

    @Test
    fun `addCombatant e removeCombatant atualizam a lista de combate`() = runTest {
        val fakeDao = FakeCombatDao()
        val repo = CombatRepositoryImpl(combatDao = fakeDao)

        val viewModel = CombatViewModel(combatRepository = repo)
        advanceUntilIdle()

        // Como o repositório semeia 4 combatentes padrão
        val initialCount = viewModel.uiState.value.combatState.combatants.size
        assertThat(initialCount).isEqualTo(4)

        viewModel.addCombatant(
            name = "Dragão Negro",
            initiative = 22,
            modifier = 3,
            maxHp = 120,
            isPlayer = false
        )
        advanceUntilIdle()

        val updatedList = viewModel.uiState.value.combatState.combatants
        assertThat(updatedList).hasSize(initialCount + 1)
        assertThat(updatedList.first().name).isEqualTo("Dragão Negro") // maior iniciativa vai pro topo

        viewModel.removeCombatant(updatedList.first().id)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.combatState.combatants).hasSize(initialCount)
    }
}
