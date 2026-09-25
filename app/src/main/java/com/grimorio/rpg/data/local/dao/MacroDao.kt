package com.grimorio.rpg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grimorio.rpg.data.local.entity.MacroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {

    @Query("SELECT * FROM macros ORDER BY created_at ASC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity): Long

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacro(id: Long)

    @Query("SELECT COUNT(*) FROM macros")
    suspend fun countMacros(): Int
}
