package com.param.newsbit.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import androidx.paging.log
import androidx.paging.map
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.entity.News
import com.param.newsbit.entity.NewsFilter
import com.param.newsbit.model.parser.ChatGPTService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class Repository @Inject constructor(
    private val newsDao: NewsDao,
    private val tStarRetrofit: TStarAPI,
    private val gptService: ChatGPTService
) : RepositoryInterface {

    private val TAG = javaClass.simpleName

    /**
     * Downloads a list of news of the given genre.
     * genre = null for top stories
     */
    override suspend fun downloadNews(genre: String, l: Int): Int {

        Log.i(TAG, "Downloading: $genre")

        val response = tStarRetrofit.downloadNews(
            if (genre == "Top Stories") null else "$genre*",
            l
        )

        Log.i(TAG, "Response code for $genre: ${response.code()}")

        if (!response.isSuccessful) {
            Log.e(TAG, "Error downloading: $genre: ${response.code()} ${response.errorBody()}")
            throw IllegalStateException("Response unsuccessful ${response.code()} when downloading $genre")
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

        newsDao.insertAll(downloadedNews).size
        return downloadedNews.size

    }

    override fun getNews(filter: NewsFilter): LiveData<PagingData<News>> {

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

    override suspend fun downloadSummary(newsUrl: String) {

        Log.i(TAG, "Downloading summary for: $newsUrl")

        val localSummary = newsDao.selectSummary(newsUrl)

        Log.i(TAG, "Local summary length: ${localSummary.length}")

        if (localSummary.isBlank()) {
            Log.i(TAG, "downloadSummary: Downloading summary")
            val newsContent = newsDao.selectContent(newsUrl)
            val gptSummary = gptService.summarize(newsContent)
            newsDao.updateSummary(newsUrl, gptSummary)
            Log.i(TAG, "ChatGPT summary character len: ${gptSummary.length}")
        }

    }

    override suspend fun refreshSummary(newsUrl: String) {

        Log.i(TAG, "Refreshing summary: $newsUrl")

        val newsContent = newsDao.selectContent(newsUrl)
        val gptSummary = gptService.summarize(newsContent)
        newsDao.updateSummary(newsUrl, gptSummary)

        Log.i(TAG, "ChatGPT summary character len: ${gptSummary.length}")

    }

    override fun getSummary(url: String) = newsDao.selectSummaryLD(url)

    override suspend fun getSummaryValue(url: String) = newsDao.selectSummary(url)

    override fun getNewsBody(url: String) = newsDao.selectBody(url)

    override suspend fun toggleBookmark(url: String, value: Boolean) {
        newsDao.updateBookmark(url, value)
    }

    override fun getBookmarkedNews() = newsDao.selectBookmarked()

    override suspend fun deleteOlderThanWeek() {
        newsDao.deleteOlderThanWeek(LocalDate.now().toString())
    }

    override suspend fun countBy(newsFilter: NewsFilter) =
        newsDao.countBy(newsFilter.startDate.toString(), newsFilter.endDate.toString())

    override fun selectBookmark(url: String) = newsDao.selectBookmark(url)

    override fun selectBookmarkLD(url: String) = newsDao.selectBookmarkLD(url)

}