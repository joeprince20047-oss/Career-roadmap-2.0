package com.example.model

import com.example.data.CareerRoadmapData
import org.json.JSONArray
import org.json.JSONObject

/**
 * Formal Roadmap Schema representing an entire structured learning curriculum.
 */
data class RoadmapSchema(
  val id: String,
  val title: String,
  val careerGoal: String,
  val description: String,
  val totalEstimatedWeeks: Int,
  val totalEstimatedHours: Int,
  val difficultyLevel: String, // "Beginner", "Intermediate", "Advanced"
  val milestones: List<RoadmapMilestoneSchema>
) {
  fun toJson(): String {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("title", title)
    obj.put("careerGoal", careerGoal)
    obj.put("description", description)
    obj.put("totalEstimatedWeeks", totalEstimatedWeeks)
    obj.put("totalEstimatedHours", totalEstimatedHours)
    obj.put("difficultyLevel", difficultyLevel)

    val milestonesArray = JSONArray()
    for (m in milestones) {
      milestonesArray.put(m.toJsonObject())
    }
    obj.put("milestones", milestonesArray)
    return obj.toString(2)
  }

  companion object {
    fun fromJson(jsonStr: String): RoadmapSchema? {
      return try {
        val obj = JSONObject(jsonStr)
        val milestonesList = mutableListOf<RoadmapMilestoneSchema>()
        val milestonesArr = obj.optJSONArray("milestones") ?: JSONArray()
        for (i in 0 until milestonesArr.length()) {
          val mObj = milestonesArr.getJSONObject(i)
          milestonesList.add(RoadmapMilestoneSchema.fromJsonObject(mObj))
        }

        RoadmapSchema(
          id = obj.optString("id", "roadmap_${System.currentTimeMillis()}"),
          title = obj.optString("title", "Career Roadmap"),
          careerGoal = obj.optString("careerGoal", "Data Scientist"),
          description = obj.optString("description", ""),
          totalEstimatedWeeks = obj.optInt("totalEstimatedWeeks", 12),
          totalEstimatedHours = obj.optInt("totalEstimatedHours", 60),
          difficultyLevel = obj.optString("difficultyLevel", "Intermediate"),
          milestones = milestonesList
        )
      } catch (_: Exception) {
        null
      }
    }

    /**
     * Constructs a full RoadmapSchema from the current calculated plan and profile.
     */
    fun createFromPlan(
      goal: String,
      plan: List<RoadmapStep>,
      stepsDone: Set<String>,
      startSkills: Map<String, Int>
    ): RoadmapSchema {
      val allSkills = plan.map { it.skill }
      val totalHours = allSkills.sumOf { CareerRoadmapData.RESOURCES[it]?.hours ?: 5 }
      val totalWeeks = plan.maxOfOrNull { it.endWeek } ?: 1

      // Group steps into 3 learning phases / milestones
      val phaseCount = if (plan.size > 6) 3 else 2
      val chunkSize = kotlin.math.max(1, (plan.size + phaseCount - 1) / phaseCount)
      val chunks = plan.chunked(chunkSize)

      val milestoneNames = listOf(
        "Phase 1: Foundations & Core Tooling",
        "Phase 2: Modeling & Core Applied Skills",
        "Phase 3: Production, Specialization & Portfolio"
      )

      val milestoneDescriptions = listOf(
        "Master foundational programming, mathematics, and essential data handling tools.",
        "Develop core predictive models, algorithms, and comprehensive evaluation techniques.",
        "Synthesize concepts into full production pipelines, cloud workflows, and capstone projects."
      )

      val milestones = chunks.mapIndexed { index, stepList ->
        val nodes = stepList.map { step ->
          val res = CareerRoadmapData.RESOURCES[step.skill]
          val prereqs = CareerRoadmapData.PREREQUISITES[step.skill] ?: emptyList()
          val skillName = CareerRoadmapData.getSkillLabel(step.skill)

          val keyTopics = getKeyTopicsForSkill(step.skill)
          val practiceTask = getPracticeTaskForSkill(step.skill)

          RoadmapSkillNodeSchema(
            skillId = step.skill,
            title = skillName,
            category = getCategoryForSkill(step.skill),
            targetLevel = (startSkills[step.skill] ?: 0) + step.levelsToGain,
            startLevel = startSkills[step.skill] ?: 0,
            levelsToGain = step.levelsToGain,
            estimatedHours = res?.hours ?: 5,
            startWeek = step.startWeek,
            endWeek = step.endWeek,
            prerequisites = prereqs,
            keyTopics = keyTopics,
            practiceTask = practiceTask,
            resourceTitle = res?.title ?: "Guided Practice",
            resourceUrl = res?.url ?: "https://www.kaggle.com/learn",
            isCompleted = step.skill in stepsDone
          )
        }

        RoadmapMilestoneSchema(
          id = "milestone_${index + 1}",
          orderIndex = index + 1,
          title = milestoneNames.getOrElse(index) { "Phase ${index + 1}: Applied Specialization" },
          phaseDescription = milestoneDescriptions.getOrElse(index) { "Advanced industry skills." },
          skills = nodes
        )
      }

      return RoadmapSchema(
        id = "schema_${goal.lowercase().replace(" ", "_")}",
        title = "$goal Comprehensive Roadmap",
        careerGoal = goal,
        description = "Structured skill progression pathway designed for aspiring $goal professionals with topological dependency sequencing.",
        totalEstimatedWeeks = totalWeeks,
        totalEstimatedHours = totalHours,
        difficultyLevel = if (goal == "Data Analyst") "Beginner to Intermediate" else "Intermediate to Advanced",
        milestones = milestones
      )
    }

    private fun getCategoryForSkill(skill: String): String {
      return when (skill) {
        "python", "excel", "git" -> "Core Tools"
        "sql", "pandas", "visualization" -> "Data Wrangling & Analysis"
        "statistics", "ml", "model_evaluation" -> "Machine Learning & Math"
        "deep_learning", "cloud", "mlops" -> "Advanced Systems & Production"
        else -> "Domain Project"
      }
    }

    private fun getKeyTopicsForSkill(skill: String): List<String> {
      return when (skill) {
        "python" -> listOf("Data types & collections", "Functions & lambdas", "OOP concepts", "Error handling & list comprehensions")
        "sql" -> listOf("SELECT & WHERE filters", "JOINs (Inner, Left, Outer)", "Aggregations & GROUP BY", "HAVING clauses & subqueries")
        "pandas" -> listOf("Series & DataFrame indexing", "Filtering & transformations", "Missing data handling", "GroupBy & Pivot tables")
        "statistics" -> listOf("Central tendency & variance", "Probability distributions", "Hypothesis testing (p-values)", "Confidence intervals & correlation")
        "visualization" -> listOf("Scatter plots & histograms", "Time-series charts", "Custom color palettes", "Interactive dash layout")
        "ml" -> listOf("Supervised vs unsupervised", "Linear & logistic regression", "Decision trees & random forests", "Overfitting & regularization")
        "model_evaluation" -> listOf("Train-test splits & cross-validation", "Confusion matrix & accuracy", "Precision, recall & F1-score", "ROC-AUC curves")
        "deep_learning" -> listOf("Neural network architectures", "Activation functions", "Loss functions & optimizers", "Convolutional & dense layers")
        "mlops" -> listOf("Pipeline orchestration", "Model versioning & registries", "Docker containerization", "CI/CD model monitoring")
        "cloud" -> listOf("Cloud storage & compute", "IAM & permissions", "Serverless workflows", "Cloud databases & API hosting")
        "git" -> listOf("Repositories & commits", "Branching & merging", "Remote origin & pull requests", "Conflict resolution")
        "projects" -> listOf("Problem formulation", "Dataset curation & EDA", "Model pipeline implementation", "Documentation & GitHub showcase")
        "excel" -> listOf("VLOOKUP & XLOOKUP", "Pivot tables & charts", "Data validation rules", "Conditional formatting & formulas")
        else -> listOf("Fundamentals", "Core applications", "Best practices", "Capstone showcase")
      }
    }

    private fun getPracticeTaskForSkill(skill: String): String {
      return when (skill) {
        "python" -> "Write a script that parses raw CSV records, cleans messy data, and computes summary statistics."
        "sql" -> "Write queries to identify top 5 customer segments by retention rate using JOINs and GROUP BY."
        "pandas" -> "Load a real-world Kaggle dataset and perform exploratory data analysis with data imputation."
        "statistics" -> "Perform an A/B test analysis to evaluate if a conversion rate difference is statistically significant."
        "visualization" -> "Create a multi-panel dashboard chart illustrating seasonal sales trends and correlation heatmaps."
        "ml" -> "Train a Random Forest classifier to predict housing prices and optimize hyperparameters."
        "model_evaluation" -> "Evaluate a medical diagnosis dataset using F1-score and ROC-AUC curve instead of raw accuracy."
        "deep_learning" -> "Build a multi-layer neural network on MNIST or CIFAR to classify image inputs."
        "mlops" -> "Package a trained model with an inference API and setup automated testing."
        "cloud" -> "Deploy a simple predictive API on AWS or Google Cloud and query it with test inputs."
        "git" -> "Create a clean Git repository with conventional commits, feature branches, and a detailed README."
        "projects" -> "Complete the Titanic competition or an end-to-end churn prediction project from scratch."
        "excel" -> "Build an automated sales dashboard using Pivot Tables and nested lookup functions."
        else -> "Build an end-to-end hands-on project demonstrating mastery."
      }
    }
  }
}

