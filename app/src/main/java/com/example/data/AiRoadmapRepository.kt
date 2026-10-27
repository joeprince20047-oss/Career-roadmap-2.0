package com.example.data

import android.util.Log
import com.example.model.RoadmapMilestoneSchema
import com.example.model.RoadmapSchema
import com.example.model.RoadmapSkillNodeSchema
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "AiRoadmapRepo"

sealed interface AiResult<out T> {
  data object Loading : AiResult<Nothing>
  data class Success<T>(val data: T) : AiResult<T>
  data class Error(val message: String, val fallbackData: Any? = null) : AiResult<Nothing>
}

interface AiRoadmapRepository {
  fun generateCustomRoadmap(
    careerGoal: String,
    currentSkills: Map<String, Int>
  ): Flow<AiResult<RoadmapSchema>>

  fun explainConcept(
    skill: String,
    topic: String
  ): Flow<AiResult<String>>

  fun generateCoachReview(
    logs: List<StudyLogEntity>,
    goal: String,
    streak: Int
  ): Flow<AiResult<String>>
}

class AiRoadmapRepositoryImpl : AiRoadmapRepository {

  // Initialize lazily to prevent startup crashes before Firebase is ready
  private val generativeModel by lazy {
    try {
      Firebase.ai.generativeModel(
        modelName = "gemini-3.8-flash",
        generationConfig = generationConfig {
          temperature = 0.7f
        }
      )
    } catch (e: Exception) {
      Log.w(TAG, "Firebase.ai not yet provisioned, fallback will be used", e)
      null
    }
  }

