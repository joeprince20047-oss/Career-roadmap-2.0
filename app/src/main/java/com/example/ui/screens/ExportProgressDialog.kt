package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssessmentEntity
import com.example.data.ProfileEntity
import com.example.data.StudyLogEntity
import com.example.data.UserEntity
import com.example.model.LearnerRank
import com.example.model.RoadmapStep
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess
import com.example.util.ExportProgressUtil

@Composable
fun ExportProgressDialog(
  user: UserEntity?,
  profile: ProfileEntity?,
  plan: List<RoadmapStep>,
  stepsDone: Set<String>,
  studyLogs: List<StudyLogEntity>,
  assessments: List<AssessmentEntity>,
  rank: LearnerRank,
  onDismiss: () -> Unit,
  onShowMessage: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedExportFormat by remember { mutableStateOf("Report") } // "Report", "StudyLogsCsv", "RoadmapCsv"

  val fullReportText = remember(user, profile, plan, stepsDone, studyLogs, assessments, rank) {
    ExportProgressUtil.generateProgressReport(
      user = user,
      profile = profile,
      plan = plan,
      stepsDone = stepsDone,
      studyLogs = studyLogs,
      assessments = assessments,
      rank = rank
    )
  }

  val studyLogsCsvText = remember(studyLogs) {
    ExportProgressUtil.generateStudyLogsCsv(studyLogs)
  }

  val roadmapCsvText = remember(plan, stepsDone) {
    ExportProgressUtil.generateRoadmapStepsCsv(plan, stepsDone)
  }

  val activeContent = when (selectedExportFormat) {
    "StudyLogsCsv" -> studyLogsCsvText
    "RoadmapCsv" -> roadmapCsvText
    else -> fullReportText
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("export_progress_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.FileDownload, contentDescription = null, tint = BrandSuccess, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Export Learning Progress", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Export your curriculum progress, logged study sessions, or test results to share or archive.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Format Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedExportFormat == "Report",
            onClick = { selectedExportFormat = "Report" },
            label = { Text("Progress Report") },
            modifier = Modifier.testTag("export_tab_report")
          )
          FilterChip(
            selected = selectedExportFormat == "StudyLogsCsv",
            onClick = { selectedExportFormat = "StudyLogsCsv" },
            label = { Text("Study Logs CSV") },
            modifier = Modifier.testTag("export_tab_studylogs")
          )
          FilterChip(
            selected = selectedExportFormat == "RoadmapCsv",
            onClick = { selectedExportFormat = "RoadmapCsv" },
            label = { Text("Roadmap CSV") },
            modifier = Modifier.testTag("export_tab_roadmap")
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Code/Text Preview Box
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
        ) {
          LazyColumn(modifier = Modifier.padding(12.dp)) {
            item {
              Text(
                text = activeContent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              ExportProgressUtil.shareProgress(
                context = context,
                text = activeContent,
                subject = "My Career Roadmap ($selectedExportFormat)"
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("share_export_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
          }

          OutlinedButton(
            onClick = {
              ExportProgressUtil.copyToClipboard(context, activeContent, selectedExportFormat)
              onShowMessage("Copied $selectedExportFormat to clipboard! 📋")
            },
            modifier = Modifier
              .weight(1f)
              .testTag("copy_export_btn"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy Text")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  )
}
