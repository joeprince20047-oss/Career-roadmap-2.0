package com.example.engine

import com.example.data.CareerRoadmapData
import com.example.model.CurriculumGapStatus
import com.example.model.CurriculumModule
import com.example.model.CurriculumPhaseGapSummary
import com.example.model.CurriculumSkillGapItem
import com.example.model.CurriculumSkillGapReport
import com.example.model.LearningGain
import com.example.model.RoadmapStep
import com.example.model.SkillGap
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object RoadmapEngine {

  fun fitScore(
    profile: Map<String, Int>,
    career: String,
    careers: Map<String, Map<String, com.example.model.SkillRequirement>> = CareerRoadmapData.CAREERS
  ): Double {
    val reqs = careers[career] ?: return 0.0
    var got = 0.0
    var need = 0.0
    for ((skill, req) in reqs) {
      val userLvl = profile[skill] ?: 0
      got += min(userLvl, req.level) * req.weight
      need += req.level * req.weight
    }
    if (need == 0.0) return 0.0
    return ((got / need) * 1000.0).roundToInt() / 10.0
  }

  fun recommend(
    profile: Map<String, Int>,
    careers: Map<String, Map<String, com.example.model.SkillRequirement>> = CareerRoadmapData.CAREERS
  ): List<Pair<String, Double>> {
    return careers.keys.map { career ->
      career to fitScore(profile, career, careers)
    }.sortedByDescending { it.second }
  }

  fun findGaps(
    profile: Map<String, Int>,
    career: String,
    careers: Map<String, Map<String, com.example.model.SkillRequirement>> = CareerRoadmapData.CAREERS
  ): List<SkillGap> {
    val reqs = careers[career] ?: return emptyList()
    val gaps = mutableListOf<SkillGap>()
    for ((skill, req) in reqs) {
      val userLvl = profile[skill] ?: 0
      val gap = req.level - userLvl
      if (gap > 0) {
        val priority = ((gap * req.weight) * 100.0).roundToInt() / 100.0
        gaps.add(SkillGap(skill = skill, gap = gap, weight = req.weight, priority = priority))
      }
    }
    return gaps.sortedByDescending { it.priority }
  }

  /**
   * Calculates the skill gap by comparing the user's current progress (skill levels, completed
   * roadmap steps, active curriculum week, and logged study minutes) against a predefined
   * AI/Data Science curriculum roadmap.
   */
  fun calculateCurriculumSkillGap(
    currentSkills: Map<String, Int>,
    completedSteps: Set<String> = emptySet(),
    currentWeek: Int = 1,
    studyMinutesBySkill: Map<String, Int> = emptyMap(),
    curriculumRoadmap: List<CurriculumModule> = CareerRoadmapData.AI_DATA_SCIENCE_CURRICULUM_ROADMAP,
    curriculumTitle: String = "AI & Data Science Curriculum Roadmap"
  ): CurriculumSkillGapReport {
    if (curriculumRoadmap.isEmpty()) {
      return CurriculumSkillGapReport(
        curriculumTitle = curriculumTitle,
        overallReadinessPercent = 100.0,
        totalSkillsCount = 0,
        masteredSkillsCount = 0,
        skillsWithGapCount = 0,
        totalCurrentLevels = 0,
        totalTargetLevels = 0,
        totalRemainingLevelGap = 0,
        estimatedHoursRemaining = 0.0,
        moduleGaps = emptyList(),
        prioritizedGaps = emptyList(),
        phaseSummaries = emptyList(),
        topRecommendedNextSkills = emptyList()
      )
    }

    val targetBySkill = curriculumRoadmap.associate { it.skillId to it.targetLevel }

    fun effectiveSkillLevel(skillId: String): Int {
      val raw = (currentSkills[skillId] ?: 0).coerceIn(0, 5)
      val target = targetBySkill[skillId] ?: 3
      return if (skillId in completedSteps) max(raw, target) else raw
    }

    var totalWeightedGot = 0.0
    var totalWeightedNeed = 0.0
    var totalCurrentLvls = 0
    var totalTargetLvls = 0

    val moduleItems = curriculumRoadmap.map { module ->
      val currentLvl = effectiveSkillLevel(module.skillId)
      val cappedCurrent = min(currentLvl, module.targetLevel)
      val levelGap = max(0, module.targetLevel - currentLvl)
      val completionRatio = if (module.targetLevel > 0) {
        ((cappedCurrent.toDouble() / module.targetLevel.toDouble()) * 1000.0).roundToInt() / 1000.0
      } else {
        1.0
      }

      totalWeightedGot += cappedCurrent * module.weight
      totalWeightedNeed += module.targetLevel * module.weight
      totalCurrentLvls += cappedCurrent
      totalTargetLvls += module.targetLevel

      // Prerequisite check: prerequisite is satisfied if user has at least level 2 or completed it
      val unmetPrereqs = module.prerequisites.filter { prereqId ->
        val prereqTarget = min(2, targetBySkill[prereqId] ?: 2)
        effectiveSkillLevel(prereqId) < prereqTarget && prereqId !in completedSteps
      }
      val isPrereqReady = unmetPrereqs.isEmpty()

      // Study hours calculation
      val loggedMinutes = studyMinutesBySkill[module.skillId] ?: 0
      val loggedHours = ((loggedMinutes / 60.0) * 10.0).roundToInt() / 10.0
      val baseHoursNeeded = levelGap * module.estimatedHoursPerLevel.toDouble()
      val studyCredit = min(loggedHours * 0.5, baseHoursNeeded * 0.4)
      val hoursRemaining = if (levelGap == 0) {
        0.0
      } else {
        max(1.0, ((baseHoursNeeded - studyCredit) * 10.0).roundToInt() / 10.0)
      }

      val status = when {
        levelGap == 0 -> CurriculumGapStatus.MASTERED
        !isPrereqReady -> CurriculumGapStatus.PREREQ_BLOCKED
        currentWeek > module.targetEndWeek -> CurriculumGapStatus.BEHIND_SCHEDULE
        levelGap >= 3 && module.weight >= 0.85 -> CurriculumGapStatus.CRITICAL_GAP
        else -> CurriculumGapStatus.ON_TRACK
      }

      val priorityScore = if (levelGap == 0) {
        0.0
      } else {
        val basePriority = levelGap * module.weight
        val prereqBonus = if (isPrereqReady) 0.5 else -0.4
        val scheduleBonus = when {
          currentWeek > module.targetEndWeek -> 0.45
          currentWeek >= module.targetStartWeek -> 0.25
          else -> 0.0
        }
        val phaseBonus = max(0, 5 - module.phaseIndex) * 0.12
        max(0.05, ((basePriority + prereqBonus + scheduleBonus + phaseBonus) * 100.0).roundToInt() / 100.0)
      }

      val focusTopic = module.coreCompetencies.firstOrNull() ?: "core fundamentals"
      val recommendedAction = when (status) {
        CurriculumGapStatus.MASTERED ->
          "Target level ${module.targetLevel}/5 achieved. Reinforce with capstone practice."
        CurriculumGapStatus.PREREQ_BLOCKED ->
          "Unlock by raising ${unmetPrereqs.joinToString { CareerRoadmapData.getSkillLabel(it) }} to Level 2+ first."
        CurriculumGapStatus.BEHIND_SCHEDULE ->
          "Schedule catch-up (+$levelGap lvl needed, ~${hoursRemaining.toInt()}h): focus on $focusTopic."
        CurriculumGapStatus.CRITICAL_GAP ->
          "High-impact AI/DS gap (+$levelGap lvl needed): prioritize $focusTopic."
        CurriculumGapStatus.ON_TRACK ->
          "Advance +$levelGap lvl (Weeks ${module.targetStartWeek}–${module.targetEndWeek}): practice $focusTopic."
      }

      CurriculumSkillGapItem(
        skillId = module.skillId,
        skillTitle = module.title,
        phaseIndex = module.phaseIndex,
        phaseTitle = module.phaseTitle,
        currentLevel = currentLvl,
        targetLevel = module.targetLevel,
        levelGap = levelGap,
        completionRatio = completionRatio,
        weight = module.weight,
        priorityScore = priorityScore,
        targetStartWeek = module.targetStartWeek,
        targetEndWeek = module.targetEndWeek,
        loggedStudyHours = loggedHours,
        estimatedHoursRemaining = hoursRemaining,
        prerequisites = module.prerequisites,
        unmetPrerequisites = unmetPrereqs,
        isPrerequisiteReady = isPrereqReady,
        status = status,
        recommendedAction = recommendedAction
      )
    }

    val overallReadiness = if (totalWeightedNeed > 0.0) {
      ((totalWeightedGot / totalWeightedNeed) * 1000.0).roundToInt() / 10.0
    } else {
      100.0
    }

    val prioritizedGaps = moduleItems
      .filter { it.levelGap > 0 }
      .sortedWith(
        compareByDescending<CurriculumSkillGapItem> { it.isPrerequisiteReady }
          .thenByDescending { it.priorityScore }
          .thenBy { it.phaseIndex }
      )

    val phaseSummaries = moduleItems
      .groupBy { it.phaseIndex to it.phaseTitle }
      .map { (phaseKey, items) ->
        var phaseGot = 0.0
        var phaseNeed = 0.0
        for (item in items) {
          phaseGot += min(item.currentLevel, item.targetLevel) * item.weight
          phaseNeed += item.targetLevel * item.weight
        }
        val phaseReadiness = if (phaseNeed > 0.0) {
          ((phaseGot / phaseNeed) * 1000.0).roundToInt() / 10.0
        } else {
          100.0
        }
        CurriculumPhaseGapSummary(
          phaseIndex = phaseKey.first,
          phaseTitle = phaseKey.second,
          readinessPercent = phaseReadiness,
          masteredCount = items.count { it.levelGap == 0 },
          totalSkillsCount = items.size,
          remainingLevelGap = items.sumOf { it.levelGap },
          estimatedHoursRemaining = ((items.sumOf { it.estimatedHoursRemaining }) * 10.0).roundToInt() / 10.0
        )
      }
      .sortedBy { it.phaseIndex }

    val unlockedNext = prioritizedGaps.filter { it.isPrerequisiteReady }.take(3)
    val topRecommended = unlockedNext.ifEmpty { prioritizedGaps.take(3) }

    val totalHoursRem = ((moduleItems.sumOf { it.estimatedHoursRemaining }) * 10.0).roundToInt() / 10.0

    return CurriculumSkillGapReport(
      curriculumTitle = curriculumTitle,
      overallReadinessPercent = overallReadiness,
      totalSkillsCount = moduleItems.size,
      masteredSkillsCount = moduleItems.count { it.levelGap == 0 },
      skillsWithGapCount = prioritizedGaps.size,
      totalCurrentLevels = totalCurrentLvls,
      totalTargetLevels = totalTargetLvls,
      totalRemainingLevelGap = moduleItems.sumOf { it.levelGap },
      estimatedHoursRemaining = totalHoursRem,
      moduleGaps = moduleItems,
      prioritizedGaps = prioritizedGaps,
      phaseSummaries = phaseSummaries,
      topRecommendedNextSkills = topRecommended
    )
  }

  /**
   * Convenience function that calculates the skill gap list for a user's current progress
   * against the predefined AI/Data Science curriculum roadmap (or a specific career goal track).
   */
  fun calculateSkillGap(
    currentSkills: Map<String, Int>,
    goal: String = "AI & Data Science",
    completedSteps: Set<String> = emptySet(),
    currentWeek: Int = 1,
    studyMinutesBySkill: Map<String, Int> = emptyMap()
  ): CurriculumSkillGapReport {
    val curriculum = CareerRoadmapData.getCurriculumRoadmapForGoal(goal)
    val title = if (goal in CareerRoadmapData.CAREERS) {
      "$goal AI/DS Curriculum Roadmap"
    } else {
      "AI & Data Science Curriculum Roadmap"
    }
    return calculateCurriculumSkillGap(
      currentSkills = currentSkills,
      completedSteps = completedSteps,
      currentWeek = currentWeek,
      studyMinutesBySkill = studyMinutesBySkill,
      curriculumRoadmap = curriculum,
      curriculumTitle = title
    )
  }

  fun buildRoadmap(
    gaps: List<SkillGap>,
    profile: Map<String, Int>,
    prereqs: Map<String, List<String>> = CareerRoadmapData.PREREQUISITES
  ): List<RoadmapStep> {
    val remaining = gaps.associateBy { it.skill }.toMutableMap()
    val done = profile.keys.toMutableSet()
    done.removeAll(remaining.keys)
    val order = mutableListOf<SkillGap>()

    while (remaining.isNotEmpty()) {
      val ready = remaining.values.filter { gap ->
        val skillPrereqs = prereqs[gap.skill] ?: emptyList()
        skillPrereqs.all { it in done || it !in remaining }
      }

      val candidatePool = if (ready.isNotEmpty()) ready else remaining.values.toList()
      val nxt = candidatePool.maxByOrNull { it.priority } ?: remaining.values.first()
      order.add(nxt)
      done.add(nxt.skill)
      remaining.remove(nxt.skill)
    }

    var week = 1
    val plan = mutableListOf<RoadmapStep>()
    for (gap in order) {
      val weeksMultiplier = CareerRoadmapData.WEEKS_PER_LEVEL[gap.skill]
        ?: CareerRoadmapData.WEEKS_PER_LEVEL["default"] ?: 1.0
      val dur = max(1, (gap.gap * weeksMultiplier).roundToInt())
      plan.add(
        RoadmapStep(
          skill = gap.skill,
          startWeek = week,
          endWeek = week + dur - 1,
          levelsToGain = gap.gap
        )
      )
      week += dur
    }
    return plan
  }

  fun assessmentImprovement(preScore: Int, postScore: Int): LearningGain {
    val rawGain = postScore - preScore
    val normalizedGain = if (preScore < 100) {
      val gain = (postScore.toDouble() - preScore.toDouble()) / (100.0 - preScore.toDouble())
      (gain * 100.0).roundToInt() / 100.0
    } else {
      0.0
    }
    return LearningGain(rawGain = rawGain, normalizedGain = normalizedGain)
  }

  fun currentWeek(startDateStr: String, today: LocalDate = LocalDate.now()): Int {
    return try {
      val start = LocalDate.parse(startDateStr)
      val days = ChronoUnit.DAYS.between(start, today)
      max(1, (days / 7).toInt() + 1)
    } catch (_: Exception) {
      1
    }
  }

  fun streakDays(dayStrings: List<String>, today: LocalDate = LocalDate.now()): Int {
    val daysSet = dayStrings.toSet()
    val todayIso = today.toString()
    var currentDay = if (todayIso in daysSet) today else today.minusDays(1)
    var count = 0
    while (currentDay.toString() in daysSet) {
      count++
      currentDay = currentDay.minusDays(1)
    }
    return count
  }
}
