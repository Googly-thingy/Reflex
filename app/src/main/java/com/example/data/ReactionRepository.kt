package com.example.data

import com.example.data.model.ReactionRecord
import kotlinx.coroutines.flow.Flow

class ReactionRepository(private val reactionDao: ReactionDao) {

    val allRecords: Flow<List<ReactionRecord>> = reactionDao.getAllRecords()
    val validRecords: Flow<List<ReactionRecord>> = reactionDao.getValidRecords()
    val personalBest: Flow<ReactionRecord?> = reactionDao.getPersonalBest()
    val bestHardwareRecord: Flow<ReactionRecord?> = reactionDao.getBestHardwareRecord()
    val bestTouchRecord: Flow<ReactionRecord?> = reactionDao.getBestTouchRecord()
    val averageReactionTime: Flow<Double?> = reactionDao.getAverageReactionTime()
    val averageHardwareReactionTime: Flow<Double?> = reactionDao.getAverageHardwareReactionTime()
    val averageTouchReactionTime: Flow<Double?> = reactionDao.getAverageTouchReactionTime()
    val totalValidTrialsCount: Flow<Int> = reactionDao.getTotalValidTrialsCount()
    val falseStartsCount: Flow<Int> = reactionDao.getFalseStartsCount()

    fun getRecentValidRecords(limit: Int): Flow<List<ReactionRecord>> =
        reactionDao.getRecentValidRecords(limit)

    suspend fun insertRecord(record: ReactionRecord): Long =
        reactionDao.insertRecord(record)

    suspend fun deleteRecord(id: Long) =
        reactionDao.deleteRecord(id)

    suspend fun clearAll() =
        reactionDao.clearAllRecords()
}
