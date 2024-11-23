package com.param.newsbit.di

import androidx.compose.Context
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.api.TStarRetrofit
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.model.parser.ChatGPTService
import com.param.newsbit.model.parser.ChatGPTServiceProd
import com.param.newsbit.notifaction.NewsNotificationService
import com.param.newsbit.repo.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase {
        return LocalDatabase.getDatabase(context)

    }

    @Provides
    @Singleton
    fun provideNewsDao(localDatabase: LocalDatabase): NewsDao {
        return localDatabase.newsDao()
    }

    @Provides
    @Singleton
    fun provideRepository(
        newsDao: NewsDao,
        tStarAPI: TStarAPI,
        chatGPTService: ChatGPTService
    ): Repository {
        return Repository(newsDao, tStarAPI, chatGPTService)
    }

    @Provides
    @Singleton
    fun providesRetrofit() = TStarRetrofit.getInstance()

    @Provides
    @Singleton
    fun providesChatGPTService(): ChatGPTService {
        return ChatGPTServiceProd
    }

    @Provides
    @Singleton
    fun providesNewsNotificationService(@ApplicationContext context: Context): NewsNotificationService {
        return NewsNotificationService(context)
    }

}

