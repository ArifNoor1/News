package com.example.news.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.news.ui.repository.SearchNewsRepository

class SearchNewsViewModelProviderFactory(
    private val app: Application,
    private val searchNewsRepository: SearchNewsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchNewsViewModel(app,searchNewsRepository) as T
    }
}