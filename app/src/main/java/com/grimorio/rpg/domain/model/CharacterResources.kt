package com.grimorio.rpg.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConditionType(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    BLINDED("Cego", "👁️‍🗨️", "Ataques contra a criatura têm vantagem; ataques da criatura têm desvantagem."),
    CHARMED("Enfeitiçado", "💖", "Não pode atacar quem a enfeitiçou. O encantador tem vantagem em testes sociais."),
    DEAFENED("Surdo", "👂", "Falha automática em qualquer teste de habilidade que exija audição."),
    FRIGHTENED("Amedrontado", "😱", "Desvantagem em testes de habilidade e ataque enquanto a fonte do medo estiver visível."),
    GRAPPLED("Agarrado", "✊", "Deslocamento reduzido a 0. Termina se o agarrador for incapacitado."),
    INCAPACITATED("Incapacitado", "💫", "Não pode realizar ações nem reações."),
    INVISIBLE("Invisível", "👻", "Impossível de ser visto sem magia. Ataques contra têm desvantagem; da criatura têm vantagem."),
    PARALYZED("Paralisado", "⚡", "Incapacitado e não pode se mover. Falha em testes de Força e Dex. Crítico automático a 1,5m."),
    PETRIFIED("Petrificado", "🗿", "Transformado em substância sólida. Peso decuplica, imune a veneno."),
    POISONED("Envenenado", "🧪", "Desvantagem em jogadas de ataque e testes de habilidade."),
    PRONE("Caído", "🧎", "Deslocamento reduzido pela metade. Ataques a 1,5m têm vantagem; à distância têm desvantagem."),
    RESTRAINED("Contido", "⛓️", "Deslocamento 0. Desvantagem em testes de Destreza e ataques."),
    STUNNED("Atordoado", "🌀", "Incapacitado, só balbucia. Falha automática em testes de Força e Destreza."),
    UNCONSCIOUS("Inconsciente", "💤", "Incapacitado, cai prostrado e larga o que estiver segurando. Crítico automático a 1,5m."),
    EXHAUSTION("Exaustão", "😫", "Nível cumulativo de fadiga corporal extrema.")
}

@Serializable
data class CharacterCondition(
    val type: ConditionType,
    val durationTurns: Int? = null
)

@Serializable
data class CharacterHealth(
    val currentHp: Int,
    val maxHp: Int,
    val tempHp: Int = 0
) {
    val percentage: Float
        get() = if (maxHp > 0) (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) else 0f

    fun applyDamage(amount: Int): CharacterHealth {
        val dmg = amount.coerceAtLeast(0)
        var remainingDmg = dmg
        var newTemp = tempHp

        if (newTemp > 0) {
            if (newTemp >= remainingDmg) {
                newTemp -= remainingDmg
                remainingDmg = 0
            } else {
                remainingDmg -= newTemp
                newTemp = 0
            }
        }

        val newCurrent = (currentHp - remainingDmg).coerceAtLeast(0)
        return copy(currentHp = newCurrent, tempHp = newTemp)
    }

    fun applyHeal(amount: Int): CharacterHealth {
        val heal = amount.coerceAtLeast(0)
        val newCurrent = (currentHp + heal).coerceAtMost(maxHp)
        return copy(currentHp = newCurrent)
    }

    fun setTemp(amount: Int): CharacterHealth {
        return copy(tempHp = amount.coerceAtLeast(0))
    }

    fun updateMaxHp(newMax: Int): CharacterHealth {
        val validMax = newMax.coerceAtLeast(1)
        val validCurrent = currentHp.coerceAtMost(validMax)
        return copy(maxHp = validMax, currentHp = validCurrent)
    }
}

@Serializable
data class SpellSlot(
    val level: Int,
    val totalSlots: Int,
    val usedSlots: Int = 0
) {
    val availableSlots: Int
        get() = (totalSlots - usedSlots).coerceAtLeast(0)

    fun use(): SpellSlot {
        return if (availableSlots > 0) copy(usedSlots = usedSlots + 1) else this
    }

    fun recover(): SpellSlot {
        return if (usedSlots > 0) copy(usedSlots = usedSlots - 1) else this
    }

    fun restoreAll(): SpellSlot {
        return copy(usedSlots = 0)
    }
}

@Serializable
data class CharacterProfile(
    val id: Long = 1L,
    val name: String = "Aventureiro",
    val characterClass: String = "Guerreiro Nível 3",
    val health: CharacterHealth = CharacterHealth(currentHp = 28, maxHp = 28, tempHp = 0),
    val spellSlots: List<SpellSlot> = listOf(
        SpellSlot(level = 1, totalSlots = 4, usedSlots = 0),
        SpellSlot(level = 2, totalSlots = 2, usedSlots = 0)
    ),
    val activeConditions: List<CharacterCondition> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)
