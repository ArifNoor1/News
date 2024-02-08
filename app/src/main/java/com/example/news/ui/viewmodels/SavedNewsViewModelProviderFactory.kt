package com.example.news.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.news.ui.repository.SavedNewsRepository

@Suppress("UNCHECKED_CAST")
class SavedNewsViewModelProviderFactory(private val repository: SavedNewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavedNewsViewModel(repository) as T
    }
}