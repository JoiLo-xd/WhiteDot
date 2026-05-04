package com.joilol.whitedot.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_data")
data class GameEntity(
    @PrimaryKey val id: Int = 1,
    val totalClicks: Float = 0f,
    val autoClicks: Float = 0f,
    val clickMultiplier: Float = 1.0f,
    val autoMultiplier: Float = 1.0f,
    val autoClickDelay: Long = 2000L,
    val critChance: Float = 0f,
    val username: String = ""
)
