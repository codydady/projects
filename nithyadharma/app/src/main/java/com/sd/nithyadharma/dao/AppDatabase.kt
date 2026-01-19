package com.sd.nithyadharma.dao

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log
import com.sd.nithyadharma.model.TempleItem
import java.io.File

// For logging
/**
 * The Room database class for your application.
 * Defines the database configuration, including entities and version.
 * This is the main access point for the underlying SQLite database.
 *
 * @param entities List of entity classes included in the database (e.g., TempleItem).
 * @param version The version number of the database schema. Increment this when you change
 * your entity structure (add/remove columns, change types).
 * @param exportSchema Set to false in production to prevent exporting schema to a file.
 */
@Database(entities = [TempleItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Abstract method to get your DAO (Data Access Object).
    // Room will generate the implementation for this method.
    abstract fun templeDao(): TempleDao

    companion object {
        @Volatile // Ensures that changes to INSTANCE are immediately visible to all threads
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "ndmobile.db" // Name of your bundled DB file in assets

        private const val PREF_NAME = "db_nuke_prefs"
        private const val KEY_LAST_VERSION = "last_version_code"

        /**
         * Provides a singleton instance of the AppDatabase.
         * This method handles the creation of the database. On the very first launch
         * after installation, it will copy the pre-populated 'temples.db' file
         * from the `src/main/assets/` folder into the app's private database directory.
         * Subsequent calls will return the already created instance.
         *
         * On app updates (detected via versionCode), it will nuke the old DB files to force
         * a fresh copy from the updated assets bundle.
         *
         * @param context The application context.
         * @return The singleton instance of AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it
            return INSTANCE ?: synchronized(this) {
                // ONE-TIME NUKE CHECK: Delete DB if new release (forces fresh asset copy)
                nukeIfNewRelease(context)

                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent memory leaks
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // This is the crucial line: it tells Room to use the database file
                    // bundled in the `assets` folder as the initial source.
                    // This copy happens only once when the database is first created on the device.
                    .createFromAsset(DATABASE_NAME)
                    // No fallbackToDestructiveMigration needed—file nuke + asset copy handles resets
                    .build()
                INSTANCE = instance
                Log.d("AppDatabase", "Database instance created/retrieved: $DATABASE_NAME.")
                instance
            }
        }

        private fun nukeIfNewRelease(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val currentVersion = try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionCode // Or longVersionCode for 64-bit
            } catch (e: PackageManager.NameNotFoundException) {
                return // Fallback: No wipe
            }

            val lastVersion = prefs.getInt(KEY_LAST_VERSION, 0)
            if (currentVersion > lastVersion) {
                // NUKE: Delete DB files (main + -shm/-wal for WAL mode/large DBs)
                val dbFile = context.getDatabasePath(DATABASE_NAME)
                dbFile.delete() // ~0.1s for 115MB
                val shmFile = File(dbFile.absolutePath + "-shm")
                val walFile = File(dbFile.absolutePath + "-wal")
                shmFile.delete()
                walFile.delete()

                // Update stored version
                prefs.edit().putInt(KEY_LAST_VERSION, currentVersion).apply()

                // Log for debugging
                Log.w("AppDatabase", "****** Nuked DB for release v$currentVersion—fresh copy from assets incoming")
            }
        }
    }
}