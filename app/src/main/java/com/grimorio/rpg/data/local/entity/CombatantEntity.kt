package com.grimorio.rpg.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "combatants")
data class CombatantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "initiative")
    val initiative: Int,

    @ColumnInfo(name = "initiative_modifier")
    val initiativeModifier: Int,

    @ColumnInfo(name = "current_hp")
    val currentHp: Int,

    @ColumnInfo(name = "max_hp")
    val maxHp: Int,

    @ColumnInfo(name = "is_player")
    val isPlayer: Boolean,

    @ColumnInfo(name = "conditions_json")
    val conditionsJson: String = "[]"
)
