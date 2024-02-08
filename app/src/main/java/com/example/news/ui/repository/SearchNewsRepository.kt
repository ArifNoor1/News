package com.example.news.ui.repository

import com.example.news.network.Networking
import com.example.news.roomdb.ArticleDatabase

class SearchNewsRepository(
    private val db : ArticleDatabase
) {
    suspend fun getSearchNews(searchQuery : String,pageNumber : Int) =
        Networking.api.searchForNews(searchQuery,pageNumber)
}