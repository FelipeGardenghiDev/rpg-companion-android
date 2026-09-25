package com.grimorio.rpg.data.repository

import com.grimorio.rpg.data.local.dao.MacroDao
import com.grimorio.rpg.data.local.entity.MacroEntity
import com.grimorio.rpg.domain.model.DiceMacro
import com.grimorio.rpg.domain.model.MacroCategory
import com.grimorio.rpg.domain.repository.MacroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MacroRepositoryImpl(
    private val macroDao: MacroDao
) : MacroRepository {

    override fun getAllMacros(): Flow<List<DiceMacro>> {
        return macroDao.getAllMacros().map { entities ->
            entities.map { entity ->
                val category = runCatching {
                    MacroCategory.valueOf(entity.category)
                }.getOrDefault(MacroCategory.CUSTOM)

                DiceMacro(
                    id = entity.id,
                    name = entity.name,
                    formula = entity.formula,
                    category = category,
                    colorHex = entity.colorHex,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun saveMacro(macro: DiceMacro): Long {
        val entity = MacroEntity(
            id = macro.id,
            name = macro.name,
            formula = macro.formula,
            category = macro.category.name,
            colorHex = macro.colorHex,
            createdAt = macro.createdAt
        )
        return macroDao.insertMacro(entity)
    }

    override suspend fun deleteMacro(id: Long) {
        macroDao.deleteMacro(id)
    }

    suspend fun seedDefaultsIfEmpty() {
        if (macroDao.countMacros() == 0) {
            val defaults = listOf(
                DiceMacro(name = "Ataque com Espada", formula = "1d20 + 5", category = MacroCategory.ATTACK),
                DiceMacro(name = "Dano de Espada", formula = "1d8 + 3", category = MacroCategory.DAMAGE),
                DiceMacro(name = "Bola de Fogo", formula = "8d6", category = MacroCategory.SPELL),
                DiceMacro(name = "Iniciativa", formula = "1d20 + 2", category = MacroCategory.SKILL)
            )
            for (macro in defaults) {
                saveMacro(macro)
            }
        }
    }
}
