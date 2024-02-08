package com.example.news.ui.repository

import com.example.news.network.Networking
import com.example.news.roomdb.ArticleDatabase

class BreakingNewsRepository(
    private val db : ArticleDatabase
) {
    suspend fun getBreakingNews(countryCode: String, pageNumber : Int) =
        Networking.api.getBreakingNews(countryCode,pageNumber)

}