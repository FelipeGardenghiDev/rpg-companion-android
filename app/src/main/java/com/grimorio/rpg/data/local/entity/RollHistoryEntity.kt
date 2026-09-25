package com.grimorio.rpg.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roll_history")
data class RollHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "formula")
    val formula: String,

    @ColumnInfo(name = "total")
    val total: Int,

    @ColumnInfo(name = "modifier")
    val modifier: Int,

    @ColumnInfo(name = "is_critical_hit")
    val isCriticalHit: Boolean,

    @ColumnInfo(name = "is_critical_fail")
    val isCriticalFail: Boolean,

    @ColumnInfo(name = "groups_json")
    val groupsJson: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
