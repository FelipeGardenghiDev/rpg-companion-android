package com.grimorio.rpg.domain.parser

import com.grimorio.rpg.domain.model.DiceGroupResult
import com.grimorio.rpg.domain.model.RollResult
import kotlin.random.Random

/**
 * Parser e Avaliador de expressões de dados de RPG em Kotlin puro.
 * Suporta:
 * - Rolagem básica: 1d20, 3d6, d8
 * - Modificadores: 1d20 + 5, 2d6 - 2 + 1d4
 * - Vantagem/Desvantagem (Keep Highest / Lowest): 2d20kh1, 2d20kl1, 4d6kh3
 * - Dados Explosivos: 3d6!, 1d10!
 */
class DiceExpressionParser(
    private val random: Random = Random.Default
) {
    // Regex para identificar termos: dados (com kh/kl ou !) ou modificadores numéricos fixos (+5, -3)
    private val tokenRegex = Regex("""([+-]?\s*\d*d\d+(?:(?:kh|kl)\d+|!)?|[+-]?\s*\d+)""", RegexOption.IGNORE_CASE)
    private val dicePattern = Regex("""^([+-]?)\s*(\d*)d(\d+)(?:(kh|kl)(\d+)|(!))?$""", RegexOption.IGNORE_CASE)

    fun parseAndRoll(rawFormula: String): RollResult {
        val sanitized = rawFormula.trim()
        if (sanitized.isEmpty()) {
            return RollResult(formula = "", total = 0, groups = emptyList(), modifier = 0)
        }

        val matches = tokenRegex.findAll(sanitized).map { it.value.replace(" ", "") }.toList()
        if (matches.isEmpty()) {
            throw IllegalArgumentException("Fórmula inválida: $rawFormula")
        }

        val groups = mutableListOf<DiceGroupResult>()
        var totalSum = 0
        var totalModifier = 0
        var hasD20 = false
        var d20KeptResult: Int? = null

        for (token in matches) {
            val diceMatch = dicePattern.find(token)
            if (diceMatch != null) {
                // É um grupo de dados (ex: "2d6", "+1d20kh1", "-1d4!")
                val sign = if (diceMatch.groupValues[1] == "-") -1 else 1
                val countStr = diceMatch.groupValues[2]
                val count = if (countStr.isEmpty()) 1 else countStr.toInt()
                val sides = diceMatch.groupValues[3].toInt()
                val keepType = diceMatch.groupValues[4].lowercase()
                val keepCountStr = diceMatch.groupValues[5]
                val isExploding = diceMatch.groupValues[6] == "!"

                val rolls = mutableListOf<Int>()
                for (i in 0 until count) {
                    var roll = rollSingle(sides)
                    rolls.add(roll)
                    if (isExploding) {
                        while (roll == sides) {
                            roll = rollSingle(sides)
                            rolls.add(roll)
                        }
                    }
                }

                val kept: List<Int>
                val dropped: List<Int>

                if (keepType == "kh" && keepCountStr.isNotEmpty()) {
                    val k = keepCountStr.toInt().coerceAtMost(rolls.size)
                    val sortedDesc = rolls.sortedDescending()
                    kept = sortedDesc.take(k)
                    dropped = sortedDesc.drop(k)
                } else if (keepType == "kl" && keepCountStr.isNotEmpty()) {
                    val k = keepCountStr.toInt().coerceAtMost(rolls.size)
                    val sortedAsc = rolls.sorted()
                    kept = sortedAsc.take(k)
                    dropped = sortedAsc.drop(k)
                } else {
                    kept = rolls
                    dropped = emptyList()
                }

                val groupSum = kept.sum() * sign
                totalSum += groupSum

                if (sides == 20) {
                    hasD20 = true
                    if (kept.size == 1) {
                        d20KeptResult = kept.first()
                    }
                }

                groups.add(
                    DiceGroupResult(
                        count = count,
                        sides = sides,
                        rolledValues = rolls,
                        keptValues = kept,
                        droppedValues = dropped,
                        isExploding = isExploding
                    )
                )
            } else {
                // É um modificador fixo (ex: "+5", "-2", "10")
                val modifier = token.toIntOrNull()
                    ?: throw IllegalArgumentException("Token não reconhecido: $token na fórmula: $rawFormula")
                totalModifier += modifier
                totalSum += modifier
            }
        }

        val isCriticalHit = hasD20 && d20KeptResult == 20
        val isCriticalFail = hasD20 && d20KeptResult == 1

        return RollResult(
            formula = sanitized,
            total = totalSum,
            groups = groups,
            modifier = totalModifier,
            isCriticalHit = isCriticalHit,
            isCriticalFail = isCriticalFail
        )
    }

    private fun rollSingle(sides: Int): Int {
        require(sides >= 1) { "O dado deve ter pelo menos 1 lado" }
        return random.nextInt(1, sides + 1)
    }
}
