package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MemoryRepository(database.practiceDao())

    // UI States
    val allItems: StateFlow<List<PracticeItem>> = repository.allPracticeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<PracticeLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter properties
    private val _selectedDifficulty = MutableStateFlow("ALL")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedType = MutableStateFlow("WORD") // "WORD", "SENTENCE", "PARAGRAPH"
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // Current Navigation State
    private val _currentTab = MutableStateFlow("practice_list") // "dashboard", "practice_list", "active_practice", "leaderboard", "settings"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Active Practice Session State
    private val _activeItem = MutableStateFlow<PracticeItem?>(null)
    val activeItem: StateFlow<PracticeItem?> = _activeItem.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _practiceResult = MutableStateFlow<PracticeResult?>(null)
    val practiceResult: StateFlow<PracticeResult?> = _practiceResult.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Challenges Status
    private val _challenges = MutableStateFlow<List<SocialChallenge>>(emptyList())
    val challenges: StateFlow<List<SocialChallenge>> = _challenges.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
            loadDailyChallenges()
        }
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
        if (tab != "active_practice") {
            // reset active practice when leaving
            _activeItem.value = null
            _spokenText.value = ""
            _practiceResult.value = null
            _isRecording.value = false
        }
    }

    fun setTypeFilter(type: String) {
        _selectedType.value = type
    }

    fun setDifficultyFilter(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun startPractice(item: PracticeItem) {
        _activeItem.value = item
        _spokenText.value = ""
        _practiceResult.value = null
        _isRecording.value = false
        _currentTab.value = "active_practice"
    }

    fun addCustomItem(content: String, type: String, difficulty: String, category: String) {
        viewModelScope.launch {
            repository.insertCustomItem(content, type, difficulty, category)
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }

    fun updateRecordingState(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setSpokenText(text: String) {
        _spokenText.value = text
    }

    // Levenshtein Text-matching Algorithm to compute memory accuracy score
    fun submitPractice(startTimeMs: Long) {
        val item = _activeItem.value ?: return
        val speakTxt = _spokenText.value.trim()
        val originalTxt = item.content.trim()

        if (speakTxt.isEmpty()) return

        val duration = System.currentTimeMillis() - startTimeMs
        val accuracy = calculateSimilarityPercentage(originalTxt, speakTxt)

        viewModelScope.launch {
            val (xpEarned, streakIncremented) = repository.savePracticeLog(
                itemId = item.id,
                itemType = item.type,
                originalText = originalTxt,
                recognizedText = speakTxt,
                accuracyScore = accuracy,
                durationMs = duration,
                difficulty = item.difficulty
            )

            // Dynamic challenge matching updates
            updateChallengesState(item, accuracy)

            _practiceResult.value = PracticeResult(
                accuracyScore = accuracy,
                xpEarned = xpEarned,
                streakIncremented = streakIncremented,
                originalText = originalTxt,
                recognizedText = speakTxt
            )
        }
    }

    fun updateUserSettings(username: String, isDarkMode: Boolean, reminders: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(username, isDarkMode, reminders)
        }
    }

    // Levenshtein algorithm for similarity percentage (works 100% offline)
    private fun calculateSimilarityPercentage(s1: String, s2: String): Int {
        // Clean texts from common punctuations to focus on pure verbal recall
        val norm1 = s1.lowercase(Locale.getDefault())
            .replace(Regex("[.,/#!$%^&*;:{}=\\-_`~()?\"'\\s+]"), " ").replace(Regex("\\s+"), " ").trim()
        val norm2 = s2.lowercase(Locale.getDefault())
            .replace(Regex("[.,/#!$%^&*;:{}=\\-_`~()?\"'\\s+]"), " ").replace(Regex("\\s+"), " ").trim()

        if (norm1 == norm2) return 100
        if (norm1.isEmpty() || norm2.isEmpty()) return 0

        val words1 = norm1.split(" ")
        val words2 = norm2.split(" ")

        val dp = Array(words1.size + 1) { IntArray(words2.size + 1) }

        for (i in 0..words1.size) {
            dp[i][0] = i
        }
        for (j in 0..words2.size) {
            dp[0][j] = j
        }

        for (i in 1..words1.size) {
            for (j in 1..words2.size) {
                if (words1[i - 1] == words2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(
                        dp[i - 1][j],      // Deletion
                        dp[i][j - 1],      // Insertion
                        dp[i - 1][j - 1]   // Substitution
                    )
                }
            }
        }

        val maxLen = maxOf(words1.size, words2.size)
        val distance = dp[words1.size][words2.size]
        val similarity = (1.0 - (distance.toDouble() / maxLen.toDouble())) * 100
        return similarity.toInt().coerceIn(0, 100)
    }

    // Challenge Management Loader
    private fun loadDailyChallenges() {
        _challenges.value = listOf(
            SocialChallenge(
                id = 1,
                title = "Rapid Starter",
                description = "Secure an accuracy score of 95%+ in any Word practice exercise.",
                type = "WORD_95",
                target = 95,
                progress = 0,
                completed = false,
                xpReward = 80
            ),
            SocialChallenge(
                id = 2,
                title = "Sentence Champion",
                description = "Examine and master two Sentence exercises at Medium or Hard difficulty.",
                type = "SENTENCE_COUNT",
                target = 2,
                progress = 0,
                completed = false,
                xpReward = 120
            ),
            SocialChallenge(
                id = 3,
                title = "Executive Recall",
                description = "Tackle any scientific or wisdom Paragraph at Hard level with 80%+ accuracy.",
                type = "PARAGRAPH_HARD",
                target = 80,
                progress = 0,
                completed = false,
                xpReward = 200
            )
        )
    }

    private fun updateChallengesState(item: PracticeItem, score: Int) {
        val updated = _challenges.value.map { challenge ->
            if (challenge.completed) return@map challenge

            var completed = false
            var progress = challenge.progress

            when (challenge.type) {
                "WORD_95" -> {
                    if (item.type == "WORD" && score >= challenge.target) {
                        completed = true
                        progress = 1
                    }
                }
                "SENTENCE_COUNT" -> {
                    if (item.type == "SENTENCE" && (item.difficulty == "MEDIUM" || item.difficulty == "HARD") && score >= 75) {
                        progress = (progress + 1).coerceAtMost(challenge.target)
                        if (progress >= challenge.target) {
                            completed = true
                        }
                    }
                }
                "PARAGRAPH_HARD" -> {
                    if (item.type == "PARAGRAPH" && item.difficulty == "HARD" && score >= challenge.target) {
                        completed = true
                        progress = 1
                    }
                }
            }

            if (completed && !challenge.completed) {
                // Award completion bonus XP if challenge just finished
                viewModelScope.launch {
                    val profile = repository.getUserProfileOnce()
                    if (profile != null) {
                        repository.updateSettings(
                            username = profile.username,
                            isDarkMode = profile.isDarkMode,
                            reminders = profile.practiceReminderEnabled
                        )
                        // Trigger profile updating with added award
                        val bonusProfile = profile.copy(totalXp = profile.totalXp + challenge.xpReward)
                        database.practiceDao().insertOrUpdateProfile(bonusProfile)
                    }
                }
            }

            challenge.copy(progress = progress, completed = completed)
        }
        _challenges.value = updated
    }
}

// Support Structs
data class PracticeResult(
    val accuracyScore: Int,
    val xpEarned: Int,
    val streakIncremented: Boolean,
    val originalText: String,
    val recognizedText: String
)

data class SocialChallenge(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val xpReward: Int
)
