package com.grimorio.rpg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grimorio.rpg.data.local.entity.CombatSessionEntity
import com.grimorio.rpg.data.local.entity.CombatantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CombatDao {

    @Query("SELECT * FROM combatants ORDER BY initiative DESC, initiative_modifier DESC, id ASC")
    fun getAllCombatants(): Flow<List<CombatantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombatant(combatant: CombatantEntity): Long

    @Update
    suspend fun updateCombatant(combatant: CombatantEntity)

    @Query("DELETE FROM combatants WHERE id = :id")
    suspend fun deleteCombatant(id: Long)

    @Query("DELETE FROM combatants")
    suspend fun clearAllCombatants()

    @Query("SELECT COUNT(*) FROM combatants")
    suspend fun countCombatants(): Int

    @Query("SELECT * FROM combat_session WHERE id = 1 LIMIT 1")
    fun getSession(): Flow<CombatSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: CombatSessionEntity)
}
