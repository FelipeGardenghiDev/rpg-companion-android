package com.grimorio.rpg.presentation.dice

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.core.audio.SoundManager
import com.grimorio.rpg.core.haptic.HapticManager
import com.grimorio.rpg.data.repository.DiceRepositoryImpl
import com.grimorio.rpg.data.repository.FakeMacroDao
import com.grimorio.rpg.data.repository.FakeRollHistoryDao
import com.grimorio.rpg.data.repository.MacroRepositoryImpl
import com.grimorio.rpg.domain.model.DiceMacro
import com.grimorio.rpg.domain.model.DiceType
import com.grimorio.rpg.domain.model.MacroCategory
import com.grimorio.rpg.domain.parser.DiceExpressionParser
import com.grimorio.rpg.domain.usecase.RollDiceUseCase
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
import kotlin.random.Random

class FakeHapticManager : HapticManager {
    var rollCount = 0
    var critHitCount = 0
    var critFailCount = 0
    var clickCount = 0

    override fun vibrateRoll() { rollCount++ }
    override fun vibrateCriticalHit() { critHitCount++ }
    override fun vibrateCriticalFail() { critFailCount++ }
    override fun vibrateClick() { clickCount++ }
}

class FakeSoundManager : SoundManager {
    var rollCount = 0
    var critHitCount = 0
    var critFailCount = 0
    var clickCount = 0
    var isReleased = false

