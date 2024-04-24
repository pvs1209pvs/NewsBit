package com.param.newsbit.repo

import android.util.Log
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.model.parser.ChatGPTNewsSummarizer
import com.param.newsbit.model.parser.RSSFeedParser
import java.time.LocalDate

class Repository(private val newsDao: NewsDao) {

    /**
     * Downloads news from the internet and directly adds them to the database.
     */
    suspend fun downloadFromInternet(genre: String, date: LocalDate) {
        val fromCloud = RSSFeedParser.getRSSFeed(genre)
        newsDao.insertAll(fromCloud)
    }

//    suspend fun downloadFromInternet(genre: String, date: LocalDate) {
//
//        val localCount = newsDao.localCount(genre, date.toString())
//
//        Log.d(javaClass.simpleName, "$localCount found locally")
//
//        if (localCount == 0) {
//            val fromCloud = RSSFeedParser.getRSSFeed(genre)
//            newsDao.insertAll(fromCloud)
//        }
//
//    }

    fun selectNewsByGenre(genre: String, date: LocalDate) =
        newsDao.selectByGenre(genre, date.toString())


    suspend fun fetchSummary(url: String) {

        val summary = newsDao.selectSummary(url)

        if (summary == null) {

            Log.d(javaClass.simpleName, "Getting summary from Chat GPT")

            val newsBody = RSSFeedParser.getTStarBody(url).replace("\"", "'")

            Log.d("News body", newsBody)

            val chatGptResponse = ChatGPTNewsSummarizer.summarize(newsBody)

            Log.d("Chat GPT Summary", chatGptResponse.toString())

            if (chatGptResponse != null) {
                newsDao.updateSummary(url, chatGptResponse)
            }

        } else {
            Log.d("fetchSummaryGPT Local", summary.length.toString())
        }

    }

    fun selectSummary(url: String) = newsDao.selectSummaryLD(url)

    suspend fun toggleBookmark(url: String, value: Boolean) {
        newsDao.toggleBookmark(url, value)
    }

    fun selectNewsBookmarked() = newsDao.selectBookmarked()

}