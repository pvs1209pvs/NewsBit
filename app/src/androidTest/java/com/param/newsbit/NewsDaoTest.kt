package com.param.newsbit

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.entity.News
import kotlinx.coroutines.test.runTest
import okhttp3.internal.toImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate


@RunWith(AndroidJUnit4::class)
class NewsDaoTest {

    private lateinit var database: LocalDatabase
    private lateinit var dao: NewsDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, LocalDatabase::class.java).build()
        dao = database.newsDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun returnNewsMatchingTitleFilter() = runTest {

        val genre = "Top Stories"
        val date = LocalDate.of(2000, 1, 1)

        val initRows = listOf(
            News("https://www.thestar.com/0", "title", genre, date, ""),
            News("https://www.thestar.com/1", "title_post", genre, date, ""),
            News("https://www.thestar.com/3", "pre_title", genre, date, ""),
            News("https://www.thestar.com/2", "wrong", genre, date, ""),
            News("https://www.thestar.com/4", "", genre, date, ""),
        )

        dao.insertAll(initRows)

        val pagedNews = dao.selectBy(genre, "title", date.toString(), date.toString())

        val loaded = pagedNews.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        Assert.assertEquals(3, loaded.data.size)

    }


    @Test
    fun returnAllNewsWhenUsingEmptyTitleFilter() = runTest {

        val genre = "Top Stories"
        val date = LocalDate.of(2000, 1, 1)

        val initData = listOf(
            News(
                "https://www.thestar.com/0",
                "title0",
                genre,
                date,
                ""
            ),
            News(
                "https://www.thestar.com/1",
                "title1",
                genre,
                date,
                ""
            ),
        )

        dao.insertAll(initData)

        val pagedNews = dao.selectBy(genre, "", date.toString(), date.toString())

        val resultPage = pagedNews.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        Assert.assertEquals(2, resultPage.data.size)

    }

    @Test
    fun returnNewsBetweenStartAndEndDate() = runTest {

        val genre = "Top Stories"

        val initData = (1..6).map {
            News(
                "https://www.thestar.com/$it",
                "title$it",
                genre,
                LocalDate.of(2000, 1, it),
                ""
            )
        }.toImmutableList()

        dao.insertAll(initData)

        val pagedNews = dao.selectBy(
            genre,
            "title",
            LocalDate.of(2000, 1, 1).toString(),
            LocalDate.of(2000, 1, 4).toString()
        )

        val resultPage = pagedNews.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        Assert.assertEquals(4, resultPage.data.size)

    }

    @Test
    fun deleteNewsOlderThanOneWeek() = runTest {

        val genre = "Top Stories"

        val initData = (1..8).map {
            News(
                "https://www.thestar.com/$it",
                "title$it",
                genre,
                LocalDate.of(2000, 1, it),
                ""
            )
        }.toImmutableList()

        val bookmarked = listOf(
            News(
                "https://www.thestar.com/9",
                "title9",
                genre,
                LocalDate.of(2000, 1, 1),
                "",
                isBookmarked = true
            )
        )

        val result = dao.run {
            insertAll(initData)
            insertAll(bookmarked)
            deleteOlderThanWeek(LocalDate.of(2000, 1, 8).toString())
            countAll()
        }

        Assert.assertEquals(8, result)

    }

}