    override fun playRollSound() { rollCount++ }
    override fun playCriticalHitSound() { critHitCount++ }
    override fun playCriticalFailSound() { critFailCount++ }
    override fun playClickSound() { clickCount++ }
    override fun release() { isReleased = true }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class MockRandom(private val fixedValues: List<Int>) : Random() {
        private var idx = 0
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextInt(from: Int, until: Int): Int {
            val v = fixedValues[idx % fixedValues.size]
            idx++
            return v
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addDie monta formula corretamente e aciona feedback de clique`() {
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()
        val viewModel = DiceViewModel(hapticManager = fakeHaptic, soundManager = fakeSound)

        viewModel.addDie(DiceType.D20)
        assertThat(viewModel.uiState.value.formula).isEqualTo("1d20")
        assertThat(fakeHaptic.clickCount).isEqualTo(1)
        assertThat(fakeSound.clickCount).isEqualTo(1)

        viewModel.addDie(DiceType.D6)
        assertThat(viewModel.uiState.value.formula).isEqualTo("1d20 + 1d6")
        assertThat(fakeHaptic.clickCount).isEqualTo(2)
        assertThat(fakeSound.clickCount).isEqualTo(2)
    }

    @Test
    fun `ajuste de modificador reflete na formula`() {
        val viewModel = DiceViewModel()

        viewModel.addDie(DiceType.D20)
        viewModel.adjustModifier(3)

        assertThat(viewModel.uiState.value.formula).isEqualTo("1d20 + 3")
        assertThat(viewModel.uiState.value.modifier).isEqualTo(3)

        viewModel.adjustModifier(-5)
        assertThat(viewModel.uiState.value.formula).isEqualTo("1d20 - 2")
        assertThat(viewModel.uiState.value.modifier).isEqualTo(-2)
    }

    @Test
    fun `ativar vantagem formata d20 como 2d20kh1`() {
        val viewModel = DiceViewModel()

        viewModel.addDie(DiceType.D20)
        viewModel.setAdvantageMode(AdvantageMode.ADVANTAGE)

        assertThat(viewModel.uiState.value.formula).isEqualTo("2d20kh1")
    }

    @Test
    fun `ativar desvantagem formata d20 como 2d20kl1`() {
        val viewModel = DiceViewModel()

        viewModel.addDie(DiceType.D20)
        viewModel.setAdvantageMode(AdvantageMode.DISADVANTAGE)

        assertThat(viewModel.uiState.value.formula).isEqualTo("2d20kl1")
    }

    @Test
    fun `rollDice normal aciona som e vibracao normais`() = runTest {
        val mockRandom = MockRandom(listOf(14))
        val parser = DiceExpressionParser(random = mockRandom)
        val useCase = RollDiceUseCase(parser = parser)
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()

        val viewModel = DiceViewModel(
            rollDiceUseCase = useCase,
            hapticManager = fakeHaptic,
            soundManager = fakeSound
        )

        viewModel.addDie(DiceType.D20)
        viewModel.rollDice()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.lastResult?.total).isEqualTo(14)
        assertThat(fakeHaptic.rollCount).isEqualTo(1)
        assertThat(fakeSound.rollCount).isEqualTo(1)
        assertThat(fakeHaptic.critHitCount).isEqualTo(0)
    }

    @Test
    fun `quickRoll de acerto critico aciona feedback triunfante`() = runTest {
        val mockRandom = MockRandom(listOf(20))
        val parser = DiceExpressionParser(random = mockRandom)
        val useCase = RollDiceUseCase(parser = parser)
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()

        val viewModel = DiceViewModel(
            rollDiceUseCase = useCase,
            hapticManager = fakeHaptic,
            soundManager = fakeSound
        )

        viewModel.quickRoll(DiceType.D20)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.lastResult?.isCriticalHit).isTrue()
        assertThat(fakeHaptic.critHitCount).isEqualTo(1)
        assertThat(fakeSound.critHitCount).isEqualTo(1)
    }

    @Test
    fun `quickRoll de falha critica aciona feedback sombrio`() = runTest {
        val mockRandom = MockRandom(listOf(1))
        val parser = DiceExpressionParser(random = mockRandom)
        val useCase = RollDiceUseCase(parser = parser)
        val fakeHaptic = FakeHapticManager()
        val fakeSound = FakeSoundManager()

        val viewModel = DiceViewModel(
            rollDiceUseCase = useCase,
            hapticManager = fakeHaptic,
            soundManager = fakeSound
        )

        viewModel.quickRoll(DiceType.D20)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.lastResult?.isCriticalFail).isTrue()
        assertThat(fakeHaptic.critFailCount).isEqualTo(1)
        assertThat(fakeSound.critFailCount).isEqualTo(1)
    }

    @Test
    fun `rollMacro executa formula da macro e atualiza lastRolledMacroName`() = runTest {
        val mockRandom = MockRandom(listOf(16))
        val parser = DiceExpressionParser(random = mockRandom)
        val useCase = RollDiceUseCase(parser = parser)

        val fakeMacroDao = FakeMacroDao()
        val macroRepo = MacroRepositoryImpl(macroDao = fakeMacroDao)

        val viewModel = DiceViewModel(
            rollDiceUseCase = useCase,
            macroRepository = macroRepo
        )

        advanceUntilIdle()

        val macro = DiceMacro(
            id = 1L,
            name = "Espada Vorpal",
            formula = "1d20 + 4",
            category = MacroCategory.ATTACK
        )

        viewModel.rollMacro(macro)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.lastResult).isNotNull()
        assertThat(state.lastResult?.total).isEqualTo(20) // 16 + 4
        assertThat(state.lastRolledMacroName).isEqualTo("⚔️ Espada Vorpal")
    }

    @Test
    fun `salvar e excluir macro reflete no estado de macros do ViewModel`() = runTest {
        val fakeMacroDao = FakeMacroDao()
        val macroRepo = MacroRepositoryImpl(macroDao = fakeMacroDao)

        val viewModel = DiceViewModel(macroRepository = macroRepo)
        advanceUntilIdle()

        // Como o repositório semeia 4 defaults
        val initialCount = viewModel.uiState.value.macros.size
        assertThat(initialCount).isEqualTo(4)

        viewModel.saveMacro(
            name = "Ataque Furtivo",
            formula = "1d20 + 7",
            category = MacroCategory.SKILL
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.macros).hasSize(initialCount + 1)
        val newMacro = viewModel.uiState.value.macros.last()
        assertThat(newMacro.name).isEqualTo("Ataque Furtivo")

        viewModel.deleteMacro(newMacro.id)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.macros).hasSize(initialCount)
    }

    @Test
    fun `clear reseta formula e dados selecionados`() {
        val viewModel = DiceViewModel()

        viewModel.addDie(DiceType.D8)
        viewModel.adjustModifier(2)
        viewModel.clear()

        val state = viewModel.uiState.value
        assertThat(state.formula).isEmpty()
        assertThat(state.modifier).isEqualTo(0)
        assertThat(state.selectedDice).isEmpty()
    }
}
