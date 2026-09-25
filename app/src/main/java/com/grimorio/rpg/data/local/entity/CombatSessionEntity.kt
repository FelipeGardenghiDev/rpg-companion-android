package com.grimorio.rpg.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "combat_session")
data class CombatSessionEntity(
    @PrimaryKey
    val id: Long = 1L,

    @ColumnInfo(name = "round")
    val round: Int = 1,

    @ColumnInfo(name = "current_turn_index")
    val currentTurnIndex: Int = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false
)
