package com.param.newsbit.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.GsonBuilder
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.database.LocalDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var localDb: LocalDatabase

//    @Inject
    lateinit var repo: Repository

    lateinit var mockWebServer: MockWebServer


    @Before
    fun setUp() {
        hiltRule.inject()

        mockWebServer = MockWebServer()

        val api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("https://www.thestar.com"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TStarAPI::class.java)

        repo = Repository(localDb.newsDao(), api)

    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }


    @Test
    fun testItem() = runBlocking {



        mockWebServer.enqueue(
            MockResponse()
                .setBody(GsonBuilder().create().toJson(Pair(1,2)))
                .addHeader("Content-Type", "application/json")
        )


//        repo.downloadNews("Business")
        println(mockWebServer.takeRequest().toString())



    }


}