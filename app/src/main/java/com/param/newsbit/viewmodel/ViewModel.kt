package com.param.newsbit.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: Repository

    val chipGenre = MutableLiveData("Top Stories")

    init {
        val newsDao = LocalDatabase.getDatabase(application).newsDao()
        repo = Repository(newsDao)
    }

    fun downloadFromInternet(genre: String, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.downloadFromInternet(genre, date)
        }
    }

    fun selectNews(date: LocalDate = LocalDate.now()) =
        chipGenre.switchMap { repo.selectNewsByGenre(it, date) }

    fun selectSummary(url: String): LiveData<String?> {

        Log.d(javaClass.simpleName, "Selecting summary $url")

        viewModelScope.launch(Dispatchers.IO) {
            repo.fetchSummary(url)
        }

        return repo.selectSummary(url)

    }

    fun toggleBookmark(url: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleBookmark(url, value)
        }
    }

    fun selectNewsBookmark() = repo.selectNewsBookmarked()

}