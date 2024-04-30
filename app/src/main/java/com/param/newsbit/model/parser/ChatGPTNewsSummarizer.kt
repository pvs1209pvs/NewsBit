package com.param.newsbit.model.parser

import android.util.Log
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object ChatGPTNewsSummarizer {

    private val TAG = javaClass.simpleName

    private const val apiKey = "sk-proj-nnNHOLJOoyGj7rOqAVtvT3BlbkFJMSdJLW6Bt4ewBdCVVCdo"
    private const val url = "https://api.openai.com/v1/chat/completions"
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val headers = Headers.Builder()
        .add("Content-Type", "application/json")
        .add("Authorization", "Bearer $apiKey")
        .build()


    private fun gptRequestBody(newsBody: String): RequestBody {

        val jsonObject = JSONObject()

        jsonObject.put("model", "gpt-3.5-turbo")

        val systemMsg = JSONObject().apply {
            put("role", "system")
            put("content", "You are a helpful assistant.")
        }

        val userMsg = JSONObject().apply {
            put("role", "user")
            put(
                "content",
                "Please summarize the following news article: $newsBody"
            )
        }

        val msgArray = JSONArray().apply {
            put(systemMsg)
            put(userMsg)
        }

        jsonObject.put("messages", msgArray)

        return jsonObject.toString().toRequestBody(mediaType)

    }

    /**
     * Throws an exception if the ChatGPT API failed to summarized the text.
     * @return News article summarized by ChatGPT API.
     */
    fun summarize(newsBody: String): String {

        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .post(gptRequestBody(newsBody))
            .build()

        return OkHttpClient().newCall(request).execute().use { response ->

            Log.i(TAG,  "Response code = ${response.code}")

            if (!response.isSuccessful || response.body == null) {
                throw IllegalStateException("ChatGPT API error = ${response.code} ${response.message}")
            }

            val responseBody = response.body!!.string()

            if (responseBody.isEmpty() || responseBody.isBlank()) {
                throw IllegalStateException("Blank/empty body return by ChatGPT API")
            }

            return@use JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

        }

    }

}