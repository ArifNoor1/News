package com.example.news.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.models.Article
import com.example.news.ui.repository.SavedNewsRepository
import kotlinx.coroutines.launch

class SavedNewsViewModel(private val savedNewsRepository : SavedNewsRepository) : ViewModel(){


    fun saveArticle(article: Article) = viewModelScope.launch {
        savedNewsRepository.upsert(article)
    }
    fun getSavedNews() = savedNewsRepository.getSavedNews()
    fun deleteArticle(article: Article) = viewModelScope.launch {
        savedNewsRepository.deleteArticle(article)
    }

}