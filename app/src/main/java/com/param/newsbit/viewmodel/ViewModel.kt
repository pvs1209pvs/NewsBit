package com.param.newsbit.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.param.newsbit.entity.NewsFilter
import com.param.newsbit.entity.NetworkStatus
import com.param.newsbit.entity.NewsViewMode
import com.param.newsbit.repo.RepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repo: RepositoryInterface,
    application: Application,
    private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private val TAG = javaClass.simpleName

    val newsFilter = MutableLiveData(
        NewsFilter(
            "Top Stories",
            "",
            LocalDate.now(),
            LocalDate.now()
        )
    )

    val viewMode = MutableLiveData(NewsViewMode.SUMMARY)

    private val _downloadNewsError = MutableLiveData(NetworkStatus.IN_PROGRESS)
    val downloadNewsError: LiveData<NetworkStatus> = _downloadNewsError

    private val _downloadSummaryStatus = MutableLiveData(NetworkStatus.IN_PROGRESS)
    val downloadSummaryStatus: LiveData<NetworkStatus> = _downloadSummaryStatus

    private val _refreshSummaryError = MutableLiveData(NetworkStatus.NOT_STARTED)
    val refreshSummaryError: LiveData<NetworkStatus> = _refreshSummaryError


    val summaryStatus = MutableLiveData(Pair(NewsViewMode.SUMMARY, NetworkStatus.NOT_STARTED))


    fun downloadNews(genre: String, limit: Int) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            Log.i(TAG, e.message.toString())
            _downloadNewsError.postValue(NetworkStatus.ERROR)
        }

        viewModelScope.launch(ioDispatcher + coroutineExceptionHandler) {
            Log.i(TAG, "downloadNews: $genre rows inserted: ${repo.downloadNews(genre, limit)}")
            _downloadNewsError.postValue(NetworkStatus.SUCCESS)
        }

    }

    fun getNewsByGenreDateTitle() = newsFilter.switchMap {
        Log.i(TAG, "getNewsByGenreDateTitle: $it")
        repo.getNews(it)
    }

    suspend fun selectNewsBody(url: String): LiveData<String> = repo.getNewsBody(url)

    fun downloadSummary(newsUrl: String) {

        summaryStatus.value = summaryStatus.value?.copy(second = NetworkStatus.NOT_STARTED)

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
            Log.e(TAG, "downloadSummary: Error found for $newsUrl")
            summaryStatus.postValue(summaryStatus.value?.copy(second = NetworkStatus.ERROR))
            Log.e(TAG, "downloadSummary: Setting status to error for $newsUrl")
        }

        viewModelScope.launch(ioDispatcher + coroutineExceptionHandler) {
            repo.downloadSummary(newsUrl)
            summaryStatus.postValue(summaryStatus.value?.copy(second = NetworkStatus.SUCCESS))
            Log.i(TAG, "downloadSummary: Download sucessful")
        }
    }

    fun refreshSummary(newsUrl: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
            _refreshSummaryError.postValue(NetworkStatus.ERROR)
        }

        viewModelScope.launch(ioDispatcher + coroutineExceptionHandler) {
            repo.refreshSummary(newsUrl)
            _refreshSummaryError.postValue(NetworkStatus.SUCCESS)
        }

    }

    fun selectSummary(url: String) = repo.getSummary(url)

    fun toggleBookmark(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookmark = repo.selectBookmark(url) == 1
            repo.toggleBookmark(url, !bookmark)
        }
    }

    fun selectNewsBookmark() = repo.getBookmarkedNews()

    fun deleteOlderThanWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteOlderThanWeek()
        }
    }

    fun getNewsBody(newsUrl: String) = repo.getNewsBody(newsUrl)

    fun getBookmark(url: String) = repo.selectBookmark(url)
    fun getBookmarkLD(url: String) = repo.selectBookmarkLD(url)

}