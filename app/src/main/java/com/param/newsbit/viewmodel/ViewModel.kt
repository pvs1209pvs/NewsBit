package com.param.newsbit.viewmodel

import android.app.Application
import android.net.http.UrlRequest.Status
import android.util.Log
import androidx.lifecycle.*
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

    private val _downloadNewsError = MutableLiveData(false)
    val downloadNewsError: LiveData<Boolean> = _downloadNewsError

    private val _downloadSummaryError = MutableLiveData(false)
    val downloadSummaryError: LiveData<Boolean> = _downloadSummaryError

    private val _refreshSummaryError = MutableLiveData(false)
    val refreshSummaryError: LiveData<Boolean> = _refreshSummaryError

    fun downloadRetro(genre: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            Log.i(TAG, e.message.toString())
            _downloadNewsError.postValue(true)
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repo.retroDownload(genre)
            _downloadNewsError.postValue(false)
        }

    }

    fun downloadFromInternet(genre: String, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.downloadArticles(genre, date)
        }
    }

    fun selectNews(date: LocalDate = LocalDate.now()) =
        chipGenre.switchMap { repo.selectNewsByGenre(it, date) }


    fun downloadSummary(url: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
            Log.i(TAG, "Error downloading summary $url")
            _downloadSummaryError.postValue(true)
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repo.downloadSummary(url)
            _downloadSummaryError.postValue(false)
        }
    }

    fun refreshSummary(articleUrl: String): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            repo.refreshSummary(articleUrl)
        }
    }

    fun refreshSummary2(articleUrl: String) {

        val coroutineExceptionHandler = CoroutineExceptionHandler{_,_ ->
            _refreshSummaryError.postValue(true)
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repo.refreshSummary(articleUrl)
            _refreshSummaryError.postValue(false)
        }

    }

    fun selectSummary(url: String) = repo.selectSummary(url)


    fun toggleBookmark(url: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleBookmark(url, value)
        }
    }

    fun selectNewsBookmark() = repo.selectNewsBookmarked()

}