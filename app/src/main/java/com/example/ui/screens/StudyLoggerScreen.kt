package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.data.StudyLogEntity
import com.example.engine.RoadmapEngine
import com.example.ui.components.HeroBanner
import com.example.ui.components.KpiMetricCard
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandAccentLight
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyLoggerScreen(
  profile: ProfileEntity,
  studyLogs: List<StudyLogEntity>,
  aiCoachFeedback: String?,
  isCoachLoading: Boolean,
  onLogStudy: (Int, String, String) -> Unit,
  onRequestCoachReview: () -> Unit,
  modifier: Modifier = Modifier
) {
  val streak = RoadmapEngine.streakDays(studyLogs.map { it.day })
  val totalMinutes = studyLogs.sumOf { it.minutes }
  val totalHours = ((totalMinutes / 60.0) * 10.0).roundToInt() / 10.0
  val avgMinutes = if (studyLogs.isNotEmpty()) totalMinutes / studyLogs.size else 0

  val availableSkills = remember(profile.goal) {
    CareerRoadmapData.CAREERS[profile.goal]?.keys?.toList()
      ?: listOf("python", "sql", "statistics", "pandas", "ml")
  }

  // --- STOPWATCH STATE ---
  var isTimerRunning by remember { mutableStateOf(false) }
  var timerSeconds by remember { mutableIntStateOf(0) }

  LaunchedEffect(isTimerRunning) {
    while (isTimerRunning) {
      delay(1000L)
      timerSeconds++
    }
  }

  val displayMinutes = timerSeconds / 60
  val displaySecs = timerSeconds % 60
  val formattedTimer = String.format("%02d:%02d", displayMinutes, displaySecs)

  // --- MANUAL LOGGER FORM STATE ---
  var selectedSkill by remember(availableSkills) { mutableStateOf(availableSkills.first()) }
  var skillMenuExpanded by remember { mutableStateOf(false) }
  var sliderMinutes by remember { mutableFloatStateOf(30f) }
  var studyNote by remember { mutableStateOf("") }
  var comprehensionRating by remember { mutableIntStateOf(4) } // 1 to 5

  // --- FILTERING STATE ---
  var selectedFilterSkill by remember { mutableStateOf("All") }

  val filteredLogs = remember(selectedFilterSkill, studyLogs) {
    if (selectedFilterSkill == "All") studyLogs
    else studyLogs.filter { it.skill.equals(selectedFilterSkill, ignoreCase = true) }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Study Logger & Session Tracker",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Time your sessions, record topics studied, and receive AI study coaching.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Top KPI metrics
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Streak",
          value = "$streak 🔥",
          subtitle = "active days",
          icon = Icons.Outlined.LocalFireDepartment,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Total Time",
          value = "$totalHours h",
          subtitle = "$totalMinutes mins logged",
          icon = Icons.Outlined.Timer,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Avg Session",
          value = "$avgMinutes m",
          subtitle = "${studyLogs.size} total entries",
          icon = Icons.Outlined.CheckCircle,
          accentColor = BrandSuccess,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // ACTIVE STOPWATCH CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("study_stopwatch_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Live Study Stopwatch",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Surface(
              shape = CircleShape,
              color = if (isTimerRunning) BrandSuccess.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = if (isTimerRunning) "● RUNNING" else "PAUSED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isTimerRunning) BrandSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Big Digital Clock
          Text(
            text = formattedTimer,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("stopwatch_display")
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Controls
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { isTimerRunning = !isTimerRunning },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isTimerRunning) BrandWarning else BrandSuccess
              ),
              modifier = Modifier.testTag("timer_play_pause_btn")
            ) {
              Icon(
                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isTimerRunning) "Pause" else "Start Timer")
            }

            OutlinedButton(
              onClick = {
                isTimerRunning = false
                timerSeconds = 0
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("timer_reset_btn")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reset")
            }
          }

          if (timerSeconds >= 60) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                val mins = kotlin.math.max(1, timerSeconds / 60)
                sliderMinutes = mins.toFloat()
                isTimerRunning = false
                timerSeconds = 0
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BrandAccentLight),
              modifier = Modifier.testTag("fill_from_timer_btn")
            ) {
              Text("Transfer ${displayMinutes}m to Log Form Below")
            }
          }
        }
      }
    }

    // AI STUDY COACH CARD
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ai_coach_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandAccent)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "AI Study Coach Review",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Button(
              onClick = onRequestCoachReview,
              enabled = !isCoachLoading,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
              modifier = Modifier.testTag("request_coach_review_btn")
            ) {
              if (isCoachLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              } else {
                Text("Analyze")
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (!aiCoachFeedback.isNullOrBlank()) {
            Text(
              text = aiCoachFeedback,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          } else {
            Text(
              text = "Tap 'Analyze' to have Gemini review your recent study logs, analyze your pacing toward becoming a ${profile.goal}, and recommend tailored focus areas.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // LOG NEW SESSION FORM
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("log_new_session_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Record Study Session",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Duration Slider
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Duration: ${sliderMinutes.roundToInt()} minutes",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Slider(
            value = sliderMinutes,
            onValueChange = { sliderMinutes = it },
            valueRange = 5f..180f,
            steps = 34,
            modifier = Modifier.testTag("session_duration_slider")
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Skill selector dropdown
          ExposedDropdownMenuBox(
            expanded = skillMenuExpanded,
            onExpandedChange = { skillMenuExpanded = it }
          ) {
            OutlinedTextField(
              value = CareerRoadmapData.getSkillLabel(selectedSkill),
              onValueChange = {},
              readOnly = true,
              label = { Text("Topic / Skill") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillMenuExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .testTag("logger_skill_dropdown")
            )
            ExposedDropdownMenu(
              expanded = skillMenuExpanded,
              onDismissRequest = { skillMenuExpanded = false }
            ) {
              availableSkills.forEach { skillKey ->
                DropdownMenuItem(
                  text = { Text(CareerRoadmapData.getSkillLabel(skillKey)) },
                  onClick = {
                    selectedSkill = skillKey
                    skillMenuExpanded = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Comprehension Rating
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Comprehension:",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              (1..5).forEach { star ->
                val isFilled = star <= comprehensionRating
                Icon(
                  imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
                  contentDescription = "Star $star",
                  tint = if (isFilled) BrandWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .size(24.dp)
                    .clickable { comprehensionRating = star }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Notes input
          OutlinedTextField(
            value = studyNote,
            onValueChange = { studyNote = it },
            label = { Text("Session Notes / Key Learnings (optional)") },
            placeholder = { Text("e.g. Practiced Kaggle Titanic tutorial, resolved NaNs") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("session_note_input"),
            minLines = 2
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val noteWithRating = if (studyNote.isNotBlank()) {
                "[$comprehensionRating/5 ★] $studyNote"
              } else {
                "[$comprehensionRating/5 ★] Study session completed"
              }
              onLogStudy(sliderMinutes.roundToInt(), selectedSkill, noteWithRating)
              studyNote = ""
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("submit_study_entry_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
          ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Study Entry")
          }
        }
      }
    }

    // STUDY LOG HISTORY SECTION
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Session History",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    }

    // Filter Chips Row
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedFilterSkill == "All",
            onClick = { selectedFilterSkill = "All" },
            label = { Text("All (${studyLogs.size})") },
            modifier = Modifier.testTag("filter_all_skills")
          )
        }
        items(availableSkills) { skillKey ->
          val count = studyLogs.count { it.skill.equals(skillKey, ignoreCase = true) }
          FilterChip(
            selected = selectedFilterSkill.equals(skillKey, ignoreCase = true),
            onClick = { selectedFilterSkill = skillKey },
            label = { Text("${CareerRoadmapData.getSkillLabel(skillKey)} ($count)") },
            modifier = Modifier.testTag("filter_$skillKey")
          )
        }
      }
    }

    if (filteredLogs.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text(
              text = if (selectedFilterSkill == "All") "No study sessions recorded yet. Start the stopwatch or use the form above to log your first session!" else "No study logs for ${CareerRoadmapData.getSkillLabel(selectedFilterSkill)} yet.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredLogs) { log ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = CareerRoadmapData.getSkillLabel(log.skill),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandAccent.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "${log.minutes} min",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = BrandAccent,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Date: ${log.day}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (log.note.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = log.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
