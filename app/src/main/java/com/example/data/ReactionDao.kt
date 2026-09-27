package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ReactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Query("SELECT * FROM reaction_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ReactionRecord>>

    @Query("SELECT * FROM reaction_records WHERE isFalseStart = 0 ORDER BY timestamp DESC")
    fun getValidRecords(): Flow<List<ReactionRecord>>

    @Query("SELECT * FROM reaction_records WHERE isFalseStart = 0 ORDER BY reactionTimeMs ASC LIMIT 1")
    fun getPersonalBest(): Flow<ReactionRecord?>

    @Query("SELECT * FROM reaction_records WHERE isFalseStart = 0 AND inputMethod != 'screen_tap' ORDER BY reactionTimeMs ASC LIMIT 1")
    fun getBestHardwareRecord(): Flow<ReactionRecord?>

    @Query("SELECT * FROM reaction_records WHERE isFalseStart = 0 AND inputMethod = 'screen_tap' ORDER BY reactionTimeMs ASC LIMIT 1")
    fun getBestTouchRecord(): Flow<ReactionRecord?>

    @Query("SELECT * FROM reaction_records WHERE isFalseStart = 0 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentValidRecords(limit: Int): Flow<List<ReactionRecord>>

    @Query("SELECT AVG(reactionTimeMs) FROM reaction_records WHERE isFalseStart = 0")
    fun getAverageReactionTime(): Flow<Double?>

    @Query("SELECT AVG(reactionTimeMs) FROM reaction_records WHERE isFalseStart = 0 AND inputMethod != 'screen_tap'")
    fun getAverageHardwareReactionTime(): Flow<Double?>

    @Query("SELECT AVG(reactionTimeMs) FROM reaction_records WHERE isFalseStart = 0 AND inputMethod = 'screen_tap'")
    fun getAverageTouchReactionTime(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM reaction_records WHERE isFalseStart = 0")
    fun getTotalValidTrialsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reaction_records WHERE isFalseStart = 1")
    fun getFalseStartsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ReactionRecord): Long

    @Query("DELETE FROM reaction_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM reaction_records")
    suspend fun clearAllRecords()
}
