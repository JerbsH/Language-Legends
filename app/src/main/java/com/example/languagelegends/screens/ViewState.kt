package com.example.languagelegends.screens

import androidx.lifecycle.ViewModel

class ViewState: ViewModel() {
    private var completedExercises: Int = 0
    private var currentLevel: Int = 1

    fun getCompletedExercises(): Int {
        return completedExercises
    }
    fun completeExercise() {
        this.completedExercises++
    }
    fun setCompletedExercises(amount: Int) {
        this.completedExercises = amount
    }
    fun getCurrentLevel(): Int {
        return currentLevel
    }
    fun setCurrentLevel(lvl: Int) {
        this.currentLevel = lvl
    }
}