package com.param.newsbit.model.parser

import android.util.Log
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object ChatGPTNewsSummarizer {

    private fun simpleResponse(newsBody: String): JSONObject {

        val jsonObject = JSONObject()

        jsonObject.put("model", "gpt-3.5-turbo")
        jsonObject.put("prompt", "tell me a joke")

        return jsonObject

    }

    private fun req(newsBody: String): JSONObject {

        val jsonObject = JSONObject()

        jsonObject.put("model", "gpt-3.5-turbo")

        jsonObject.put("type", "json_object")

        val message = JSONObject().apply {
            put("role", "user")
            put("content", "What is 2+2?")
        }

        val messageArray = JSONArray().put(message)

        jsonObject.put("messages", messageArray)

        return jsonObject

    }

    private fun buildChatGPTRequestBody(newsBody: String): JSONObject {

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
                "Please summarize the following new article: $newsBody"
            )
        }

        val msgArray = JSONArray().apply {
            put(systemMsg)
            put(userMsg)
        }

        jsonObject.put("messages", msgArray)

        return jsonObject

    }

    fun summarize(message: String): String? {

        Log.d(javaClass.simpleName, "Summarize with chat gpt")

        val apiKey = "sk-proj-nnNHOLJOoyGj7rOqAVtvT3BlbkFJMSdJLW6Bt4ewBdCVVCdo"
        val url = "https://api.openai.com/v1/chat/completions"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = buildChatGPTRequestBody(message).toString().toRequestBody(mediaType)

        val headers = Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Authorization", "Bearer $apiKey")
            .build()

        val request = Request
            .Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build()

        return OkHttpClient().newCall(request).execute().use {

            Log.d("Response Code", it.code.toString())

            if (!it.isSuccessful) {
                return@use null
            }

            if (it.body == null) {
                return@use null
            }

            val responseBody = it.body!!.string()

            if (responseBody.isEmpty() || responseBody.isBlank()) {
                return@use null
            }

            return@use JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

        }

    }

}