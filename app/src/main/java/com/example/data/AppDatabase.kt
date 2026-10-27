package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val username: String,
  val createdAt: String
)

@Entity(tableName = "profiles")
data class ProfileEntity(
  @PrimaryKey val userId: Long,
  val goal: String,
  val skillsStartJson: String,
  val skillsNowJson: String,
  val startDate: String
) {
  fun getStartSkills(): Map<String, Int> = parseSkillMap(skillsStartJson)
  fun getNowSkills(): Map<String, Int> = parseSkillMap(skillsNowJson)

  companion object {
    fun parseSkillMap(jsonStr: String): Map<String, Int> {
      return try {
        val obj = JSONObject(jsonStr)
        val map = mutableMapOf<String, Int>()
        for (key in obj.keys()) {
          map[key] = obj.optInt(key, 0)
        }
        map
      } catch (_: Exception) {
        emptyMap()
      }
    }

    fun skillMapToJson(map: Map<String, Int>): String {
      return JSONObject(map).toString()
    }
  }
}

@Entity(tableName = "study_logs")
data class StudyLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val day: String,
  val minutes: Int,
  val skill: String,
  val note: String
)

@Entity(tableName = "steps_done")
data class StepDoneEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val skill: String,
  val doneAt: String
)

@Entity(tableName = "assessments")
data class AssessmentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val kind: String, // "pre" or "post"
  val score: Int,
  val detailJson: String,
  val takenAt: String
)

@Entity(tableName = "feedback")
data class FeedbackEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val clarity: Int,
  val usefulness: Int,
  val topicOrder: Int,
  val commentText: String,
  val createdAt: String
)

@Entity(tableName = "skill_proficiency_history")
data class SkillProficiencyHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val skill: String,
  val proficiencyLevel: Int,
  val fitScore: Double,
  val skillsSnapshotJson: String,
  val recordedDate: String,
  val timestamp: Long = System.currentTimeMillis()
) {
  fun getSkillsSnapshot(): Map<String, Int> = ProfileEntity.parseSkillMap(skillsSnapshotJson)

  fun getAverageProficiency(careerSkills: Collection<String>): Double {
    val snapshot = getSkillsSnapshot()
    val keys = if (careerSkills.isNotEmpty()) careerSkills else snapshot.keys
    if (keys.isEmpty()) return 0.0
    val sum = keys.sumOf { (snapshot[it] ?: 0).coerceIn(0, 5) }
    return kotlin.math.round((sum.toDouble() / keys.size.toDouble()) * 100.0) / 100.0
  }
}

@Dao
interface AppDao {
  // Users
  @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserById(id: Long): UserEntity?

  @Query("SELECT * FROM users ORDER BY createdAt DESC")
  fun getAllUsersFlow(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users")
  suspend fun getAllUsers(): List<UserEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity): Long

  // Profile
  @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
  fun getProfileFlow(userId: Long): Flow<ProfileEntity?>

  @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
  suspend fun getProfile(userId: Long): ProfileEntity?

  @Query("SELECT * FROM profiles")
  suspend fun getAllProfiles(): List<ProfileEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveProfile(profile: ProfileEntity)

  @Query("DELETE FROM profiles WHERE userId = :userId")
  suspend fun deleteProfile(userId: Long)

  // Steps Done
  @Query("SELECT * FROM steps_done WHERE userId = :userId")
  fun getStepsDoneFlow(userId: Long): Flow<List<StepDoneEntity>>

  @Query("SELECT * FROM steps_done WHERE userId = :userId")
  suspend fun getStepsDone(userId: Long): List<StepDoneEntity>

  @Query("SELECT * FROM steps_done")
  suspend fun getAllStepsDone(): List<StepDoneEntity>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertStepDone(step: StepDoneEntity)

  @Query("DELETE FROM steps_done WHERE userId = :userId")
  suspend fun deleteStepsDone(userId: Long)

  // Study Logs
  @Query("SELECT * FROM study_logs WHERE userId = :userId ORDER BY day DESC")
  fun getStudyLogsFlow(userId: Long): Flow<List<StudyLogEntity>>

  @Query("SELECT * FROM study_logs WHERE userId = :userId ORDER BY day ASC")
  suspend fun getStudyLogs(userId: Long): List<StudyLogEntity>

