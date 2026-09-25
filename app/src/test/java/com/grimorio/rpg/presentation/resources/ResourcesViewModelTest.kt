package com.grimorio.rpg.presentation.resources

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.repository.CharacterRepositoryImpl
import com.grimorio.rpg.data.repository.FakeCharacterDao
import com.grimorio.rpg.domain.model.CharacterHealth
import com.grimorio.rpg.domain.model.CharacterProfile
import com.grimorio.rpg.domain.model.ConditionType
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
class ResourcesViewModelTest {

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
    fun `applyDamage reduz vida e aciona feedback tátil e sonoro`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repo = CharacterRepositoryImpl(characterDao = fakeDao)
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()

        repo.saveCharacter(
            CharacterProfile(health = CharacterHealth(currentHp = 20, maxHp = 20))
        )

        val viewModel = ResourcesViewModel(
            characterRepository = repo,
            hapticManager = fakeHaptic,
            soundManager = fakeSound
        )
        advanceUntilIdle()

        viewModel.applyDamage(5)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.character.health.currentHp).isEqualTo(15)
        assertThat(fakeHaptic.clickCount).isEqualTo(1)
        assertThat(fakeSound.clickCount).isEqualTo(1)
    }

    @Test
    fun `confirmLongRest restaura vida e aciona som triunfante`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repo = CharacterRepositoryImpl(characterDao = fakeDao)
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()

        repo.saveCharacter(
            CharacterProfile(health = CharacterHealth(currentHp = 4, maxHp = 30))
        )

        val viewModel = ResourcesViewModel(
            characterRepository = repo,
            hapticManager = fakeHaptic,
            soundManager = fakeSound
        )
        advanceUntilIdle()

        viewModel.confirmLongRest()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.character.health.currentHp).isEqualTo(30)
        assertThat(fakeHaptic.critHitCount).isEqualTo(1)
        assertThat(fakeSound.critHitCount).isEqualTo(1)
    }

    @Test
    fun `toggleCondition alterna presenca da condicao no perfil`() = runTest {
        val fakeDao = FakeCharacterDao()
        val repo = CharacterRepositoryImpl(characterDao = fakeDao)

        repo.saveCharacter(CharacterProfile())

        val viewModel = ResourcesViewModel(characterRepository = repo)
        advanceUntilIdle()

        viewModel.toggleCondition(ConditionType.STUNNED)
        advanceUntilIdle()

        val active = viewModel.uiState.value.character.activeConditions
        assertThat(active).hasSize(1)
        assertThat(active[0].type).isEqualTo(ConditionType.STUNNED)
    }
}
