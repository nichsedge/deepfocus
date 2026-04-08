package com.sans.deepfocus.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val mode: String, // "POMODORO" or "STOPWATCH"
    val tag: String? = null
)

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT SUM(duration) FROM sessions WHERE startTime >= :since")
    fun getTotalFocusTimeSince(since: Long): Flow<Long?>

    @Query("SELECT COUNT(*) FROM sessions WHERE startTime >= :since")
    fun getSessionCountSince(since: Long): Flow<Int>

    @Query("SELECT * FROM sessions WHERE startTime >= :since")
    fun getSessionsSince(since: Long): Flow<List<SessionEntity>>
}

@Database(entities = [SessionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "deepfocus-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
