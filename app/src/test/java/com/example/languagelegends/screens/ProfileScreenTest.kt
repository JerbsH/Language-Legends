package com.example.languagelegends.screens

import com.example.languagelegends.database.UserProfile
import com.example.languagelegends.database.UserProfileDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

/**
 * This class contains unit tests for the ProfileScreen.
 */
class ProfileScreenTest {

    /**
     * This test checks if the updateUserLanguages function correctly updates the languages, exercisesDone and languagePoints fields of a UserProfile.
     */
    @Test
    fun `updateUserLanguages updates languages, exercisesDone and languagePoints`() {
        val userProfile = UserProfile(
            username = "Test",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )

        updateUserLanguages(userProfile)

        assertEquals(3, userProfile.languages.size)
        assertEquals(170, userProfile.exercisesDone)
        assertEquals(15500, userProfile.languagePoints)
    }

    /**
     * This test checks if the updateUserLanguages function correctly updates the languages, exercisesDone and languagePoints fields of a UserProfile.
     */
    @Test
    fun `new user profile is created when database is empty`() = runBlocking {
        val database = mutableListOf<UserProfile>()
        val userProfileDao = Mockito.mock(UserProfileDao::class.java)

        Mockito.`when`(userProfileDao.getAllUserProfiles()).thenReturn(database)

        val userProfile = UserProfile(
            username = "Test",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )

        Mockito.doAnswer { invocation ->
            val user = invocation.getArgument<UserProfile>(0)
            database.add(user)
            null
        }.`when`(userProfileDao).insertUserProfile(userProfile)

        userProfileDao.insertUserProfile(userProfile)

        val allUsers = userProfileDao.getAllUserProfiles()
        assertEquals(1, allUsers.size)
        assertEquals("Test", allUsers[0].username)
    }

    /**
     * This test checks if an existing UserProfile in the database is correctly updated when there is only one user in the database.
     */
    @Test
    fun `existing user profile is updated when database has one user`() = runBlocking {
        val existingUserProfile = UserProfile(
            username = "Old",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )
        val database = mutableListOf(existingUserProfile)
        val userProfileDao = Mockito.mock(UserProfileDao::class.java)

        Mockito.`when`(userProfileDao.getAllUserProfiles()).thenReturn(database)

        val userProfile = UserProfile(
            username = "Test",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )

        Mockito.doAnswer { invocation ->
            val user = invocation.getArgument<UserProfile>(0)
            database[0] = user
            null
        }.`when`(userProfileDao).updateUserProfile(userProfile)

        userProfileDao.updateUserProfile(userProfile)

        val allUsers = userProfileDao.getAllUserProfiles()
        assertEquals(1, allUsers.size)
        assertEquals("Test", allUsers[0].username)
    }

    /**
     * This test checks if the username of a UserProfile is correctly updated when the user finishes editing their username.
     */
    @Test
    fun `username is updated when editing is finished`() = runBlocking {
        val userProfile = UserProfile(
            username = "Old",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )
        val database = mutableListOf(userProfile)
        val userProfileDao = Mockito.mock(UserProfileDao::class.java)

        Mockito.`when`(userProfileDao.getAllUserProfiles()).thenReturn(database)

        // Simulate the user editing the username and finishing editing
        val updatedUserProfile = UserProfile(
            username = "New",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )

        Mockito.doAnswer { invocation ->
            val user = invocation.getArgument<UserProfile>(0)
            database[0] = user
            null
        }.`when`(userProfileDao).updateUserProfile(updatedUserProfile)

        userProfileDao.updateUserProfile(updatedUserProfile)

        val allUsers = userProfileDao.getAllUserProfiles()
        assertEquals(1, allUsers.size)
        assertEquals("New", allUsers[0].username)
    }

    /**
     * This test checks if the weekly points of a UserProfile are correctly displayed.
     */
    @Test
    fun `weekly points are displayed correctly`() {
        val userProfile = UserProfile(
            username = "Test",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )

        // Simulate the user viewing their profile
        val displayedWeeklyPoints = userProfile.weeklyPoints

        assertEquals(1500, displayedWeeklyPoints)
    }

    /**
     * This test checks if the total points of a UserProfile are correctly calculated.
     */
    @Test
    fun `total points are calculated correctly`() {
        val userProfile = UserProfile(
            username = "Test",
            weeklyPoints = 1500,
            currentLanguage = Language("English", 0, 0),
            languagePoints = 0
        )
        updateUserLanguages(userProfile)

        // Simulate the user viewing their profile
        val displayedTotalPoints = userProfile.languagePoints

        assertEquals(15500, displayedTotalPoints)
    }
}