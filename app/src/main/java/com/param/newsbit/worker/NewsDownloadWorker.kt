package com.param.newsbit.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.param.newsbit.repo.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NewsDownloadWorker @AssistedInject constructor(
    @Assisted private val repository: Repository,
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val TAG = javaClass.simpleName

/*
    private suspend fun genreDownloader(genre: String) {

        Log.i(TAG, "Downloading $genre using Worker Manager")

        val response = tStarRetrofit.downloadNews(
            if (genre == "Top Stories") null else "$genre*",
            20
        )

        if (!response.isSuccessful) {
            Log.e(
                TAG,
                "Error downloading News using Retrofit ${response.code()} = ${response.errorBody()}"
            )
            throw IllegalStateException("Error downloading News using Retrofit = ${response.code()} ${response.errorBody()}")
        }

        if (response.body() == null) {
            Log.e(TAG, "Response body null")
            throw IllegalStateException("Response body null")
        }

        val allNews = response.body()!!.rows.map {

            val content = it.content.joinToString(" ") { paragraph ->
                val htmlTagRegex = "<[^>]+>".toRegex()
                paragraph.replace(htmlTagRegex, "")
            }

            val pubDate = Instant
                .ofEpochMilli(it.starttime.utc.toLong())
                .atZone(ZoneId.systemDefault()).toLocalDate()

            News(
                url = it.url,
                title = it.title,
                genre = genre,
                pubDate = pubDate,
                content = content,
                imageUrl = it.preview.url
            )

        }

        Log.i(TAG, "${allNews.size} $genre News downloaded using Retrofit")
//        allNews.forEach {
//            Log.i(TAG, "Downloaded News: ${it.title.substring(0, 10)} ${it.pubDate}")
//        }

        newsDao.insertAll(allNews)

    }
*/

    override suspend fun doWork(): Result {

        try {

            val newsGenre = listOf(
                "Top Stories",
                "Business",
                "Real Estate",
                "Opinion",
                "Politics",
                "Entertainment",
                "Life"
            )

            newsGenre.forEach {
                repository.downloadNews(it)
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }

        return Result.failure()

    }


}