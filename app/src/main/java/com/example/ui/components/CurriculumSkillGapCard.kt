package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CareerRoadmapData
import com.example.engine.RoadmapEngine
import com.example.model.CurriculumGapStatus
import com.example.model.CurriculumSkillGapItem
import com.example.model.CurriculumSkillGapReport
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandMuted
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning

@Composable
fun CurriculumSkillGapCard(
  currentSkills: Map<String, Int>,
  goal: String,
  completedSteps: Set<String> = emptySet(),
  currentWeek: Int = 1,
  studyMinutesBySkill: Map<String, Int> = emptyMap(),
  precomputedReport: CurriculumSkillGapReport? = null,
  modifier: Modifier = Modifier
) {
  var useFullCurriculum by remember { mutableStateOf(false) }
  var showAllGaps by remember { mutableStateOf(false) }

  val report = remember(
    currentSkills,
    goal,
    completedSteps,
    currentWeek,
    studyMinutesBySkill,
    useFullCurriculum,
    precomputedReport
  ) {
    if (!useFullCurriculum && precomputedReport != null) {
      precomputedReport
    } else if (useFullCurriculum) {
      RoadmapEngine.calculateCurriculumSkillGap(
        currentSkills = currentSkills,
        completedSteps = completedSteps,
        currentWeek = currentWeek,
        studyMinutesBySkill = studyMinutesBySkill,
        curriculumRoadmap = CareerRoadmapData.AI_DATA_SCIENCE_CURRICULUM_ROADMAP,
        curriculumTitle = "Predefined AI & Data Science Curriculum"
      )
    } else {
      RoadmapEngine.calculateSkillGap(
        currentSkills = currentSkills,
        goal = goal,
        completedSteps = completedSteps,
        currentWeek = currentWeek,
        studyMinutesBySkill = studyMinutesBySkill
      )
    }
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("curriculum_skill_gap_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            tint = BrandAccent,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "AI/DS Curriculum Skill Gap Analysis",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = report.curriculumTitle,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = BrandAccent.copy(alpha = 0.14f)
        ) {
          Text(
            text = "${report.overallReadinessPercent}% Ready",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = BrandAccent,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("curriculum_gap_readiness_badge")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Benchmark Toggle: Role-specific vs Full Predefined AI/DS Curriculum
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = !useFullCurriculum,
          onClick = { useFullCurriculum = false },
          label = { Text("$goal Track") },
          modifier = Modifier.testTag("gap_mode_role_track")
        )
        FilterChip(
          selected = useFullCurriculum,
          onClick = { useFullCurriculum = true },
          label = { Text("Full AI/DS Curriculum") },
          modifier = Modifier.testTag("gap_mode_full_curriculum")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Summary Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        GapStatColumn(
          label = "Skill Gaps",
          value = "${report.skillsWithGapCount} / ${report.totalSkillsCount}",
          valueColor = if (report.skillsWithGapCount == 0) BrandSuccess else BrandWarning
        )
        GapStatColumn(
          label = "Levels Needed",
          value = "+${report.totalRemainingLevelGap} lvls",
          valueColor = BrandAccent
        )
        GapStatColumn(
          label = "Est. Study Left",
          value = "~${report.estimatedHoursRemaining.toInt()}h",
          valueColor = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Phase-by-Phase Readiness Breakdown
      Text(
        text = "Curriculum Phase Progress",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))

      report.phaseSummaries.forEach { phase ->
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = phase.phaseTitle,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "${phase.masteredCount}/${phase.totalSkillsCount} mastered · ${phase.readinessPercent}%",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { (phase.readinessPercent / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (phase.readinessPercent >= 100.0) BrandSuccess else BrandAccent,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(12.dp))

      if (report.prioritizedGaps.isEmpty()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BrandSuccess,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "All curriculum target levels achieved! You have 0 remaining skill gaps.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = BrandSuccess
          )
        }
      } else {
        Text(
          text = "Prioritized Skill Gaps to Close",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val displayedGaps = if (showAllGaps) {
          report.prioritizedGaps
        } else {
          report.prioritizedGaps.take(4)
        }

        displayedGaps.forEach { gapItem ->
          CurriculumGapItemRow(item = gapItem)
          Spacer(modifier = Modifier.height(8.dp))
        }

        if (report.prioritizedGaps.size > 4) {
          TextButton(
            onClick = { showAllGaps = !showAllGaps },
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .testTag("toggle_all_curriculum_gaps_btn")
          ) {
            Text(
              text = if (showAllGaps) "Show Top Priority Gaps Only"
              else "View All ${report.prioritizedGaps.size} Skill Gaps"
            )
          }
        }
      }
    }
  }
}

@Composable
private fun GapStatColumn(
  label: String,
  value: String,
  valueColor: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = valueColor
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun CurriculumGapItemRow(item: CurriculumSkillGapItem) {
  val statusColor = when (item.status) {
    CurriculumGapStatus.MASTERED -> BrandSuccess
    CurriculumGapStatus.CRITICAL_GAP -> BrandError
    CurriculumGapStatus.BEHIND_SCHEDULE -> BrandWarning
    CurriculumGapStatus.ON_TRACK -> BrandAccent
    CurriculumGapStatus.PREREQ_BLOCKED -> BrandMuted
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("curriculum_gap_item_${item.skillId}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.skillTitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Lvl ${item.currentLevel} → Target Lvl ${item.targetLevel} (Gap: -${item.levelGap}) · Priority ${item.priorityScore}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = CircleShape,
          color = statusColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = item.status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = if (item.status == CurriculumGapStatus.CRITICAL_GAP) {
              Icons.Default.PriorityHigh
            } else {
              Icons.Default.Schedule
            },
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = item.recommendedAction,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "~${item.estimatedHoursRemaining.toInt()}h",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = BrandAccent
        )
      }
    }
  }
}
