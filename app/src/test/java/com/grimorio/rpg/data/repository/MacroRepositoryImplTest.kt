package com.grimorio.rpg.data.repository

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.local.dao.MacroDao
import com.grimorio.rpg.data.local.entity.MacroEntity
import com.grimorio.rpg.domain.model.DiceMacro
import com.grimorio.rpg.domain.model.MacroCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeMacroDao : MacroDao {
    private val storage = mutableListOf<MacroEntity>()
    private val flow = MutableStateFlow<List<MacroEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllMacros(): Flow<List<MacroEntity>> = flow

    override suspend fun insertMacro(macro: MacroEntity): Long {
        val assignedId = if (macro.id == 0L) nextId++ else macro.id
        val newEntity = macro.copy(id = assignedId)
        storage.removeAll { it.id == assignedId }
        storage.add(newEntity)
        flow.value = storage.toList()
        return assignedId
    }

    override suspend fun deleteMacro(id: Long) {
        storage.removeAll { it.id == id }
        flow.value = storage.toList()
    }

    override suspend fun countMacros(): Int = storage.size
}

class MacroRepositoryImplTest {

    @Test
    fun `saveMacro persiste nova macro e emite no fluxo`() = runTest {
        val fakeDao = FakeMacroDao()
        val repository = MacroRepositoryImpl(macroDao = fakeDao)

        val macro = DiceMacro(
            name = "Espada Vorpal",
            formula = "1d20 + 8",
            category = MacroCategory.ATTACK
        )

        repository.saveMacro(macro)

        val list = repository.getAllMacros().first()
        assertThat(list).hasSize(1)
        assertThat(list[0].name).isEqualTo("Espada Vorpal")
        assertThat(list[0].formula).isEqualTo("1d20 + 8")
        assertThat(list[0].category).isEqualTo(MacroCategory.ATTACK)
    }

    @Test
    fun `deleteMacro remove a macro do fluxo`() = runTest {
        val fakeDao = FakeMacroDao()
        val repository = MacroRepositoryImpl(macroDao = fakeDao)

        val id = repository.saveMacro(
            DiceMacro(name = "Fogo", formula = "8d6", category = MacroCategory.SPELL)
        )

        assertThat(repository.getAllMacros().first()).hasSize(1)

        repository.deleteMacro(id)

        assertThat(repository.getAllMacros().first()).isEmpty()
    }

    @Test
    fun `seedDefaultsIfEmpty insere atalhos iniciais quando banco esta vazio`() = runTest {
        val fakeDao = FakeMacroDao()
        val repository = MacroRepositoryImpl(macroDao = fakeDao)

        repository.seedDefaultsIfEmpty()

        val list = repository.getAllMacros().first()
        assertThat(list).hasSize(4)
        assertThat(list.map { it.name }).contains("Ataque com Espada")
        assertThat(list.map { it.name }).contains("Bola de Fogo")
    }
}
