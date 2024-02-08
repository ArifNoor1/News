package com.example.news.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.news.NewsApplication
import com.example.news.models.NewsResponse
import com.example.news.ui.repository.SearchNewsRepository
import com.example.news.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class SearchNewsViewModel(
    application: Application,
    val newsRepository: SearchNewsRepository
) : AndroidViewModel(application) {
    val searchNewsMutableLiveData : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse : NewsResponse? = null
    var newSearchQuery:String? = null
    var oldSearchQuery:String? = null

    fun getSearchNews(searchQuery : String) = viewModelScope.launch {
       // safeSearchNewsCall(searchQuery)
        searchNewsMutableLiveData.postValue(Resource.Loading())
        val response = newsRepository.getSearchNews(searchQuery,searchNewsPage)
        searchNewsMutableLiveData.postValue(handleSearchNewsResponse(response))
    }

    private fun handleSearchNewsResponse(response : Response<NewsResponse>) : Resource<NewsResponse> {
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery){
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeSearchNewsCall(searchQuery: String){
        newSearchQuery = searchQuery
        searchNewsMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.getSearchNews(searchQuery,searchNewsPage)
                searchNewsMutableLiveData.postValue(handleSearchNewsResponse(response))
            } else {
                searchNewsMutableLiveData.postValue(Resource.Error("No internet connection"))
            }
        } catch (t : Throwable){
            when (t){
                is IOException -> searchNewsMutableLiveData.postValue(Resource.Error("Network failure"))
                else -> searchNewsMutableLiveData.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun hasInternetConnection() : Boolean{
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type){
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false

                }
            }
        }
        return false
    }
}