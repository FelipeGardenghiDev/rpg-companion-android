package com.grimorio.rpg.data.repository

import com.google.common.truth.Truth.assertThat
import com.grimorio.rpg.data.local.dao.RollHistoryDao
import com.grimorio.rpg.data.local.entity.RollHistoryEntity
import com.grimorio.rpg.domain.model.DiceGroupResult
import com.grimorio.rpg.domain.model.RollResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeRollHistoryDao : RollHistoryDao {
    private val storage = mutableListOf<RollHistoryEntity>()
    private val flow = MutableStateFlow<List<RollHistoryEntity>>(emptyList())

    override fun getRecentRolls(limit: Int): Flow<List<RollHistoryEntity>> = flow

    override suspend fun insertRoll(roll: RollHistoryEntity): Long {
        storage.add(0, roll)
        flow.value = storage.toList()
        return storage.size.toLong()
    }

    override suspend fun clearAll() {
        storage.clear()
        flow.value = emptyList()
    }
}

class DiceRepositoryImplTest {

    @Test
    fun `saveRoll serializa grupos de dados e persiste via DAO`() = runTest {
        val fakeDao = FakeRollHistoryDao()
        val repository = DiceRepositoryImpl(rollHistoryDao = fakeDao)

        val roll = RollResult(
            formula = "2d20kh1 + 5",
            total = 24,
            groups = listOf(
                DiceGroupResult(
                    count = 2,
                    sides = 20,
                    rolledValues = listOf(8, 19),
                    keptValues = listOf(19),
                    droppedValues = listOf(8),
                    isExploding = false
                )
            ),
            modifier = 5,
            isCriticalHit = false,
            isCriticalFail = false
        )

        repository.saveRoll(roll)

        val savedList = repository.getRecentRolls().first()
        assertThat(savedList).hasSize(1)
        assertThat(savedList[0].formula).isEqualTo("2d20kh1 + 5")
        assertThat(savedList[0].total).isEqualTo(24)
        assertThat(savedList[0].groups).hasSize(1)
        assertThat(savedList[0].groups[0].keptValues).containsExactly(19)
        assertThat(savedList[0].groups[0].droppedValues).containsExactly(8)
    }

    @Test
    fun `clearHistory limpa todos os registros do DAO`() = runTest {
        val fakeDao = FakeRollHistoryDao()
        val repository = DiceRepositoryImpl(rollHistoryDao = fakeDao)

        repository.saveRoll(
            RollResult(
                formula = "1d20",
                total = 20,
                groups = emptyList(),
                modifier = 0,
                isCriticalHit = true
            )
        )

        assertThat(repository.getRecentRolls().first()).hasSize(1)

        repository.clearHistory()

        assertThat(repository.getRecentRolls().first()).isEmpty()
    }
}
