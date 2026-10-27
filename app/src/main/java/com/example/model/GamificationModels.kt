package com.example.model

import com.example.data.AssessmentEntity
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.data.StudyLogEntity
import com.example.engine.RoadmapEngine

enum class BadgeCategory(val displayName: String) {
  STREAK("Consistency"),
  STUDY_HOURS("Dedication"),
  ROADMAP("Milestones"),
  ASSESSMENTS("Skill Checks"),
  MASTERY("Mastery")
}

data class Badge(
  val id: String,
  val title: String,
  val description: String,
  val iconEmoji: String,
  val xpReward: Int,
  val category: BadgeCategory,
  val isUnlocked: Boolean,
  val progress: Float, // 0.0 to 1.0
  val progressText: String
)

data class LearnerRank(
  val level: Int,
  val title: String,
  val currentXp: Int,
  val nextLevelXp: Int,
  val progressToNext: Float
)

object GamificationEngine {

  fun evaluateBadges(
    profile: ProfileEntity?,
    studyLogs: List<StudyLogEntity>,
    stepsDone: Set<String>,
    roadmapPlan: List<RoadmapStep>,
    assessments: List<AssessmentEntity>
  ): List<Badge> {
    val streak = RoadmapEngine.streakDays(studyLogs.map { it.day })
    val totalMinutes = studyLogs.sumOf { it.minutes }
    val completedStepsCount = stepsDone.size
    val totalSteps = roadmapPlan.size.coerceAtLeast(1)

    val preAssessment = assessments.firstOrNull { it.kind == "pre" }
    val postAssessment = assessments.lastOrNull { it.kind == "post" }
    val learningGain = if (preAssessment != null && postAssessment != null) {
      RoadmapEngine.assessmentImprovement(preAssessment.score, postAssessment.score).normalizedGain
    } else 0.0

    val nowSkills = profile?.getNowSkills() ?: emptyMap()
    val maxSkillLevel = nowSkills.values.maxOrNull() ?: 0

    return listOf(
      // 1. Consistency / Streaks
      Badge(
        id = "streak_1",
        title = "Streak Starter",
        description = "Log a study session on at least 1 day.",
        iconEmoji = "🔥",
        xpReward = 50,
        category = BadgeCategory.STREAK,
        isUnlocked = streak >= 1,
        progress = (streak / 1f).coerceIn(0f, 1f),
        progressText = "$streak / 1 day"
      ),
      Badge(
        id = "streak_3",
        title = "Habit Hero",
        description = "Maintain a 3-day consecutive study streak.",
        iconEmoji = "⚡",
        xpReward = 100,
        category = BadgeCategory.STREAK,
        isUnlocked = streak >= 3,
        progress = (streak / 3f).coerceIn(0f, 1f),
        progressText = "$streak / 3 days"
      ),
      Badge(
        id = "streak_7",
        title = "Week Warrior",
        description = "Complete a full 7-day study streak without missing a day.",
        iconEmoji = "👑",
        xpReward = 250,
        category = BadgeCategory.STREAK,
        isUnlocked = streak >= 7,
        progress = (streak / 7f).coerceIn(0f, 1f),
        progressText = "$streak / 7 days"
      ),

      // 2. Study Time / Dedication
      Badge(
        id = "hours_1",
        title = "First Hour",
        description = "Log at least 60 minutes of focused study time.",
        iconEmoji = "⏱️",
        xpReward = 50,
        category = BadgeCategory.STUDY_HOURS,
        isUnlocked = totalMinutes >= 60,
        progress = (totalMinutes / 60f).coerceIn(0f, 1f),
        progressText = "$totalMinutes / 60 min"
      ),
      Badge(
        id = "hours_3",
        title = "Deep Diver",
        description = "Log 180+ minutes (3 hours) of dedicated study.",
        iconEmoji = "⏳",
        xpReward = 150,
        category = BadgeCategory.STUDY_HOURS,
        isUnlocked = totalMinutes >= 180,
        progress = (totalMinutes / 180f).coerceIn(0f, 1f),
        progressText = "$totalMinutes / 180 min"
      ),
      Badge(
        id = "hours_8",
        title = "Century Club",
        description = "Accumulate 500+ minutes (~8.5 hours) of logged study.",
        iconEmoji = "🏆",
        xpReward = 300,
        category = BadgeCategory.STUDY_HOURS,
        isUnlocked = totalMinutes >= 500,
        progress = (totalMinutes / 500f).coerceIn(0f, 1f),
        progressText = "$totalMinutes / 500 min"
      ),

      // 3. Roadmap Milestones
      Badge(
        id = "milestone_1",
        title = "First Milestone",
        description = "Mark your first sequential roadmap step complete.",
        iconEmoji = "🎯",
        xpReward = 75,
        category = BadgeCategory.ROADMAP,
        isUnlocked = completedStepsCount >= 1,
        progress = (completedStepsCount / 1f).coerceIn(0f, 1f),
        progressText = "$completedStepsCount / 1 step"
      ),
      Badge(
        id = "milestone_half",
        title = "Halfway There",
        description = "Complete 50% of your career roadmap steps.",
        iconEmoji = "🚀",
        xpReward = 200,
        category = BadgeCategory.ROADMAP,
        isUnlocked = completedStepsCount >= (totalSteps + 1) / 2,
        progress = (completedStepsCount.toFloat() / ((totalSteps + 1) / 2).toFloat()).coerceIn(0f, 1f),
        progressText = "$completedStepsCount / ${(totalSteps + 1) / 2} steps"
      ),
      Badge(
        id = "milestone_graduate",
        title = "Roadmap Graduate",
        description = "Complete 100% of all planned roadmap milestones.",
        iconEmoji = "🎓",
        xpReward = 500,
        category = BadgeCategory.ROADMAP,
        isUnlocked = completedStepsCount >= totalSteps && totalSteps > 0,
        progress = (completedStepsCount.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f),
        progressText = "$completedStepsCount / $totalSteps steps"
      ),

      // 4. Assessments & Learning Gain
      Badge(
        id = "quiz_baseline",
        title = "Baseline Scout",
        description = "Complete the Pre-Test baseline assessment.",
        iconEmoji = "📝",
        xpReward = 75,
        category = BadgeCategory.ASSESSMENTS,
        isUnlocked = preAssessment != null,
        progress = if (preAssessment != null) 1f else 0f,
        progressText = if (preAssessment != null) "Completed (${preAssessment.score}%)" else "Take Pre-Test"
      ),
      Badge(
        id = "quiz_gain",
        title = "Growth Mindset",
        description = "Achieve a normalized learning gain (g > 0.3) after studying.",
        iconEmoji = "📈",
        xpReward = 200,
        category = BadgeCategory.ASSESSMENTS,
        isUnlocked = learningGain >= 0.3,
        progress = (learningGain.toFloat() / 0.3f).coerceIn(0f, 1f),
        progressText = if (postAssessment != null) "g = $learningGain" else "Take Post-Test"
      ),

      // 5. Mastery
      Badge(
        id = "mastery_expert",
        title = "Skill Master",
        description = "Reach Level 4 or 5 proficiency in any core skill.",
        iconEmoji = "🌟",
        xpReward = 150,
        category = BadgeCategory.MASTERY,
        isUnlocked = maxSkillLevel >= 4,
        progress = (maxSkillLevel / 4f).coerceIn(0f, 1f),
        progressText = "Top Level: $maxSkillLevel / 4"
      )
    )
  }

