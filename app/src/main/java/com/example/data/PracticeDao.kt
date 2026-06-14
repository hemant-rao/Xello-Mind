package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    // Practice Items
    @Query("SELECT * FROM practice_items")
    fun getAllPracticeItems(): Flow<List<PracticeItem>>

    @Query("SELECT * FROM practice_items WHERE type = :type ORDER BY id ASC")
    fun getPracticeItemsByType(type: String): Flow<List<PracticeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeItem(item: PracticeItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeItems(items: List<PracticeItem>)

    @Query("DELETE FROM practice_items WHERE id = :id")
    suspend fun deletePracticeItem(id: Int)

    @Query("SELECT COUNT(*) FROM practice_items")
    suspend fun getPracticeItemsCount(): Int

    // Practice Logs
    @Query("SELECT * FROM practice_logs ORDER BY timestamp DESC")
    fun getAllPracticeLogs(): Flow<List<PracticeLog>>

    @Query("SELECT * FROM practice_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPracticeLogs(limit: Int): Flow<List<PracticeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeLog(log: PracticeLog)

    // User Profile
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // Leaderboard Entries
    @Query("SELECT * FROM leaderboard_entries ORDER BY rank ASC")
    fun getLeaderboardEntries(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntry>)

    @Update
    suspend fun updateLeaderboardEntry(entry: LeaderboardEntry)
}
