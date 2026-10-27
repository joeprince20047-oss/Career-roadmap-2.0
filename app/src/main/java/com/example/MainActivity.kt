package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.example.engine.RoadmapEngine
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavTab
import com.example.ui.CareerRoadmapViewModel
import com.example.ui.screens.ExportProgressDialog
import com.example.ui.screens.FeedbackAdminScreen
import com.example.ui.screens.GamificationDialog
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ReminderSettingsDialog
import com.example.ui.screens.SkillCheckScreen
import com.example.ui.screens.SkillTrackerScreen
import com.example.ui.screens.StudyLoggerScreen
import com.example.ui.screens.ThemeSelectionDialog
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.UserSwitcherDialog
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.util.configureAppCheck
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val viewModel: CareerRoadmapViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure App Check with launch intent before any UI or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()
    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val systemDark = isSystemInDarkTheme()
      val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        CareerRoadmapApp(
          viewModel = viewModel,
          isDarkTheme = isDarkTheme
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerRoadmapApp(
  viewModel: CareerRoadmapViewModel,
  isDarkTheme: Boolean = false
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
  val roadmapPlan by viewModel.roadmapPlan.collectAsStateWithLifecycle()
  val roadmapSchema by viewModel.roadmapSchema.collectAsStateWithLifecycle()
  val stepsDone by viewModel.stepsDone.collectAsStateWithLifecycle()
  val studyLogs by viewModel.studyLogs.collectAsStateWithLifecycle()
  val assessments by viewModel.assessments.collectAsStateWithLifecycle()
  val skillProficiencyHistory by viewModel.skillProficiencyHistory.collectAsStateWithLifecycle()
  val curriculumSkillGapReport by viewModel.curriculumSkillGapReport.collectAsStateWithLifecycle()
  val quizResult by viewModel.quizResult.collectAsStateWithLifecycle()
  val aiCoachFeedback by viewModel.aiCoachFeedback.collectAsStateWithLifecycle()
  val isCoachLoading by viewModel.isCoachLoading.collectAsStateWithLifecycle()
  val aiExplanation by viewModel.aiExplanation.collectAsStateWithLifecycle()
  val isExplanationLoading by viewModel.isExplanationLoading.collectAsStateWithLifecycle()
  val badges by viewModel.badges.collectAsStateWithLifecycle()
  val learnerRank by viewModel.learnerRank.collectAsStateWithLifecycle()
  val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

  val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
  val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
  val allFeedbacks by viewModel.allFeedbacks.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var showUserSwitcher by remember { mutableStateOf(false) }
  var showGamificationDialog by remember { mutableStateOf(false) }
  var showReminderDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }

  LaunchedEffect(uiMessage) {
    uiMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearMessage()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Career Roadmap",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            // Learner Rank quick indicator
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable { showGamificationDialog = true }
                .padding(vertical = 2.dp)
            ) {
              Text(
                text = "Lvl ${learnerRank.level} · ${learnerRank.currentXp} XP 🏆",
                style = MaterialTheme.typography.labelSmall,
                color = BrandAccent,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        },
        actions = {
          // 1. Gamification Badges Button
          IconButton(
            onClick = { showGamificationDialog = true },
            modifier = Modifier.testTag("top_badges_icon_btn")
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = "Achievements & Badges",
              tint = BrandWarning
            )
          }

          // 2. Daily Reminders Button
          IconButton(
            onClick = { showReminderDialog = true },
            modifier = Modifier.testTag("top_reminder_icon_btn")
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = "Daily Reminders",
              tint = BrandAccent
            )
          }

          // 3. Export Progress Button
          IconButton(
            onClick = { showExportDialog = true },
            modifier = Modifier.testTag("top_export_icon_btn")
          ) {
            Icon(
              imageVector = Icons.Default.FileDownload,
              contentDescription = "Export Progress",
              tint = BrandSuccess
            )
          }

          // 4. Dark Mode / Theme Selector Button
          IconButton(
            onClick = { showThemeDialog = true },
            modifier = Modifier.testTag("top_theme_icon_btn")
          ) {
            Icon(
              imageVector = when (themeMode) {
                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                ThemeMode.LIGHT -> Icons.Default.LightMode
                ThemeMode.DARK -> Icons.Default.DarkMode
              },
              contentDescription = "Theme appearance: ${themeMode.title}",
              tint = if (isDarkTheme) Color(0xFFFFD166) else BrandAccent
            )
          }

          // 5. Feedback & Admin Action
          IconButton(
            onClick = { viewModel.setTab(AppNavTab.FEEDBACK) },
            modifier = Modifier.testTag("top_feedback_icon_btn")
          ) {
            Icon(
              imageVector = if (selectedTab == AppNavTab.FEEDBACK) Icons.Filled.Feedback else Icons.Outlined.Feedback,
              contentDescription = "Feedback & Admin",
              tint = if (selectedTab == AppNavTab.FEEDBACK) BrandAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // 5. User Profile Switcher
          IconButton(
            onClick = { showUserSwitcher = true },
            modifier = Modifier.testTag("user_switcher_icon_btn")
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Switch profile",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      if (currentProfile != null) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = selectedTab == AppNavTab.TODAY,
            onClick = { viewModel.setTab(AppNavTab.TODAY) },
            icon = {
              Icon(
                imageVector = if (selectedTab == AppNavTab.TODAY) Icons.Filled.Today else Icons.Outlined.Today,
                contentDescription = "Today"
              )
            },
            label = { Text("Today") },
            modifier = Modifier.testTag("nav_tab_today"),
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandAccent, indicatorColor = BrandAccent.copy(alpha = 0.15f))
          )

          NavigationBarItem(
            selected = selectedTab == AppNavTab.PLAN,
            onClick = { viewModel.setTab(AppNavTab.PLAN) },
            icon = {
              Icon(
                imageVector = if (selectedTab == AppNavTab.PLAN) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                contentDescription = "Roadmap"
              )
            },
            label = { Text("Roadmap") },
            modifier = Modifier.testTag("nav_tab_plan"),
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandAccent, indicatorColor = BrandAccent.copy(alpha = 0.15f))
          )

          NavigationBarItem(
            selected = selectedTab == AppNavTab.SKILLS,
            onClick = { viewModel.setTab(AppNavTab.SKILLS) },
            icon = {
              Icon(
                imageVector = if (selectedTab == AppNavTab.SKILLS) Icons.Filled.Stars else Icons.Outlined.Stars,
                contentDescription = "Skills"
              )
            },
            label = { Text("Skills") },
            modifier = Modifier.testTag("nav_tab_skills"),
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandAccent, indicatorColor = BrandAccent.copy(alpha = 0.15f))
          )

          NavigationBarItem(
            selected = selectedTab == AppNavTab.LOGGER,
            onClick = { viewModel.setTab(AppNavTab.LOGGER) },
            icon = {
              Icon(
                imageVector = if (selectedTab == AppNavTab.LOGGER) Icons.Filled.Timer else Icons.Outlined.Timer,
                contentDescription = "Study Log"
              )
            },
            label = { Text("Logger") },
            modifier = Modifier.testTag("nav_tab_logger"),
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandAccent, indicatorColor = BrandAccent.copy(alpha = 0.15f))
          )

          NavigationBarItem(
            selected = selectedTab == AppNavTab.SKILL_CHECK,
            onClick = { viewModel.setTab(AppNavTab.SKILL_CHECK) },
            icon = {
              Icon(
                imageVector = if (selectedTab == AppNavTab.SKILL_CHECK) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                contentDescription = "Quiz"
              )
            },
            label = { Text("Quiz") },
            modifier = Modifier.testTag("nav_tab_quiz"),
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandAccent, indicatorColor = BrandAccent.copy(alpha = 0.15f))
          )

          NavigationBarItem(
            selected = selectedTab == AppNavTab.PROGRESS,
            onClick = { viewModel.setTab(AppNavTab.PROGRESS) },
            icon = {
              Icon(
                imageVector = if (selectedTab == AppNavTab.PROGRESS) Icons.Filled.Insights else Icons.Outlined.Insights,
                contentDescription = "Stats"
              )
            },
            label = { Text("Stats") },
            modifier = Modifier.testTag("nav_tab_progress"),
            colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandAccent, indicatorColor = BrandAccent.copy(alpha = 0.15f))
          )
        }
      }
    }
  ) { innerPadding ->
    val user = currentUser
    val profile = currentProfile

    if (user != null && profile == null) {
      OnboardingScreen(
        username = user.username,
        onSaveProfile = { goal, startSkills ->
          viewModel.saveProfile(goal, startSkills)
        },
        modifier = Modifier.padding(innerPadding)
      )
    } else if (user != null && profile != null) {
      when (selectedTab) {
        AppNavTab.TODAY -> {
          TodayScreen(
            user = user,
            profile = profile,
            plan = roadmapPlan,
            stepsDone = stepsDone,
            studyLogs = studyLogs,
            onMarkComplete = { skill -> viewModel.markStepComplete(skill) },
            onLogStudy = { minutes, skill, note -> viewModel.logStudy(minutes, skill, note) },
            onSwitchUserClicked = { showUserSwitcher = true },
            modifier = Modifier.padding(innerPadding)
          )
        }

        AppNavTab.PLAN -> {
          PlanScreen(
            profile = profile,
            plan = roadmapPlan,
            roadmapSchema = roadmapSchema,
            stepsDone = stepsDone,
            aiExplanation = aiExplanation,
            isExplanationLoading = isExplanationLoading,
            onExplainConcept = { skill, topic -> viewModel.requestConceptExplanation(skill, topic) },
            onDismissExplanation = { viewModel.clearExplanation() },
            modifier = Modifier.padding(innerPadding)
          )
        }

        AppNavTab.SKILLS -> {
          val studyMinutesBySkill = remember(studyLogs) {
            studyLogs.groupBy { it.skill }.mapValues { (_, list) -> list.sumOf { it.minutes } }
          }
          SkillTrackerScreen(
            profile = profile,
            skillProficiencyHistory = skillProficiencyHistory,
            stepsDone = stepsDone,
            studyMinutesBySkill = studyMinutesBySkill,
            curriculumSkillGapReport = curriculumSkillGapReport,
            onUpdateSkillLevel = { skill, lvl -> viewModel.updateSkillLevel(skill, lvl) },
            modifier = Modifier.padding(innerPadding)
          )
        }

        AppNavTab.LOGGER -> {
          StudyLoggerScreen(
            profile = profile,
            studyLogs = studyLogs,
            aiCoachFeedback = aiCoachFeedback,
            isCoachLoading = isCoachLoading,
            onLogStudy = { minutes, skill, note -> viewModel.logStudy(minutes, skill, note) },
            onRequestCoachReview = { viewModel.requestCoachReview() },
            modifier = Modifier.padding(innerPadding)
          )
        }

        AppNavTab.SKILL_CHECK -> {
          SkillCheckScreen(
            profile = profile,
            assessments = assessments,
            quizResult = quizResult,
            onSubmitQuiz = { kind, answers -> viewModel.submitQuiz(kind, answers) },
            onLevelUpSkill = { skill ->
              val current = profile.getNowSkills()[skill] ?: 0
              viewModel.updateSkillLevel(skill, current + 1)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }

        AppNavTab.PROGRESS -> {
          ProgressScreen(
            profile = profile,
            studyLogs = studyLogs,
            assessments = assessments,
            stepsDone = stepsDone,
            skillProficiencyHistory = skillProficiencyHistory,
            onResetPlan = { viewModel.resetPlan() },
            modifier = Modifier.padding(innerPadding)
          )
        }

        AppNavTab.FEEDBACK, AppNavTab.ADMIN -> {
          FeedbackAdminScreen(
            allUsers = allUsers,
            allProfiles = allProfiles,
            allFeedback = allFeedbacks,
            studyLogs = studyLogs,
            onSubmitFeedback = { clarity, usefulness, order, comment ->
              viewModel.submitFeedback(clarity, usefulness, order, comment)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  // 1. Gamification Badges Modal
  if (showGamificationDialog) {
    GamificationDialog(
      rank = learnerRank,
      badges = badges,
      onDismiss = { showGamificationDialog = false }
    )
  }

  // 2. Daily Reminders Modal
  if (showReminderDialog) {
    val streak = RoadmapEngine.streakDays(studyLogs.map { it.day })
    val nextSkill = roadmapPlan.firstOrNull { it.skill !in stepsDone }?.skill
    ReminderSettingsDialog(
      streak = streak,
      nextSkill = nextSkill,
      onDismiss = { showReminderDialog = false },
      onShowMessage = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
      }
    )
  }

  // 3. Export Progress Modal
  if (showExportDialog) {
    ExportProgressDialog(
      user = currentUser,
      profile = currentProfile,
      plan = roadmapPlan,
      stepsDone = stepsDone,
      studyLogs = studyLogs,
      assessments = assessments,
      rank = learnerRank,
      onDismiss = { showExportDialog = false },
      onShowMessage = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
      }
    )
  }

  // 4. User Profile Switcher Modal
  if (showUserSwitcher) {
    UserSwitcherDialog(
      currentUser = currentUser,
      allUsers = allUsers,
      onDismiss = { showUserSwitcher = false },
      onSwitchUser = { name -> viewModel.switchOrRegisterUser(name) }
    )
  }

  // 5. Theme Selection Modal
  if (showThemeDialog) {
    ThemeSelectionDialog(
      currentMode = themeMode,
      isCurrentlyDark = isDarkTheme,
      onSelectMode = { newMode ->
        viewModel.setThemeMode(newMode)
      },
      onDismiss = { showThemeDialog = false }
    )
  }
}
