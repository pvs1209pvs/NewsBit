package com.param.newsbit.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.entity.News
import com.param.newsbit.entity.NewsFilter
import com.param.newsbit.model.parser.ChatGPTSummarizer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class Repository @Inject constructor(
    private val newsDao: NewsDao,
    private val tStarRetrofit: TStarAPI,
) {

    private val TAG = javaClass.simpleName

    var reduction = mutableListOf<Double>()

    /**
     * Downloads a list of news of the given genre.
     * genre = null for top stories
     */
    suspend fun downloadNews(genre: String): Int {

        Log.i(TAG, "Downloading: $genre")

        val response = tStarRetrofit.downloadNews(
            if (genre == "Top Stories") null else "$genre*",
            20
        )

        Log.i(TAG, "Response code for $genre: ${response.code()}")

        if (!response.isSuccessful) {
            Log.e(TAG, "Error downloading: $genre: ${response.code()} ${response.errorBody()}")
            throw IllegalStateException("Error downloading: $genre: ${response.code()} ${response.errorBody()}")
        }

        val downloadedNews = response.body()?.rows?.map {

            val content = it.content.joinToString(" ") { paragraph ->
                paragraph.replace("<[^>]+>".toRegex(), "") // removes HTML tags
            }

            val pubDate = Instant
                .ofEpochMilli(it.starttime.utc.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            News(
                url = it.url,
                title = it.title,
                genre = genre,
                pubDate = pubDate,
                content = content,
                imageUrl = it.preview.url
            )

        } ?: emptyList()

        Log.i(TAG, "News articles downloaded for $genre: ${downloadedNews.size}")

        return newsDao.insertAll(downloadedNews).size

    }

    fun getNews(filter: NewsFilter): LiveData<PagingData<News>> {

        return Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20),
            pagingSourceFactory = {
                newsDao.selectBy(
                    filter.genre,
                    filter.searchQuery,
                    filter.startDate.toString(),
                    filter.endDate.toString()
                )
            }
        ).liveData

    }

    suspend fun downloadSummary(newsUrl: String) {

        Log.i(TAG, "Downloading summary for: $newsUrl")

        val localSummary = newsDao.selectSummary(newsUrl)

        Log.i(TAG, "Local summary length: ${localSummary.length}")

        if (localSummary.isBlank()) {
            val newsContent = newsDao.selectContent(newsUrl)
            val gptSummary = ChatGPTSummarizer.summarize(newsContent)
            newsDao.updateSummary(newsUrl, gptSummary)

            reduction.add((newsContent.length - gptSummary.length) / (newsContent.length * 1.0))
            Log.i(TAG, "Reduction by: ${reduction.average()}")
        }

    }

    suspend fun refreshSummary(newsUrl: String) {

        Log.i(TAG, "Refreshing summary $newsUrl")

        val newsContent = newsDao.selectContent(newsUrl)
        val gptSummary = ChatGPTSummarizer.summarize(newsContent)
        newsDao.updateSummary(newsUrl, gptSummary)

        Log.i(TAG, "ChatGPT summary character len: ${gptSummary.length}")

    }

    fun getSummary(url: String) = newsDao.selectSummaryLD(url)

    suspend fun getNewsBody(url: String) = MutableLiveData(newsDao.selectContent(url))

    suspend fun toggleBookmark(url: String, value: Boolean) {
        newsDao.updateBookmark(url, value)
    }

    fun getBookmarkedNews() = newsDao.selectBookmarked()

    suspend fun deleteOlderThanWeek() {
        newsDao.deleteOlderThanWeek(LocalDate.now().toString())
    }

    suspend fun countBy(newsFilter: NewsFilter) =
        newsDao.countBy(newsFilter.startDate.toString(), newsFilter.endDate.toString())

}