package com.itsthwng.twallpaper.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Centralized database migrations management
 * 
 * IMPORTANT RULES:
 * 1. Always increment version number when changing schema
 * 2. Always add migration for version changes
 * 3. Test migrations before release
 * 4. Document all schema changes
 * 
 * Version History:
 * - Version 1: Initial database
 * - Version 2: Changed isSelected to isSelectedLock and isSelectedHome in WallpaperEntity
 * - Version 3: Added lastSetLockTime and lastSetHomeTime columns to track wallpaper history timestamps
 * - Version 4: Added is_test_data column to identify test data
 * - Version 5: Ensure all columns exist (migration fix)
 * - Version 6: No schema changes
 * - Version 7: Added isSelectedHome and isSelectedLock to ZipperImageEntity (recreated table with indices)
 */
object DatabaseMigrations {
    
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                // Due to significant schema changes, we need to recreate the table
                // This is a destructive migration but necessary due to the extensive changes
                
                // Step 1: Create temporary table with new schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS wallpapers_entity_new (
                        wallpaperId INTEGER PRIMARY KEY NOT NULL,
                        access_type INTEGER NOT NULL,
                        thumbnail TEXT NOT NULL,
                        name TEXT NOT NULL,
                        category_id TEXT NOT NULL,
                        wallpaper_type INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        is_featured INTEGER NOT NULL,
                        tags TEXT NOT NULL,
                        is_favorite INTEGER NOT NULL,
                        isSelectedLock INTEGER NOT NULL,
                        isSelectedHome INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Step 2: Check if old table exists and has data
                val oldTableExists = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='wallpapers_entity'").use {
                    it.moveToFirst()
                }
                
                if (oldTableExists) {
                    val oldColumns = getTableColumns(database, "wallpapers_entity")
                    
                    // Build dynamic insert query based on existing columns
                    val commonColumns = mutableListOf<String>()
                    val selectColumns = mutableListOf<String>()
                    
                    // Map old columns to new columns where possible
                    val columnMappings = mapOf(
                        "wallpaperId" to "wallpaperId",
                        "access_type" to "access_type",
                        "accessType" to "access_type", // handle both naming conventions
                        "thumbnail" to "thumbnail",
                        "title" to "name", // title renamed to name
                        "name" to "name",
                        "categoryId" to "category_id",
                        "category_id" to "category_id",
                        "wallpaper_type" to "wallpaper_type",
                        "wallpaperType" to "wallpaper_type",
                        "created_at" to "created_at",
                        "createdAt" to "created_at",
                        "updated_at" to "updated_at", 
                        "updatedAt" to "updated_at",
                        "content" to "content",
                        "is_featured" to "is_featured",
                        "isFeatured" to "is_featured",
                        "tags" to "tags",
                        "isFavorite" to "is_favorite",
                        "is_favorite" to "is_favorite"
                    )
                    
                    columnMappings.forEach { (oldCol, newCol) ->
                        if (oldColumns.contains(oldCol)) {
                            commonColumns.add(newCol)
                            selectColumns.add(oldCol)
                        }
                    }
                    
                    // Handle special case for isSelected
                    if (oldColumns.contains("isSelected")) {
                        commonColumns.add("isSelectedHome")
                        selectColumns.add("CASE WHEN isSelected = 1 THEN 1 WHEN isSelected = 2 THEN 3 ELSE 0 END")
                        commonColumns.add("isSelectedLock")
                        selectColumns.add("CASE WHEN isSelected = 2 THEN 3 ELSE 0 END")
                    } else {
                        if (oldColumns.contains("isSelectedHome")) {
                            commonColumns.add("isSelectedHome")
                            selectColumns.add("isSelectedHome")
                        }
                        if (oldColumns.contains("isSelectedLock")) {
                            commonColumns.add("isSelectedLock")
                            selectColumns.add("isSelectedLock")
                        }
                    }
                    
                    // Add default values for new columns that might not exist in old table
                    val requiredColumns = listOf(
                        "wallpaperId" to "COALESCE(wallpaperId, 0)",
                        "access_type" to "0",
                        "thumbnail" to "''",
                        "name" to "''",
                        "category_id" to "''",
                        "wallpaper_type" to "0",
                        "created_at" to "0",
                        "updated_at" to "0",
                        "content" to "''",
                        "is_featured" to "0",
                        "tags" to "''",
                        "is_favorite" to "0",
                        "isSelectedLock" to "0",
                        "isSelectedHome" to "0"
                    )
                    
                    requiredColumns.forEach { (col, defaultValue) ->
                        if (!commonColumns.contains(col)) {
                            commonColumns.add(col)
                            selectColumns.add(defaultValue)
                        }
                    }
                    
                    // Step 3: Copy data if there are common columns
                    if (commonColumns.isNotEmpty()) {
                        // Ensure we have the same number of columns and select expressions
                        if (commonColumns.size != selectColumns.size) {
                            throw IllegalStateException("Column count mismatch: ${commonColumns.size} columns but ${selectColumns.size} select expressions")
                        }
                        
                        val insertQuery = """
                            INSERT INTO wallpapers_entity_new (${commonColumns.joinToString(", ")})
                            SELECT ${selectColumns.joinToString(", ")} FROM wallpapers_entity
                        """.trimIndent()
                        database.execSQL(insertQuery)
                    }
                    
                    // Step 4: Drop old table
                    database.execSQL("DROP TABLE wallpapers_entity")
                }
                
                // Step 5: Rename new table to original name
                database.execSQL("ALTER TABLE wallpapers_entity_new RENAME TO wallpapers_entity")
                
            } catch (e: Exception) {
                e.printStackTrace()
                // If migration fails, try to at least create the new table
                database.execSQL("DROP TABLE IF EXISTS wallpapers_entity")
                createWallpapersTable(database)
            }
        }
    }
    
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns for tracking wallpaper set time
            database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN lastSetLockTime INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN lastSetHomeTime INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns for tracking wallpaper download status and test data
            database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN is_downloaded INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN is_test_data INTEGER NOT NULL DEFAULT 0")
        }
    }
    
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Check if columns already exist before adding them
            val columns = getTableColumns(database, "wallpapers_entity")
            
            if (!columns.contains("is_downloaded")) {
                database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN is_downloaded INTEGER NOT NULL DEFAULT 0")
            }
            if (!columns.contains("is_test_data")) {
                database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN is_test_data INTEGER NOT NULL DEFAULT 0")
            }
            if (!columns.contains("lastSetLockTime")) {
                database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN lastSetLockTime INTEGER NOT NULL DEFAULT 0")
            }
            if (!columns.contains("lastSetHomeTime")) {
                database.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN lastSetHomeTime INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Không thay đổi gì ở schema, để trống
        }
    }
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN no_shuffle INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN price_points INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN price_tier INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE zipper_image_entity ADD COLUMN chainType INTEGER NOT NULL DEFAULT 0")
        }
    }


    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add history tracking columns to zipper_image_entity
            // SQLite doesn't support ALTER COLUMN, so we need to recreate the table

            // Step 1: Create new table with correct schema including indices
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS zipper_image_entity_new (
                    accessType INTEGER NOT NULL DEFAULT 0,
                    categoryId TEXT NOT NULL DEFAULT '',
                    zipperId INTEGER PRIMARY KEY NOT NULL,
                    content TEXT NOT NULL DEFAULT '',
                    name TEXT NOT NULL DEFAULT '',
                    fileName TEXT NOT NULL DEFAULT '',
                    ordinalNumber INTEGER NOT NULL DEFAULT 0,
                    contentLeft TEXT NOT NULL DEFAULT '',
                    contentRight TEXT NOT NULL DEFAULT '',
                    type TEXT,
                    chainType INTEGER NOT NULL DEFAULT 0,
                    isSelectedHome INTEGER NOT NULL DEFAULT 0,
                    isSelectedLock INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // Step 2: Create indices for performance (Android best practice)
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_zipper_isSelectedHome
                ON zipper_image_entity_new(isSelectedHome)
            """.trimIndent())

            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_zipper_isSelectedLock
                ON zipper_image_entity_new(isSelectedLock)
            """.trimIndent())

            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_zipper_categoryId
                ON zipper_image_entity_new(categoryId)
            """.trimIndent())

            // Step 3: Copy existing data from old table
            database.execSQL("""
                INSERT INTO zipper_image_entity_new (
                    accessType, categoryId, zipperId, content, name,
                    fileName, ordinalNumber, contentLeft, contentRight, type, chainType,
                    isSelectedHome, isSelectedLock
                )
                SELECT
                    COALESCE(accessType, 0) as accessType,
                    COALESCE(categoryId, '') as categoryId,
                    zipperId,
                    COALESCE(content, '') as content,
                    COALESCE(name, '') as name,
                    COALESCE(fileName, '') as fileName,
                    COALESCE(ordinalNumber, 0) as ordinalNumber,
                    COALESCE(contentLeft, '') as contentLeft,
                    COALESCE(contentRight, '') as contentRight,
                    type,
                    chainType,
                    0 as isSelectedHome,
                    0 as isSelectedLock
                FROM zipper_image_entity
            """.trimIndent())

            // Step 4: Drop old table
            database.execSQL("DROP TABLE zipper_image_entity")

            // Step 5: Rename new table to original name
            database.execSQL("ALTER TABLE zipper_image_entity_new RENAME TO zipper_image_entity")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE wallpapers_entity ADD COLUMN is_for_you INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Thêm cột mới với giá trị mặc định = 0
            database.execSQL("ALTER TABLE zipper_image_entity ADD COLUMN pricePoints INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE zipper_image_entity ADD COLUMN priceTier INTEGER NOT NULL DEFAULT 0")
        }
    }

    // Helper functions
    private fun getTableColumns(database: SupportSQLiteDatabase, tableName: String): Set<String> {
        val columns = mutableSetOf<String>()
        database.query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(nameIndex))
            }
        }
        return columns
    }
    
    private fun createWallpapersTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS wallpapers_entity (
                wallpaperId INTEGER PRIMARY KEY NOT NULL,
                access_type INTEGER NOT NULL,
                thumbnail TEXT NOT NULL,
                name TEXT NOT NULL,
                category_id TEXT NOT NULL,
                wallpaper_type INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                content TEXT NOT NULL,
                is_featured INTEGER NOT NULL,
                tags TEXT NOT NULL,
                is_favorite INTEGER NOT NULL,
                isSelectedLock INTEGER NOT NULL,
                isSelectedHome INTEGER NOT NULL
            )
        """.trimIndent())
    }
    
    /**
     * Get all migrations for Room database builder
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10
            // Add future migrations here
        )
    }
}