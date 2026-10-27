package com.example.data

import com.example.model.CurriculumModule
import com.example.model.QuizQuestion
import com.example.model.ResourceItem
import com.example.model.SkillRequirement

object CareerRoadmapData {

  val SKILL_LABELS = mapOf(
    "ml" to "Machine Learning",
    "sql" to "SQL",
    "mlops" to "MLOps",
    "git" to "Git / GitHub",
    "deep_learning" to "Deep Learning",
    "model_evaluation" to "Model Evaluation",
    "python" to "Python",
    "statistics" to "Statistics & Probability",
    "pandas" to "Pandas",
    "visualization" to "Data Visualization",
    "projects" to "Portfolio Projects",
    "cloud" to "Cloud Computing",
    "excel" to "Microsoft Excel"
  )

  fun getSkillLabel(skillKey: String): String {
    return SKILL_LABELS[skillKey]
      ?: skillKey.replace("_", " ").split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
  }

  val WEEKS_PER_LEVEL = mapOf(
    "default" to 1.0,
    "projects" to 2.0,
    "statistics" to 1.5,
    "deep_learning" to 2.0
  )

  // Careers and their required skill levels + weights
  val CAREERS: Map<String, Map<String, SkillRequirement>> = mapOf(
    "Data Scientist" to mapOf(
      "python" to SkillRequirement("python", 4, 1.0),
      "sql" to SkillRequirement("sql", 3, 0.8),
      "statistics" to SkillRequirement("statistics", 4, 1.0),
      "pandas" to SkillRequirement("pandas", 4, 0.9),
      "visualization" to SkillRequirement("visualization", 3, 0.7),
      "ml" to SkillRequirement("ml", 4, 1.0),
      "model_evaluation" to SkillRequirement("model_evaluation", 4, 0.9),
      "git" to SkillRequirement("git", 3, 0.5),
      "projects" to SkillRequirement("projects", 3, 0.8),
      "deep_learning" to SkillRequirement("deep_learning", 2, 0.4),
      "cloud" to SkillRequirement("cloud", 2, 0.3),
      "mlops" to SkillRequirement("mlops", 1, 0.2)
    ),
    "Data Analyst" to mapOf(
      "sql" to SkillRequirement("sql", 4, 1.0),
      "excel" to SkillRequirement("excel", 4, 0.9),
      "visualization" to SkillRequirement("visualization", 4, 1.0),
      "pandas" to SkillRequirement("pandas", 3, 0.8),
      "statistics" to SkillRequirement("statistics", 3, 0.8),
      "python" to SkillRequirement("python", 3, 0.6),
      "projects" to SkillRequirement("projects", 3, 0.7)
    ),
    "ML Engineer" to mapOf(
      "python" to SkillRequirement("python", 5, 1.0),
      "ml" to SkillRequirement("ml", 4, 1.0),
      "deep_learning" to SkillRequirement("deep_learning", 4, 0.8),
      "git" to SkillRequirement("git", 4, 0.8),
      "cloud" to SkillRequirement("cloud", 4, 0.9),
      "mlops" to SkillRequirement("mlops", 4, 1.0),
      "model_evaluation" to SkillRequirement("model_evaluation", 4, 0.7),
      "projects" to SkillRequirement("projects", 4, 0.8)
    )
  )

  // Prerequisites graph
  val PREREQUISITES: Map<String, List<String>> = mapOf(
    "pandas" to listOf("python"),
    "ml" to listOf("python", "statistics"),
    "model_evaluation" to listOf("ml"),
    "deep_learning" to listOf("ml"),
    "mlops" to listOf("ml", "git"),
    "cloud" to listOf("python"),
    "visualization" to listOf("pandas"),
    "projects" to listOf("pandas", "ml")
  )

  // Curated learning resources
  val RESOURCES: Map<String, ResourceItem> = mapOf(
    "python" to ResourceItem("python", "Kaggle Learn: Python", "https://www.kaggle.com/learn/python", 5),
    "sql" to ResourceItem("sql", "Kaggle Learn: Intro to SQL", "https://www.kaggle.com/learn/intro-to-sql", 4),
    "statistics" to ResourceItem("statistics", "Khan Academy: Statistics and probability", "https://www.khanacademy.org/math/statistics-probability", 20),
    "pandas" to ResourceItem("pandas", "Kaggle Learn: Pandas", "https://www.kaggle.com/learn/pandas", 4),
    "visualization" to ResourceItem("visualization", "Kaggle Learn: Data Visualization", "https://www.kaggle.com/learn/data-visualization", 4),
    "ml" to ResourceItem("ml", "Kaggle Learn: Intro to Machine Learning", "https://www.kaggle.com/learn/intro-to-machine-learning", 3),
    "model_evaluation" to ResourceItem("model_evaluation", "scikit-learn: Model evaluation guide", "https://scikit-learn.org/stable/modules/model_evaluation.html", 4),
    "git" to ResourceItem("git", "Pro Git book (free)", "https://git-scm.com/book/en/v2", 6),
    "deep_learning" to ResourceItem("deep_learning", "Kaggle Learn: Intro to Deep Learning", "https://www.kaggle.com/learn/intro-to-deep-learning", 4),
    "projects" to ResourceItem("projects", "Kaggle: Titanic competition", "https://www.kaggle.com/c/titanic", 10),
    "mlops" to ResourceItem("mlops", "Made With ML", "https://madewithml.com", 15),
    "cloud" to ResourceItem("cloud", "AWS Skill Builder", "https://skillbuilder.aws", 6),
    "excel" to ResourceItem("excel", "Microsoft Excel help and learning", "https://support.microsoft.com/excel", 5)
  )

