package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.ThemeMode

@Composable
fun ThemeSelectionDialog(
  currentMode: ThemeMode,
  isCurrentlyDark: Boolean,
  onSelectMode: (ThemeMode) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("theme_selection_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = BrandAccent.copy(alpha = 0.15f),
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            tint = BrandAccent,
            modifier = Modifier
              .padding(8.dp)
              .size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "App Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isCurrentlyDark) "Dark mode active 🌙" else "Light mode active ☀️",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 8.dp)
      ) {
        ThemeModeOptionCard(
          title = ThemeMode.SYSTEM.title,
          subtitle = ThemeMode.SYSTEM.subtitle,
          icon = Icons.Default.BrightnessAuto,
          isSelected = currentMode == ThemeMode.SYSTEM,
          testTag = "theme_option_system",
          onClick = { onSelectMode(ThemeMode.SYSTEM) }
        )

        ThemeModeOptionCard(
          title = ThemeMode.LIGHT.title,
          subtitle = ThemeMode.LIGHT.subtitle,
          icon = Icons.Default.LightMode,
          isSelected = currentMode == ThemeMode.LIGHT,
          testTag = "theme_option_light",
          onClick = { onSelectMode(ThemeMode.LIGHT) }
        )

        ThemeModeOptionCard(
          title = ThemeMode.DARK.title,
          subtitle = ThemeMode.DARK.subtitle,
          icon = Icons.Default.DarkMode,
          isSelected = currentMode == ThemeMode.DARK,
          testTag = "theme_option_dark",
          onClick = { onSelectMode(ThemeMode.DARK) }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        modifier = Modifier.testTag("theme_dialog_done_btn")
      ) {
        Text("Done")
      }
    }
  )
}

@Composable
private fun ThemeModeOptionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val borderColor = if (isSelected) BrandAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  val containerColor = if (isSelected) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
  } else {
    MaterialTheme.colorScheme.surface
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = isSelected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(selectedColor = BrandAccent)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) BrandAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}
