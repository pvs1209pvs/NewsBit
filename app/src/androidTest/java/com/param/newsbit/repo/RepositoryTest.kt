package com.param.newsbit.repo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.param.newsbit.ChatGPTServiceTest
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.entity.News
import com.param.newsbit.entity.NewsJson
import com.param.newsbit.entity.Preview
import com.param.newsbit.entity.Row
import com.param.newsbit.entity.StartTime
import com.param.newsbit.model.parser.ChatGPTService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import java.time.LocalDate


@RunWith(MockitoJUnitRunner::class)
class RepositoryTest {

    private lateinit var repository: Repository
    private lateinit var database: LocalDatabase
    private lateinit var dao: NewsDao

    @Mock
    private lateinit var newsService: TStarAPI

    private val chatGPTServiceTest: ChatGPTService = ChatGPTServiceTest

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java).build()
        dao = database.newsDao()
        repository = Repository(dao, newsService, chatGPTServiceTest)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun returnDownloadedNewsWhenSuccessfulAPIResponse() = runTest {

        val limit = 10

        val rows = (1..limit).map {
            Row(
                content = listOf("<p>para1</p>", "<p>para2</p>"),
                title = "Title$it",
                url = "https://www.thestar.com/$it",
                preview = Preview("https://bloximages.chicago2.vip.townnews.com/thestar.com/content/tncms/assets/v3/editorial/0/32/03230c2a-a940-11ef-a271-936794d4da05/673ef7c6ef518.image.jpg?resize=640%2C427"),
                starttime = StartTime("1732328100000")
            )
        }

        val response = Response.success(
            200,
            NewsJson(rows)
        )

        `when`(newsService.downloadNews(c = null, l = limit)).thenReturn(response)

        val result = repository.downloadNews("Top Stories", limit)

        Assert.assertEquals(limit, result)

    }

    @Test
    fun throwExceptionWhenUnsuccessfulAPIResponse() = runTest {

        val errorBody = """{rows : []}""".toResponseBody("application/json".toMediaType())

        val response = Response.error<NewsJson>(400, errorBody)

        `when`(newsService.downloadNews(c = null, l = 20)).thenReturn(response)

        val result = try {
            repository.downloadNews("Top Stories", 20)
            null
        } catch (exception: IllegalStateException) {
            exception
        }

        Assert.assertEquals(
            "Response unsuccessful 400 when downloading Top Stories",
            result!!.message
        )

    }

    @Test
    fun returnEmptyListWhenAPIResponseBodyIsEmpty() = runTest {

        val response = Response.success<NewsJson>(NewsJson(emptyList()))

        `when`(newsService.downloadNews(c = null, l = 20)).thenReturn(response)

        val result = repository.downloadNews("Top Stories", 20)

        Assert.assertEquals(0, result)

    }

    @Test
    fun returnEmptyListWhenAPIResponseBodyIsNull() = runTest {

        val response = Response.success<NewsJson>(null)

        `when`(newsService.downloadNews(c = null, l = 20)).thenReturn(response)

        val result = repository.downloadNews("Top Stories", 20)

        Assert.assertEquals(0, result)

    }

    @Test
    fun downloadSummaryFromChatGPTServiceAndInsertInDatabaseIfAbsent() = runTest {

        val news = News(
            "https://www.thestar.com/0",
            "Title",
            "Top Stories",
            LocalDate.of(2000, 1, 1),
            "This a very long article about growing economy."
        )

        dao.insertAll(listOf(news))
        repository.downloadSummary(news.url)

        val summaryActual = dao.selectSummary(news.url)

        Assert.assertEquals("Growing economy article.", summaryActual)

    }

    @Test
    fun returnExistingSummaryFromDatabaseWhenPresent() = runTest {

        val news = News(
            "https://www.thestar.com/0",
            "Title",
            "Top Stories",
            LocalDate.of(2000, 1, 1),
            content = "This a very long article about growing economy.",
            summary = "Pre-existing summary."
        )

        dao.insertAll(listOf(news))
        repository.downloadSummary(news.url)

        val summaryActual = dao.selectSummary(news.url)

        Assert.assertEquals("Pre-existing summary.", summaryActual)

    }

}