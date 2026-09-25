package com.grimorio.rpg.domain.model

import kotlinx.serialization.Serializable

/**
 * Resultado detalhado de uma rolagem para exibição e histórico.
 */
@Serializable
data class RollResult(
    val formula: String,
    val total: Int,
    val groups: List<DiceGroupResult>,
    val modifier: Int,
    val isCriticalHit: Boolean = false,
    val isCriticalFail: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Detalhes de um conjunto de dados dentro da expressão (ex: os 2d20 dentro de "2d20kh1 + 1d4 + 3").
 */
@Serializable
data class DiceGroupResult(
    val count: Int,
    val sides: Int,
    val rolledValues: List<Int>,
    val keptValues: List<Int>,
    val droppedValues: List<Int> = emptyList(),
    val isExploding: Boolean = false
)
