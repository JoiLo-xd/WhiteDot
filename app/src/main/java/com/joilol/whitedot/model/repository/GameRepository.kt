package com.joilol.whitedot.model.repository

import com.joilol.whitedot.model.local.GameDao
import com.joilol.whitedot.model.local.GameEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val gameData: Flow<GameEntity?> = gameDao.getGameData()

    suspend fun saveData(
        totalClicks: Float,
        autoClicks: Float,
        clickMult: Float = 1f,
        autoMult: Float = 1f,
        autoDelay: Long = 2000L,
        critChance: Float = 0f,
        username: String = ""
    ) {
        val entity = GameEntity(
            totalClicks = totalClicks,
            autoClicks = autoClicks,
            clickMultiplier = clickMult,
            autoMultiplier = autoMult,
            autoClickDelay = autoDelay,
            critChance = critChance,
            username = username
        )
        gameDao.insertOrUpdate(entity)
    }
}
