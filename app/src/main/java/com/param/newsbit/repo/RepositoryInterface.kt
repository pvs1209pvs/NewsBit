package com.param.newsbit.repo

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.param.newsbit.entity.News
import com.param.newsbit.entity.NewsFilter

interface RepositoryInterface {

    suspend fun downloadNews(genre: String, l: Int): Int

    fun getNews(filter: NewsFilter): LiveData<PagingData<News>>

    suspend fun downloadSummary(newsUrl: String)

    suspend fun refreshSummary(newsUrl: String)

    fun getSummary(url: String) : LiveData<String>

    suspend fun getNewsBody(url: String) : LiveData<String>

    suspend fun toggleBookmark(url: String, value: Boolean)

    fun getBookmarkedNews() : LiveData<List<News>>

    suspend fun deleteOlderThanWeek()

    suspend fun countBy(newsFilter: NewsFilter) : Long

}