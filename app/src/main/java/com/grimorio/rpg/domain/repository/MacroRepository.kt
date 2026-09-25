package com.grimorio.rpg.domain.repository

import com.grimorio.rpg.domain.model.DiceMacro
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistência e gerenciamento de macros e atalhos de dados.
 */
interface MacroRepository {
    fun getAllMacros(): Flow<List<DiceMacro>>
    suspend fun saveMacro(macro: DiceMacro): Long
    suspend fun deleteMacro(id: Long)
}
