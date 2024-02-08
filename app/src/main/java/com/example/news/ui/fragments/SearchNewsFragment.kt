package com.example.news.ui.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.NewsApplication
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.roomdb.ArticleDatabase
import com.example.news.ui.repository.BreakingNewsRepository
import com.example.news.ui.repository.SearchNewsRepository
import com.example.news.ui.viewmodels.BreakingNewsViewModel
import com.example.news.ui.viewmodels.BreakingNewsViewModelProviderFactory
import com.example.news.ui.viewmodels.SearchNewsViewModel
import com.example.news.ui.viewmodels.SearchNewsViewModelProviderFactory
import com.example.news.utils.Constants
import com.example.news.utils.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.news.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {


    lateinit var searchViewModel: SearchNewsViewModel
    lateinit var searchNewsAdapter: NewsAdapter
    lateinit var searcRecyclerView: RecyclerView
    lateinit var searcProgressBar : ProgressBar
    lateinit var etSearch : EditText
    val TAG = "SearchNewsFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searcRecyclerView = view.findViewById(R.id.rvSearchNews)
        searcProgressBar = view.findViewById(R.id.paginationSearchProgressBar)
        etSearch = view.findViewById(R.id.etSearch)

        val searchNewsRepository = SearchNewsRepository(ArticleDatabase.getDatabase(requireContext()))
        val searchNewsViewModelProviderFactory = SearchNewsViewModelProviderFactory(
            app = Application(),
            searchNewsRepository)
        searchViewModel = ViewModelProvider(this,
            searchNewsViewModelProviderFactory)[SearchNewsViewModel::class.java]
        setUpRecyclerView()

        searchNewsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putString("article", Gson().toJson(it))
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        var job : Job? = null
        etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()){
                        searchViewModel.getSearchNews(editable.toString())
                    }
                }
            }
        }


        searchViewModel.searchNewsMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {newsResponse ->
                        searchNewsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = searchViewModel.searchNewsPage == totalPages
                        if (isLastPage){
                            searcRecyclerView.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {message ->
                        Log.d(TAG, "An error occurred: $message")
                        Toast.makeText(activity,"An error occurred $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        })

    }
    private fun hideProgressBar(){
        searcProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }
    private fun showProgressBar(){
        searcProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false
    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate){
                searchViewModel.getSearchNews(etSearch.text.toString())
                isScrolling = false
            }
        }
    }

    private fun setUpRecyclerView(){
        searchNewsAdapter = NewsAdapter()
        searcRecyclerView.apply {
            adapter = searchNewsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }

    }
}