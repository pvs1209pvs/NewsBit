package com.param.newsbit.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.entity.News
import com.param.newsbit.model.parser.ChatGPTNewsSummarizer
import com.param.newsbit.model.parser.RSSFeedParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

class Repository(private val newsDao: NewsDao) {

    suspend fun downloadFromInternet(genre: String, date: LocalDate) {

        val jsonObj = JSONObject().apply {
            put("year", date.year)
            put("month", date.monthValue)
            put("day", date.dayOfMonth)
        }

        val dateString = jsonObj.toString()

        val localCount = newsDao.localCount(genre, dateString)

        Log.d(javaClass.simpleName, "$localCount found locally")

        if (localCount == 0) {
            val fromCloud = RSSFeedParser.getRSSFeed(genre)
            newsDao.insertAll(fromCloud)
        }

    }

    fun selectNewsByGenre(genre: String, date: LocalDate): LiveData<List<News>> {

        val jsonObj = JSONObject().apply {
            put("year", date.year)
            put("month", date.monthValue)
            put("day", date.dayOfMonth)
        }

        val dateString = jsonObj.toString()

        return newsDao.selectByGenre(genre, dateString)

    }

    suspend fun fetchSummary(url: String) {

        val summary = newsDao.selectSummary(url)

        if (summary == null) {

            Log.d(javaClass.simpleName, "Getting summary from Chat GPT")

            val newsBody = RSSFeedParser.getTStarBody(url)
            val chatGptResponse = ChatGPTNewsSummarizer.summarize(newsBody)

            if (chatGptResponse != null) {
                newsDao.updateSummary(url, chatGptResponse)
            }

        } else {
            Log.d("fetchSummaryGPT Local", summary.length.toString())
        }

    }

    fun selectSummary(url: String) = newsDao.selectSummaryLD(url)

    suspend fun toggleBookmark(url: String, value: Boolean) {
//        Log.d("Change bookmark", "${news.isBookmarked} -> ${!news.isBookmarked}")
        newsDao.toggleBookmark(url, value)
    }

    fun selectNewsBookmarked() = newsDao.selectBookmarked()

    fun selectBookmar(url: String) = newsDao.selectBookmark(url)

}