  @Query("SELECT * FROM study_logs")
  suspend fun getAllStudyLogs(): List<StudyLogEntity>

  @Insert
  suspend fun insertStudyLog(log: StudyLogEntity)

  // Assessments
  @Query("SELECT * FROM assessments WHERE userId = :userId ORDER BY takenAt ASC")
  fun getAssessmentsFlow(userId: Long): Flow<List<AssessmentEntity>>

  @Query("SELECT * FROM assessments WHERE userId = :userId ORDER BY takenAt ASC")
  suspend fun getAssessments(userId: Long): List<AssessmentEntity>

  @Query("SELECT * FROM assessments ORDER BY takenAt ASC")
  suspend fun getAllAssessments(): List<AssessmentEntity>

  @Insert
  suspend fun insertAssessment(assessment: AssessmentEntity)

  // Feedback
  @Query("SELECT * FROM feedback ORDER BY createdAt DESC")
  fun getFeedbackFlow(): Flow<List<FeedbackEntity>>

  @Query("SELECT * FROM feedback")
  suspend fun getAllFeedback(): List<FeedbackEntity>

  @Insert
  suspend fun insertFeedback(feedback: FeedbackEntity)

  // Skill Proficiency History Over Time
  @Query("SELECT * FROM skill_proficiency_history WHERE userId = :userId ORDER BY recordedDate ASC, timestamp ASC, id ASC")
  fun getSkillProficiencyHistoryFlow(userId: Long): Flow<List<SkillProficiencyHistoryEntity>>

  @Query("SELECT * FROM skill_proficiency_history WHERE userId = :userId AND skill = :skill ORDER BY recordedDate ASC, timestamp ASC, id ASC")
  fun getSkillProficiencyHistoryBySkillFlow(userId: Long, skill: String): Flow<List<SkillProficiencyHistoryEntity>>

  @Query("SELECT * FROM skill_proficiency_history WHERE userId = :userId ORDER BY recordedDate ASC, timestamp ASC, id ASC")
  suspend fun getSkillProficiencyHistory(userId: Long): List<SkillProficiencyHistoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSkillProficiencyHistory(entry: SkillProficiencyHistoryEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSkillProficiencyHistoryList(entries: List<SkillProficiencyHistoryEntity>)

  @Query("DELETE FROM skill_proficiency_history WHERE userId = :userId")
  suspend fun deleteSkillProficiencyHistory(userId: Long)
}

class SkillProficiencyRepository(private val dao: AppDao) {

  fun observeUserSkillHistory(userId: Long): Flow<List<SkillProficiencyHistoryEntity>> {
    return dao.getSkillProficiencyHistoryFlow(userId)
  }

  fun observeSkillHistoryForSkill(userId: Long, skill: String): Flow<List<SkillProficiencyHistoryEntity>> {
    return dao.getSkillProficiencyHistoryBySkillFlow(userId, skill)
  }

  suspend fun getUserSkillHistory(userId: Long): List<SkillProficiencyHistoryEntity> {
    return dao.getSkillProficiencyHistory(userId)
  }

  suspend fun recordSkillProficiencyChange(
    userId: Long,
    goal: String,
    skill: String,
    proficiencyLevel: Int,
    skillsSnapshot: Map<String, Int>,
    recordedDate: String,
    timestamp: Long = System.currentTimeMillis()
  ): Long {
    val fitScore = com.example.engine.RoadmapEngine.fitScore(skillsSnapshot, goal)
    val entry = SkillProficiencyHistoryEntity(
      userId = userId,
      skill = skill,
      proficiencyLevel = proficiencyLevel.coerceIn(0, 5),
      fitScore = fitScore,
      skillsSnapshotJson = ProfileEntity.skillMapToJson(skillsSnapshot),
      recordedDate = recordedDate,
      timestamp = timestamp
    )
    return dao.insertSkillProficiencyHistory(entry)
  }

  suspend fun clearUserSkillHistory(userId: Long) {
    dao.deleteSkillProficiencyHistory(userId)
  }
}

@Database(
  entities = [
    UserEntity::class,
    ProfileEntity::class,
    StudyLogEntity::class,
    StepDoneEntity::class,
    AssessmentEntity::class,
    FeedbackEntity::class,
    SkillProficiencyHistoryEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun appDao(): AppDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "career_roadmap.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
