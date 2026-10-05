package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.data.model.RecordingEntity
import com.example.data.model.ReportEntity
import com.example.data.model.SavedRecordingEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RecordingEntity::class,
        ReportEntity::class,
        SavedRecordingEntity::class,
        IncidentEntity::class,
        IncidentEvidenceEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recordingDao(): RecordingDao
    abstract fun reportDao(): ReportDao
    abstract fun savedRecordingDao(): SavedRecordingDao
    abstract fun incidentDao(): IncidentDao
    abstract fun incidentEvidenceDao(): IncidentEvidenceDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protect_yourself_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
