package com.ruddell.utils

import com.google.gson.Gson
import com.ruddell.extensions.fromJson
import com.ruddell.extensions.toJson
import com.ruddell.models.Credentials
import extensions.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import models.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class JiraUser(
    val firstName: String,
    val lastName: String,
    val email: String,
    val userId: String
)

enum class TeamMember(val user: JiraUser) {
    Chris(JiraUser("Chris", "Ruddell", "chris.ruddell@bigcommerce.com", "70121:b4afeb5f-662f-4186-9ff6-c70fc9f1c2c4")),
    Danielle(JiraUser("Danielle", "Cushing", "danielle.cushing@bigcommerce.com", "5e4c2821fd328a0c9cec2456")),
    Vivek(JiraUser("Vivek", "Vishwanath", "vivek.vishwanath@bigcommerce.com", "61e74c82fd5a690068c5a26d")),
    Tina(JiraUser("Tina", "Ho", "tina.ho@bigcommerce.com", "61e74f61eb1fb60071a6c0b9")),
    Kymarly(JiraUser("Kymarly", "Henry", "kymarly.henry@bigcommerce.com", "61e7475f9d17400069d8fd7a")),
}

object JiraHelper {

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
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(Credentials.JIRA_USERNAME, Credentials.JIRA_TOKEN)
                }
                sendWithoutRequest { true }
            }
        }
    }

    suspend fun getUsersByEmail(emails: List<String>): List<JiraUser> {
        val allUsers = mutableListOf<JiraUser>()
        emails.forEach { email ->
            val users = getAllUsers(email)
            allUsers.addAll(users)
        }
        if (allUsers.isEmpty()) {
            bcprint("No users found for emails: $emails")
            getAllUsers(emails.firstOrNull() ?: "", printOutput = true)
        }
        return allUsers
    }

    suspend fun getAllUsers(query: String? = null, printOutput: Boolean = false): List<JiraUser> {
        var index = 0
        var allUsers = emptyList<JiraUser>()
        var isDone = false
        val pageSize = 1000
        val maxPages = 6
        while (!isDone) {
            val users = _getAllUsers(index, pageSize, query, printOutput)
            allUsers = allUsers + users
            index += pageSize
            isDone = users.isEmpty() || users.size < pageSize || index > (maxPages * pageSize)
        }
        return allUsers
    }

    private suspend fun _getAllUsers(index: Int = 0, pageSize: Int = 1000, query: String? = null, printOutput: Boolean = false): List<JiraUser> {
        val queryParam = query?.let { "&query=$query" } ?: ""
        val requestUrl = "${Credentials.JIRA_BASE_URL}/api/3/user/search?maxResults=$pageSize&startAt=$index$queryParam"
        val response = ktorClient.get(requestUrl).bodyAsText()
        if (printOutput) {
            bcprint(requestUrl)
            bcprint(response)
        }
        // decode using gson:
        val users: List<Map<String, Any>> = Gson().fromJson(response, List::class.java as Class<List<Map<String, Any>>>)

        return users.map { userMap ->
            val displayName = userMap["displayName"] as? String ?: ""
            JiraUser(
                firstName = displayName.split(" ").firstOrNull() ?: "",
                lastName = displayName.split(" ").lastOrNull() ?: "",
                email = userMap["emailAddress"] as? String ?: "",
                userId = userMap["accountId"] as? String ?: ""
            )
        }
    }

    suspend fun getClosedIssuesForUser(assignee: String, daysSince: Int = 90): List<SprintIssue> {
        var index = 1
        var isDone = false
        val maxPages = 6
        val pageSize = 100
        val allIssues = mutableListOf<SprintIssue>()
        while (!isDone) {
            val issues = _getClosedIssuesForUser(assignee, daysSince, index, pageSize)
            allIssues.addAll(issues)
            index += pageSize
            isDone = issues.isEmpty() || issues.size < pageSize || index > (maxPages * pageSize)
        }
        return allIssues
    }

    private suspend fun _getClosedIssuesForUser(assignee: String, daysSince: Int = 90, index: Int = 1, maxResults: Int = 100): List<SprintIssue> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysSince)
        val formatted = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        val requestUrl = "${Credentials.JIRA_BASE_URL}/api/2/search?jql=assignee=$assignee%20AND%20updated>=$formatted%20AND%20issuetype%20in%20(Story,Task,Bug)&fields=id,key,summary,assignee,issuetype,customfield_10026,status&expand=changelog&status=Closed&startAt=$index&maxResults=$maxResults"

        val issuesTxt = ktorClient.get(requestUrl).bodyAsText()
        val jiraResponse: JiraResponse<SprintIssue> = issuesTxt.fromJson() ?: JiraResponse(values = emptyList())
        if (jiraResponse.issues.isNullOrEmpty()) {
            bcprint("No issues found for $assignee")
            bcprint(issuesTxt)
        }
        return jiraResponse.issues ?: emptyList()
    }
}
