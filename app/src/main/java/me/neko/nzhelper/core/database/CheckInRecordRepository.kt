package me.neko.nzhelper.core.database

import android.content.Context
import com.google.gson.JsonParser
import me.neko.nzhelper.NzApplication
import me.neko.nzhelper.core.database.entity.CheckInRecordEntity
import me.neko.nzhelper.core.model.CheckInRecord
import java.security.MessageDigest

object CheckInRecordRepository {

    private val gson = NzApplication.gson

    data class MergeResult(val added: Int, val existing: Int)

    private data class HanimeBackup(
        val checkInRecords: List<HanimeCheckInRecord> = emptyList()
    )

    private data class HanimeCheckInRecord(
        val id: Long = 0,
        val date: String = "",
        val time: String = "",
        val type: String = "",
        val sideDishes: String = "",
        val feeling: String = ""
    )

    fun isHanimeCheckInBackup(json: String): Boolean = try {
        val root = JsonParser.parseString(json)
        root.isJsonObject && root.asJsonObject.has("checkInRecords") &&
            root.asJsonObject.get("checkInRecords").isJsonArray
    } catch (_: Exception) {
        false
    }

    fun parseHanimeCheckInBackup(json: String): List<CheckInRecord> {
        val backup = try {
            gson.fromJson(json, HanimeBackup::class.java)
        } catch (_: Exception) {
            null
        } ?: return emptyList()

        return backup.checkInRecords
            .asSequence()
            .filter { it.date.isNotBlank() }
            .map { record ->
                CheckInRecord(
                    sourceKey = hanimeSourceKey(record),
                    date = record.date,
                    time = record.time,
                    type = record.type,
                    sideDishes = record.sideDishes,
                    feeling = record.feeling
                )
            }
            .distinctBy { it.sourceKey }
            .toList()
    }

    suspend fun loadAll(context: Context): List<CheckInRecord> =
        AppDatabase.get(context).checkInRecordDao().getAll().map { it.toModel() }

    suspend fun count(context: Context): Int = AppDatabase.get(context).checkInRecordDao().count()

    suspend fun merge(context: Context, records: List<CheckInRecord>): MergeResult {
        if (records.isEmpty()) return MergeResult(added = 0, existing = 0)
        val uniqueRecords = records.distinctBy { it.sourceKey }
        val results = AppDatabase.get(context).checkInRecordDao()
            .insertIgnoringConflicts(uniqueRecords.map { it.toEntity() })
        val added = results.count { it != -1L }
        return MergeResult(added = added, existing = uniqueRecords.size - added)
    }

    private fun hanimeSourceKey(record: HanimeCheckInRecord): String {
        val source = listOf(
            record.id.toString(), record.date, record.time, record.type,
            record.sideDishes, record.feeling
        ).joinToString(separator = "\u0000")
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { "%02x".format(it.toInt() and 0xff) }
        return "han1meviewer:$digest"
    }

    private fun CheckInRecordEntity.toModel() = CheckInRecord(
        sourceKey = sourceKey,
        date = date,
        time = time,
        type = type,
        sideDishes = sideDishes,
        feeling = feeling
    )

    private fun CheckInRecord.toEntity() = CheckInRecordEntity(
        sourceKey = sourceKey,
        date = date,
        time = time,
        type = type,
        sideDishes = sideDishes,
        feeling = feeling
    )
}
