package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssessmentEntity
import com.example.data.CareerRoadmapData
import com.example.data.ProfileEntity
import com.example.model.QuizQuestion
import com.example.ui.QuizSubmissionResult
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandError
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandWarning

@Composable
fun SkillCheckScreen(
  profile: ProfileEntity,
  assessments: List<AssessmentEntity>,
  quizResult: QuizSubmissionResult?,
  onSubmitQuiz: (String, Map<Int, String>) -> Unit,
  onLevelUpSkill: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val careerSkills = CareerRoadmapData.CAREERS[profile.goal]?.keys ?: emptySet()
  val preAssessments = assessments.filter { it.kind == "pre" }
  val postAssessments = assessments.filter { it.kind == "post" }

  var selectedKind by remember {
    mutableStateOf(if (preAssessments.isEmpty()) "pre" else "post")
  }

  // Quiz Mode: "Exam" (submit at end) or "Practice" (instant answer feedback)
  var quizMode by remember { mutableStateOf("Exam") }

  val formLetter = if (selectedKind == "pre") "A" else "B"
  val questions = remember(formLetter, profile.goal) {
    CareerRoadmapData.QUIZ_QUESTIONS.filter {
      it.form == formLetter && it.skill in careerSkills
    }
  }

  val selectedAnswers = remember(selectedKind) { mutableStateMapOf<Int, String>() }
  var validationError by remember { mutableStateOf<String?>(null) }
  var hasSubmitted by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Skill Check & Learning Gain",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Form A measures your baseline. Form B measures your post-study knowledge gain with normalized metrics.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Form Selector (Pre vs Post)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FilterChip(
          selected = selectedKind == "pre",
          onClick = {
            selectedKind = "pre"
            validationError = null
            hasSubmitted = false
          },
          label = { Text("Pre-Test (Baseline)") },
          leadingIcon = if (preAssessments.isNotEmpty()) {
            { Icon(Icons.Default.Check, contentDescription = null, tint = BrandSuccess) }
          } else null,
          modifier = Modifier.testTag("tab_pre_test")
        )

        FilterChip(
          selected = selectedKind == "post",
          onClick = {
            selectedKind = "post"
            validationError = null
            hasSubmitted = false
          },
          label = { Text("Post-Test (After Study)") },
          leadingIcon = if (postAssessments.isNotEmpty()) {
            { Icon(Icons.Default.Check, contentDescription = null, tint = BrandSuccess) }
          } else null,
          modifier = Modifier.testTag("tab_post_test")
        )
      }
    }

    // Mode Selector (Exam vs Practice)
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Mode: $quizMode Mode",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = quizMode == "Exam",
            onClick = { quizMode = "Exam" },
            label = { Text("Exam") },
            modifier = Modifier.testTag("mode_exam")
          )
          FilterChip(
            selected = quizMode == "Practice",
            onClick = { quizMode = "Practice" },
            label = { Text("Practice with Explanations") },
            modifier = Modifier.testTag("mode_practice")
          )
        }
      }
    }

    // Baseline alert or previous score banner
    if (selectedKind == "pre" && preAssessments.isNotEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = BrandSuccess.copy(alpha = 0.15f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = BrandSuccess)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Baseline Recorded: ${preAssessments.last().score}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandSuccess
              )
              Text(
                text = "Take Form B (Post-Test) to calculate your normalized learning gain.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    if (selectedKind == "post" && preAssessments.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = BrandWarning.copy(alpha = 0.2f))
        ) {
          Text(
            text = "⚠️ Take the Pre-Test baseline first so your normalized learning gain (g) can be accurately computed.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }

    // Result Card
    if (quizResult != null && hasSubmitted) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Quiz Score: ${quizResult.score}%",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${quizResult.correctCount} of ${quizResult.totalQuestions} questions correct",
              style = MaterialTheme.typography.bodyMedium
            )

            if (quizResult.learningGain != null) {
              val g = quizResult.learningGain.normalizedGain
              val interpretation = when {
                g >= 0.7 -> "High Learning Gain (Outstanding mastery!)"
                g >= 0.3 -> "Medium Learning Gain (Solid conceptual progress)"
                else -> "Low Learning Gain (Review recommended topics)"
              }

              Spacer(modifier = Modifier.height(10.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = BrandSuccess.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        text = "Normalized Learning Gain (g)",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandSuccess,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "$g",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandSuccess
                      )
                    }
                    Text(
                      text = "+${quizResult.learningGain.rawGain} pts raw gain",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = interpretation,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandSuccess,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Skill Breakdown & Auto Level-Up:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )

            quizResult.skillBreakdown.forEach { (skill, counts) ->
              val isAllCorrect = counts.first == counts.second
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "• ${CareerRoadmapData.getSkillLabel(skill)}: ${counts.first}/${counts.second}",
                  style = MaterialTheme.typography.bodyMedium
                )
                if (isAllCorrect && onLevelUpSkill != null) {
                  Button(
                    onClick = { onLevelUpSkill(skill) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSuccess),
                    modifier = Modifier.height(32.dp)
                  ) {
                    Text("Level Up +1", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Questions List
    itemsIndexed(questions) { index, question ->
      val userAns = selectedAnswers[question.id]
      val showFeedback = (quizMode == "Practice" && userAns != null) || hasSubmitted
      val isCorrect = userAns.equals(question.answer, ignoreCase = true)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("quiz_card_${question.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (showFeedback) {
            if (isCorrect) BrandSuccess.copy(alpha = 0.08f) else BrandError.copy(alpha = 0.06f)
          } else {
            MaterialTheme.colorScheme.surface
          }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Question ${index + 1}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = CareerRoadmapData.getSkillLabel(question.skill),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = question.question,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
          )

          Spacer(modifier = Modifier.height(10.dp))

          val options = listOf(
            "A" to question.a,
            "B" to question.b,
            "C" to question.c,
            "D" to question.d
          )

          options.forEach { (key, text) ->
            val isSelected = userAns == key
            val isRightChoice = key.equals(question.answer, ignoreCase = true)

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  selectedAnswers[question.id] = key
                  validationError = null
                }
                .padding(vertical = 4.dp, horizontal = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = {
                  selectedAnswers[question.id] = key
                  validationError = null
                },
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "$key. $text",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (showFeedback && isRightChoice) BrandSuccess
                else if (showFeedback && isSelected && !isRightChoice) BrandError
                else if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Practice Mode / Post-Submission Explanation
          if (showFeedback) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isCorrect) BrandSuccess.copy(alpha = 0.12f) else BrandError.copy(alpha = 0.1f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) BrandSuccess else BrandError,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (isCorrect) "Correct! Answer: ${question.answer}" else "Incorrect. Correct answer: ${question.answer}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) BrandSuccess else BrandError
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = getExplanationForQuestion(question),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    if (validationError != null) {
      item {
        Text(
          text = validationError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold
        )
      }
    }

    if (questions.isNotEmpty()) {
      item {
        Button(
          onClick = {
            if (selectedAnswers.size < questions.size) {
              validationError = "Please answer all ${questions.size} questions before submitting."
            } else {
              hasSubmitted = true
              onSubmitQuiz(selectedKind, selectedAnswers)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("submit_quiz_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BrandAccent)
        ) {
          Icon(Icons.Outlined.Quiz, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Submit $formLetter Assessment")
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

private fun getExplanationForQuestion(question: QuizQuestion): String {
  return when (question.id) {
    1 -> "len([1, 2, 3, 4]) counts the number of elements in the list, which is 4."
    2 -> "In Python, the 'def' keyword is used to define a function (e.g., def my_function():)."
    3 -> "HAVING filters rows after GROUP BY aggregations, whereas WHERE filters individual rows before grouping."
    4 -> "A LEFT JOIN preserves every row from the left table, filling in NULLs for any columns from the right table with no match."
    5 -> "The Median is the middle value and is not skewed by extreme outliers, unlike the Mean, Range, or Variance."
    6 -> "A p-value < 0.05 indicates the observed data would be very unlikely to occur by random chance if the null hypothesis were true."
    7 -> "df.head() returns the first 5 rows of a Pandas DataFrame by default."
    8 -> "df.isnull().sum() checks for null boolean values and sums them per column."
    9 -> "Overfitting occurs when a model learns noise and specific details of the training set, failing to generalize to new data."
    10 -> "Predicting house prices from labelled historical data is supervised learning because the training examples include target outputs."
    11 -> "F1 score combines precision and recall (harmonic mean) and is far more informative than accuracy on imbalanced classes."
    12 -> "A separate test set gives an unbiased evaluation of how well the final model performs on unseen, real-world data."
    13 -> "git commit records staged snapshots in the repository history, whereas git add stages changes and git push sends to remote."
    14 -> "git clone creates a local copy of a remote repository on your machine."
    15 -> "A scatter plot plots individual observation pairs and is ideal for observing correlation and relationships between two continuous variables."
    16 -> "A histogram groups continuous values into bins to illustrate the underlying frequency distribution of a single numerical variable."
    else -> "This concept tests core understanding and real-world application of ${CareerRoadmapData.getSkillLabel(question.skill)}."
  }
}
