package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareerRoadmapData
import com.example.data.SkillProficiencyHistoryEntity
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandAccentLight
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandMuted
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class ProficiencyBarPoint(
  val id: Long,
  val dateLabel: String,
  val fullDate: String,
  val value: Double,
  val maxValue: Double,
  val displayValue: String,
  val updatedSkillKey: String,
  val updatedSkillLevel: Int,
  val fitScore: Double,
  val avgProficiency: Double,
  val targetValue: Double? = null
)

/**
 * Transforms Room [SkillProficiencyHistoryEntity] records into bar chart points
 * for the selected view mode ("overall_fit", "avg_level", or a specific skill key).
 */
fun buildProficiencyBarPoints(
  history: List<SkillProficiencyHistoryEntity>,
  goal: String,
  selectedFilter: String,
  maxBars: Int = 8
): List<ProficiencyBarPoint> {
  if (history.isEmpty()) return emptyList()
  val careerReqs = CareerRoadmapData.CAREERS[goal] ?: emptyMap()
  val careerSkills = careerReqs.keys
  val recent = if (history.size > maxBars) history.takeLast(maxBars) else history

  return recent.map { entry ->
    val snapshot = entry.getSkillsSnapshot()
    val avgLvl = entry.getAverageProficiency(careerSkills)
    val shortDate = if (entry.recordedDate.length >= 10) {
      entry.recordedDate.substring(5, 10) // MM-DD
    } else {
      entry.recordedDate
    }

    when (selectedFilter) {
      "overall_fit" -> {
        val fit = ((entry.fitScore * 10.0).roundToInt() / 10.0).coerceIn(0.0, 100.0)
        ProficiencyBarPoint(
          id = entry.id,
          dateLabel = shortDate,
          fullDate = entry.recordedDate,
          value = fit,
          maxValue = 100.0,
          displayValue = "${fit.roundToInt()}%",
          updatedSkillKey = entry.skill,
          updatedSkillLevel = entry.proficiencyLevel,
          fitScore = fit,
          avgProficiency = avgLvl,
          targetValue = 100.0
        )
      }
      "avg_level" -> {
        val avgTarget = if (careerReqs.isNotEmpty()) {
          val sumTarget = careerReqs.values.sumOf { it.level }
          ((sumTarget.toDouble() / careerReqs.size.toDouble()) * 10.0).roundToInt() / 10.0
        } else 3.0
        ProficiencyBarPoint(
          id = entry.id,
          dateLabel = shortDate,
          fullDate = entry.recordedDate,
          value = avgLvl.coerceIn(0.0, 5.0),
          maxValue = 5.0,
          displayValue = "$avgLvl",
          updatedSkillKey = entry.skill,
          updatedSkillLevel = entry.proficiencyLevel,
          fitScore = entry.fitScore,
          avgProficiency = avgLvl,
          targetValue = avgTarget
        )
      }
      else -> {
        val skillLvl = (snapshot[selectedFilter] ?: if (entry.skill == selectedFilter) entry.proficiencyLevel else 0)
          .coerceIn(0, 5)
          .toDouble()
        val targetLvl = (careerReqs[selectedFilter]?.level ?: 3).toDouble()
        ProficiencyBarPoint(
          id = entry.id,
          dateLabel = shortDate,
          fullDate = entry.recordedDate,
          value = skillLvl,
          maxValue = 5.0,
          displayValue = "L${skillLvl.roundToInt()}",
          updatedSkillKey = entry.skill,
          updatedSkillLevel = entry.proficiencyLevel,
          fitScore = entry.fitScore,
          avgProficiency = avgLvl,
          targetValue = targetLvl
        )
      }
    }
  }
}

/**
 * Interactive Bar Chart visualizing a user's skill proficiency progression over time
 * based on timestamped snapshots stored in the Room database.
 */