  // Full Quiz Questions Dataset (Form A: Pre-test, Form B: Post-test)
  val QUIZ_QUESTIONS: List<QuizQuestion> = listOf(
    QuizQuestion(
      id = 1,
      skill = "python",
      form = "A",
      question = "What does len([1, 2, 3, 4]) return?",
      a = "Error",
      b = "3",
      c = "5",
      d = "4",
      answer = "D"
    ),
    QuizQuestion(
      id = 2,
      skill = "python",
      form = "B",
      question = "Which keyword defines a function in Python?",
      a = "def",
      b = "func",
      c = "function",
      d = "define",
      answer = "A"
    ),
    QuizQuestion(
      id = 3,
      skill = "sql",
      form = "A",
      question = "Which clause filters rows AFTER grouping?",
      a = "WHERE",
      b = "HAVING",
      c = "ORDER BY",
      d = "LIMIT",
      answer = "B"
    ),
    QuizQuestion(
      id = 4,
      skill = "sql",
      form = "B",
      question = "Which JOIN keeps all rows from the left table even without a match?",
      a = "INNER JOIN",
      b = "RIGHT JOIN",
      c = "CROSS JOIN",
      d = "LEFT JOIN",
      answer = "D"
    ),
    QuizQuestion(
      id = 5,
      skill = "statistics",
      form = "A",
      question = "Which measure is least affected by outliers?",
      a = "Mean",
      b = "Range",
      c = "Variance",
      d = "Median",
      answer = "D"
    ),
    QuizQuestion(
      id = 6,
      skill = "statistics",
      form = "B",
      question = "A p-value below 0.05 usually means the result is...",
      a = "unlikely if the null hypothesis were true",
      b = "proof the null hypothesis is false",
      c = "always practically important",
      d = "caused by a biased sample",
      answer = "A"
    ),
    QuizQuestion(
      id = 7,
      skill = "pandas",
      form = "A",
      question = "Which method shows the first 5 rows of a DataFrame?",
      a = "df.top()",
      b = "df.first()",
      c = "df.head()",
      d = "df.start()",
      answer = "C"
    ),
    QuizQuestion(
      id = 8,
      skill = "pandas",
      form = "B",
      question = "Which expression counts missing values in each column?",
      a = "df.isnull().sum()",
      b = "df.count_na()",
      c = "df.missing()",
      d = "df.nulls()",
      answer = "A"
    ),
    QuizQuestion(
      id = 9,
      skill = "ml",
      form = "A",
      question = "Overfitting means the model...",
      a = "does well on training data but poorly on new data",
      b = "does poorly on all data",
      c = "is too simple to learn patterns",
      d = "has too few features",
      answer = "A"
    ),
    QuizQuestion(
      id = 10,
      skill = "ml",
      form = "B",
      question = "Which of these is a supervised learning task?",
      a = "Grouping customers into segments",
      b = "Predicting house prices from labelled examples",
      c = "Reducing dimensions with PCA",
      d = "Finding topics in unlabelled text",
      answer = "B"
    ),
    QuizQuestion(
      id = 11,
      skill = "model_evaluation",
      form = "A",
      question = "Which metric suits a heavily imbalanced classification problem better than accuracy?",
      a = "Mean squared error",
      b = "R-squared",
      c = "F1 score",
      d = "Mean absolute error",
      answer = "C"
    ),
    QuizQuestion(
      id = 12,
      skill = "model_evaluation",
      form = "B",
      question = "Why do we keep a separate test set?",
      a = "To speed up training",
      b = "To estimate performance on unseen data",
      c = "To add more features",
      d = "To reduce the data size",
      answer = "B"
    ),
    QuizQuestion(
      id = 13,
      skill = "git",
      form = "A",
      question = "Which command saves staged changes to the repository history?",
      a = "git push",
      b = "git add",
      c = "git commit",
      d = "git clone",
      answer = "C"
    ),
    QuizQuestion(
      id = 14,
      skill = "git",
      form = "B",
      question = "Which command downloads a copy of a remote repository?",
      a = "git commit",
      b = "git merge",
      c = "git status",
      d = "git clone",
      answer = "D"
    ),
    QuizQuestion(
      id = 15,
      skill = "visualization",
      form = "A",
      question = "Which chart best shows the relationship between two numeric variables?",
      a = "Pie chart",
      b = "Bar chart of counts",
      c = "Scatter plot",
      d = "Donut chart",
      answer = "C"
    ),
    QuizQuestion(
      id = 16,
      skill = "visualization",
      form = "B",
      question = "A histogram is mainly used to show...",
      a = "the distribution of one numeric variable",
      b = "trends across categories",
      c = "parts of a whole",
      d = "correlation between three variables",
      answer = "A"
    )
  )

