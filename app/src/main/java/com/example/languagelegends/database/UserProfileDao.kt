package com.example.languagelegends.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * This interface represents a Data Access Object (DAO) for the UserProfile entity.
 * It provides methods to perform database operations on the UserProfile table.
 */
@Dao
interface UserProfileDao {

    /**
     * Inserts a new UserProfile into the database.
     * @param userProfile The UserProfile object to be inserted.
     */
    @Insert
    suspend fun insertUserProfile(userProfile: UserProfile)

    /**
     * Retrieves all UserProfiles from the database.
     * @return A list of UserProfile objects.
     */
    @Query("SELECT * FROM user_profile")
    suspend fun getAllUserProfiles(): List<UserProfile>

    /**
     * Deletes all UserProfiles from the database.
     */
    @Query("DELETE FROM user_profile")
    suspend fun clearDatabase()

    /**
     * Updates an existing UserProfile in the database.
     * @param userProfile The UserProfile object to be updated.
     */
    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)
}