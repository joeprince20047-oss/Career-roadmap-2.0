package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.data.SkillProficiencyHistoryEntity
import com.example.engine.RoadmapEngine
import com.example.model.CurriculumSkillGapReport
import com.example.ui.components.CurriculumSkillGapCard
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.SkillProficiencyOverTimeBarChart
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandMuted
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillTrackerScreen(
  profile: ProfileEntity,
  skillProficiencyHistory: List<SkillProficiencyHistoryEntity> = emptyList(),
  stepsDone: Set<String> = emptySet(),
  studyMinutesBySkill: Map<String, Int> = emptyMap(),
  curriculumSkillGapReport: CurriculumSkillGapReport? = null,
  onUpdateSkillLevel: (String, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val goal = profile.goal
  val startSkills = profile.getStartSkills()
  val nowSkills = profile.getNowSkills()
  val requiredSkills = CareerRoadmapData.CAREERS[goal] ?: emptyMap()

  val fitNow = RoadmapEngine.fitScore(nowSkills, goal)
  val fitStart = RoadmapEngine.fitScore(startSkills, goal)
  val pointsGained = ((fitNow - fitStart) * 10.0).roundToInt() / 10.0

  val allCareerSkills = requiredSkills.keys.toList()

  var selectedCategoryFilter by remember { mutableStateOf("All") }
  var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Gaps Only", "Mastered"

  val categories = listOf("All", "Core Tools", "Data Wrangling", "Machine Learning & Math", "Systems & Production")

  fun getCategory(skill: String): String {
    return when (skill) {
      "python", "excel", "git" -> "Core Tools"
      "sql", "pandas", "visualization" -> "Data Wrangling"
      "statistics", "ml", "model_evaluation" -> "Machine Learning & Math"
      "deep_learning", "cloud", "mlops", "projects" -> "Systems & Production"
      else -> "Core Tools"
    }
  }

  val filteredSkills = allCareerSkills.filter { skill ->
    val catMatch = selectedCategoryFilter == "All" || getCategory(skill) == selectedCategoryFilter
    val currentLvl = nowSkills[skill] ?: 0
    val targetLvl = requiredSkills[skill]?.level ?: 1
    val statusMatch = when (selectedStatusFilter) {
      "Gaps Only" -> currentLvl < targetLvl
      "Mastered" -> currentLvl >= targetLvl
      else -> true
    }
    catMatch && statusMatch
  }

  val totalMastered = allCareerSkills.count { (nowSkills[it] ?: 0) >= (requiredSkills[it]?.level ?: 1) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Skill Competency Tracker",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Manage your competency levels (0–5), unlock prerequisites, and reach target proficiency for $goal.",
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
          title = "Career Fit",
          value = "$fitNow%",
          subtitle = "+$pointsGained pts from start",
          accentColor = BrandSuccess,
          icon = Icons.Outlined.TrendingUp,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Mastered",
          value = "$totalMastered / ${allCareerSkills.size}",
          subtitle = "competencies at goal",
          icon = Icons.Outlined.CheckCircle,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Skill Proficiency Over Time Bar Chart (Room DB)
    item {
      SkillProficiencyOverTimeBarChart(
        history = skillProficiencyHistory,
        goal = goal
      )
    }

    // Predefined AI/Data Science Curriculum Skill Gap Analysis
    item {
      CurriculumSkillGapCard(
        currentSkills = nowSkills,
        goal = goal,
        completedSteps = stepsDone,
        currentWeek = RoadmapEngine.currentWeek(profile.startDate),
        studyMinutesBySkill = studyMinutesBySkill,
        precomputedReport = curriculumSkillGapReport
      )
    }

    // Category Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { cat ->
          FilterChip(
            selected = selectedCategoryFilter == cat,
            onClick = { selectedCategoryFilter = cat },
            label = { Text(cat) },
            modifier = Modifier.testTag("filter_cat_$cat")
          )
        }
      }
    }

    // Status Filter Chips
    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        listOf("All", "Gaps Only", "Mastered").forEach { status ->
          FilterChip(
            selected = selectedStatusFilter == status,
            onClick = { selectedStatusFilter = status },
            label = { Text(status) },
            modifier = Modifier.testTag("filter_status_$status")
          )
        }
      }
    }

    // Skill Cards
    items(filteredSkills) { skillKey ->
      val req = requiredSkills[skillKey]
      val targetLevel = req?.level ?: 3
      val currentLevel = nowSkills[skillKey] ?: 0
      val startLevel = startSkills[skillKey] ?: 0
      val isMastered = currentLevel >= targetLevel

      // Check prerequisites
      val prereqs = CareerRoadmapData.PREREQUISITES[skillKey] ?: emptyList()
      val unmetPrereqs = prereqs.filter { (nowSkills[it] ?: 0) < 2 }
      val isUnlocked = unmetPrereqs.isEmpty()

      val resource = CareerRoadmapData.RESOURCES[skillKey]

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isMastered) MaterialTheme.colorScheme.surface
          else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("skill_tracker_card_$skillKey")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = CareerRoadmapData.getSkillLabel(skillKey),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                if (isMastered) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Mastered",
                    tint = BrandSuccess,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Text(
                text = getCategory(skillKey),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isMastered) BrandSuccess.copy(alpha = 0.15f)
              else if (isUnlocked) BrandAccent.copy(alpha = 0.15f)
              else BrandMuted.copy(alpha = 0.2f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                  tint = if (isMastered) BrandSuccess else if (isUnlocked) BrandAccent else BrandMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isMastered) "Goal Met" else if (isUnlocked) "In Progress" else "Prereq Locked",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isMastered) BrandSuccess else if (isUnlocked) BrandAccent else BrandMuted
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Level Pips (0 to 5)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Proficiency Level: $currentLevel / 5",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Target: Lvl $targetLevel",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = BrandAccent
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // 5 Level Step Pips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            for (lvl in 1..5) {
              val isAchieved = lvl <= currentLevel
              val isTarget = lvl == targetLevel
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(12.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(
                    if (isAchieved) BrandSuccess
                    else if (isTarget) BrandAccent.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant
                  )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Level adjustment controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(36.dp)
                  .clickable(enabled = currentLevel > 0) {
                    onUpdateSkillLevel(skillKey, (currentLevel - 1).coerceAtLeast(0))
                  }
                  .testTag("decrement_level_$skillKey")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Remove, contentDescription = "Decrease level", modifier = Modifier.size(16.dp))
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Text(
                text = "Level $currentLevel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.width(12.dp))

              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                  .size(36.dp)
                  .clickable(enabled = currentLevel < 5) {
                    onUpdateSkillLevel(skillKey, (currentLevel + 1).coerceAtMost(5))
                  }
                  .testTag("increment_level_$skillKey")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase level",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            if (resource != null) {
              OutlinedButton(
                onClick = {
                  try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                    context.startActivity(intent)
                  } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("resource_btn_$skillKey")
              ) {
                Text(
                  text = "Study (~${resource.hours}h)",
                  style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
              }
            }
          }

          if (unmetPrereqs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "⚠️ Unmet Prerequisites: ${unmetPrereqs.joinToString { CareerRoadmapData.getSkillLabel(it) }}. Level up prerequisites first for optimal comprehension.",
              style = MaterialTheme.typography.labelSmall,
              color = BrandWarning
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
