package com.grimorio.rpg.domain.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class DiceExpressionParserTest {

    // Teste com gerador de números determinístico simulado
    private class MockRandom(private val predefinedValues: List<Int>) : Random() {
        private var index = 0
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextInt(from: Int, until: Int): Int {
            val value = predefinedValues[index % predefinedValues.size]
            index++
            return value
        }
    }

    @Test
    fun `parseAndRoll calcula soma simples de dados e modificador corretamente`() {
        // Simula tirar 4 e 5 no 2d6
        val mockRandom = MockRandom(listOf(4, 5))
        val parser = DiceExpressionParser(random = mockRandom)

        val result = parser.parseAndRoll("2d6 + 3")

        assertThat(result.total).isEqualTo(12) // 4 + 5 + 3
        assertThat(result.groups).hasSize(1)
        assertThat(result.groups[0].keptValues).containsExactly(4, 5)
        assertThat(result.modifier).isEqualTo(3)
        assertThat(result.isCriticalHit).isFalse()
    }

    @Test
    fun `parseAndRoll com Keep Highest (Vantagem) escolhe o maior valor`() {
        // Simula rolar 8 e 19 no 2d20
        val mockRandom = MockRandom(listOf(8, 19))
        val parser = DiceExpressionParser(random = mockRandom)

        val result = parser.parseAndRoll("2d20kh1 + 5")

        assertThat(result.total).isEqualTo(24) // 19 + 5
        assertThat(result.groups[0].keptValues).containsExactly(19)
        assertThat(result.groups[0].droppedValues).containsExactly(8)
    }

    @Test
    fun `parseAndRoll com Keep Lowest (Desvantagem) escolhe o menor valor`() {
        // Simula rolar 18 e 3 no 2d20
        val mockRandom = MockRandom(listOf(18, 3))
        val parser = DiceExpressionParser(random = mockRandom)

        val result = parser.parseAndRoll("2d20kl1 + 2")

        assertThat(result.total).isEqualTo(5) // 3 + 2
        assertThat(result.groups[0].keptValues).containsExactly(3)
        assertThat(result.groups[0].droppedValues).containsExactly(18)
    }

    @Test
    fun `parseAndRoll detecta Acerto Critico no d20`() {
        val mockRandom = MockRandom(listOf(20))
        val parser = DiceExpressionParser(random = mockRandom)

        val result = parser.parseAndRoll("1d20 + 7")

        assertThat(result.total).isEqualTo(27)
        assertThat(result.isCriticalHit).isTrue()
        assertThat(result.isCriticalFail).isFalse()
    }

    @Test
    fun `parseAndRoll detecta Falha Critica no d20`() {
        val mockRandom = MockRandom(listOf(1))
        val parser = DiceExpressionParser(random = mockRandom)

        val result = parser.parseAndRoll("1d20 + 4")

        assertThat(result.total).isEqualTo(5)
        assertThat(result.isCriticalHit).isFalse()
        assertThat(result.isCriticalFail).isTrue()
    }

    @Test
    fun `parseAndRoll calcula 4d6kh3 de criacao de atributos mantendo os 3 maiores`() {
        // Simula rolar 2, 5, 4, 6
        val mockRandom = MockRandom(listOf(2, 5, 4, 6))
        val parser = DiceExpressionParser(random = mockRandom)

        val result = parser.parseAndRoll("4d6kh3")

        assertThat(result.total).isEqualTo(15) // 6 + 5 + 4 = 15
        assertThat(result.groups[0].keptValues).containsExactly(6, 5, 4)
        assertThat(result.groups[0].droppedValues).containsExactly(2)
    }
}
