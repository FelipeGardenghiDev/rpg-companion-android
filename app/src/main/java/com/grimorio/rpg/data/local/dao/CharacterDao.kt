package com.grimorio.rpg.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grimorio.rpg.data.local.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM character_profile WHERE id = 1 LIMIT 1")
    fun getCharacter(): Flow<CharacterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCharacter(entity: CharacterEntity)

    @Query("SELECT COUNT(*) FROM character_profile")
    suspend fun countCharacters(): Int
}