  /**
   * Predefined 4-Phase AI & Data Science Curriculum Roadmap against which learner progress is compared.
   */
  val AI_DATA_SCIENCE_CURRICULUM_ROADMAP: List<CurriculumModule> = listOf(
    // Phase 1: Foundations & Core Programming (Weeks 1–4)
    CurriculumModule(
      skillId = "python",
      title = "Python for AI & Data Science",
      phaseIndex = 1,
      phaseTitle = "Phase 1: Foundations & Core Programming",
      targetLevel = 4,
      weight = 1.0,
      estimatedHoursPerLevel = 5,
      targetStartWeek = 1,
      targetEndWeek = 2,
      prerequisites = emptyList(),
      coreCompetencies = listOf("Data structures & comprehensions", "Functions & OOP", "NumPy vectorization")
    ),
    CurriculumModule(
      skillId = "git",
      title = "Git & Reproducible Workflows",
      phaseIndex = 1,
      phaseTitle = "Phase 1: Foundations & Core Programming",
      targetLevel = 3,
      weight = 0.6,
      estimatedHoursPerLevel = 3,
      targetStartWeek = 2,
      targetEndWeek = 3,
      prerequisites = emptyList(),
      coreCompetencies = listOf("Version control", "Branching & PRs", "Experiment tracking hygiene")
    ),
    CurriculumModule(
      skillId = "sql",
      title = "SQL & Relational Data Querying",
      phaseIndex = 1,
      phaseTitle = "Phase 1: Foundations & Core Programming",
      targetLevel = 3,
      weight = 0.85,
      estimatedHoursPerLevel = 4,
      targetStartWeek = 3,
      targetEndWeek = 4,
      prerequisites = emptyList(),
      coreCompetencies = listOf("JOINs & aggregations", "Window functions", "Analytical CTEs")
    ),
    CurriculumModule(
      skillId = "statistics",
      title = "Statistics & Probability Foundations",
      phaseIndex = 1,
      phaseTitle = "Phase 1: Foundations & Core Programming",
      targetLevel = 4,
      weight = 1.0,
      estimatedHoursPerLevel = 6,
      targetStartWeek = 3,
      targetEndWeek = 5,
      prerequisites = emptyList(),
      coreCompetencies = listOf("Distributions & Bayes", "Hypothesis testing & p-values", "Confidence intervals")
    ),

    // Phase 2: Data Wrangling & Exploratory Analysis (Weeks 5–7)
    CurriculumModule(
      skillId = "pandas",
      title = "Pandas & Feature Engineering",
      phaseIndex = 2,
      phaseTitle = "Phase 2: Data Wrangling & Exploratory Analysis",
      targetLevel = 4,
      weight = 0.9,
      estimatedHoursPerLevel = 4,
      targetStartWeek = 5,
      targetEndWeek = 6,
      prerequisites = listOf("python"),
      coreCompetencies = listOf("DataFrame indexing & joins", "Missing value imputation", "GroupBy & feature pipelines")
    ),
    CurriculumModule(
      skillId = "visualization",
      title = "Data Visualization & Storytelling",
      phaseIndex = 2,
      phaseTitle = "Phase 2: Data Wrangling & Exploratory Analysis",
      targetLevel = 3,
      weight = 0.75,
      estimatedHoursPerLevel = 4,
      targetStartWeek = 6,
      targetEndWeek = 7,
      prerequisites = listOf("pandas"),
      coreCompetencies = listOf("Distributions & correlation plots", "Multivariate charts", "Executive dashboards")
    ),

    // Phase 3: Machine Learning & Model Evaluation (Weeks 8–11)
    CurriculumModule(
      skillId = "ml",
      title = "Machine Learning Algorithms",
      phaseIndex = 3,
      phaseTitle = "Phase 3: Machine Learning & Model Evaluation",
      targetLevel = 4,
      weight = 1.0,
      estimatedHoursPerLevel = 6,
      targetStartWeek = 8,
      targetEndWeek = 10,
      prerequisites = listOf("python", "statistics"),
      coreCompetencies = listOf("Regression & classification", "Tree ensembles (RF/XGBoost)", "Clustering & PCA")
    ),
    CurriculumModule(
      skillId = "model_evaluation",
      title = "Model Evaluation & Validation",
      phaseIndex = 3,
      phaseTitle = "Phase 3: Machine Learning & Model Evaluation",
      targetLevel = 4,
      weight = 0.9,
      estimatedHoursPerLevel = 4,
      targetStartWeek = 10,
      targetEndWeek = 11,
      prerequisites = listOf("ml"),
      coreCompetencies = listOf("Cross-validation", "Precision/Recall/F1 & ROC-AUC", "Bias-variance & calibration")
    ),

    // Phase 4: Deep Learning, MLOps & Production AI (Weeks 12–16)
    CurriculumModule(
      skillId = "deep_learning",
      title = "Deep Learning & Neural Architectures",
      phaseIndex = 4,
      phaseTitle = "Phase 4: Deep Learning, MLOps & Production AI",
      targetLevel = 3,
      weight = 0.85,
      estimatedHoursPerLevel = 6,
      targetStartWeek = 12,
      targetEndWeek = 14,
      prerequisites = listOf("ml"),
      coreCompetencies = listOf("Backpropagation & optimizers", "CNNs & Transformers", "Transfer learning & embeddings")
    ),
    CurriculumModule(
      skillId = "cloud",
      title = "Cloud Computing for AI Workloads",
      phaseIndex = 4,
      phaseTitle = "Phase 4: Deep Learning, MLOps & Production AI",
      targetLevel = 3,
      weight = 0.7,
      estimatedHoursPerLevel = 4,
      targetStartWeek = 13,
      targetEndWeek = 14,
      prerequisites = listOf("python"),
      coreCompetencies = listOf("Cloud storage & compute", "Managed ML endpoints", "Containerized deployment")
    ),
    CurriculumModule(
      skillId = "mlops",
      title = "MLOps & Model Deployment",
      phaseIndex = 4,
      phaseTitle = "Phase 4: Deep Learning, MLOps & Production AI",
      targetLevel = 3,
      weight = 0.8,
      estimatedHoursPerLevel = 5,
      targetStartWeek = 14,
      targetEndWeek = 15,
      prerequisites = listOf("ml", "git"),
      coreCompetencies = listOf("Model registry & versioning", "Inference APIs", "Drift monitoring & CI/CD")
    ),
    CurriculumModule(
      skillId = "projects",
      title = "End-to-End AI/DS Capstone Projects",
      phaseIndex = 4,
      phaseTitle = "Phase 4: Deep Learning, MLOps & Production AI",
      targetLevel = 4,
      weight = 0.95,
      estimatedHoursPerLevel = 6,
      targetStartWeek = 15,
      targetEndWeek = 16,
      prerequisites = listOf("pandas", "ml"),
      coreCompetencies = listOf("Problem framing & EDA", "End-to-end training pipeline", "Deployed portfolio showcase")
    )
  )

