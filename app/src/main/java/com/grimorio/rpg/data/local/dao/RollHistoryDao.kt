package com.grimorio.rpg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grimorio.rpg.data.local.entity.RollHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RollHistoryDao {

    @Query("SELECT * FROM roll_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRolls(limit: Int = 30): Flow<List<RollHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoll(roll: RollHistoryEntity): Long

    @Query("DELETE FROM roll_history")
    suspend fun clearAll()
}
