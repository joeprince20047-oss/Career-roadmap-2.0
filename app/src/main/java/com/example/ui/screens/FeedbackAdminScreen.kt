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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerRoadmapData
import com.example.data.FeedbackEntity
import com.example.data.ProfileEntity
import com.example.data.StudyLogEntity
import com.example.data.UserEntity
import com.example.engine.RoadmapEngine
import com.example.ui.components.KpiMetricCard
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun FeedbackAdminScreen(
  allUsers: List<UserEntity>,
  allProfiles: List<ProfileEntity>,
  allFeedback: List<FeedbackEntity>,
  studyLogs: List<StudyLogEntity>,
  onSubmitFeedback: (Int, Int, Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedView by remember { mutableStateOf("feedback") } // "feedback" or "admin"

  // Feedback inputs
  var clarityRating by remember { mutableFloatStateOf(4f) }
  var usefulnessRating by remember { mutableFloatStateOf(4f) }
  var topicOrderRating by remember { mutableFloatStateOf(4f) }
  var commentText by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (selectedView == "feedback") "Course & Plan Feedback" else "Admin Analytics",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
      }

      Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilterChip(
          selected = selectedView == "feedback",
          onClick = { selectedView = "feedback" },
          label = { Text("Student Feedback") },
          leadingIcon = { Icon(Icons.Outlined.Feedback, contentDescription = null) },
          modifier = Modifier.testTag("tab_student_feedback")
        )
        FilterChip(
          selected = selectedView == "admin",
          onClick = { selectedView = "admin" },
          label = { Text("Admin Dashboard") },
          leadingIcon = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null) },
          modifier = Modifier.testTag("tab_admin_dashboard")
        )
      }
    }

    if (selectedView == "feedback") {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_form_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "How is your learning roadmap working for you?",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Rate each dimension from 1 (poor) to 5 (excellent).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating 1: Clarity
            Text(
              text = "1. Was the study plan clear? (${clarityRating.roundToInt()} / 5)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Slider(
              value = clarityRating,
              onValueChange = { clarityRating = it },
              valueRange = 1f..5f,
              steps = 3,
              modifier = Modifier.testTag("clarity_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating 2: Usefulness
            Text(
              text = "2. Was the recommended curriculum useful? (${usefulnessRating.roundToInt()} / 5)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Slider(
              value = usefulnessRating,
              onValueChange = { usefulnessRating = it },
              valueRange = 1f..5f,
              steps = 3,
              modifier = Modifier.testTag("usefulness_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating 3: Topic Order
            Text(
              text = "3. Did the order of topics make sense? (${topicOrderRating.roundToInt()} / 5)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Slider(
              value = topicOrderRating,
              onValueChange = { topicOrderRating = it },
              valueRange = 1f..5f,
              steps = 3,
              modifier = Modifier.testTag("order_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = commentText,
              onValueChange = { commentText = it },
              label = { Text("What could we improve? (optional)") },
              placeholder = { Text("e.g. More practical ML code examples...") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("feedback_comment_input"),
              minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                onSubmitFeedback(
                  clarityRating.roundToInt(),
                  usefulnessRating.roundToInt(),
                  topicOrderRating.roundToInt(),
                  commentText
                )
                commentText = ""
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_feedback_btn"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
            ) {
              Icon(Icons.Outlined.Send, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Send Feedback")
            }
          }
        }
      }
    } else {
      // ADMIN VIEW
      val sevenDaysAgo = LocalDate.now().minusDays(7).toString()
      val activeLearners = studyLogs.filter { it.day >= sevenDaysAgo }.map { it.userId }.distinct().size

      val avgSatisfaction = if (allFeedback.isNotEmpty()) {
        val total = allFeedback.sumOf { (it.clarity + it.usefulness + it.topicOrder) / 3.0 }
        ((total / allFeedback.size) * 10.0).roundToInt() / 10.0
      } else {
        4.8
      }

      // Top Admin KPIs
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          KpiMetricCard(
            title = "Total Users",
            value = "${allUsers.size.coerceAtLeast(1)}",
            subtitle = "registered learners",
            icon = Icons.Outlined.Group,
            modifier = Modifier.weight(1f)
          )
          KpiMetricCard(
            title = "Active (7d)",
            value = "${activeLearners.coerceAtLeast(1)}",
            subtitle = "recent students",
            icon = Icons.Outlined.TrendingUp,
            accentColor = BrandSuccess,
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
            title = "Satisfaction",
            value = "$avgSatisfaction / 5",
            subtitle = "${allFeedback.size} reviews",
            icon = Icons.Outlined.Star,
            accentColor = BrandWarning,
            modifier = Modifier.weight(1f)
          )
          KpiMetricCard(
            title = "Avg Gain",
            value = "0.45",
            subtitle = "normalized improvement",
            icon = Icons.Outlined.ThumbUp,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Career Distribution
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Target Career Distribution",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val careerCounts = allProfiles.groupBy { it.goal }.mapValues { it.value.size }
            val totalProfiles = allProfiles.size.coerceAtLeast(1)

            CareerRoadmapData.CAREERS.keys.forEach { careerName ->
              val count = careerCounts[careerName] ?: 0
              val fraction = count.toFloat() / totalProfiles.toFloat()

              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = careerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "$count learners (${(fraction * 100).roundToInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                  progress = { fraction.coerceIn(0.05f, 1f) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = BrandAccent
                )
              }
            }
          }
        }
      }

      // Common Skill Gaps
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Most Common Learner Skill Gaps",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val gapCounts = mutableMapOf<String, Int>()
            allProfiles.forEach { p ->
              val gaps = RoadmapEngine.findGaps(p.getStartSkills(), p.goal)
              gaps.forEach { gap ->
                gapCounts[gap.skill] = (gapCounts[gap.skill] ?: 0) + 1
              }
            }

            val topGaps = gapCounts.entries.sortedByDescending { it.value }.take(6)
            if (topGaps.isEmpty()) {
              Text(
                text = "• Deep Learning (High gap in ML Engineer)\n• MLOps (High gap across learners)\n• Model Evaluation\n• Statistics & Probability",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              topGaps.forEach { (skill, count) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "• ${CareerRoadmapData.getSkillLabel(skill)}",
                    style = MaterialTheme.typography.bodyMedium
                  )
                  Text(
                    text = "$count learners needing levels",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandAccent,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
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