data class RoadmapMilestoneSchema(
  val id: String,
  val orderIndex: Int,
  val title: String,
  val phaseDescription: String,
  val skills: List<RoadmapSkillNodeSchema>
) {
  fun toJsonObject(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("orderIndex", orderIndex)
    obj.put("title", title)
    obj.put("phaseDescription", phaseDescription)

    val skillsArr = JSONArray()
    for (s in skills) {
      skillsArr.put(s.toJsonObject())
    }
    obj.put("skills", skillsArr)
    return obj
  }

  companion object {
    fun fromJsonObject(obj: JSONObject): RoadmapMilestoneSchema {
      val skillsList = mutableListOf<RoadmapSkillNodeSchema>()
      val skillsArr = obj.optJSONArray("skills") ?: JSONArray()
      for (i in 0 until skillsArr.length()) {
        val sObj = skillsArr.getJSONObject(i)
        skillsList.add(RoadmapSkillNodeSchema.fromJsonObject(sObj))
      }
      return RoadmapMilestoneSchema(
        id = obj.optString("id", "milestone"),
        orderIndex = obj.optInt("orderIndex", 1),
        title = obj.optString("title", "Phase"),
        phaseDescription = obj.optString("phaseDescription", ""),
        skills = skillsList
      )
    }
  }
}

