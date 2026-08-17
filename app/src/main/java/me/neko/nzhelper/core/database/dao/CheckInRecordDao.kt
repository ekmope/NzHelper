package me.neko.nzhelper.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import me.neko.nzhelper.core.database.entity.CheckInRecordEntity

@Dao
interface CheckInRecordDao {

    @Query("SELECT * FROM check_in_records ORDER BY date ASC, time ASC, sourceKey ASC")
    suspend fun getAll(): List<CheckInRecordEntity>

    @Query("SELECT COUNT(*) FROM check_in_records")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringConflicts(items: List<CheckInRecordEntity>): List<Long>
}
