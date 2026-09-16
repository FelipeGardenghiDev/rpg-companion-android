package com.grimorio.rpg.domain.model

/**
 * Representa os tipos de dados poliédricos suportados pelo motor.
 */
enum class DiceType(val sides: Int, val displayName: String) {
    D4(4, "d4"),
    D6(6, "d6"),
    D8(8, "d8"),
    D10(10, "d10"),
    D12(12, "d12"),
    D20(20, "d20"),
    D100(100, "d100");

    companion object {
        fun fromSides(sides: Int): DiceType? {
            return entries.find { it.sides == sides }
        }
    }
}