data class RoadmapSkillNodeSchema(
  val skillId: String,
  val title: String,
  val category: String,
  val targetLevel: Int,
  val startLevel: Int,
  val levelsToGain: Int,
  val estimatedHours: Int,
  val startWeek: Int,
  val endWeek: Int,
  val prerequisites: List<String>,
  val keyTopics: List<String>,
  val practiceTask: String,
  val resourceTitle: String,
  val resourceUrl: String,
  val isCompleted: Boolean
) {
  fun toJsonObject(): JSONObject {
    val obj = JSONObject()
    obj.put("skillId", skillId)
    obj.put("title", title)
    obj.put("category", category)
    obj.put("targetLevel", targetLevel)
    obj.put("startLevel", startLevel)
    obj.put("levelsToGain", levelsToGain)
    obj.put("estimatedHours", estimatedHours)
    obj.put("startWeek", startWeek)
    obj.put("endWeek", endWeek)
    obj.put("practiceTask", practiceTask)
    obj.put("resourceTitle", resourceTitle)
    obj.put("resourceUrl", resourceUrl)
    obj.put("isCompleted", isCompleted)

    val prereqArr = JSONArray()
    prerequisites.forEach { prereqArr.put(it) }
    obj.put("prerequisites", prereqArr)

    val topicsArr = JSONArray()
    keyTopics.forEach { topicsArr.put(it) }
    obj.put("keyTopics", topicsArr)

    return obj
  }

  companion object {
    fun fromJsonObject(obj: JSONObject): RoadmapSkillNodeSchema {
      val prereqs = mutableListOf<String>()
      val pArr = obj.optJSONArray("prerequisites") ?: JSONArray()
      for (i in 0 until pArr.length()) {
        prereqs.add(pArr.getString(i))
      }

      val topics = mutableListOf<String>()
      val tArr = obj.optJSONArray("keyTopics") ?: JSONArray()
      for (i in 0 until tArr.length()) {
        topics.add(tArr.getString(i))
      }

      return RoadmapSkillNodeSchema(
        skillId = obj.optString("skillId", "skill"),
        title = obj.optString("title", "Skill"),
        category = obj.optString("category", "General"),
        targetLevel = obj.optInt("targetLevel", 3),
        startLevel = obj.optInt("startLevel", 0),
        levelsToGain = obj.optInt("levelsToGain", 1),
        estimatedHours = obj.optInt("estimatedHours", 5),
        startWeek = obj.optInt("startWeek", 1),
        endWeek = obj.optInt("endWeek", 1),
        prerequisites = prereqs,
        keyTopics = topics,
        practiceTask = obj.optString("practiceTask", ""),
        resourceTitle = obj.optString("resourceTitle", ""),
        resourceUrl = obj.optString("resourceUrl", ""),
        isCompleted = obj.optBoolean("isCompleted", false)
      )
    }
  }
}
