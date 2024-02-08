package com.example.news.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.news.R
import com.example.news.models.Article
import com.example.news.models.NewsResponse
import com.example.news.roomdb.ArticleDatabase
import com.example.news.ui.repository.SavedNewsRepository
import com.example.news.ui.viewmodels.SavedNewsViewModel
import com.example.news.ui.viewmodels.SavedNewsViewModelProviderFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class ArticleFragment : Fragment(R.layout.fragment_article) {

    lateinit var articleWebView : WebView
    lateinit var articleFab : FloatingActionButton
    val TAG = "ArticleFragment"
   // val args : ArticleFragmentArgs by navArgs()
    lateinit var article : Article
    lateinit var savedNewsViewModel: SavedNewsViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedNewsRepository = SavedNewsRepository(ArticleDatabase.getDatabase(requireContext()))
        val savedNewsViewModelProviderFactory = SavedNewsViewModelProviderFactory(savedNewsRepository)
        savedNewsViewModel = ViewModelProvider(this,savedNewsViewModelProviderFactory).get(SavedNewsViewModel::class.java)

        articleFab = view.findViewById(R.id.fab)
        articleWebView = view.findViewById(R.id.webView)

        // var article = args.article
        val jsonArticle = arguments?.getString("article")
        if (jsonArticle != null){
            article = Gson().fromJson(jsonArticle,Article::class.java)
        }
        articleWebView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url.toString())
        }
        articleFab.setOnClickListener {
            savedNewsViewModel.saveArticle(article)
            Snackbar.make(view,"Article saved successfully",Snackbar.LENGTH_LONG).show()
            Log.d(TAG, "onViewCreated: Error")
        }
    }
}