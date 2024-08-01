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

        Log.i(TAG, "$localCount $genre News in local database")

        if (localCount == 0) {

            Log.i(TAG, "Downloading $genre")

            val response =
                tStarRetrofit.downloadNews(if (genre == "Top Stories") null else "$genre*", 20)

            if (!response.isSuccessful) {
                Log.e(
                    TAG,
                    "Error downloading News using retro ${response.code()} = ${response.errorBody()}"
                )
                throw IllegalStateException("Error downloading News using retrofit = ${response.code()} ${response.errorBody()}")
            }

            if (response.body() == null) {
                Log.e(TAG, "Response body null")
                throw IllegalStateException("Response body null")
            }

            val allNews = response.body()!!.rows.map {

                val content = it.content.joinToString(" ") { paragraph ->
                    // Replace HTML tag with blank text.
                    paragraph.replace("<[^>]+>".toRegex(), "")
                }

                val pubDate = Instant
                    .ofEpochMilli(it.starttime.utc.toLong())
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                News(
                    url = it.url,
                    title = it.title,
                    genre = genre,
                    content = content,
                    imageUrl = it.preview.url,
                    isBookmarked = false,
                    pubDate = pubDate
                )

            }

            Log.i(TAG, "${allNews.size} $genre News downloaded using Retrofit")
            allNews.forEach {
                Log.i(TAG, "Downloaded News: ${it.title.substring(0,10)} ${it.pubDate}")
            }

            newsDao.insertAll(allNews)

        }

    }

    fun getNewsByGenre(genre: String, date: LocalDate) =
        newsDao.selectByGenre(genre, date.toString())


    suspend fun downloadSummary(newsUrl: String) {

        Log.i(TAG, "Downloading summary $newsUrl")

        val localSummary = newsDao.selectSummary(newsUrl)

        Log.i(TAG, "Local summary length ${localSummary.length}")

        if (localSummary.isBlank()) {
            val newsContent = newsDao.selectContent(newsUrl)
            val gptSummary = ChatGPTSummarizer.summarize(newsContent)
            newsDao.updateSummary(newsUrl, gptSummary)
            Log.i(TAG, "ChatGPT summary (len) ${gptSummary.length}")
        }

    }

    suspend fun refreshSummary(newsUrl: String) {

        Log.i(TAG, "Refreshing summary $newsUrl")

        val newsContent = newsDao.selectContent(newsUrl)
        val gptSummary = ChatGPTSummarizer.summarize(newsContent)
        newsDao.updateSummary(newsUrl, gptSummary)
        Log.i(TAG, "ChatGPT summary (len) ${gptSummary.length}")

    }

    fun getSummary(url: String) = newsDao.selectSummaryLD(url)

    fun getNewsBody(url: String) = newsDao.selectBody(url)

    suspend fun toggleBookmark(url: String, value: Boolean) {
        newsDao.toggleBookmark(url, value)
    }

    fun getBookmarkedNews() = newsDao.selectBookmarked()

    suspend fun deleteOlderThanWeek(){
        newsDao.deleteOlderThanWeek(LocalDate.now().toString())
    }

    //
    fun selectAll() = newsDao.selectAll()
    //

}