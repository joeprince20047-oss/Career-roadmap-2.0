package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.data.StudyLogEntity
import com.example.data.UserEntity
import com.example.engine.RoadmapEngine
import com.example.model.RoadmapStep
import com.example.model.StepStatus
import com.example.ui.components.HeroBanner
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.RoadmapStepCard
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
  user: UserEntity,
  profile: ProfileEntity,
  plan: List<RoadmapStep>,
  stepsDone: Set<String>,
  studyLogs: List<StudyLogEntity>,
  onMarkComplete: (String) -> Unit,
  onLogStudy: (Int, String, String) -> Unit,
  onSwitchUserClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentWeekNum = RoadmapEngine.currentWeek(profile.startDate)
  val streakCount = RoadmapEngine.streakDays(studyLogs.map { it.day })

  // Last 7 days study minutes
  val sevenDaysAgo = LocalDate.now().minusDays(7).toString()
  val last7DaysMinutes = studyLogs.filter { it.day >= sevenDaysAgo }.sumOf { it.minutes }

  val finishedCount = plan.count { it.skill in stepsDone }
  val progressPct = if (plan.isNotEmpty()) {
    ((finishedCount.toDouble() / plan.size.toDouble()) * 100).roundToInt()
  } else 100
  val totalWeeks = plan.maxOfOrNull { it.endWeek } ?: 1

  val todoSteps = plan.filter { it.skill !in stepsDone }
  val focusSteps = todoSteps.filter { it.startWeek <= currentWeekNum }
  val upcomingStep = todoSteps.firstOrNull { it.startWeek > currentWeekNum }

  // Study log state
  var studyMinutes by remember { mutableFloatStateOf(30f) }
  val availableSkills = plan.map { it.skill }.ifEmpty { listOf("python", "sql", "ml") }
  var selectedSkill by remember(availableSkills) { mutableStateOf(availableSkills.first()) }
  var skillDropdownExpanded by remember { mutableStateOf(false) }
  var studyNote by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      HeroBanner(
        title = "Hi ${user.username} 👋",
        subtitle = "Goal: ${profile.goal} · Week $currentWeekNum of ~$totalWeeks",
        streak = streakCount,
        onUserClick = onSwitchUserClicked
      )
    }

    // KPIs Row 1
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Streak",
          value = "$streakCount 🔥",
          subtitle = "days in a row",
          icon = Icons.Outlined.LocalFireDepartment,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Last 7 Days",
          value = "$last7DaysMinutes min",
          subtitle = "study time",
          icon = Icons.Outlined.Timer,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // KPIs Row 2
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Progress",
          value = "$progressPct%",
          subtitle = "$finishedCount of ${plan.size} steps",
          icon = Icons.Outlined.CheckCircle,
          accentColor = BrandSuccess,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Current Week",
          value = "Week $currentWeekNum",
          subtitle = "target ~${totalWeeks}w",
          icon = Icons.Outlined.DateRange,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Focus Now Section
    item {
      Text(
        text = "Focus Now",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
      )
    }

    if (plan.isEmpty() || todoSteps.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = BrandSuccess.copy(alpha = 0.15f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "🎉 All Roadmap Steps Complete!",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = BrandSuccess
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "You have completed all planned skill steps. Head to 'Skill Check' to take the Post-Test assessment and compute your learning gain!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    } else if (focusSteps.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Text(
            text = "Nothing currently due this week! Next step starts in week ${upcomingStep?.startWeek ?: currentWeekNum + 1}.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    } else {
      items(focusSteps) { step ->
        val status = if (step.endWeek < currentWeekNum) StepStatus.BEHIND else StepStatus.THIS_WEEK
        RoadmapStepCard(
          step = step,
          status = status,
          resource = CareerRoadmapData.RESOURCES[step.skill],
          onMarkComplete = { onMarkComplete(step.skill) }
        )
      }
    }

    if (upcomingStep != null) {
      item {
        Text(
          text = "Next up: ${CareerRoadmapData.getSkillLabel(upcomingStep.skill)} (starts week ${upcomingStep.startWeek})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Log Today's Study Section
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Log Today's Study",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("study_log_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Minutes Studied: ${studyMinutes.roundToInt()} min",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Slider(
            value = studyMinutes,
            onValueChange = { studyMinutes = it },
            valueRange = 10f..180f,
            steps = 33, // 5 min increments
            modifier = Modifier.testTag("study_minutes_slider")
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Skill Dropdown
          ExposedDropdownMenuBox(
            expanded = skillDropdownExpanded,
            onExpandedChange = { skillDropdownExpanded = it }
          ) {
            OutlinedTextField(
              value = CareerRoadmapData.getSkillLabel(selectedSkill),
              onValueChange = {},
              readOnly = true,
              label = { Text("Topic / Skill") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillDropdownExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .testTag("study_skill_dropdown")
            )
            ExposedDropdownMenu(
              expanded = skillDropdownExpanded,
              onDismissRequest = { skillDropdownExpanded = false }
            ) {
              availableSkills.forEach { s ->
                DropdownMenuItem(
                  text = { Text(CareerRoadmapData.getSkillLabel(s)) },
                  onClick = {
                    selectedSkill = s
                    skillDropdownExpanded = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = studyNote,
            onValueChange = { studyNote = it },
            label = { Text("What did you work on? (optional)") },
            placeholder = { Text("e.g., Finished lesson on Pandas indexing") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("study_note_input"),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              onLogStudy(studyMinutes.roundToInt(), selectedSkill, studyNote)
              studyNote = ""
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_study_log_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
          ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Study Session")
          }
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
