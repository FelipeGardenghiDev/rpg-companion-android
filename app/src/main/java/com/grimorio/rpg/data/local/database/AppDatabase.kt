package com.grimorio.rpg.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grimorio.rpg.data.local.dao.CharacterDao
import com.grimorio.rpg.data.local.dao.CombatDao
import com.grimorio.rpg.data.local.dao.MacroDao
import com.grimorio.rpg.data.local.dao.RollHistoryDao
import com.grimorio.rpg.data.local.entity.CharacterEntity
import com.grimorio.rpg.data.local.entity.CombatSessionEntity
import com.grimorio.rpg.data.local.entity.CombatantEntity
import com.grimorio.rpg.data.local.entity.MacroEntity
import com.grimorio.rpg.data.local.entity.RollHistoryEntity

@Database(
    entities = [
        RollHistoryEntity::class,
        MacroEntity::class,
        CharacterEntity::class,
        CombatantEntity::class,
        CombatSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rollHistoryDao(): RollHistoryDao
    abstract fun macroDao(): MacroDao
    abstract fun characterDao(): CharacterDao
    abstract fun combatDao(): CombatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grimorio_rpg.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
