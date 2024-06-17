package com.ruddell.utils

import com.ruddell.extensions.fromJson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import models.PrsResponse
import java.text.SimpleDateFormat
import java.util.*

object GithubHelper {

    private val GITHUB_TOKEN = BuildConfig.GITHUB_TOKEN
    @OptIn(ExperimentalSerializationApi::class)
    val serializer = Json { isLenient = true; ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }
    private val ktorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(serializer)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
        }
    }

    fun dateFormat(daysSince: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(daysSince + 1))
        val formatted = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        return formatted
    }

    suspend fun prsClosedBy(user: String, daysSince: Int = 90): Int {
        val formatted = dateFormat(daysSince)
        val url = "https://api.github.com/search/issues?q=author:$user+is:pr+closed:>$formatted"
        val resp = ktorClient.get(url) {
            header("Authorization", "token $GITHUB_TOKEN")
        }
        val body = resp.bodyAsText()
        val response: PrsResponse = body.fromJson() ?: PrsResponse()
        return response.total_count ?: 0
    }

    suspend fun prsReviewedBy(user: String, daysSince: Int = 90): Int {
        val formatted = dateFormat(daysSince)
        val url = "https://api.github.com/search/issues?q=reviewed-by:$user+-author:$user+is:pr+is:closed+closed:>$formatted"
        val resp = ktorClient.get(url) {
            header("Authorization", "token $GITHUB_TOKEN")
        }
        val body = resp.bodyAsText()
        val response: PrsResponse = body.fromJson() ?: PrsResponse()
        return response.total_count ?: 0
    }

}