  /**
   * Returns the AI/Data Science curriculum roadmap customized with role-specific target levels
   * when a career track is specified, while preserving the structured 4-phase AI/DS curriculum.
   */
  fun getCurriculumRoadmapForGoal(goal: String = "AI & Data Science"): List<CurriculumModule> {
    val careerReqs = CAREERS[goal] ?: return AI_DATA_SCIENCE_CURRICULUM_ROADMAP
    val baseModules = AI_DATA_SCIENCE_CURRICULUM_ROADMAP.map { module ->
      val req = careerReqs[module.skillId]
      if (req != null) {
        module.copy(
          targetLevel = maxOf(req.level, 1),
          weight = req.weight
        )
      } else {
        module
      }
    }.toMutableList()

    // Include any role-specific skill (e.g., excel for Data Analyst) if not already in the base list
    for ((skillId, req) in careerReqs) {
      if (baseModules.none { it.skillId == skillId }) {
        baseModules.add(
          CurriculumModule(
            skillId = skillId,
            title = getSkillLabel(skillId),
            phaseIndex = 2,
            phaseTitle = "Phase 2: Data Wrangling & Exploratory Analysis",
            targetLevel = req.level,
            weight = req.weight,
            estimatedHoursPerLevel = 4,
            targetStartWeek = 4,
            targetEndWeek = 5,
            prerequisites = PREREQUISITES[skillId] ?: emptyList(),
            coreCompetencies = listOf("Spreadsheets & Pivot Tables", "Lookup functions", "Business reporting")
          )
        )
      }
    }
    return baseModules.sortedWith(compareBy({ it.phaseIndex }, { it.targetStartWeek }))
  }

  fun allSkills(): List<String> {
    return CAREERS.values.flatMap { it.keys }.distinct().sorted()
  }
}
