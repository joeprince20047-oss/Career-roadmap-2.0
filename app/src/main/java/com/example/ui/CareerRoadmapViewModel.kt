package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiResult
import com.example.data.AiRoadmapRepository
import com.example.data.AiRoadmapRepositoryImpl
import com.example.data.AppDatabase
import com.example.data.AssessmentEntity
import com.example.data.CareerRoadmapData
import com.example.data.FeedbackEntity
import com.example.data.ProfileEntity
import com.example.data.SkillProficiencyHistoryEntity
import com.example.data.SkillProficiencyRepository
import com.example.data.StepDoneEntity
import com.example.data.StudyLogEntity
import com.example.data.UserEntity
import com.example.engine.RoadmapEngine
import com.example.model.CurriculumSkillGapReport
import com.example.model.LearningGain
import com.example.model.RoadmapSchema
import com.example.model.RoadmapStep
import com.example.model.SkillGap
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate

enum class AppNavTab(val title: String) {
  TODAY("Today"),
  PLAN("Roadmap"),
  SKILLS("Skills"),
  LOGGER("Study Log"),
  SKILL_CHECK("Quiz"),
  PROGRESS("Stats"),
  FEEDBACK("Feedback"),
  ADMIN("Admin")
}

data class QuizSubmissionResult(
  val score: Int,
  val totalQuestions: Int,
  val correctCount: Int,
  val skillBreakdown: Map<String, Pair<Int, Int>>,
  val learningGain: LearningGain? = null
)

class CareerRoadmapViewModel(application: Application) : AndroidViewModel(application) {
  private val dao = AppDatabase.getDatabase(application).appDao()
  private val skillProficiencyRepository = SkillProficiencyRepository(dao)
  private val aiRepository: AiRoadmapRepository = AiRoadmapRepositoryImpl()

  private val _currentUser = MutableStateFlow<UserEntity?>(null)
  val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

  private val _currentProfile = MutableStateFlow<ProfileEntity?>(null)
  val currentProfile: StateFlow<ProfileEntity?> = _currentProfile.asStateFlow()

  private val _stepsDone = MutableStateFlow<Set<String>>(emptySet())
  val stepsDone: StateFlow<Set<String>> = _stepsDone.asStateFlow()

  private val _studyLogs = MutableStateFlow<List<StudyLogEntity>>(emptyList())
  val studyLogs: StateFlow<List<StudyLogEntity>> = _studyLogs.asStateFlow()

  private val _assessments = MutableStateFlow<List<AssessmentEntity>>(emptyList())
  val assessments: StateFlow<List<AssessmentEntity>> = _assessments.asStateFlow()

  private val _skillProficiencyHistory = MutableStateFlow<List<SkillProficiencyHistoryEntity>>(emptyList())
  val skillProficiencyHistory: StateFlow<List<SkillProficiencyHistoryEntity>> = _skillProficiencyHistory.asStateFlow()

  private val _allUsers = MutableStateFlow<List<UserEntity>>(emptyList())
  val allUsers: StateFlow<List<UserEntity>> = _allUsers.asStateFlow()

  private val _allProfiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
  val allProfiles: StateFlow<List<ProfileEntity>> = _allProfiles.asStateFlow()

  private val _allFeedbacks = MutableStateFlow<List<FeedbackEntity>>(emptyList())
  val allFeedbacks: StateFlow<List<FeedbackEntity>> = _allFeedbacks.asStateFlow()

  private val _selectedTab = MutableStateFlow(AppNavTab.TODAY)
  val selectedTab: StateFlow<AppNavTab> = _selectedTab.asStateFlow()

  private val _roadmapPlan = MutableStateFlow<List<RoadmapStep>>(emptyList())
  val roadmapPlan: StateFlow<List<RoadmapStep>> = _roadmapPlan.asStateFlow()

  private val _roadmapSchema = MutableStateFlow<RoadmapSchema?>(null)
  val roadmapSchema: StateFlow<RoadmapSchema?> = _roadmapSchema.asStateFlow()

  private val _skillGaps = MutableStateFlow<List<SkillGap>>(emptyList())
  val skillGaps: StateFlow<List<SkillGap>> = _skillGaps.asStateFlow()

  private val _curriculumSkillGapReport = MutableStateFlow<CurriculumSkillGapReport?>(null)
  val curriculumSkillGapReport: StateFlow<CurriculumSkillGapReport?> = _curriculumSkillGapReport.asStateFlow()

