package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.SkillProficiencyRepository
import com.example.ui.components.buildProficiencyBarPoints
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Career Roadmap", appName)
  }

  @Test
  fun `theme preferences default and persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Test saving and retrieving dark mode
    com.example.ui.theme.ThemePreferences.setThemeMode(context, com.example.ui.theme.ThemeMode.DARK)
    assertEquals(com.example.ui.theme.ThemeMode.DARK, com.example.ui.theme.ThemePreferences.getThemeMode(context))

    // Test saving and retrieving light mode
    com.example.ui.theme.ThemePreferences.setThemeMode(context, com.example.ui.theme.ThemeMode.LIGHT)
    assertEquals(com.example.ui.theme.ThemeMode.LIGHT, com.example.ui.theme.ThemePreferences.getThemeMode(context))

    // Test saving and retrieving system default
    com.example.ui.theme.ThemePreferences.setThemeMode(context, com.example.ui.theme.ThemeMode.SYSTEM)
    assertEquals(com.example.ui.theme.ThemeMode.SYSTEM, com.example.ui.theme.ThemePreferences.getThemeMode(context))
  }

  @Test
  fun `skill proficiency over time persisted in Room and transformed for bar chart`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val repo = SkillProficiencyRepository(db.appDao())

    val snap1 = mapOf("python" to 1, "sql" to 1, "statistics" to 1)
    val snap2 = mapOf("python" to 3, "sql" to 2, "statistics" to 2)
    val snap3 = mapOf("python" to 4, "sql" to 3, "statistics" to 3)

    repo.recordSkillProficiencyChange(
      userId = 1L,
      goal = "Data Scientist",
      skill = "python",
      proficiencyLevel = 1,
      skillsSnapshot = snap1,
      recordedDate = "2026-10-20",
      timestamp = 1000L
    )
    repo.recordSkillProficiencyChange(
      userId = 1L,
      goal = "Data Scientist",
      skill = "python",
      proficiencyLevel = 3,
      skillsSnapshot = snap2,
      recordedDate = "2026-10-23",
      timestamp = 2000L
    )
    repo.recordSkillProficiencyChange(
      userId = 1L,
      goal = "Data Scientist",
      skill = "sql",
      proficiencyLevel = 3,
      skillsSnapshot = snap3,
      recordedDate = "2026-10-27",
      timestamp = 3000L
    )

    val history = repo.observeUserSkillHistory(1L).first()
    assertEquals(3, history.size)
    assertTrue(history[2].fitScore > history[0].fitScore)

    // Verify overall_fit bar points
    val fitPoints = buildProficiencyBarPoints(history, "Data Scientist", "overall_fit")
    assertEquals(3, fitPoints.size)
    assertEquals("10-20", fitPoints[0].dateLabel)
    assertEquals("10-27", fitPoints[2].dateLabel)
    assertTrue(fitPoints[2].value > fitPoints[0].value)

    // Verify per-skill ("python") bar points
    val pythonPoints = buildProficiencyBarPoints(history, "Data Scientist", "python")
    assertEquals(3, pythonPoints.size)
    assertEquals(1.0, pythonPoints[0].value, 0.01)
    assertEquals(3.0, pythonPoints[1].value, 0.01)
    assertEquals(4.0, pythonPoints[2].value, 0.01)

    db.close()
  }

  @Test
  fun `calculateCurriculumSkillGap compares user progress against predefined AI Data Science curriculum roadmap`() {
    val userProgress = mapOf(
      "python" to 4,         // Target is 4 -> Gap 0 (MASTERED)
      "git" to 3,            // Target is 3 -> Gap 0 (MASTERED)
      "sql" to 2,            // Target is 3 -> Gap 1
      "statistics" to 1,     // Target is 4 -> Gap 3 (unmet prereq for ml since < 2)
      "pandas" to 2,         // Target is 4 -> Gap 2 (python is 4 >= 2 so prereq ready)
      "visualization" to 1,  // Target is 3 -> Gap 2 (pandas is 2 >= 2 so prereq ready)
      "ml" to 0              // Target is 4 -> Gap 4 (blocked by statistics < 2)
    )

    val report = com.example.engine.RoadmapEngine.calculateCurriculumSkillGap(
      currentSkills = userProgress,
      completedSteps = setOf("sql"), // sql marked completed -> effective level becomes target (3)
      currentWeek = 5,
      studyMinutesBySkill = mapOf("pandas" to 120)
    )

    assertEquals(12, report.totalSkillsCount)
    // python (lvl 4), git (lvl 3), and sql (completedStep) are mastered -> 3 mastered skills
    assertEquals(3, report.masteredSkillsCount)
    assertEquals(9, report.skillsWithGapCount)
    assertTrue(report.overallReadinessPercent > 25.0 && report.overallReadinessPercent < 90.0)

    // Check individual module gap calculations
    val pythonItem = report.moduleGaps.first { it.skillId == "python" }
    assertEquals(0, pythonItem.levelGap)
    assertEquals(com.example.model.CurriculumGapStatus.MASTERED, pythonItem.status)

    val sqlItem = report.moduleGaps.first { it.skillId == "sql" }
    assertEquals(0, sqlItem.levelGap)
    assertEquals(com.example.model.CurriculumGapStatus.MASTERED, sqlItem.status)

    val pandasItem = report.moduleGaps.first { it.skillId == "pandas" }
    assertEquals(2, pandasItem.levelGap)
    assertTrue(pandasItem.isPrerequisiteReady)
    assertEquals(com.example.model.CurriculumGapStatus.ON_TRACK, pandasItem.status)

    // ML requires python (4 >= 2) and statistics (1 < 2), so statistics is an unmet prerequisite
    val mlItem = report.moduleGaps.first { it.skillId == "ml" }
    assertEquals(4, mlItem.levelGap)
    assertEquals(listOf("statistics"), mlItem.unmetPrerequisites)
    assertEquals(com.example.model.CurriculumGapStatus.PREREQ_BLOCKED, mlItem.status)

    // Unlocked gaps should be prioritized ahead of prerequisite-blocked gaps
    assertTrue(report.prioritizedGaps.first().isPrerequisiteReady)
    assertEquals(4, report.phaseSummaries.size)
  }
}

