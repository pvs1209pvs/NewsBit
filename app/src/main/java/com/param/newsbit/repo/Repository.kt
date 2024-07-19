package com.param.newsbit.repo

import android.util.Log
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.ArticleDownloader
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

    /**
     * Downloads a list of news of the given genre.
     * genre = null for top stories
     */
    suspend fun downloadNews(genre: String) {

        val localCount = newsDao.localCount(genre, LocalDate.now().toString())

        Log.i(TAG, "$localCount locally found")

        if (localCount == 0) {

            Log.i(TAG, "Downloading $genre")

            val response =
                tStarRetrofit.downloadNews(if (genre == "Top Stories") null else genre, 20)

            if (!response.isSuccessful) {
                Log.e(
                    TAG,
                    "Error downloading news using retro ${response.code()} = ${response.errorBody()}"
                )
                throw IllegalStateException("Error downloading news using retrofit = ${response.code()} ${response.errorBody()}")
            }

            if (response.body() == null) {
                Log.e(TAG, "Response body null")
                throw IllegalStateException("Response body null")
            }

            val allNews = response.body()!!.rows.map {
                News(
                    url = it.url,
                    title = it.title,
                    genre = genre,
                    summary = "",
                    imageUrl = it.preview.url,
                    isBookmarked = false,
                    pubDate = Instant.ofEpochMilli(it.starttime.utc.toLong())
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }

            Log.i(TAG, "retroDownload: Downloaded ${allNews.size} $genre articles ")

            newsDao.insertAll(allNews)

        }

    }

    fun getNewsByGenre(genre: String, date: LocalDate) =
        newsDao.selectByGenre(genre, date.toString())


    suspend fun downloadSummary(articleUrl: String) {

        Log.i(TAG, "Downloading summary $articleUrl")

        val localSummary = newsDao.selectSummary(articleUrl)

        Log.i(TAG, "Local summary found ${localSummary != null}")

        if (localSummary.isNullOrBlank()) { // only download if summary hasn't been downloaded before

            val newsBody = ArticleDownloader.getNewsBody(articleUrl).replace("\"", "'")
            Log.i(TAG, "News body (len) ${newsBody.length}")

            val gptSummary = ChatGPTSummarizer.summarize(newsBody)
            Log.i(TAG, "ChatGPT summary (len) ${gptSummary.length}")

            newsDao.updateSummary(articleUrl, gptSummary)

        }

    }

    suspend fun refreshSummary(articleUrl: String) {

        Log.i(TAG, "Refreshing summary $articleUrl")

        val newsBody = ArticleDownloader.getNewsBody(articleUrl).replace("\"", "'")
        val gptSummary = ChatGPTSummarizer.summarize(newsBody)

        newsDao.updateSummary(articleUrl, gptSummary)

    }

    fun getSummary(url: String) = newsDao.selectSummaryLD(url)

    suspend fun toggleBookmark(url: String, value: Boolean) {
        newsDao.toggleBookmark(url, value)
    }

    fun getBookmarkedNews() = newsDao.selectBookmarked()


}