package com.grimorio.rpg.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MacroCategory(val displayName: String, val iconEmoji: String) {
    ATTACK("Ataque", "⚔️"),
    DAMAGE("Dano", "💥"),
    SPELL("Magia", "🔮"),
    SKILL("Perícia", "📜"),
    CUSTOM("Geral", "🎲")
}

@Serializable
data class DiceMacro(
    val id: Long = 0,
    val name: String,
    val formula: String,
    val category: MacroCategory = MacroCategory.CUSTOM,
    val colorHex: String = "#E5A93C",
    val createdAt: Long = System.currentTimeMillis()
)
