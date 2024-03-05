package com.example.languagelegends.screens

import androidx.lifecycle.ViewModel

class ViewState: ViewModel() {
    var completedExercises: Int = 0
    fun completeExercise() {
        this.completedExercises++
        // Add to points
        // Add progress to database
    }
}