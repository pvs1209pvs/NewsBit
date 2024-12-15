package com.param.newsbit.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.param.newsbit.entity.News

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(newsList: List<News>) : List<Long>

    @Query("SELECT * FROM news_table " +
            "WHERE genre = :genre AND " +
            "title LIKE '%' || :searchQuery || '%' AND " +
            "pubDate BETWEEN :startDate AND :endDate " +
            "ORDER BY pubDate DESC")
    fun selectBy(
        genre:String,
        searchQuery:String,
        startDate: String, endDate: String
    ) : PagingSource<Int, News>

    @Query("SELECT COUNT(*) FROM news_table " +
            "WHERE pubDate BETWEEN :startDate AND :endDate ")
    suspend fun countBy(startDate: String, endDate: String) : Long

    @Query("SELECT COUNT(*) FROM news_table")
    suspend fun countAll() : Long

    @Query("SELECT content FROM news_table " +
            "WHERE url = :url")
    suspend fun selectContent(url: String): String


    @Query("SELECT content FROM news_table " +
            "WHERE url = :url")
    fun selectBody(url: String): LiveData<String>

    @Query("SELECT summary FROM news_table " +
            "WHERE url = :url")
    suspend fun selectSummary(url: String): String

    @Query("SELECT summary FROM news_table " +
            "WHERE url = :url")
    fun selectSummaryLD(url: String): LiveData<String>

    @Query("UPDATE news_table " +
            "SET summary = :newSummary " +
            "WHERE url = :url")
    suspend fun updateSummary(url: String, newSummary: String)

    @Query("UPDATE news_table " +
            "SET isBookmarked = :value " +
            "WHERE url = :url")
    suspend fun updateBookmark(url: String, value: Boolean)

    @Query("SELECT * FROM news_table " +
            "WHERE isBookmarked = 1 " +
            "ORDER BY pubDate DESC")
    fun selectBookmarked(): LiveData<List<News>>

    @Query("SELECT isBookmarked FROM news_table " +
            "WHERE url = :url")
    fun selectBookmark(url: String): Int

    @Query("SELECT isBookmarked FROM news_table " +
            "WHERE url = :url")
    fun selectBookmarkLD(url: String): LiveData<Int>

    // 604800 seconds in a week
    @Query("DELETE FROM news_table " +
            "WHERE (UNIXEPOCH(:today)-UNIXEPOCH(pubDate) >= 604800) AND " +
            "isBookmarked = 0")
    suspend fun deleteOlderThanWeek(today: String)

    @Query("DELETE FROM news_table")
    suspend fun clearTable()

}
