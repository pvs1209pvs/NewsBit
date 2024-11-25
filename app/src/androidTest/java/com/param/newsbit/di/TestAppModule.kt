package com.param.newsbit.di

import android.content.Context
import androidx.room.Room
import com.param.newsbit.model.parser.ChatGPTServiceTest
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.repo.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named("test_db")
    fun providesDatabase(@ApplicationContext context: Context): LocalDatabase {

        return Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    }

    @Provides
    @Named("test_news_dao")
    fun providesNewsDao(@Named("test_db") localDatabase: LocalDatabase): NewsDao {
        return localDatabase.newsDao()
    }

    @Provides
    @Named("test_repo")
    fun providesRepository(
        @Named("test_news_dao") newsDao: NewsDao,
        tStarAPI: TStarAPI,
    ): Repository {
        return Repository(newsDao, tStarAPI, ChatGPTServiceTest)
    }

}