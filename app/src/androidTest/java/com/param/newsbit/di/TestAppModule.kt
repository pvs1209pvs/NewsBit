package com.param.newsbit.di

import androidx.compose.Context
import androidx.room.Room
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.repo.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named("test_db")
    fun providesLocalDatabaseInMemory(@ApplicationContext context: Context): LocalDatabase {
        val testDb =  Room
            .inMemoryDatabaseBuilder(context, LocalDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        return testDb

    }

    @Provides
    @Named("test_repo")
    fun providesRepository(@Named("test_db") testDb:LocalDatabase): Repository {

        val mockWebServer = MockWebServer()

        val api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TStarAPI::class.java)

        val repo = Repository(testDb.newsDao(), api)

        return repo

    }

}