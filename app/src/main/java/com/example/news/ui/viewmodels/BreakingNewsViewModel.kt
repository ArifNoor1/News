package com.example.news.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.NewsApplication
import com.example.news.models.NewsResponse
import com.example.news.ui.repository.BreakingNewsRepository
import com.example.news.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class BreakingNewsViewModel(
    app : Application,
    private val breakingNewsRepository: BreakingNewsRepository
): AndroidViewModel(app) {

    val breakingNewsMutableLiveData : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse : NewsResponse? = null

    init {
        getBreakingNews("in")
    }

    fun getBreakingNews(countryCode : String) = viewModelScope.launch {
       // safeBreakingNewsCall(countryCode)
        breakingNewsMutableLiveData.postValue(Resource.Loading())
        val response = breakingNewsRepository.getBreakingNews(countryCode,breakingNewsPage)
        breakingNewsMutableLiveData.postValue(handleBreakingNewsResponse(response))

    }

    private fun handleBreakingNewsResponse(response : Response<NewsResponse>) : Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNewsMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = breakingNewsRepository.getBreakingNews(countryCode,breakingNewsPage)
                breakingNewsMutableLiveData.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNewsMutableLiveData.postValue(Resource.Error("No internet connection"))
            }
        } catch (t : Throwable){
            when (t){
                is IOException -> breakingNewsMutableLiveData.postValue(Resource.Error("Network failure"))
                else -> breakingNewsMutableLiveData.postValue(Resource.Error("Conversion Error"))
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
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE-> true
                    TYPE_ETHERNET-> true
                    else -> false

                }
            }
        }
        return false
    }

}