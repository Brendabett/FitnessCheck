// ChallengeViewModelFactory.kt
package com.brenda.fitnesscheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brenda.fitnesscheck.repository.ChallengeRepository

class ChallengeViewModelFactory(private val repository: ChallengeRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChallengeViewModel::class.java)) {
            return ChallengeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}