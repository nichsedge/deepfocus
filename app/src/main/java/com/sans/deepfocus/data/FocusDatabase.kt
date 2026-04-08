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

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val name: String,
    val color: Int = 0xFF6200EE.toInt() 
)

@Entity(tableName = "sounds")
data class SoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val uri: String,
    val isCustom: Boolean = false,
    val isSelected: Boolean = false
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

    @Query("UPDATE sessions SET tag = :tag WHERE id = :sessionId")
    suspend fun updateSessionTag(sessionId: Long, tag: String?)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)
}

@Dao
interface SoundDao {
    @Query("SELECT * FROM sounds")
    fun getAllSounds(): Flow<List<SoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: SoundEntity)

    @Query("UPDATE sounds SET isSelected = (id = :soundId)")
    suspend fun selectSound(soundId: Long)

    @Query("SELECT * FROM sounds WHERE isSelected = 1 LIMIT 1")
    fun getSelectedSound(): Flow<SoundEntity?>

    @Delete
    suspend fun deleteSound(sound: SoundEntity)
}

@Database(
    entities = [SessionEntity::class, SoundEntity::class, TagEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun soundDao(): SoundDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "deepfocus-db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
