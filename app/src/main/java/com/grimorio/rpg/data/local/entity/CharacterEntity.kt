package com.grimorio.rpg.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_profile")
data class CharacterEntity(
    @PrimaryKey
    val id: Long = 1L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "character_class")
    val characterClass: String,

    @ColumnInfo(name = "current_hp")
    val currentHp: Int,

    @ColumnInfo(name = "max_hp")
    val maxHp: Int,

    @ColumnInfo(name = "temp_hp")
    val tempHp: Int,

    @ColumnInfo(name = "spell_slots_json")
    val spellSlotsJson: String,

    @ColumnInfo(name = "active_conditions_json")
    val activeConditionsJson: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
