package me.neko.nzhelper.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_in_records")
data class CheckInRecordEntity(
    @PrimaryKey val sourceKey: String,
    val date: String,
    val time: String,
    val type: String,
    val sideDishes: String,
    val feeling: String
)
