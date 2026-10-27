package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.data.AssessmentEntity
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.data.StudyLogEntity
import com.example.data.UserEntity
import com.example.engine.RoadmapEngine
import com.example.model.Badge
import com.example.model.LearnerRank
import com.example.model.RoadmapStep
import java.time.LocalDate
import kotlin.math.roundToInt

object ExportProgressUtil {

  fun generateProgressReport(
    user: UserEntity?,
    profile: ProfileEntity?,
    plan: List<RoadmapStep>,
    stepsDone: Set<String>,
    studyLogs: List<StudyLogEntity>,
    assessments: List<AssessmentEntity>,
    rank: LearnerRank
  ): String {
    val username = user?.username ?: "Learner"
    val goal = profile?.goal ?: "Data Scientist"
    val startSkills = profile?.getStartSkills() ?: emptyMap()
    val nowSkills = profile?.getNowSkills() ?: emptyMap()

    val fitStart = RoadmapEngine.fitScore(startSkills, goal)
    val fitNow = RoadmapEngine.fitScore(nowSkills, goal)
    val streak = RoadmapEngine.streakDays(studyLogs.map { it.day })
    val totalMinutes = studyLogs.sumOf { it.minutes }
    val totalHours = ((totalMinutes / 60.0) * 10.0).roundToInt() / 10.0

    val pre = assessments.firstOrNull { it.kind == "pre" }
    val post = assessments.lastOrNull { it.kind == "post" }
    val gainObj = if (pre != null && post != null) {
      RoadmapEngine.assessmentImprovement(pre.score, post.score)
    } else null

    val finishedSteps = plan.count { it.skill in stepsDone }
    val progressPct = if (plan.isNotEmpty()) (finishedSteps * 100) / plan.size else 0

    return buildString {
      appendLine("=========================================")
      appendLine("🎓 CAREER ROADMAP: LEARNER PROGRESS REPORT")
      appendLine("=========================================")
      appendLine("Learner: $username")
      appendLine("Target Career Goal: $goal")
      appendLine("Date: ${LocalDate.now()}")
      appendLine("Learner Rank: ${rank.title} (Level ${rank.level} · ${rank.currentXp} XP)")
      appendLine()
      appendLine("📊 KEY PERFORMANCE INDICATORS")
      appendLine("-----------------------------------------")
      appendLine("• Readiness Score: $fitNow% (started at $fitStart%)")
      appendLine("• Study Streak: $streak days in a row 🔥")
      appendLine("• Total Study Time: $totalHours hours ($totalMinutes minutes across ${studyLogs.size} sessions)")
      appendLine("• Roadmap Progress: $progressPct% ($finishedSteps of ${plan.size} milestones completed)")
      if (gainObj != null) {
        appendLine("• Normalized Learning Gain: g = ${gainObj.normalizedGain} (+${gainObj.rawGain} points raw gain)")
      }
      appendLine()
      appendLine("🗺️ ROADMAP MILESTONES STATUS")
      appendLine("-----------------------------------------")
      plan.forEach { step ->
        val isDone = step.skill in stepsDone
        val statusSymbol = if (isDone) "[✓] COMPLETED" else "[ ] PENDING"
        appendLine("$statusSymbol ${CareerRoadmapData.getSkillLabel(step.skill)} (Weeks ${step.startWeek}-${step.endWeek})")
      }
      appendLine()
      appendLine("📈 SKILL MASTERY LEVELS")
      appendLine("-----------------------------------------")
      val reqs = CareerRoadmapData.CAREERS[goal] ?: emptyMap()
      reqs.forEach { (skill, req) ->
        val currentLvl = nowSkills[skill] ?: 0
        val targetLvl = req.level
        appendLine("• ${CareerRoadmapData.getSkillLabel(skill)}: Level $currentLvl / $targetLvl (Goal: ${if (currentLvl >= targetLvl) "Met ★" else "In Progress"})")
      }
      appendLine()
      if (assessments.isNotEmpty()) {
        appendLine("📝 ASSESSMENT SCORES")
        appendLine("-----------------------------------------")
        assessments.forEach { asm ->
          val label = if (asm.kind == "pre") "Pre-Test (Baseline Form A)" else "Post-Test (Form B)"
          appendLine("• $label: ${asm.score}% on ${asm.takenAt}")
        }
        appendLine()
      }
      appendLine("Generated via Career Roadmap Android App")
      appendLine("=========================================")
    }
  }

  fun generateStudyLogsCsv(studyLogs: List<StudyLogEntity>): String {
    return buildString {
      appendLine("id,date,minutes_studied,skill,notes")
      studyLogs.forEach { log ->
        val cleanNote = log.note.replace("\"", "\"\"")
        appendLine("${log.id},${log.day},${log.minutes},\"${log.skill}\",\"$cleanNote\"")
      }
    }
  }

  fun generateRoadmapStepsCsv(plan: List<RoadmapStep>, stepsDone: Set<String>): String {
    return buildString {
      appendLine("skill,skill_name,start_week,end_week,levels_to_gain,status")
      plan.forEach { step ->
        val isDone = step.skill in stepsDone
        val status = if (isDone) "COMPLETED" else "PENDING"
        val label = CareerRoadmapData.getSkillLabel(step.skill).replace("\"", "\"\"")
        appendLine("\"${step.skill}\",\"$label\",${step.startWeek},${step.endWeek},${step.levelsToGain},$status")
      }
    }
  }

  fun shareProgress(context: Context, text: String, subject: String = "My Career Roadmap Progress") {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, text)
      putExtra(Intent.EXTRA_SUBJECT, subject)
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Progress Report via")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(shareIntent)
  }

  fun copyToClipboard(context: Context, text: String, label: String = "Roadmap Progress") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
  }
}
