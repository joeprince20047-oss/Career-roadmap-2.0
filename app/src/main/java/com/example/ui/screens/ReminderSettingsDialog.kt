package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess
import com.example.util.ReminderUtil

@Composable
fun ReminderSettingsDialog(
  streak: Int,
  nextSkill: String?,
  onDismiss: () -> Unit,
  onShowMessage: (String) -> Unit
) {
  val context = LocalContext.current
  var isEnabled by remember { mutableStateOf(ReminderUtil.isReminderEnabled(context)) }
  val (initialHour, initialMinute) = remember { ReminderUtil.getReminderTime(context) }
  var selectedHour by remember { mutableIntStateOf(initialHour) }
  var selectedMinute by remember { mutableIntStateOf(initialMinute) }

  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
    if (isGranted) {
      onShowMessage("Notification permission granted! 🔔")
    } else {
      onShowMessage("Notifications denied in system settings.")
    }
  }

  val presetTimes = listOf(
    8 to "08:00 AM (Morning)",
    14 to "02:00 PM (Afternoon)",
    19 to "07:00 PM (Evening)",
    21 to "09:00 PM (Night)"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("reminder_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = BrandAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Daily Study Reminders", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Get daily notifications to build your habit streak and finish your milestones on schedule.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Switch Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Enable Daily Reminder",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isEnabled) "Active every day" else "Disabled",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isEnabled,
              onCheckedChange = { checked ->
                isEnabled = checked
                if (checked && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
              },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandAccent),
              modifier = Modifier.testTag("reminder_switch")
            )
          }
        }

        if (isEnabled) {
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Reminder Time:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))

          presetTimes.forEach { (hour, label) ->
            FilterChip(
              selected = selectedHour == hour,
              onClick = {
                selectedHour = hour
                selectedMinute = 0
              },
              label = { Text(label) },
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .testTag("time_chip_$hour")
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Test Notification Button
          OutlinedButton(
            onClick = {
              if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              } else {
                ReminderUtil.sendTestNotification(context, streak, nextSkill)
                onShowMessage("Test reminder sent to your notification drawer! 🔔")
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("send_test_notification_btn"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Send Test Notification Now")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          ReminderUtil.setReminder(context, isEnabled, selectedHour, selectedMinute)
          onShowMessage(if (isEnabled) "Daily reminder set for ${String.format("%02d:%02d", selectedHour, selectedMinute)}! ⏰" else "Daily reminders turned off.")
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
        modifier = Modifier.testTag("save_reminder_btn")
      ) {
        Text("Save Settings")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
