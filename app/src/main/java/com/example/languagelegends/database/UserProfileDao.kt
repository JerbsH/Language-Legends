package com.example.languagelegends.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserProfileDao {
    @Insert
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Query("SELECT * FROM user_profile")
    suspend fun getAllUserProfiles(): List<UserProfile>

    @Query("DELETE FROM user_profile")
    suspend fun clearDatabase()

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

}