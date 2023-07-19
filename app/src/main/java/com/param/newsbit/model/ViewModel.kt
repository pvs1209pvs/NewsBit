package com.param.newsbit.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.param.newsbit.model.database.LocalDatabase
import com.param.newsbit.model.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: Repository

    init {
        val newsDao = LocalDatabase.getDatabase(application).newsDao()
        repo = Repository(newsDao)
    }

    fun smartSelect(genre: String, date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.fetchFromCloud(genre, date)
        }
    }

    fun selectNews(genre: String, now: LocalDate) = repo.selectNewsByGenre(genre, now)

    fun selectBookmark(url:String) = repo.selectBookmar(url)

    fun fetchSummaryGTP(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.selectSummary(url)
        }
    }

    fun selectSummary(url: String): LiveData<String?> {

        Log.d(javaClass.simpleName + "/selectSummary", url)

        viewModelScope.launch(Dispatchers.IO) {
            repo.fetchSummary(url)
        }

        return repo.selectSummary(url)

    }

    fun toogleBookmark(url: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.toggleBookmark(url, value)
        }
    }

    fun selectNewsBookmark() = repo.selectNewsBookmarked()

}