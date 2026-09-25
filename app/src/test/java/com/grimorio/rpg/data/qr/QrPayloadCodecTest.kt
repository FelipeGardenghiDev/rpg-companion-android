package com.grimorio.rpg.data.qr

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.domain.model.CharacterHealth
import com.grimorio.rpg.domain.model.CharacterProfile
import com.grimorio.rpg.domain.model.Combatant
import com.grimorio.rpg.domain.model.DiceMacro
import com.grimorio.rpg.domain.model.MacroCategory
import com.grimorio.rpg.domain.model.SharePayloadType
import com.grimorio.rpg.domain.model.ShareablePayload
import com.grimorio.rpg.domain.model.SpellSlot
import org.junit.Test

class QrPayloadCodecTest {

    @Test
    fun `encode e decode de CharacterProfile com compressao GZIP preserva todos os dados`() {
        val character = CharacterProfile(
            name = "Thorgar, o Bárbaro",
            characterClass = "Bárbaro Nível 5",
            health = CharacterHealth(currentHp = 45, maxHp = 52, tempHp = 8),
            spellSlots = listOf(SpellSlot(level = 1, totalSlots = 2, usedSlots = 1))
        )

        val payload = ShareablePayload(
            type = SharePayloadType.CHARACTER,
            title = "Ficha: ${character.name}",
            character = character
        )

        val encoded = QrPayloadCodec.encode(payload, compress = true)
        assertThat(encoded).startsWith("GRIMORIO_GZ:")

        val decodeResult = QrPayloadCodec.decode(encoded)
        assertThat(decodeResult.isSuccess).isTrue()

        val decodedPayload = decodeResult.getOrThrow()
        assertThat(decodedPayload.type).isEqualTo(SharePayloadType.CHARACTER)
        assertThat(decodedPayload.title).isEqualTo("Ficha: Thorgar, o Bárbaro")
        assertThat(decodedPayload.character).isNotNull()
        assertThat(decodedPayload.character?.name).isEqualTo("Thorgar, o Bárbaro")
        assertThat(decodedPayload.character?.health?.currentHp).isEqualTo(45)
        assertThat(decodedPayload.character?.health?.tempHp).isEqualTo(8)
    }

    @Test
    fun `encode e decode de Encontro de Combate mantem lista de combatentes`() {
        val combatants = listOf(
            Combatant(id = 1, name = "Lich Ancestral", initiative = 21, currentHp = 135, maxHp = 135, isPlayer = false),
            Combatant(id = 2, name = "Guerreiro Sagrado", initiative = 18, currentHp = 60, maxHp = 60, isPlayer = true)
        )

        val payload = ShareablePayload(
            type = SharePayloadType.COMBAT_ENCOUNTER,
            title = "Encontro com 2 Combatentes",
            combatants = combatants
        )

        val encoded = QrPayloadCodec.encode(payload, compress = true)
        val decoded = QrPayloadCodec.decode(encoded).getOrThrow()

        assertThat(decoded.type).isEqualTo(SharePayloadType.COMBAT_ENCOUNTER)
        assertThat(decoded.combatants).hasSize(2)
        assertThat(decoded.combatants?.first()?.name).isEqualTo("Lich Ancestral")
    }

    @Test
    fun `encode e decode de Macro de dados`() {
        val macro = DiceMacro(
            name = "Espada Sagrada do Sol",
            formula = "1d20 + 9",
            category = MacroCategory.ATTACK
        )

        val payload = ShareablePayload(
            type = SharePayloadType.MACRO,
            title = "Atalho: ${macro.name}",
            macro = macro
        )

        val encoded = QrPayloadCodec.encode(payload, compress = true)
        val decoded = QrPayloadCodec.decode(encoded).getOrThrow()

        assertThat(decoded.type).isEqualTo(SharePayloadType.MACRO)
        assertThat(decoded.macro?.name).isEqualTo("Espada Sagrada do Sol")
        assertThat(decoded.macro?.formula).isEqualTo("1d20 + 9")
    }

    @Test
    fun `decode de string invalida retorna Failure`() {
        val invalid = "Texto_Sem_Formato_Grimorio"
        val result = QrPayloadCodec.decode(invalid)

        assertThat(result.isFailure).isTrue()
    }
}
