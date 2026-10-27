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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CareerRoadmapData
import com.example.engine.RoadmapEngine
import com.example.ui.components.HeroBanner
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
  username: String,
  onSaveProfile: (String, Map<String, Int>) -> Unit,
  modifier: Modifier = Modifier
) {
  val careers = remember { CareerRoadmapData.CAREERS.keys.toList() }
  var selectedGoal by remember { mutableStateOf(careers.first()) }
  var goalDropdownExpanded by remember { mutableStateOf(false) }

  val allSkills = remember { CareerRoadmapData.allSkills() }
  val skillLevels = remember {
    mutableStateMapOf<String, Float>().apply {
      allSkills.forEach { put(it, 0f) }
      // Default common beginner skills
      put("python", 2f)
      put("sql", 1f)
      put("statistics", 1f)
    }
  }

  // Calculate live fit score
  val currentLevelsInt = skillLevels.mapValues { it.value.roundToInt() }
  val liveFitScore = remember(selectedGoal, skillLevels.toMap()) {
    RoadmapEngine.fitScore(currentLevelsInt, selectedGoal)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      HeroBanner(
        title = "Welcome, $username! 👋",
        subtitle = "Set up your target career and self-rate your starting skills to generate your customized study roadmap."
      )
    }

    item {
      Text(
        text = "1. Choose Target Career",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(6.dp))

      ExposedDropdownMenuBox(
        expanded = goalDropdownExpanded,
        onExpandedChange = { goalDropdownExpanded = it }
      ) {
        OutlinedTextField(
          value = selectedGoal,
          onValueChange = {},
          readOnly = true,
          label = { Text("Target Career Goal") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalDropdownExpanded) },
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth()
            .testTag("career_goal_dropdown")
        )
        ExposedDropdownMenu(
          expanded = goalDropdownExpanded,
          onDismissRequest = { goalDropdownExpanded = false }
        ) {
          careers.forEach { c ->
            DropdownMenuItem(
              text = { Text(c) },
              onClick = {
                selectedGoal = c
                goalDropdownExpanded = false
              }
            )
          }
        }
      }
    }

    // Live Fit Score Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Live Match for $selectedGoal",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "$liveFitScore% Baseline Match",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (liveFitScore > 50) BrandSuccess.copy(alpha = 0.2f) else BrandAccent.copy(alpha = 0.2f)
          ) {
            Text(
              text = if (liveFitScore > 50) "Strong Base" else "High Growth Potential",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (liveFitScore > 50) BrandSuccess else BrandAccent,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    item {
      Text(
        text = "2. Rate Your Current Skill Levels",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "0 = No experience, 5 = Expert. Be honest to get the most efficient roadmap!",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    items(allSkills) { skillKey ->
      val currentVal = skillLevels[skillKey] ?: 0f
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = CareerRoadmapData.getSkillLabel(skillKey),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Level: ${currentVal.roundToInt()} / 5",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Slider(
            value = currentVal,
            onValueChange = { skillLevels[skillKey] = it },
            valueRange = 0f..5f,
            steps = 4,
            modifier = Modifier.testTag("slider_$skillKey")
          )
        }
      }
    }

    item {
      Button(
        onClick = {
          onSaveProfile(selectedGoal, skillLevels.mapValues { it.value.roundToInt() })
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("create_roadmap_btn"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Generate My Personalized Roadmap")
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
