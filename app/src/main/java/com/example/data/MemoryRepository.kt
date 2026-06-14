package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryRepository(private val dao: PracticeDao) {

    val allPracticeItems: Flow<List<PracticeItem>> = dao.getAllPracticeItems()
    val allLogs: Flow<List<PracticeLog>> = dao.getAllPracticeLogs()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val leaderboard: Flow<List<LeaderboardEntry>> = dao.getLeaderboardEntries()

    fun getItemsByType(type: String): Flow<List<PracticeItem>> = dao.getPracticeItemsByType(type)
    fun getRecentLogs(limit: Int): Flow<List<PracticeLog>> = dao.getRecentPracticeLogs(limit)

    suspend fun insertCustomItem(content: String, type: String, difficulty: String, category: String) {
        val item = PracticeItem(
            type = type,
            content = content,
            difficulty = difficulty,
            category = category,
            isCustom = true
        )
        dao.insertPracticeItem(item)
    }

    suspend fun deleteItem(id: Int) = dao.deletePracticeItem(id)

    suspend fun getUserProfileOnce(): UserProfile? = dao.getUserProfileOnce()

    // Check database state and prepopulate if empty
    suspend fun checkAndPrepopulate() {
        withContext(Dispatchers.IO) {
            try {
                // Prepopulate items if database is empty
                if (dao.getPracticeItemsCount() == 0) {
                    val defaultItems = listOf(
                        // Easy Words
                        PracticeItem(type = "WORD", content = "Attention", difficulty = "EASY", category = "Focus"),
                        PracticeItem(type = "WORD", content = "Focus", difficulty = "EASY", category = "Focus"),
                        PracticeItem(type = "WORD", content = "Memory", difficulty = "EASY", category = "Science"),
                        PracticeItem(type = "WORD", content = "Recall", difficulty = "EASY", category = "Science"),
                        PracticeItem(type = "WORD", content = "Wisdom", difficulty = "EASY", category = "Wisdom"),
                        PracticeItem(type = "WORD", content = "Brainpower", difficulty = "EASY", category = "General"),
                        PracticeItem(type = "WORD", content = "Cognition", difficulty = "EASY", category = "Science"),

                        // Medium Words
                        PracticeItem(type = "WORD", content = "Neuroplasticity", difficulty = "MEDIUM", category = "Science"),
                        PracticeItem(type = "WORD", content = "Hippocampus", difficulty = "MEDIUM", category = "Science"),
                        PracticeItem(type = "WORD", content = "Mnemonics", difficulty = "MEDIUM", category = "Wisdom"),
                        PracticeItem(type = "WORD", content = "Synapse", difficulty = "MEDIUM", category = "Science"),
                        PracticeItem(type = "WORD", content = "Metacognition", difficulty = "MEDIUM", category = "Focus"),

                        // Hard Words
                        PracticeItem(type = "WORD", content = "Nootropic Compounds", difficulty = "HARD", category = "Science"),
                        PracticeItem(type = "WORD", content = "Consolidation Theory", difficulty = "HARD", category = "Science"),
                        PracticeItem(type = "WORD", content = "Phonological Loop", difficulty = "HARD", category = "Focus"),
                        PracticeItem(type = "WORD", content = "Anterograde Amnesia", difficulty = "HARD", category = "Science"),
                        PracticeItem(type = "WORD", content = "Visuospatial Sketchpad", difficulty = "HARD", category = "Visual"),

                        // Easy Sentences
                        PracticeItem(type = "SENTENCE", content = "Focus on the present moment.", difficulty = "EASY", category = "Focus"),
                        PracticeItem(type = "SENTENCE", content = "Reading books sharpens your memory.", difficulty = "EASY", category = "Wisdom"),
                        PracticeItem(type = "SENTENCE", content = "Healthy sleep repairs brain cells.", difficulty = "EASY", category = "Science"),
                        PracticeItem(type = "SENTENCE", content = "Practice makes a person perfect.", difficulty = "EASY", category = "General"),

                        // Medium Sentences
                        PracticeItem(type = "SENTENCE", content = "Active recall is the most effective way to retain new information.", difficulty = "MEDIUM", category = "Science"),
                        PracticeItem(type = "SENTENCE", content = "A healthy mind resides in a healthy body through daily exercise.", difficulty = "MEDIUM", category = "Wisdom"),
                        PracticeItem(type = "SENTENCE", content = "Your brain is like a muscle; the more you train it, the stronger it grows.", difficulty = "MEDIUM", category = "General"),

                        // Hard Sentences
                        PracticeItem(type = "SENTENCE", content = "The hippocampus plays a fundamental role in consolidating short-term memory into long-term retention.", difficulty = "HARD", category = "Science"),
                        PracticeItem(type = "SENTENCE", content = "Cognitive flexibility refers to our mental ability to switch between thinking about different concepts.", difficulty = "HARD", category = "Focus"),
                        PracticeItem(type = "SENTENCE", content = "Mnemonic techniques, such as the Method of Loci, have been utilized since ancient classical times.", difficulty = "HARD", category = "Wisdom"),

                        // Easy Paragraphs
                        PracticeItem(type = "PARAGRAPH", content = "To keep your memory sharp, simple lifestyle modifications make a huge difference. Regular physical exercise boosts oxygen flow to your brain. Getting eight hours of deep sleep allows your hippocampus to organize and consolidate everything you learned.", difficulty = "EASY", category = "Wisdom"),
                        PracticeItem(type = "PARAGRAPH", content = "Healthy eating fuels cognitive function. Consuming foods rich in omega-three fatty acids, like walnuts, flax seeds, and salmon, provides the building blocks for brain cells. Consuming fresh green leafy vegetables protects your nervous system from early fatigue.", difficulty = "EASY", category = "Science"),

                        // Medium Paragraphs
                        PracticeItem(type = "PARAGRAPH", content = "Science shows that practicing active retrieval is vastly superior to passive reading. When you look at information and immediately force yourself to recall it without looking, you strengthen neural pathways. With each effort, your brain hardens these connections.", difficulty = "MEDIUM", category = "Science"),
                        PracticeItem(type = "PARAGRAPH", content = "Focus is the entryway to memory. In our hyper-distracted modern era, multitasking acts as a direct inhibitor to learning. If you try to compile a report while reviewing emails and texting, your prefrontal cortex becomes overloaded, ensuring no deep memories are formed.", difficulty = "MEDIUM", category = "Focus"),

                        // Hard Paragraphs
                        PracticeItem(type = "PARAGRAPH", content = "The process of neuroplasticity refers to the brain's remarkable ability to reorganize itself by forming new neural connections throughout life. This mechanism allows the neurons to adjust response to new situations, environmental changes, or cognitive training. By challenging ourselves with novel practices, we stimulate synaptic transmission.", difficulty = "HARD", category = "Science"),
                        PracticeItem(type = "PARAGRAPH", content = "Psychologists define working memory as a cognitive system with a limited capacity that can hold information temporarily. It is critical for reasoning, decision-making, and behavior. Unlike traditional passive memory, working memory operates as an active mental workspace, processing and manipulating information on the fly, which degrades in efficiency under stress.", difficulty = "HARD", category = "Focus")
                    )
                    dao.insertPracticeItems(defaultItems)
                }

                // Initial Profile if not exists
                val currentProfile = dao.getUserProfileOnce()
                if (currentProfile == null) {
                    dao.insertOrUpdateProfile(
                        UserProfile(
                            id = 1,
                            username = "Memory Champ",
                            totalXp = 150, // Starting default
                            currentStreak = 1,
                            lastPracticeDate = getTodayDateString(),
                            isDarkMode = true,
                            practiceReminderEnabled = true
                        )
                    )
                }

                // Initial Bots on Leaderboard to compete
                seedInitialLeaderboard()

            } catch (e: Exception) {
                Log.e("MemoryRepository", "Prepopulation failed", e)
            }
        }
    }

    private suspend fun seedInitialLeaderboard() {
        val bots = listOf(
            LeaderboardEntry(1, "Sophia_Synapse", 1250, 1, "#E91E63", challengeActive = true, challengeTargetScore = 85),
            LeaderboardEntry(2, "Aarav_Recall", 1020, 2, "#2196F3", challengeActive = false),
            LeaderboardEntry(3, "Vivaan_Focus", 940, 3, "#FFEB3B", challengeActive = true, challengeTargetScore = 90),
            LeaderboardEntry(4, "Memory Champ", 150, 4, "#4CAF50", isCurrentUser = true),
            LeaderboardEntry(5, "Mia_ZenMind", 410, 5, "#9C27B0", challengeActive = false),
            LeaderboardEntry(6, "Rohan_Brainy", 320, 6, "#FF9800", challengeActive = true, challengeTargetScore = 80)
        )
        dao.insertLeaderboardEntries(bots)
    }

    // Save practice log, update Streak, increase XP, and update Leaderboard!
    suspend fun savePracticeLog(
        itemId: Int,
        itemType: String,
        originalText: String,
        recognizedText: String,
        accuracyScore: Int,
        durationMs: Long,
        difficulty: String
    ): Pair<Int, Boolean> { // returns (EarnedXP, StreakIncremented)
        return withContext(Dispatchers.IO) {
            val log = PracticeLog(
                itemId = itemId,
                itemType = itemType,
                originalText = originalText,
                recognizedText = recognizedText,
                accuracyScore = accuracyScore,
                durationMs = durationMs
            )
            dao.insertPracticeLog(log)

            // Get profile and calculate Streak/XP rewards
            val profile = dao.getUserProfileOnce() ?: UserProfile()
            val todayStr = getTodayDateString()
            val lastPracStr = profile.lastPracticeDate

            val (newStreak, streakIncremented) = calculateNewStreak(lastPracStr, todayStr, profile.currentStreak)

            // Compute XP base and multiplier
            val difficultyMultiplier = when (difficulty.uppercase()) {
                "MEDIUM" -> 1.5
                "HARD" -> 2.0
                else -> 1.0
            }
            // Accuracy scales XP points
            val earnedXp = ((accuracyScore / 100.0) * 100 * difficultyMultiplier).toInt().coerceAtLeast(10)

            val updatedXp = profile.totalXp + earnedXp

            // Update user profile
            val updatedProfile = profile.copy(
                totalXp = updatedXp,
                currentStreak = newStreak,
                lastPracticeDate = todayStr
            )
            dao.insertOrUpdateProfile(updatedProfile)

            // Update Leaderboard Rank for Current User dynamically!
            updateLeaderboardRanks(updatedXp)

            Pair(earnedXp, streakIncremented)
        }
    }

    private fun calculateNewStreak(lastPractice: String, today: String, currentStreak: Int): Pair<Int, Boolean> {
        if (lastPractice.isEmpty()) {
            return Pair(1, true)
        }
        if (lastPractice == today) {
            return Pair(currentStreak, false) // Did it today already, keep current streak
        }

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val lastDate = sdf.parse(lastPractice)
            val todayDate = sdf.parse(today)
            if (lastDate != null && todayDate != null) {
                val diffMs = todayDate.time - lastDate.time
                val diffDays = diffMs / (1000 * 60 * 60 * 24)
                if (diffDays == 1L) {
                    Pair(currentStreak + 1, true) // Consecutive day increase!
                } else if (diffDays > 1L) {
                    Pair(1, true) // Missed a day, resets to 1
                } else {
                    Pair(currentStreak, false) // Retroactive or error case safety
                }
            } else {
                Pair(1, true)
            }
        } catch (e: Exception) {
            Pair(1, true)
        }
    }

    private suspend fun updateLeaderboardRanks(currentUserNewXp: Int) {
        val staticCompetitors = listOf(
            LeaderboardEntry(1, "Sophia_Synapse", 1250, 1, "#E91E63", challengeActive = true, challengeTargetScore = 85),
            LeaderboardEntry(2, "Aarav_Recall", 1020, 2, "#2196F3", challengeActive = false),
            LeaderboardEntry(3, "Vivaan_Focus", 940, 3, "#FFEB3B", challengeActive = true, challengeTargetScore = 90),
            LeaderboardEntry(4, "Memory Champ", currentUserNewXp, 4, "#4CAF50", isCurrentUser = true),
            LeaderboardEntry(5, "Mia_ZenMind", 410, 5, "#9C27B0", challengeActive = false),
            LeaderboardEntry(6, "Rohan_Brainy", 320, 6, "#FF9800", challengeActive = true, challengeTargetScore = 80)
        )

        // Sort all entries descending by score/XP and assign ranks
        val sortedList = staticCompetitors.sortedByDescending { it.totalXp }
        val rankedList = sortedList.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
        dao.insertLeaderboardEntries(rankedList)
    }

    suspend fun updateSettings(username: String, isDarkMode: Boolean, reminders: Boolean) {
        withContext(Dispatchers.IO) {
            val profile = dao.getUserProfileOnce() ?: UserProfile()
            val updated = profile.copy(
                username = username,
                isDarkMode = isDarkMode,
                practiceReminderEnabled = reminders
            )
            dao.insertOrUpdateProfile(updated)

            // Also sync username on Leaderboard
            dao.getLeaderboardEntries().collect { list ->
                val updatedList = list.map {
                    if (it.isCurrentUser) it.copy(username = username) else it
                }
                dao.insertLeaderboardEntries(updatedList)
            }
        }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
