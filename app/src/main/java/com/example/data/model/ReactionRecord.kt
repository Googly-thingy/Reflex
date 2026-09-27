package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single reflex trial record.
 * Precision timestamping in nanoseconds converted to fractional milliseconds.
 */
@Entity(tableName = "reaction_records")
data class ReactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reactionTimeMs: Double,
    val stimulusType: String, // "tactile_vibration", "auditory", "dual"
    val inputMethod: String,  // "volume_down", "volume_up", "screen_tap"
    val sessionMode: String,  // "single", "series_5", "series_10"
    val roundNumber: Int,
    val isFalseStart: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
