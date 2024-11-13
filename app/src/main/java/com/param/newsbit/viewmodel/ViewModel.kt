package com.param.newsbit.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.repo.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repo: Repository,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = javaClass.simpleName

    val chipGenre = MutableLiveData("Top Stories")
    val viewMode = MutableLiveData("Show Summary")

    val searchQuery = MutableLiveData("")
//     val searchQuery : LiveData<String> = _searchQuery

    private val _downloadNewsError = MutableLiveData(NetworkStatus.IN_PROGRESS)
    val downloadNewsError: LiveData<NetworkStatus> = _downloadNewsError

    private val _downloadSummaryError = MutableLiveData(NetworkStatus.IN_PROGRESS)
    val downloadSummaryError: LiveData<NetworkStatus> = _downloadSummaryError

    private val _refreshSummaryError = MutableLiveData(NetworkStatus.NOT_STARTED)
    val refreshSummaryError: LiveData<NetworkStatus> = _refreshSummaryError

    fun downloadNews(genre: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            Log.i(TAG, e.message.toString())
            _downloadNewsError.postValue(NetworkStatus.ERROR)
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repo.downloadNews(genre)
            _downloadNewsError.postValue(NetworkStatus.SUCCESS)
        }

    }

    fun getNewsByTitleGenre(): LiveData<PagingData<News>> {

        // genre, title
        val genreTitleMediator = MediatorLiveData<Pair<String?, String?>>().apply {

            addSource(chipGenre) { genre ->
                value = Pair(genre, searchQuery.value)
            }

            addSource(searchQuery) { sq ->
                value = Pair(chipGenre.value, sq)
            }

        }

        return genreTitleMediator.switchMap {

            if (it.first != null && it.second != null) {
                repo.getNewByTitleGenre(it.first.toString(), it.second.toString())
            } else {
                chipGenre.switchMap { genre -> repo.getNews(genre, LocalDate.now()) }
            }

        }

    }

    suspend fun selectNewsBody(url: String) = repo.getNewsBody(url)

    fun downloadSummary(newsUrl: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
            Log.e(TAG, "Error downloading summary for $newsUrl")
            _downloadSummaryError.postValue(NetworkStatus.ERROR)
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repo.downloadSummary(newsUrl)
            _downloadSummaryError.postValue(NetworkStatus.SUCCESS)
        }
    }

    fun refreshSummary(newsUrl: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
            _refreshSummaryError.postValue(NetworkStatus.ERROR)
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repo.refreshSummary(newsUrl)
            _refreshSummaryError.postValue(NetworkStatus.SUCCESS)
        }

    }

    fun selectSummary(url: String) = repo.getSummary(url)

    fun toggleBookmark(url: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleBookmark(url, value)
        }
    }

    fun selectNewsBookmark() = repo.getBookmarkedNews()

    fun deleteOlderThanWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteOlderThanWeek()
        }
    }

}