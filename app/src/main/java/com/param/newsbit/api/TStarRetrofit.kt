package com.param.newsbit.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class TStarRetrofit {

    companion object {

        private const val BASE_URL = "https://www.thestar.com"

        @Volatile
        private var INSTANCE: TStarAPI? = null

        fun getInstance(): TStarAPI {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TStarAPI::class.java)

                INSTANCE = instance
                return instance

            }

        }

    }

}