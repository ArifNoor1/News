package com.example.news.ui.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.example.news.ui.viewmodels.BreakingNewsViewModel
import com.example.news.ui.viewmodels.BreakingNewsViewModelProviderFactory
import com.example.news.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.news.utils.Resource
import com.google.gson.Gson


class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: BreakingNewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var rvRecyclerView: RecyclerView
    lateinit var progressBar : ProgressBar
    lateinit var btnRetry : Button
    lateinit var itemErrorMessage : View
    lateinit var tvErrorMessage : TextView
    val TAG = "BreakingNewsFragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvRecyclerView = view.findViewById(R.id.rvBreakingNews)
        progressBar = view.findViewById(R.id.paginationProgressBar)
        btnRetry = view.findViewById(R.id.btnRetry)
        itemErrorMessage = view.findViewById(R.id.itemErrorMessage)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)

        val breakingNewsRepository = BreakingNewsRepository(ArticleDatabase.getDatabase(requireContext()))
        val breakingNewsViewModelProviderFactory = BreakingNewsViewModelProviderFactory(
            app = Application(),
            breakingNewsRepository)
        viewModel = ViewModelProvider(this,
            breakingNewsViewModelProviderFactory)[BreakingNewsViewModel::class.java]
        setUpRecyclerView()

        newsAdapter.setOnItemClickListener {
            Log.d(TAG, "Error")
            val bundle = Bundle().apply {
                putString("article",Gson().toJson(it))
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }


        viewModel.breakingNewsMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let {newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if (isLastPage){
                                rvRecyclerView.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {message ->
                        Log.d(TAG, "An error occurred: $message")
                        Toast.makeText(activity,"An error occurred $message",Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        })
        btnRetry.setOnClickListener {
            viewModel.getBreakingNews("in")
        }
    }

    private fun hideProgressBar(){
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }
    private fun showProgressBar(){
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemErrorMessage.visibility = View.INVISIBLE
        isError = false
    }
    private fun showErrorMessage(message: String) {
        itemErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = message
        isError = true
    }

    var isError = false
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

            val isNoErrors = !isError

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate){
                viewModel.getBreakingNews("in")
                isScrolling = false
            }
        }
    }


    private fun setUpRecyclerView(){
        newsAdapter = NewsAdapter()
        rvRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }

    }
}