package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AssessmentEntity
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.data.SkillProficiencyHistoryEntity
import com.example.data.StudyLogEntity
import com.example.engine.RoadmapEngine
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.LearningGainComparisonChart
import com.example.ui.components.SkillMasteryBar
import com.example.ui.components.SkillProficiencyOverTimeBarChart
import com.example.ui.components.SkillRadarChart
import com.example.ui.components.StudyActivityBarChart
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandSuccess
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
  profile: ProfileEntity,
  studyLogs: List<StudyLogEntity>,
  assessments: List<AssessmentEntity>,
  stepsDone: Set<String>,
  skillProficiencyHistory: List<SkillProficiencyHistoryEntity> = emptyList(),
  onResetPlan: () -> Unit,
  modifier: Modifier = Modifier
) {
  val totalMinutes = studyLogs.sumOf { it.minutes }
  val totalHours = ((totalMinutes / 60.0) * 10.0).roundToInt() / 10.0
  val distinctDays = studyLogs.map { it.day }.distinct().size

  val pre = assessments.firstOrNull { it.kind == "pre" }
  val post = assessments.lastOrNull { it.kind == "post" }
  val learningGainObj = if (pre != null && post != null) {
    RoadmapEngine.assessmentImprovement(pre.score, post.score)
  } else null

  val learningGainDisplay = learningGainObj?.normalizedGain?.toString() ?: "N/A"

  // 14 days activity calculation
  val today = LocalDate.now()
  val last14Days = (13 downTo 0).map { today.minusDays(it.toLong()).toString() }
  val minutesByDay = studyLogs.groupBy { it.day }.mapValues { (_, logs) -> logs.sumOf { it.minutes } }

  var showResetDialog by remember { mutableStateOf(false) }

  val reqSkills = CareerRoadmapData.CAREERS[profile.goal] ?: emptyMap()
  val startSkills = profile.getStartSkills()
  val nowSkills = profile.getNowSkills()
  val targetLevels = reqSkills.mapValues { it.value.level }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Learning Progress & Analytics",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Visual breakdown of your skill proficiency over time, radar mastery, 14-day study velocity, and assessment improvements.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Top KPIs
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Total Study",
          value = "$totalHours h",
          subtitle = "$totalMinutes mins logged",
          icon = Icons.Outlined.Timer,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Days Studied",
          value = "$distinctDays",
          subtitle = "active sessions",
          icon = Icons.Outlined.DateRange,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Steps Done",
          value = "${stepsDone.size}",
          subtitle = "milestones completed",
          icon = Icons.Outlined.CheckCircle,
          accentColor = BrandSuccess,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Learning Gain",
          value = learningGainDisplay,
          subtitle = if (learningGainObj != null) "+${learningGainObj.rawGain} pts raw" else "take post-test",
          icon = Icons.Outlined.TrendingUp,
          accentColor = BrandAccent,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 1. SKILL PROFICIENCY OVER TIME BAR CHART (Room Database Backed)
    item {
      SkillProficiencyOverTimeBarChart(
        history = skillProficiencyHistory,
        goal = profile.goal
      )
    }

    // 2. SKILL RADAR POLAR CHART
    item {
      SkillRadarChart(
        skills = reqSkills.keys.toList(),
        startLevels = startSkills,
        currentLevels = nowSkills,
        targetLevels = targetLevels
      )
    }

    // 3. 14-DAY STUDY ACTIVITY BAR CHART
    item {
      StudyActivityBarChart(
        days = last14Days,
        minutesByDay = minutesByDay
      )
    }

    // 4. LEARNING GAIN COMPARISON CHART
    item {
      LearningGainComparisonChart(
        preScore = pre?.score,
        postScore = post?.score,
        normalizedGain = learningGainObj?.normalizedGain
      )
    }

    // Skill Mastery Section
    item {
      Text(
        text = "Skill Mastery Bars",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    items(reqSkills.keys.toList()) { s ->
      val req = reqSkills[s]
      if (req != null) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.padding(12.dp)) {
            SkillMasteryBar(
              skill = s,
              startLevel = startSkills[s] ?: 0,
              currentLevel = nowSkills[s] ?: (startSkills[s] ?: 0),
              targetLevel = req.level
            )
          }
        }
      }
    }

    // Assessment History
    if (assessments.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Assessment History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }

      items(assessments) { asm ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (asm.kind == "pre") "Pre-Test (Baseline Form A)" else "Post-Test (Assessment Form B)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Taken on: ${asm.takenAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = BrandAccent.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${asm.score}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandAccent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Reset Plan Option
    item {
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedButton(
        onClick = { showResetDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("reset_plan_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandError)
      ) {
        Icon(Icons.Outlined.RestartAlt, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Change Target Career / Reset Plan")
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Reset Career Plan?") },
      text = {
        Text("This will clear your current roadmap and completed milestones so you can choose a new career or start over. Your past study logs and test scores are kept.")
      },
      confirmButton = {
        Button(
          onClick = {
            showResetDialog = false
            onResetPlan()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BrandError)
        ) {
          Text("Reset Plan")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showResetDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
