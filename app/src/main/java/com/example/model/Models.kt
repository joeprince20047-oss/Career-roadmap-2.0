package com.example.model

data class SkillRequirement(
  val skill: String,
  val level: Int,
  val weight: Double
)

data class ResourceItem(
  val skill: String,
  val title: String,
  val url: String,
  val hours: Int
)

data class QuizQuestion(
  val id: Int,
  val skill: String,
  val form: String, // "A" (pre-test) or "B" (post-test)
  val question: String,
  val a: String,
  val b: String,
  val c: String,
  val d: String,
  val answer: String // "A", "B", "C", "D"
)

data class SkillGap(
  val skill: String,
  val gap: Int,
  val weight: Double,
  val priority: Double
)

data class RoadmapStep(
  val skill: String,
  val startWeek: Int,
  val endWeek: Int,
  val levelsToGain: Int
)

data class LearningGain(
  val rawGain: Int,
  val normalizedGain: Double
)

enum class StepStatus {
  COMPLETED,
  THIS_WEEK,
  BEHIND,
  UPCOMING
}

/**
 * Defines a single skill module within the predefined AI/Data Science curriculum roadmap.
 */
data class CurriculumModule(
  val skillId: String,
  val title: String,
  val phaseIndex: Int,
  val phaseTitle: String,
  val targetLevel: Int,
  val weight: Double,
  val estimatedHoursPerLevel: Int,
  val targetStartWeek: Int,
  val targetEndWeek: Int,
  val prerequisites: List<String> = emptyList(),
  val coreCompetencies: List<String> = emptyList()
)

enum class CurriculumGapStatus(val label: String) {
  MASTERED("Mastered"),
  ON_TRACK("Ready / On Track"),
  BEHIND_SCHEDULE("Behind Schedule"),
  CRITICAL_GAP("Critical Gap"),
  PREREQ_BLOCKED("Prereq Locked")
}

/**
 * Detailed skill gap result for a single module in the AI/Data Science curriculum roadmap.
 */
data class CurriculumSkillGapItem(
  val skillId: String,
  val skillTitle: String,
  val phaseIndex: Int,
  val phaseTitle: String,
  val currentLevel: Int,
  val targetLevel: Int,
  val levelGap: Int,
  val completionRatio: Double,
  val weight: Double,
  val priorityScore: Double,
  val targetStartWeek: Int,
  val targetEndWeek: Int,
  val loggedStudyHours: Double,
  val estimatedHoursRemaining: Double,
  val prerequisites: List<String>,
  val unmetPrerequisites: List<String>,
  val isPrerequisiteReady: Boolean,
  val status: CurriculumGapStatus,
  val recommendedAction: String
)

/**
 * Aggregated skill gap metrics for a curriculum phase.
 */
data class CurriculumPhaseGapSummary(
  val phaseIndex: Int,
  val phaseTitle: String,
  val readinessPercent: Double,
  val masteredCount: Int,
  val totalSkillsCount: Int,
  val remainingLevelGap: Int,
  val estimatedHoursRemaining: Double
)

/**
 * Overall AI/Data Science curriculum skill gap analysis report comparing user's current progress
 * against the predefined curriculum roadmap.
 */
data class CurriculumSkillGapReport(
  val curriculumTitle: String,
  val overallReadinessPercent: Double,
  val totalSkillsCount: Int,
  val masteredSkillsCount: Int,
  val skillsWithGapCount: Int,
  val totalCurrentLevels: Int,
  val totalTargetLevels: Int,
  val totalRemainingLevelGap: Int,
  val estimatedHoursRemaining: Double,
  val moduleGaps: List<CurriculumSkillGapItem>,
  val prioritizedGaps: List<CurriculumSkillGapItem>,
  val phaseSummaries: List<CurriculumPhaseGapSummary>,
  val topRecommendedNextSkills: List<CurriculumSkillGapItem>
)