  fun calculateRank(badges: List<Badge>, studyLogs: List<StudyLogEntity>): LearnerRank {
    // XP from unlocked badges + 10 XP per study session logged
    val badgeXp = badges.filter { it.isUnlocked }.sumOf { it.xpReward }
    val sessionXp = studyLogs.size * 15
    val totalXp = badgeXp + sessionXp

    val rankTiers = listOf(
      0 to "Novice Learner",
      150 to "Apprentice Scholar",
      350 to "Career Explorer",
      650 to "Applied Practitioner",
      1050 to "Domain Specialist",
      1550 to "Roadmap Master",
      2200 to "Industry Expert"
    )

    var currentLevel = 1
    var title = "Novice Learner"
    var nextLevelXp = 150

    for (i in rankTiers.indices) {
      val (xpThreshold, rankTitle) = rankTiers[i]
      if (totalXp >= xpThreshold) {
        currentLevel = i + 1
        title = rankTitle
        nextLevelXp = rankTiers.getOrNull(i + 1)?.first ?: (xpThreshold + 800)
      } else {
        break
      }
    }

    val prevThreshold = rankTiers.getOrNull(currentLevel - 1)?.first ?: 0
    val progress = ((totalXp - prevThreshold).toFloat() / (nextLevelXp - prevThreshold).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    return LearnerRank(
      level = currentLevel,
      title = title,
      currentXp = totalXp,
      nextLevelXp = nextLevelXp,
      progressToNext = progress
    )
  }
}
