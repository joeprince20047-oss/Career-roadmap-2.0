package com.example

import com.example.data.CareerRoadmapData
import com.example.engine.RoadmapEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testFitScoreCalculation() {
    // If all skills are level 0, fit score should be 0.0
    val zeroProfile = emptyMap<String, Int>()
    val score = RoadmapEngine.fitScore(zeroProfile, "Data Scientist")
    assertEquals(0.0, score, 0.01)

    // Perfect profile matches 100%
    val maxProfile = CareerRoadmapData.CAREERS["Data Analyst"]!!.mapValues { it.value.level }
    val maxScore = RoadmapEngine.fitScore(maxProfile, "Data Analyst")
    assertEquals(100.0, maxScore, 0.01)
  }

  @Test
  fun testFindGapsAndRoadmapOrdering() {
    val starterProfile = mapOf("python" to 2, "sql" to 1)
    val gaps = RoadmapEngine.findGaps(starterProfile, "Data Scientist")
    assertTrue(gaps.isNotEmpty())

    // Ensure gaps are sorted by descending priority (gap * weight)
    for (i in 0 until gaps.size - 1) {
      assertTrue(gaps[i].priority >= gaps[i + 1].priority)
    }

    val plan = RoadmapEngine.buildRoadmap(gaps, starterProfile)
    assertTrue(plan.isNotEmpty())
    // Weeks should be strictly monotonically progressing
    for (i in 0 until plan.size - 1) {
      assertEquals(plan[i].endWeek + 1, plan[i + 1].startWeek)
    }
  }

  @Test
  fun testNormalizedLearningGain() {
    // Pre = 40, Post = 70
    // Gain = (70 - 40) / (100 - 40) = 30 / 60 = 0.50
    val result = RoadmapEngine.assessmentImprovement(40, 70)
    assertEquals(30, result.rawGain)
    assertEquals(0.5, result.normalizedGain, 0.01)
  }
}
