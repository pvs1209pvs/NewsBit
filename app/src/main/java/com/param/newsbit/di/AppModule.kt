package com.param.newsbit.di

import androidx.compose.Context
import androidx.room.Room
import com.param.newsbit.api.TStarAPI
import com.param.newsbit.api.TStarRetrofit
import com.param.newsbit.dao.NewsDao
import com.param.newsbit.database.LocalDatabase
import com.param.newsbit.model.parser.ChatGPTService
import com.param.newsbit.model.parser.ChatGPTServiceProd
import com.param.newsbit.notifaction.NewsNotificationService
import com.param.newsbit.repo.Repository
import com.param.newsbit.repo.RepositoryInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesIODispatcher() =  Dispatchers.IO

    @Provides
    @Singleton
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase {

        return Room.databaseBuilder(
            context.applicationContext,
            LocalDatabase::class.java,
            "local_database"
        ).fallbackToDestructiveMigration().build()

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
    ): RepositoryInterface {
        return Repository(newsDao, tStarAPI, chatGPTService)
    }

    @Provides
    @Singleton
    fun providesRetrofit(): TStarAPI {

        return Retrofit.Builder()
            .baseUrl("https://www.thestar.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TStarAPI::class.java)

    }

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

