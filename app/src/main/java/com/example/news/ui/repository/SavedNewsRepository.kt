package com.example.news.ui.repository

import com.example.news.models.Article
import com.example.news.roomdb.ArticleDatabase

class SavedNewsRepository(private val db : ArticleDatabase) {

    suspend fun upsert(article: Article) = db.articleDao().upsert(article)
    fun getSavedNews() = db.articleDao().getAllArticles()
    suspend fun deleteArticle(article: Article) = db.articleDao().deleteArticle(article)
}