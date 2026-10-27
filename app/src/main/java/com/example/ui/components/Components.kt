package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerRoadmapData
import com.example.model.ResourceItem
import com.example.model.RoadmapStep
import com.example.model.StepStatus
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandAccentDark
import com.example.ui.theme.BrandAccentLight
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandMuted
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning

@Composable
fun HeroBanner(
  title: String,
  subtitle: String,
  streak: Int? = null,
  onUserClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val gradient = Brush.linearGradient(
    colors = listOf(
      BrandAccent,
      BrandAccentLight,
      BrandSuccess
    )
  )

  Card(
    shape = RoundedCornerShape(20.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("hero_banner"),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Box(
      modifier = Modifier
        .background(gradient)
        .padding(20.dp)
        .fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
          )
        }

        if (streak != null && streak > 0) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.25f),
            modifier = Modifier
              .padding(start = 12.dp)
              .testTag("streak_badge")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "$streak 🔥",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun KpiMetricCard(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  accentColor: Color = MaterialTheme.colorScheme.primary
) {
  Card(
    modifier = modifier.testTag("kpi_card_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = title.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
    }
  }
}

@Composable
fun RoadmapStepCard(
  step: RoadmapStep,
  status: StepStatus,
  resource: ResourceItem?,
  onMarkComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val skillTitle = CareerRoadmapData.getSkillLabel(step.skill)

  val (statusLabel, statusColor) = when (status) {
    StepStatus.COMPLETED -> "Completed" to BrandSuccess
    StepStatus.THIS_WEEK -> "This Week" to BrandAccent
    StepStatus.BEHIND -> "Behind" to BrandError
    StepStatus.UPCOMING -> "Upcoming" to BrandMuted
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("step_card_${step.skill}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (status == StepStatus.THIS_WEEK) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = skillTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
          shape = CircleShape,
          color = statusColor.copy(alpha = 0.15f),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text(
            text = statusLabel,
            color = statusColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Weeks ${step.startWeek} to ${step.endWeek} · Gain ${step.levelsToGain} skill level${if (step.levelsToGain > 1) "s" else ""}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (resource != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                context.startActivity(intent)
              } catch (_: Exception) {}
            }
            .testTag("resource_link_${step.skill}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "📚 ${resource.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "Estimated study time: ~${resource.hours} hours",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open resource",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (status == StepStatus.COMPLETED) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = BrandSuccess,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Completed",
              style = MaterialTheme.typography.labelMedium,
              color = BrandSuccess,
              fontWeight = FontWeight.SemiBold
            )
          }
        } else {
          Button(
            onClick = onMarkComplete,
            modifier = Modifier.testTag("mark_complete_btn_${step.skill}"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (status == StepStatus.BEHIND) BrandError else BrandAccent
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.CheckCircleOutline,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Mark Completed")
          }
        }
      }
    }
  }
}

@Composable
fun SkillMasteryBar(
  skill: String,
  startLevel: Int,
  currentLevel: Int,
  targetLevel: Int,
  modifier: Modifier = Modifier
) {
  val label = CareerRoadmapData.getSkillLabel(skill)
  val progress = (currentLevel.toFloat() / targetLevel.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Lvl $currentLevel / $targetLevel (started: $startLevel)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp)),
      color = if (currentLevel >= targetLevel) BrandSuccess else BrandAccent,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
  }
}