  private val _quizResult = MutableStateFlow<QuizSubmissionResult?>(null)
  val quizResult: StateFlow<QuizSubmissionResult?> = _quizResult.asStateFlow()

  private val _aiCoachFeedback = MutableStateFlow<String?>(null)
  val aiCoachFeedback: StateFlow<String?> = _aiCoachFeedback.asStateFlow()

  private val _isCoachLoading = MutableStateFlow(false)
  val isCoachLoading: StateFlow<Boolean> = _isCoachLoading.asStateFlow()

  private val _aiExplanation = MutableStateFlow<String?>(null)
  val aiExplanation: StateFlow<String?> = _aiExplanation.asStateFlow()

  private val _isExplanationLoading = MutableStateFlow(false)
  val isExplanationLoading: StateFlow<Boolean> = _isExplanationLoading.asStateFlow()

  private val _uiMessage = MutableStateFlow<String?>(null)
  val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

  private val _badges = MutableStateFlow<List<com.example.model.Badge>>(emptyList())
  val badges: StateFlow<List<com.example.model.Badge>> = _badges.asStateFlow()

  private val _learnerRank = MutableStateFlow(
    com.example.model.LearnerRank(level = 1, title = "Novice Learner", currentXp = 0, nextLevelXp = 150, progressToNext = 0f)
  )
  val learnerRank: StateFlow<com.example.model.LearnerRank> = _learnerRank.asStateFlow()

  private val _themeMode = MutableStateFlow(ThemePreferences.getThemeMode(application))
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  fun setThemeMode(mode: ThemeMode) {
    _themeMode.value = mode
    ThemePreferences.setThemeMode(getApplication(), mode)
  }

  fun toggleThemeMode() {
    val nextMode = when (_themeMode.value) {
      ThemeMode.SYSTEM -> ThemeMode.DARK
      ThemeMode.DARK -> ThemeMode.LIGHT
      ThemeMode.LIGHT -> ThemeMode.SYSTEM
    }
    setThemeMode(nextMode)
  }

  init {
    initAppUser()
  }

  private fun initAppUser() {
    viewModelScope.launch(Dispatchers.IO) {
      val users = dao.getAllUsers()
      val user = if (users.isEmpty()) {
        val defaultUser = UserEntity(
          username = "Alex",
          createdAt = LocalDate.now().toString()
        )
        val id = dao.insertUser(defaultUser)
        val starterSkills = mapOf(
          "python" to 1,
          "sql" to 1,
          "statistics" to 1,
          "pandas" to 0,
          "visualization" to 1,
          "ml" to 0,
          "model_evaluation" to 0,
          "git" to 1,
          "projects" to 0,
          "deep_learning" to 0,
          "cloud" to 0,
          "mlops" to 0
        )
        val currentSkills = mapOf(
          "python" to 3,
          "sql" to 2,
          "statistics" to 3,
          "pandas" to 2,
          "visualization" to 2,
          "ml" to 1,
          "model_evaluation" to 1,
          "git" to 2,
          "projects" to 1,
          "deep_learning" to 0,
          "cloud" to 0,
          "mlops" to 0
        )
        val initialProfile = ProfileEntity(
          userId = id,
          goal = "Data Scientist",
          skillsStartJson = ProfileEntity.skillMapToJson(starterSkills),
          skillsNowJson = ProfileEntity.skillMapToJson(currentSkills),
          startDate = LocalDate.now().minusDays(5).toString()
        )
        dao.saveProfile(initialProfile)

        // Seed chronological skill proficiency history in Room over the last 5 days
        seedDefaultSkillHistory(userId = id, goal = "Data Scientist", starterSkills = starterSkills)

        // Starter study sessions
        dao.insertStudyLog(
          StudyLogEntity(
            userId = id,
            day = LocalDate.now().minusDays(2).toString(),
            minutes = 45,
            skill = "python",
            note = "[4/5 ★] Mastered list comprehensions and generators"
          )
        )
        dao.insertStudyLog(
          StudyLogEntity(
            userId = id,
            day = LocalDate.now().minusDays(1).toString(),
            minutes = 60,
            skill = "statistics",
            note = "[4/5 ★] Calculated hypothesis tests and variance"
          )
        )
        dao.insertStudyLog(
          StudyLogEntity(
            userId = id,
            day = LocalDate.now().toString(),
            minutes = 30,
            skill = "sql",
            note = "[5/5 ★] Practiced GROUP BY, HAVING, and JOINs"
          )
        )

        // Pre-test baseline
        val detailObj = JSONObject().apply {
          put("python", org.json.JSONArray(listOf(1, 1)))
          put("statistics", org.json.JSONArray(listOf(1, 1)))
          put("sql", org.json.JSONArray(listOf(0, 1)))
          put("pandas", org.json.JSONArray(listOf(0, 1)))
        }
        dao.insertAssessment(
          AssessmentEntity(
            userId = id,
            kind = "pre",
            score = 50,
            detailJson = detailObj.toString(),
            takenAt = LocalDate.now().minusDays(5).toString()
          )
        )

        defaultUser.copy(id = id)
      } else {
        val existingUser = users.first()
        val existingProfile = dao.getProfile(existingUser.id)
        if (existingProfile != null && dao.getSkillProficiencyHistory(existingUser.id).isEmpty()) {
          seedDefaultSkillHistory(
            userId = existingUser.id,
            goal = existingProfile.goal,
            starterSkills = existingProfile.getStartSkills()
          )
        }
        existingUser
      }

      _currentUser.value = user
      observeUserData(user.id)
      loadAdminData()
    }
  }