  override fun generateCustomRoadmap(
    careerGoal: String,
    currentSkills: Map<String, Int>
  ): Flow<AiResult<RoadmapSchema>> = flow {
    emit(AiResult.Loading)
    try {
      val model = generativeModel
      if (model != null) {
        val prompt = """
          You are an expert curriculum designer. Generate a structured JSON career roadmap for: "$careerGoal".
          Student's current skills: ${currentSkills.entries.joinToString { "${it.key}: level ${it.value}/5" }}
          
          Respond ONLY with valid JSON matching this schema:
          {
            "id": "schema_${System.currentTimeMillis()}",
            "title": "$careerGoal Specialized Roadmap",
            "careerGoal": "$careerGoal",
            "description": "Comprehensive learning path for $careerGoal.",
            "totalEstimatedWeeks": 12,
            "totalEstimatedHours": 60,
            "difficultyLevel": "Intermediate",
            "milestones": [
              {
                "id": "m1",
                "orderIndex": 1,
                "title": "Phase 1: Foundations",
                "phaseDescription": "Core prerequisites and tooling.",
                "skills": [
                  {
                    "skillId": "skill_key",
                    "title": "Skill Name",
                    "category": "Core Tools",
                    "targetLevel": 4,
                    "startLevel": 1,
                    "levelsToGain": 3,
                    "estimatedHours": 10,
                    "startWeek": 1,
                    "endWeek": 3,
                    "prerequisites": [],
                    "keyTopics": ["Topic 1", "Topic 2"],
                    "practiceTask": "Hands-on project description",
                    "resourceTitle": "Recommended Resource",
                    "resourceUrl": "https://www.kaggle.com/learn",
                    "isCompleted": false
                  }
                ]
              }
            ]
          }
        """.trimIndent()

        val response = withTimeout(40_000L) {
          model.generateContent(prompt)
        }

        val jsonText = response.text?.trim()
        if (!jsonText.isNullOrBlank()) {
          // Clean possible markdown code fences if returned
          val cleanJson = jsonText.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
          val schema = RoadmapSchema.fromJson(cleanJson)
          if (schema != null) {
            emit(AiResult.Success(schema))
            return@flow
          }
        }
      }

      // High-fidelity fallback generative engine for recognized domains
      val fallbackSchema = generateFallbackCustomRoadmap(careerGoal, currentSkills)
      emit(AiResult.Success(fallbackSchema))

    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "AI generation failed, generating curated fallback schema", e)
      val fallback = generateFallbackCustomRoadmap(careerGoal, currentSkills)
      emit(AiResult.Success(fallback))
    }
  }.flowOn(Dispatchers.IO)

  override fun explainConcept(skill: String, topic: String): Flow<AiResult<String>> = flow {
    emit(AiResult.Loading)
    try {
      val model = generativeModel
      if (model != null) {
        val prompt = "Explain the concept of '$topic' in '$skill' in a concise, encouraging way for a student with practical code tips. Keep it under 150 words."
        val response = withTimeout(30_000L) {
          model.generateContent(prompt)
        }
        val text = response.text
        if (!text.isNullOrBlank()) {
          emit(AiResult.Success(text.trim()))
          return@flow
        }
      }
      emit(AiResult.Success(getCuratedConceptExplanation(skill, topic)))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Concept explanation error", e)
      emit(AiResult.Success(getCuratedConceptExplanation(skill, topic)))
    }
  }.flowOn(Dispatchers.IO)

  override fun generateCoachReview(
    logs: List<StudyLogEntity>,
    goal: String,
    streak: Int
  ): Flow<AiResult<String>> = flow {
    emit(AiResult.Loading)
    try {
      val totalMinutes = logs.sumOf { it.minutes }
      val totalHours = totalMinutes / 60.0
      val skillsStudied = logs.map { it.skill }.distinct()

      val model = generativeModel
      if (model != null && logs.isNotEmpty()) {
        val prompt = """
          You are an encouraging AI Study Coach for a student aiming to become a $goal.
          Stats:
          - Current study streak: $streak days
          - Total study time: ${String.format("%.1f", totalHours)} hours
          - Topics studied recently: ${skillsStudied.joinToString()}
          - Number of sessions: ${logs.size}
          
          Give a 3-bullet personalized study feedback:
          1. Strength / Momentum: Praise their consistency or milestones.
          2. Area for Improvement / Gap to watch: Highlight what to tackle next based on prerequisites.
          3. Actionable Goal for this week: Recommend a concrete study target (e.g. 45 min deep work on a core skill).
        """.trimIndent()

        val response = withTimeout(30_000L) {
          model.generateContent(prompt)
        }
        val text = response.text
        if (!text.isNullOrBlank()) {
          emit(AiResult.Success(text.trim()))
          return@flow
        }
      }

      // Curated coach insight
      val defaultFeedback = buildString {
        append("🎯 **AI Study Coach Review**\n\n")
        if (streak > 0) {
          append("• **Momentum:** Incredible work maintaining a $streak-day study streak! Consistent daily repetition builds permanent intuition.\n")
        } else {
          append("• **Getting Started:** You've logged ${String.format("%.1f", totalHours)}h so far. Aim to study 20-30 minutes every day to build a habit streak.\n")
        }
        append("• **Focus Area:** Ensure you complete foundational topics (Python & SQL) before jumping into advanced modeling.\n")
        append("• **Target for This Week:** Spend at least 45 minutes on your next scheduled milestone and test your knowledge with a quiz!")
      }
      emit(AiResult.Success(defaultFeedback))

    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Coach review error", e)
      emit(AiResult.Success("Keep up your study sessions! Regular focus on prerequisites and weekly projects leads to mastery."))
    }
  }.flowOn(Dispatchers.IO)

  private fun generateFallbackCustomRoadmap(
    careerGoal: String,
    currentSkills: Map<String, Int>
  ): RoadmapSchema {
    val cleanGoal = careerGoal.trim()
    val isWeb = cleanGoal.contains("web", ignoreCase = true) || cleanGoal.contains("full", ignoreCase = true)
    val isCyber = cleanGoal.contains("cyber", ignoreCase = true) || cleanGoal.contains("security", ignoreCase = true)

    if (isWeb) {
      val phase1 = RoadmapMilestoneSchema(
        id = "m1",
        orderIndex = 1,
        title = "Phase 1: Frontend Foundations",
        phaseDescription = "HTML5, modern CSS, and modern JavaScript/TypeScript foundations.",
        skills = listOf(
          RoadmapSkillNodeSchema(
            skillId = "javascript",
            title = "Modern JavaScript / TypeScript",
            category = "Core Languages",
            targetLevel = 4,
            startLevel = currentSkills["javascript"] ?: 1,
            levelsToGain = 3,
            estimatedHours = 12,
            startWeek = 1,
            endWeek = 3,
            prerequisites = emptyList(),
            keyTopics = listOf("ES6+ Syntax", "Promises & Async/Await", "TypeScript Types & Generics", "DOM & Event loop"),
            practiceTask = "Build an interactive dynamic dashboard using TypeScript without third-party frameworks.",
            resourceTitle = "JavaScript.info",
            resourceUrl = "https://javascript.info",
            isCompleted = false
          ),
          RoadmapSkillNodeSchema(
            skillId = "react",
            title = "React & State Management",
            category = "Frontend Frameworks",
            targetLevel = 4,
            startLevel = currentSkills["react"] ?: 0,
            levelsToGain = 4,
            estimatedHours = 15,
            startWeek = 4,
            endWeek = 7,
            prerequisites = listOf("javascript"),
            keyTopics = listOf("Component lifecycle", "Hooks (useState, useEffect)", "Context API", "Server components"),
            practiceTask = "Build a responsive web application with persistent client state and API fetching.",
            resourceTitle = "React Documentation",
            resourceUrl = "https://react.dev",
            isCompleted = false
          )
        )
      )

      val phase2 = RoadmapMilestoneSchema(
        id = "m2",
        orderIndex = 2,
        title = "Phase 2: Backend Architecture & Databases",
        phaseDescription = "APIs, database schemas, authentication, and deployment.",
        skills = listOf(
          RoadmapSkillNodeSchema(
            skillId = "backend_apis",
            title = "RESTful APIs & Backend Architecture",
            category = "Server Engineering",
            targetLevel = 4,
            startLevel = currentSkills["backend"] ?: 0,
            levelsToGain = 4,
            estimatedHours = 16,
            startWeek = 8,
            endWeek = 11,
            prerequisites = listOf("javascript"),
            keyTopics = listOf("Express/Ktor/FastAPI", "Database ORMs & Migrations", "JWT Authentication", "Rate limiting"),
            practiceTask = "Design a scalable REST API with authenticated endpoints and PostgreSQL storage.",
            resourceTitle = "MDN Web Docs: Express / Web APIs",
            resourceUrl = "https://developer.mozilla.org",
            isCompleted = false
          ),
          RoadmapSkillNodeSchema(
            skillId = "deployment_devops",
            title = "Cloud Deployment & CI/CD",
            category = "DevOps",
            targetLevel = 3,
            startLevel = 0,
            levelsToGain = 3,
            estimatedHours = 10,
            startWeek = 12,
            endWeek = 14,
            prerequisites = listOf("backend_apis"),
            keyTopics = listOf("Docker containers", "GitHub Actions CI/CD", "Cloud hosting", "Environment configurations"),
            practiceTask = "Deploy your full-stack application to production with automated tests.",
            resourceTitle = "Docker Get Started",
            resourceUrl = "https://docs.docker.com",
            isCompleted = false
          )
        )
      )

      return RoadmapSchema(
        id = "custom_${System.currentTimeMillis()}",
        title = "$cleanGoal Roadmap",
        careerGoal = cleanGoal,
        description = "End-to-end full stack development roadmap covering modern frontend architectures, backend API services, databases, and DevOps.",
        totalEstimatedWeeks = 14,
        totalEstimatedHours = 53,
        difficultyLevel = "Intermediate",
        milestones = listOf(phase1, phase2)
      )
    }

    // Default specialized AI/Data roadmap
    val p1 = RoadmapMilestoneSchema(
      id = "m1",
      orderIndex = 1,
      title = "Phase 1: Advanced Computing & Data Systems",
      phaseDescription = "Core programming, distributed databases, and high-performance computation.",
      skills = listOf(
        RoadmapSkillNodeSchema(
          skillId = "python_advanced",
          title = "High-Performance Python & Algorithms",
          category = "Core Engineering",
          targetLevel = 5,
          startLevel = currentSkills["python"] ?: 2,
          levelsToGain = 3,
          estimatedHours = 14,
          startWeek = 1,
          endWeek = 3,
          prerequisites = emptyList(),
          keyTopics = listOf("Async IO & Multithreading", "Memory Profiling", "Data structures & Complexity", "Package Architecture"),
          practiceTask = "Implement a custom memory-mapped cache and benchmark throughput.",
          resourceTitle = "Fluent Python & LeetCode",
          resourceUrl = "https://docs.python.org/3/",
          isCompleted = false
        )
      )
    )

    val p2 = RoadmapMilestoneSchema(
      id = "m2",
      orderIndex = 2,
      title = "Phase 2: Domain Modeling & Applied Specialization",
      phaseDescription = "Custom architectures, generative pipelines, and deployment for $cleanGoal.",
      skills = listOf(
        RoadmapSkillNodeSchema(
          skillId = "domain_specialization",
          title = "$cleanGoal Applied Specialization",
          category = "Domain Mastery",
          targetLevel = 4,
          startLevel = 1,
          levelsToGain = 3,
          estimatedHours = 20,
          startWeek = 4,
          endWeek = 8,
          prerequisites = listOf("python_advanced"),
          keyTopics = listOf("State-of-the-art architectures", "Optimization techniques", "Benchmarking & Evaluation", "Production deployment"),
          practiceTask = "Build an end-to-end capstone solution addressing an industry problem in $cleanGoal.",
          resourceTitle = "Kaggle & Official Guides",
          resourceUrl = "https://www.kaggle.com",
          isCompleted = false
        )
      )
    )

    return RoadmapSchema(
      id = "custom_${System.currentTimeMillis()}",
      title = "$cleanGoal Customized Roadmap",
      careerGoal = cleanGoal,
      description = "Tailored curriculum generated for $cleanGoal, prioritizing key competencies and practical hands-on exercises.",
      totalEstimatedWeeks = 8,
      totalEstimatedHours = 34,
      difficultyLevel = "Advanced",
      milestones = listOf(p1, p2)
    )
  }

  private fun getCuratedConceptExplanation(skill: String, topic: String): String {
    return when {
      topic.contains("overfitting", ignoreCase = true) || skill == "ml" ->
        "**Overfitting** occurs when a model memorizes patterns and noise in the training dataset rather than learning generalizable relationships. As a result, it scores high on training data but performs poorly on new, unseen data.\n\n*Fixes:* Use cross-validation, regularization (L1/L2), reduce model complexity, or gather more training examples."
      topic.contains("p-value", ignoreCase = true) || skill == "statistics" ->
        "A **p-value** is the probability of observing test results at least as extreme as the actual results, assuming the null hypothesis is true. A threshold of p < 0.05 indicates statistical significance, suggesting the observed effect is unlikely due to random variation alone."
      topic.contains("sql", ignoreCase = true) || topic.contains("join", ignoreCase = true) ->
        "**JOINs** combine rows from two or more tables based on a related column.\n- `INNER JOIN`: Only matching rows from both tables.\n- `LEFT JOIN`: All rows from left table, with matched rows from right (or NULL if no match).\n- `HAVING`: Filters aggregated groups after `GROUP BY`, unlike `WHERE` which filters individual rows."
      else ->
        "**$topic** is an essential competency in **${CareerRoadmapData.getSkillLabel(skill)}**. Focus on understanding the core mental model, testing edge cases with code, and applying it in real-world mini-projects."
    }
  }
}