@Composable
fun SkillProficiencyOverTimeBarChart(
  history: List<SkillProficiencyHistoryEntity>,
  goal: String,
  modifier: Modifier = Modifier
) {
  val careerReqs = CareerRoadmapData.CAREERS[goal] ?: emptyMap()
  val skillKeys = remember(goal) { careerReqs.keys.toList() }

  var selectedFilter by remember { mutableStateOf("overall_fit") }
  val points = remember(history, goal, selectedFilter) {
    buildProficiencyBarPoints(history, goal, selectedFilter)
  }

  var selectedBarIndex by remember(points.size, selectedFilter) {
    mutableIntStateOf((points.size - 1).coerceAtLeast(0))
  }

  val firstVal = points.firstOrNull()?.value ?: 0.0
  val lastVal = points.lastOrNull()?.value ?: 0.0
  val delta = ((lastVal - firstVal) * 10.0).roundToInt() / 10.0
  val deltaText = when {
    points.size < 2 -> "Baseline"
    selectedFilter == "overall_fit" -> if (delta >= 0) "+${delta.roundToInt()}% Fit" else "${delta.roundToInt()}% Fit"
    else -> if (delta >= 0) "+$delta Lvl" else "$delta Lvl"
  }

  val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("skill_proficiency_barchart_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Skill Proficiency Over Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Room DB historical progression (${history.size} snapshots)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (delta >= 0) BrandSuccess.copy(alpha = 0.16f) else BrandWarning.copy(alpha = 0.16f)
        ) {
          Text(
            text = deltaText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (delta >= 0) BrandSuccess else BrandWarning,
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("proficiency_growth_badge")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Filter Chips: Career Fit %, Avg Level, and Individual Skills
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("proficiency_filter_row")
      ) {
        item {
          FilterChip(
            selected = selectedFilter == "overall_fit",
            onClick = { selectedFilter = "overall_fit" },
            label = { Text("Career Fit %") },
            modifier = Modifier.testTag("proficiency_filter_overall_fit")
          )
        }
        item {
          FilterChip(
            selected = selectedFilter == "avg_level",
            onClick = { selectedFilter = "avg_level" },
            label = { Text("Avg Level (0–5)") },
            modifier = Modifier.testTag("proficiency_filter_avg_level")
          )
        }
        items(skillKeys) { skillKey ->
          FilterChip(
            selected = selectedFilter == skillKey,
            onClick = { selectedFilter = skillKey },
            label = { Text(CareerRoadmapData.getSkillLabel(skillKey)) },
            modifier = Modifier.testTag("proficiency_filter_$skillKey")
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Legend Row
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
      ) {
        LegendPill(color = BrandAccentLight, text = "Early Snapshot")
        LegendPill(color = BrandAccent, text = "Progressing")
        LegendPill(color = BrandSuccess, text = "Goal / Peak")
        if (selectedFilter != "overall_fit") {
          val targetLvl = points.lastOrNull()?.targetValue
          if (targetLvl != null) {
            LegendPill(color = BrandWarning, text = "Target (${targetLvl})")
          }
        }
      }

      if (points.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No skill proficiency history recorded yet. Update any skill level to log progress.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Bar Chart Area with Y-axis + Grid Canvas + Interactive Bars
        val maxScale = points.first().maxValue.coerceAtLeast(1.0)
        val targetVal = points.lastOrNull()?.targetValue

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
          verticalAlignment = Alignment.Bottom
        ) {
          // Y-Axis Labels Column
          Column(
            modifier = Modifier
              .width(34.dp)
              .fillMaxHeight()
              .padding(top = 18.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
          ) {
            val topLabel = if (selectedFilter == "overall_fit") "100%" else "5.0"
            val midLabel = if (selectedFilter == "overall_fit") "50%" else "2.5"
            val botLabel = if (selectedFilter == "overall_fit") "0%" else "0.0"
            Text(text = topLabel, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = midLabel, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = botLabel, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Chart Plot Box
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            // Background Grid Lines & Target Line
            Canvas(
              modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp, bottom = 22.dp)
            ) {
              val plotHeight = size.height
              // 3 horizontal grid lines (top, 50%, bottom)
              listOf(0f, 0.5f, 1f).forEach { ratio ->
                val y = plotHeight * ratio
                drawLine(
                  color = gridColor,
                  start = Offset(0f, y),
                  end = Offset(size.width, y),
                  strokeWidth = 1.dp.toPx()
                )
              }

              // Dashed Target Level line when viewing a specific skill or avg level
              if (targetVal != null && selectedFilter != "overall_fit") {
                val targetRatio = (1.0 - (targetVal / maxScale).coerceIn(0.0, 1.0)).toFloat()
                val targetY = plotHeight * targetRatio
                drawLine(
                  color = BrandWarning.copy(alpha = 0.85f),
                  start = Offset(0f, targetY),
                  end = Offset(size.width, targetY),
                  strokeWidth = 1.5.dp.toPx(),
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
              }
            }

            // Bars Row
            Row(
              modifier = Modifier.fillMaxSize(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.Bottom
            ) {
              points.forEachIndexed { idx, pt ->
                val fraction = (pt.value / pt.maxValue).toFloat().coerceIn(0.06f, 1f)
                val isSelected = idx == selectedBarIndex
                val isTargetMet = if (selectedFilter == "overall_fit") {
                  pt.value >= 75.0
                } else {
                  pt.targetValue != null && pt.value >= pt.targetValue
                }

                val barColor = when {
                  isTargetMet -> BrandSuccess
                  idx == 0 && points.size > 1 -> BrandAccentLight
                  else -> BrandAccent
                }

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Bottom,
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { selectedBarIndex = idx }
                    .testTag("proficiency_bar_$idx")
                ) {
                  // Value label above bar
                  Text(
                    text = pt.displayValue,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                  )

                  Spacer(modifier = Modifier.height(3.dp))

                  // Vertical Bar
                  Box(
                    modifier = Modifier
                      .width(22.dp)
                      .height((120f * fraction).dp)
                      .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                      .background(if (isSelected) barColor else barColor.copy(alpha = 0.78f))
                      .then(
                        if (isSelected) {
                          Modifier.border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                          )
                        } else Modifier
                      )
                  )

                  Spacer(modifier = Modifier.height(4.dp))

                  // Date label on X-axis
                  Text(
                    text = pt.dateLabel,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                  )
                }
              }
            }
          }
        }

        // Selected Bar Snapshot Details Banner
        val selectedPoint = points.getOrNull(selectedBarIndex) ?: points.last()
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("proficiency_selected_detail")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Snapshot ${selectedBarIndex + 1} · ${selectedPoint.fullDate}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Updated ${CareerRoadmapData.getSkillLabel(selectedPoint.updatedSkillKey)} → Lvl ${selectedPoint.updatedSkillLevel}/5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Fit: ${selectedPoint.fitScore.roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = BrandAccent
              )
              Text(
                text = "Avg Lvl: ${selectedPoint.avgProficiency}/5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Visual Radar (Polar) Chart comparing Required, Baseline, and Current skill levels.
 */
@Composable
fun SkillRadarChart(
  skills: List<String>,
  startLevels: Map<String, Int>,
  currentLevels: Map<String, Int>,
  targetLevels: Map<String, Int>,
  modifier: Modifier = Modifier
) {
  val n = skills.size.coerceAtLeast(3)
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val surfaceVariantColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("skill_radar_chart_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Skill Competency Radar",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Legend
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        LegendPill(color = BrandMuted, text = "Target Required")
        LegendPill(color = BrandAccentLight, text = "Start Level")
        LegendPill(color = BrandAccent, text = "Current Level")
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(240.dp)) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val maxRadius = (size.width / 2f) * 0.72f
          val maxLevel = 5f

          // Draw concentric polygon grid lines (levels 1 to 5)
          for (lvl in 1..5) {
            val r = maxRadius * (lvl / maxLevel)
            val gridPath = Path()
            for (i in 0 until n) {
              val angle = (2 * PI * i / n) - (PI / 2)
              val x = center.x + r * cos(angle).toFloat()
              val y = center.y + r * sin(angle).toFloat()
              if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
            }
            gridPath.close()
            drawPath(
              path = gridPath,
              color = surfaceVariantColor,
              style = Stroke(width = 1f)
            )
          }

          // Draw radial axes & skill labels
          for (i in 0 until n) {
            val angle = (2 * PI * i / n) - (PI / 2)
            val endX = center.x + maxRadius * cos(angle).toFloat()
            val endY = center.y + maxRadius * sin(angle).toFloat()
            drawLine(
              color = surfaceVariantColor,
              start = center,
              end = Offset(endX, endY),
              strokeWidth = 1f
            )

            // Draw label text
            val labelR = maxRadius + 22.dp.toPx()
            val labelX = center.x + labelR * cos(angle).toFloat()
            val labelY = center.y + labelR * sin(angle).toFloat()

            val skillLabel = CareerRoadmapData.getSkillLabel(skills[i]).take(7)
            val paint = android.graphics.Paint().apply {
              color = onSurfaceColor.toArgb()
              textSize = 24f
              textAlign = android.graphics.Paint.Align.CENTER
              isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
              skillLabel,
              labelX,
              labelY + 8f,
              paint
            )
          }

          // Helper function to build a polygon path for values
          fun buildLevelPath(values: Map<String, Int>): Path {
            val path = Path()
            for (i in 0 until n) {
              val skill = skills[i]
              val lvl = (values[skill] ?: 0).toFloat().coerceIn(0f, 5f)
              val r = maxRadius * (lvl / maxLevel)
              val angle = (2 * PI * i / n) - (PI / 2)
              val x = center.x + r * cos(angle).toFloat()
              val y = center.y + r * sin(angle).toFloat()
              if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            return path
          }

          // 1. Draw Target Required (Muted outline + light fill)
          val targetPath = buildLevelPath(targetLevels)
          drawPath(
            path = targetPath,
            color = BrandMuted.copy(alpha = 0.2f)
          )
          drawPath(
            path = targetPath,
            color = BrandMuted,
            style = Stroke(width = 2.dp.toPx())
          )

          // 2. Draw Start Levels (Dashed/faint)
          val startPath = buildLevelPath(startLevels)
          drawPath(
            path = startPath,
            color = BrandAccentLight.copy(alpha = 0.2f)
          )
          drawPath(
            path = startPath,
            color = BrandAccentLight,
            style = Stroke(width = 1.5.dp.toPx())
          )

          // 3. Draw Current Levels (Vibrant Brand Accent fill + stroke)
          val currentPath = buildLevelPath(currentLevels)
          drawPath(
            path = currentPath,
            color = BrandAccent.copy(alpha = 0.45f)
          )
          drawPath(
            path = currentPath,
            color = BrandAccent,
            style = Stroke(width = 2.5.dp.toPx())
          )

          // Draw vertex dots for current levels
          for (i in 0 until n) {
            val skill = skills[i]
            val lvl = (currentLevels[skill] ?: 0).toFloat().coerceIn(0f, 5f)
            val r = maxRadius * (lvl / maxLevel)
            val angle = (2 * PI * i / n) - (PI / 2)
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            drawCircle(
              color = BrandAccent,
              radius = 4.dp.toPx(),
              center = Offset(x, y)
            )
          }
        }
      }
    }
  }
}

/**
 * 14-day study activity bar chart with day-of-week labels and duration values.
 */
@Composable
fun StudyActivityBarChart(
  days: List<String>,
  minutesByDay: Map<String, Int>,
  modifier: Modifier = Modifier
) {
  val maxMinutes = (minutesByDay.values.maxOrNull() ?: 60).coerceAtLeast(45)

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("activity_barchart_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Study Consistency (Last 14 Days)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Max: ${maxMinutes}m",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        days.forEach { dateStr ->
          val mins = minutesByDay[dateStr] ?: 0
          val fraction = (mins.toFloat() / maxMinutes.toFloat()).coerceIn(0.04f, 1f)
          val dayLabel = dateStr.takeLast(2)

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            if (mins > 0) {
              Text(
                text = "${mins}m",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = BrandAccent
              )
              Spacer(modifier = Modifier.height(2.dp))
            }

            Box(
              modifier = Modifier
                .width(14.dp)
                .height((80 * fraction).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                  if (mins >= 45) BrandSuccess
                  else if (mins > 0) BrandAccent
                  else MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = dayLabel,
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

/**
 * Side-by-side quiz baseline vs post-test score comparison with learning gain badge.
 */
@Composable
fun LearningGainComparisonChart(
  preScore: Int?,
  postScore: Int?,
  normalizedGain: Double?,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("learning_gain_chart_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Assessment Learning Gain Comparison",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        // Pre-test bar
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.width(90.dp)
        ) {
          Text(
            text = if (preScore != null) "$preScore%" else "None",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandMuted
          )
          Spacer(modifier = Modifier.height(6.dp))
          val preFraction = ((preScore ?: 0) / 100f).coerceIn(0.08f, 1f)
          Box(
            modifier = Modifier
              .width(48.dp)
              .height((110 * preFraction).dp)
              .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
              .background(BrandMuted)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Pre-Test (Baseline)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Arrow or Gain indicator
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = 24.dp)
        ) {
          if (normalizedGain != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = BrandSuccess.copy(alpha = 0.18f)
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "Gain (g)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = BrandSuccess
                )
                Text(
                  text = "$normalizedGain",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = BrandSuccess
                )
              }
            }
          } else {
            Text(
              text = "➔",
              fontSize = 24.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Post-test bar
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.width(90.dp)
        ) {
          Text(
            text = if (postScore != null) "$postScore%" else "Pending",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandAccent
          )
          Spacer(modifier = Modifier.height(6.dp))
          val postFraction = ((postScore ?: 0) / 100f).coerceIn(0.08f, 1f)
          Box(
            modifier = Modifier
              .width(48.dp)
              .height((110 * postFraction).dp)
              .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
              .background(BrandAccent)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Post-Test (Studied)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
private fun LegendPill(color: Color, text: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
