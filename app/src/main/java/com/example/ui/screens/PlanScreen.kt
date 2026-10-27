package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.engine.RoadmapEngine
import com.example.model.RoadmapSchema
import com.example.model.RoadmapStep
import com.example.model.StepStatus
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.SkillMasteryBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandMuted
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanScreen(
  profile: ProfileEntity,
  plan: List<RoadmapStep>,
  roadmapSchema: RoadmapSchema?,
  stepsDone: Set<String>,
  aiExplanation: String?,
  isExplanationLoading: Boolean,
  onExplainConcept: (String, String) -> Unit,
  onDismissExplanation: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentWeekNum = RoadmapEngine.currentWeek(profile.startDate)
  val startSkills = profile.getStartSkills()
  val nowSkills = profile.getNowSkills()

  val fitStart = RoadmapEngine.fitScore(startSkills, profile.goal)
  val fitNow = RoadmapEngine.fitScore(nowSkills, profile.goal)
  val pointsGained = ((fitNow - fitStart) * 10.0).roundToInt() / 10.0

  val stepsLeft = plan.count { it.skill !in stepsDone }
  var viewMode by remember { mutableStateOf("schema") } // "schema" or "timeline"

  var selectedTopicForAi by remember { mutableStateOf<Pair<String, String>?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = roadmapSchema?.title ?: "${profile.goal} Learning Roadmap",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Target Goal: ${profile.goal} · Difficulty: ${roadmapSchema?.difficultyLevel ?: "Intermediate"}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
      )
    }

    // Readiness KPIs
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Readiness Start",
          value = "$fitStart%",
          subtitle = "Baseline score",
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Readiness Now",
          value = "$fitNow%",
          subtitle = if (pointsGained >= 0) "+$pointsGained points" else "$pointsGained points",
          accentColor = BrandSuccess,
          icon = Icons.Outlined.TrendingUp,
          modifier = Modifier.weight(1f)
        )
        KpiMetricCard(
          title = "Steps Left",
          value = "$stepsLeft",
          subtitle = "of ${plan.size} total",
          icon = Icons.Outlined.CheckCircle,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // View Mode Toggle (Schema Structure vs Weekly Timeline)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilterChip(
          selected = viewMode == "schema",
          onClick = { viewMode = "schema" },
          label = { Text("Structured Schema Milestones") },
          modifier = Modifier.testTag("tab_roadmap_schema")
        )
        FilterChip(
          selected = viewMode == "timeline",
          onClick = { viewMode = "timeline" },
          label = { Text("Weekly Gantt Timeline") },
          modifier = Modifier.testTag("tab_roadmap_timeline")
        )
      }
    }

    if (viewMode == "schema" && roadmapSchema != null) {
      // SCHEMA VIEW: Phases / Milestones
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Curriculum Overview",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = roadmapSchema.description,
              style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Text(
                text = "⏱ ~${roadmapSchema.totalEstimatedHours} Study Hours",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "📅 ~${roadmapSchema.totalEstimatedWeeks} Weeks Total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Milestones
      roadmapSchema.milestones.forEach { milestone ->
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("milestone_${milestone.id}")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = milestone.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Surface(
                  shape = CircleShape,
                  color = BrandAccent.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "${milestone.skills.size} Topics",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = milestone.phaseDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(14.dp))

              milestone.skills.forEach { skillNode ->
                val isCompleted = skillNode.skillId in stepsDone
                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) {
                      BrandSuccess.copy(alpha = 0.08f)
                    } else {
                      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("node_${skillNode.skillId}")
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCompleted) {
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = BrandSuccess,
                            modifier = Modifier.size(18.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                          text = skillNode.title,
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.Bold
                        )
                      }

                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                      ) {
                        Text(
                          text = "W${skillNode.startWeek}–W${skillNode.endWeek} · ${skillNode.estimatedHours}h",
                          style = MaterialTheme.typography.labelSmall,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }

                    if (skillNode.prerequisites.isNotEmpty()) {
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = "Requires: ${skillNode.prerequisites.joinToString { CareerRoadmapData.getSkillLabel(it) }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Key Topics:",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalArrangement = Arrangement.spacedBy(4.dp),
                      modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                      skillNode.keyTopics.forEach { topic ->
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = MaterialTheme.colorScheme.surface,
                          modifier = Modifier
                            .clickable {
                              selectedTopicForAi = skillNode.skillId to topic
                              onExplainConcept(skillNode.skillId, topic)
                            }
                            .border(
                              width = 0.5.dp,
                              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                              shape = RoundedCornerShape(8.dp)
                            )
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Text(
                              text = topic,
                              style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                              imageVector = Icons.Default.AutoAwesome,
                              contentDescription = "Explain topic",
                              tint = BrandAccent,
                              modifier = Modifier.size(12.dp)
                            )
                          }
                        }
                      }
                    }

                    if (skillNode.practiceTask.isNotBlank()) {
                      Spacer(modifier = Modifier.height(6.dp))
                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Row(
                          modifier = Modifier.padding(8.dp),
                          verticalAlignment = Alignment.Top
                        ) {
                          Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = BrandWarning,
                            modifier = Modifier.size(16.dp)
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = skillNode.practiceTask,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // TIMELINE VIEW
      item {
        Text(
          text = "Sequential Weekly Timeline",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }

      // Legend
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          LegendItem(color = BrandSuccess, label = "Completed")
          LegendItem(color = BrandAccent, label = "This Week")
          LegendItem(color = BrandError, label = "Behind")
          LegendItem(color = BrandMuted, label = "Upcoming")
        }
      }

      items(plan) { step ->
        val isDone = step.skill in stepsDone
        val status = when {
          isDone -> StepStatus.COMPLETED
          step.endWeek < currentWeekNum -> StepStatus.BEHIND
          step.startWeek <= currentWeekNum -> StepStatus.THIS_WEEK
          else -> StepStatus.UPCOMING
        }

        val statusColor = when (status) {
          StepStatus.COMPLETED -> BrandSuccess
          StepStatus.THIS_WEEK -> BrandAccent
          StepStatus.BEHIND -> BrandError
          StepStatus.UPCOMING -> BrandMuted
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_step_${step.skill}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              if (isDone) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = BrandSuccess,
                  modifier = Modifier.size(20.dp)
                )
              } else {
                Text(
                  text = "W${step.startWeek}",
                  color = statusColor,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = CareerRoadmapData.getSkillLabel(step.skill),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Weeks ${step.startWeek}–${step.endWeek} (${step.endWeek - step.startWeek + 1} wk) · +${step.levelsToGain} levels",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = CircleShape,
              color = statusColor.copy(alpha = 0.15f)
            ) {
              Text(
                text = when (status) {
                  StepStatus.COMPLETED -> "Done"
                  StepStatus.THIS_WEEK -> "Active"
                  StepStatus.BEHIND -> "Late"
                  StepStatus.UPCOMING -> "Next"
                },
                color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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

  // AI Concept Explanation Dialog
  if (selectedTopicForAi != null) {
    AlertDialog(
      onDismissRequest = {
        selectedTopicForAi = null
        onDismissExplanation()
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandAccent)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = selectedTopicForAi?.second ?: "Topic Concept")
        }
      },
      text = {
        if (isExplanationLoading) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(color = BrandAccent)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Gemini is crafting a breakdown...")
          }
        } else {
          Text(
            text = aiExplanation ?: "Select a concept to view a concise, code-friendly breakdown.",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            selectedTopicForAi = null
            onDismissExplanation()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
        ) {
          Text("Got it")
        }
      }
    )
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
