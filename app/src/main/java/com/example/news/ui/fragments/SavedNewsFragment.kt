package com.example.news.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.models.Article
import com.example.news.roomdb.ArticleDatabase
import com.example.news.ui.repository.SavedNewsRepository
import com.example.news.ui.viewmodels.SavedNewsViewModel
import com.example.news.ui.viewmodels.SavedNewsViewModelProviderFactory
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

    lateinit var savedNewsRecyclerView: RecyclerView
    lateinit var savedNewsAdapter: NewsAdapter
    lateinit var savedNewsViewModel: SavedNewsViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val savedNewsRepository = SavedNewsRepository(ArticleDatabase.getDatabase(requireContext()))
        val savedNewsViewModelProviderFactory = SavedNewsViewModelProviderFactory(savedNewsRepository)
        savedNewsViewModel = ViewModelProvider(this,savedNewsViewModelProviderFactory).get(SavedNewsViewModel::class.java)
        savedNewsRecyclerView = view.findViewById(R.id.rvSavedNews)

        setUpRecyclerView()

        savedNewsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putString("article", Gson().toJson(it))
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }


        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or  ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = savedNewsAdapter.differ.currentList[position]
                savedNewsViewModel.deleteArticle(article)
                Snackbar.make(view,"Successfully deleted article",Snackbar.LENGTH_LONG).apply {
                    setAction("Undu"){
                        savedNewsViewModel.saveArticle(article)
                    }
                    show()
                }
            }

        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(savedNewsRecyclerView)
        }
        savedNewsViewModel.getSavedNews().observe(viewLifecycleOwner, Observer { articles ->
            savedNewsAdapter.differ.submitList(articles)
        })

    }

    private fun setUpRecyclerView(){
        savedNewsAdapter = NewsAdapter()

        savedNewsRecyclerView.apply {
            adapter = savedNewsAdapter
            layoutManager = LinearLayoutManager(activity)

        }

    }
}