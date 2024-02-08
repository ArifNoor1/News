package com.example.news.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.news.ui.repository.BreakingNewsRepository

class BreakingNewsViewModelProviderFactory(
    private val app : Application,
    private val breakingNewsRepository: BreakingNewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BreakingNewsViewModel(app,breakingNewsRepository) as T
    }
}