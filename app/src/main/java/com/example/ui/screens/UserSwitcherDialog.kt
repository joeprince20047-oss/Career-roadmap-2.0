package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserEntity
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess

@Composable
fun UserSwitcherDialog(
  currentUser: UserEntity?,
  allUsers: List<UserEntity>,
  onDismiss: () -> Unit,
  onSwitchUser: (String) -> Unit
) {
  var newUsername by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BrandAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Switch Learner Profile")
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Choose an existing student profile or enter a new name to create a new personalized roadmap.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (allUsers.isNotEmpty()) {
          Text(
            text = "Existing Profiles:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height((allUsers.size * 52).coerceAtMost(160).dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(allUsers) { user ->
              val isSelected = user.id == currentUser?.id
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) BrandAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onSwitchUser(user.username)
                    onDismiss()
                  }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = BrandSuccess,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = newUsername,
          onValueChange = { newUsername = it },
          label = { Text("New Learner Username") },
          placeholder = { Text("e.g. Maya") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_username_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (newUsername.isNotBlank()) {
            onSwitchUser(newUsername)
            onDismiss()
          }
        },
        enabled = newUsername.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
        modifier = Modifier.testTag("confirm_switch_user_btn")
      ) {
        Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Create / Switch")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