  private suspend fun seedDefaultSkillHistory(
    userId: Long,
    goal: String,
    starterSkills: Map<String, Int>
  ) {
    val baseTs = System.currentTimeMillis() - (5 * 86_400_000L)
    val running = starterSkills.toMutableMap()

    // Snapshot 1: Day -5 (Baseline)
    skillProficiencyRepository.recordSkillProficiencyChange(
      userId = userId,
      goal = goal,
      skill = "python",
      proficiencyLevel = running["python"] ?: 1,
      skillsSnapshot = running.toMap(),
      recordedDate = LocalDate.now().minusDays(5).toString(),
      timestamp = baseTs
    )

    // Snapshot 2: Day -4 (Python + Git progress)
    running["python"] = ((running["python"] ?: 1) + 1).coerceAtMost(5)
    running["git"] = ((running["git"] ?: 1) + 1).coerceAtMost(5)
    skillProficiencyRepository.recordSkillProficiencyChange(
      userId = userId,
      goal = goal,
      skill = "python",
      proficiencyLevel = running["python"] ?: 2,
      skillsSnapshot = running.toMap(),
      recordedDate = LocalDate.now().minusDays(4).toString(),
      timestamp = baseTs + 86_400_000L
    )

    // Snapshot 3: Day -3 (Pandas & Statistics progress)
    running["pandas"] = ((running["pandas"] ?: 0) + 1).coerceAtMost(5)
    running["statistics"] = ((running["statistics"] ?: 1) + 1).coerceAtMost(5)
    skillProficiencyRepository.recordSkillProficiencyChange(
      userId = userId,
      goal = goal,
      skill = "pandas",
      proficiencyLevel = running["pandas"] ?: 1,
      skillsSnapshot = running.toMap(),
      recordedDate = LocalDate.now().minusDays(3).toString(),
      timestamp = baseTs + (2 * 86_400_000L)
    )

    // Snapshot 4: Day -2 (Python + Visualization progress)
    running["python"] = ((running["python"] ?: 2) + 1).coerceAtMost(5)
    running["visualization"] = ((running["visualization"] ?: 1) + 1).coerceAtMost(5)
    skillProficiencyRepository.recordSkillProficiencyChange(
      userId = userId,
      goal = goal,
      skill = "python",
      proficiencyLevel = running["python"] ?: 3,
      skillsSnapshot = running.toMap(),
      recordedDate = LocalDate.now().minusDays(2).toString(),
      timestamp = baseTs + (3 * 86_400_000L)
    )

    // Snapshot 5: Day -1 (Statistics & ML fundamentals)
    running["statistics"] = ((running["statistics"] ?: 2) + 1).coerceAtMost(5)
    running["ml"] = ((running["ml"] ?: 0) + 1).coerceAtMost(5)
    running["pandas"] = ((running["pandas"] ?: 1) + 1).coerceAtMost(5)
    skillProficiencyRepository.recordSkillProficiencyChange(
      userId = userId,
      goal = goal,
      skill = "statistics",
      proficiencyLevel = running["statistics"] ?: 3,
      skillsSnapshot = running.toMap(),
      recordedDate = LocalDate.now().minusDays(1).toString(),
      timestamp = baseTs + (4 * 86_400_000L)
    )

    // Snapshot 6: Today (SQL, Model Evaluation & Projects)
    running["sql"] = ((running["sql"] ?: 1) + 1).coerceAtMost(5)
    running["model_evaluation"] = ((running["model_evaluation"] ?: 0) + 1).coerceAtMost(5)
    running["projects"] = ((running["projects"] ?: 0) + 1).coerceAtMost(5)
    skillProficiencyRepository.recordSkillProficiencyChange(
      userId = userId,
      goal = goal,
      skill = "sql",
      proficiencyLevel = running["sql"] ?: 2,
      skillsSnapshot = running.toMap(),
      recordedDate = LocalDate.now().toString(),
      timestamp = baseTs + (5 * 86_400_000L)
    )
  }

  private fun observeUserData(userId: Long) {
    viewModelScope.launch(Dispatchers.IO) {
      dao.getProfileFlow(userId).collectLatest { profile ->
        _currentProfile.value = profile
        recalculateRoadmap(profile)
      }
    }

    viewModelScope.launch(Dispatchers.IO) {
      dao.getStepsDoneFlow(userId).collectLatest { steps ->
        _stepsDone.value = steps.map { it.skill }.toSet()
        recalculateRoadmap(_currentProfile.value)
      }
    }

    viewModelScope.launch(Dispatchers.IO) {
      dao.getStudyLogsFlow(userId).collectLatest { logs ->
        _studyLogs.value = logs
        recalculateRoadmap(_currentProfile.value)
        updateGamification()
      }
    }

    viewModelScope.launch(Dispatchers.IO) {
      dao.getAssessmentsFlow(userId).collectLatest { asm ->
        _assessments.value = asm
        updateGamification()
      }
    }

    viewModelScope.launch(Dispatchers.IO) {
      skillProficiencyRepository.observeUserSkillHistory(userId).collectLatest { history ->
        _skillProficiencyHistory.value = history
      }
    }
  }

  fun loadAdminData() {
    viewModelScope.launch(Dispatchers.IO) {
      _allUsers.value = dao.getAllUsers()
      _allProfiles.value = dao.getAllProfiles()
      _allFeedbacks.value = dao.getAllFeedback()
    }
  }

  fun setTab(tab: AppNavTab) {
    _selectedTab.value = tab
    _quizResult.value = null
  }

  fun clearMessage() {
    _uiMessage.value = null
  }

  fun clearExplanation() {
    _aiExplanation.value = null
  }

  private fun recalculateRoadmap(profile: ProfileEntity?) {
    if (profile == null) {
      _roadmapPlan.value = emptyList()
      _skillGaps.value = emptyList()
      _curriculumSkillGapReport.value = null
      _roadmapSchema.value = null
      return
    }
    val startGaps = RoadmapEngine.findGaps(profile.getStartSkills(), profile.goal)
    val currentGaps = RoadmapEngine.findGaps(profile.getNowSkills(), profile.goal)
    _skillGaps.value = currentGaps

    val studyMinutesBySkill = _studyLogs.value
      .groupBy { it.skill }
      .mapValues { (_, list) -> list.sumOf { it.minutes } }
    val currentWeek = RoadmapEngine.currentWeek(profile.startDate)

    _curriculumSkillGapReport.value = RoadmapEngine.calculateSkillGap(
      currentSkills = profile.getNowSkills(),
      goal = profile.goal,
      completedSteps = _stepsDone.value,
      currentWeek = currentWeek,
      studyMinutesBySkill = studyMinutesBySkill
    )

    val plan = RoadmapEngine.buildRoadmap(startGaps, profile.getStartSkills())
    _roadmapPlan.value = plan

    // Build the formal Roadmap Schema
    val schema = RoadmapSchema.createFromPlan(
      goal = profile.goal,
      plan = plan,
      stepsDone = _stepsDone.value,
      startSkills = profile.getStartSkills()
    )
    _roadmapSchema.value = schema
    updateGamification()
  }

  fun updateGamification() {
    val b = com.example.model.GamificationEngine.evaluateBadges(
      profile = _currentProfile.value,
      studyLogs = _studyLogs.value,
      stepsDone = _stepsDone.value,
      roadmapPlan = _roadmapPlan.value,
      assessments = _assessments.value
    )
    _badges.value = b
    _learnerRank.value = com.example.model.GamificationEngine.calculateRank(b, _studyLogs.value)
  }

  fun saveProfile(goal: String, startSkills: Map<String, Int>) {
    val user = _currentUser.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val profile = ProfileEntity(
        userId = user.id,
        goal = goal,
        skillsStartJson = ProfileEntity.skillMapToJson(startSkills),
        skillsNowJson = ProfileEntity.skillMapToJson(startSkills),
        startDate = LocalDate.now().toString()
      )
      dao.saveProfile(profile)
      dao.deleteStepsDone(user.id)
      skillProficiencyRepository.clearUserSkillHistory(user.id)
      val firstSkill = startSkills.keys.firstOrNull() ?: "overall"
      skillProficiencyRepository.recordSkillProficiencyChange(
        userId = user.id,
        goal = goal,
        skill = firstSkill,
        proficiencyLevel = startSkills[firstSkill] ?: 0,
        skillsSnapshot = startSkills,
        recordedDate = LocalDate.now().toString()
      )
      _uiMessage.value = "Roadmap Schema generated for $goal!"
      setTab(AppNavTab.TODAY)
    }
  }

  fun markStepComplete(skill: String) {
    val user = _currentUser.value ?: return
    val profile = _currentProfile.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.insertStepDone(
        StepDoneEntity(
          userId = user.id,
          skill = skill,
          doneAt = LocalDate.now().toString()
        )
      )

      val targetLevel = CareerRoadmapData.CAREERS[profile.goal]?.get(skill)?.level ?: 3
      val currentNow = profile.getNowSkills().toMutableMap()
      val newLevel = maxOf(currentNow[skill] ?: 0, targetLevel)
      currentNow[skill] = newLevel

      val updatedProfile = profile.copy(
        skillsNowJson = ProfileEntity.skillMapToJson(currentNow)
      )
      dao.saveProfile(updatedProfile)
      skillProficiencyRepository.recordSkillProficiencyChange(
        userId = user.id,
        goal = profile.goal,
        skill = skill,
        proficiencyLevel = newLevel,
        skillsSnapshot = currentNow,
        recordedDate = LocalDate.now().toString()
      )
      _uiMessage.value = "${CareerRoadmapData.getSkillLabel(skill)} marked complete! 🎉"
    }
  }

  fun updateSkillLevel(skill: String, level: Int) {
    val user = _currentUser.value ?: return
    val profile = _currentProfile.value ?: return
    val clamped = level.coerceIn(0, 5)
    viewModelScope.launch(Dispatchers.IO) {
      val currentNow = profile.getNowSkills().toMutableMap()
      currentNow[skill] = clamped
      val updatedProfile = profile.copy(
        skillsNowJson = ProfileEntity.skillMapToJson(currentNow)
      )
      dao.saveProfile(updatedProfile)
      skillProficiencyRepository.recordSkillProficiencyChange(
        userId = user.id,
        goal = profile.goal,
        skill = skill,
        proficiencyLevel = clamped,
        skillsSnapshot = currentNow,
        recordedDate = LocalDate.now().toString()
      )
      _uiMessage.value = "${CareerRoadmapData.getSkillLabel(skill)} level set to $clamped / 5"
    }
  }

  fun logStudy(minutes: Int, skill: String, note: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.insertStudyLog(
        StudyLogEntity(
          userId = user.id,
          day = LocalDate.now().toString(),
          minutes = minutes,
          skill = skill,
          note = note
        )
      )
      _uiMessage.value = "Logged $minutes min study session! 🔥"
    }
  }

  fun requestCoachReview() {
    val profile = _currentProfile.value ?: return
    val logs = _studyLogs.value
    val streak = RoadmapEngine.streakDays(logs.map { it.day })

    viewModelScope.launch {
      aiRepository.generateCoachReview(logs, profile.goal, streak).collectLatest { res ->
        when (res) {
          is AiResult.Loading -> _isCoachLoading.value = true
          is AiResult.Success -> {
            _isCoachLoading.value = false
            _aiCoachFeedback.value = res.data
          }
          is AiResult.Error -> {
            _isCoachLoading.value = false
            _aiCoachFeedback.value = res.message
          }
        }
      }
    }
  }

  fun requestConceptExplanation(skill: String, topic: String) {
    viewModelScope.launch {
      aiRepository.explainConcept(skill, topic).collectLatest { res ->
        when (res) {
          is AiResult.Loading -> _isExplanationLoading.value = true
          is AiResult.Success -> {
            _isExplanationLoading.value = false
            _aiExplanation.value = res.data
          }
          is AiResult.Error -> {
            _isExplanationLoading.value = false
            _aiExplanation.value = res.message
          }
        }
      }
    }
  }

  fun generateCustomAiRoadmap(customGoal: String, skills: Map<String, Int>) {
    viewModelScope.launch {
      aiRepository.generateCustomRoadmap(customGoal, skills).collectLatest { res ->
        when (res) {
          is AiResult.Loading -> {
            _uiMessage.value = "AI is designing your custom curriculum schema..."
          }
          is AiResult.Success -> {
            _roadmapSchema.value = res.data
            _uiMessage.value = "Custom AI Roadmap generated for ${res.data.careerGoal}!"
          }
          is AiResult.Error -> {
            _uiMessage.value = "Could not generate custom roadmap: ${res.message}"
          }
        }
      }
    }
  }

  fun submitQuiz(kind: String, answers: Map<Int, String>) {
    val user = _currentUser.value ?: return
    val profile = _currentProfile.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val formLetter = if (kind.equals("pre", ignoreCase = true)) "A" else "B"
      val careerSkills = CareerRoadmapData.CAREERS[profile.goal]?.keys ?: emptySet()
      val questions = CareerRoadmapData.QUIZ_QUESTIONS.filter {
        it.form == formLetter && it.skill in careerSkills
      }

      if (questions.isEmpty()) return@launch

      var correctCount = 0
      val breakdown = mutableMapOf<String, Pair<Int, Int>>()
      val detailJsonObj = JSONObject()

      for (q in questions) {
        val userAns = answers[q.id] ?: ""
        val isCorrect = userAns.equals(q.answer, ignoreCase = true)
        if (isCorrect) correctCount++

        val current = breakdown[q.skill] ?: (0 to 0)
        breakdown[q.skill] = (current.first + if (isCorrect) 1 else 0) to (current.second + 1)
      }

      for ((skill, counts) in breakdown) {
        detailJsonObj.put(skill, org.json.JSONArray(listOf(counts.first, counts.second)))
      }

      val score = ((correctCount.toDouble() / questions.size.toDouble()) * 100).toInt()
      dao.insertAssessment(
        AssessmentEntity(
          userId = user.id,
          kind = kind.lowercase(),
          score = score,
          detailJson = detailJsonObj.toString(),
          takenAt = LocalDate.now().toString()
        )
      )

      var learningGain: LearningGain? = null
      if (kind.equals("post", ignoreCase = true)) {
        val prevPre = _assessments.value.firstOrNull { it.kind == "pre" }
        if (prevPre != null) {
          learningGain = RoadmapEngine.assessmentImprovement(prevPre.score, score)
        }
      }

      _quizResult.value = QuizSubmissionResult(
        score = score,
        totalQuestions = questions.size,
        correctCount = correctCount,
        skillBreakdown = breakdown,
        learningGain = learningGain
      )
      _uiMessage.value = "Quiz submitted! Score: $score%"
    }
  }

  fun submitFeedback(clarity: Int, usefulness: Int, topicOrder: Int, comment: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.insertFeedback(
        FeedbackEntity(
          userId = user.id,
          clarity = clarity,
          usefulness = usefulness,
          topicOrder = topicOrder,
          commentText = comment,
          createdAt = LocalDate.now().toString()
        )
      )
      loadAdminData()
      _uiMessage.value = "Thank you! Your feedback helps optimize the roadmaps."
    }
  }

  fun resetPlan() {
    val user = _currentUser.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.deleteProfile(user.id)
      dao.deleteStepsDone(user.id)
      skillProficiencyRepository.clearUserSkillHistory(user.id)
      _currentProfile.value = null
      _roadmapPlan.value = emptyList()
      _roadmapSchema.value = null
      _skillGaps.value = emptyList()
      _curriculumSkillGapReport.value = null
      _uiMessage.value = "Plan reset. You can now set up a new career roadmap."
    }
  }

  fun switchOrRegisterUser(username: String) {
    val cleanName = username.trim()
    if (cleanName.isEmpty()) return
    viewModelScope.launch(Dispatchers.IO) {
      val existing = dao.getUserByUsername(cleanName)
      val user = if (existing != null) {
        existing
      } else {
        val newUser = UserEntity(
          username = cleanName,
          createdAt = LocalDate.now().toString()
        )
        val id = dao.insertUser(newUser)
        newUser.copy(id = id)
      }
      _currentUser.value = user
      observeUserData(user.id)
      loadAdminData()
      _uiMessage.value = "Switched to learner: ${user.username}"
    }
  }
}
