package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Sprint(
    val id: Long,
    val name: String? = null,
    val state: String? = null,
    val self: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val completeDate: String? = null,
    val goal: String? = null,
)

data class SprintWithDates(
    val id: Long,
    val self: String? = null,
    val state: String? = null,
    val name: String? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val completeDate: Date? = null
)

@Serializable
data class SprintIssue(
    val id: Int,
    val key: String,
    val fields: IssueFields,
    val sprintId: Int? = null,
    val changelog: JiraChangelog? = null,
)

@Serializable
data class JiraChangelog(
    val maxResults: Int? = null,
    val total: Int? = null,
    val histories: List<JiraHistory>? = null
)

@Serializable
data class JiraHistory(
    val id: Long? = null,
    val author: Assignee? = null,
    val created: String? = null, // 2023-07-28T11:19:50.673-0500
    val items: List<JiraHistoryItem>? = null
)

// e.g., 2023-07-28T11:19:50.673-0500


@Serializable
data class JiraHistoryItem(
    val field: String,
    val fromString: String? = null,
    val toString: String? = null,
)

@Serializable
data class IssueFields(
    val epic: Epic? = null,
    @SerialName("customfield_10026") val storyPoints: Float? = null,
    val closedSprints: List<Sprint>? = null,
    val status: Status? = null,
    val assignee: Assignee? = null,
    val issuetype: IssueType? = null,
    @SerialName("summary") val title: String? = null,
    val updated: String? = null,
    val comment: JiraComment? = null,
)

@Serializable
data class JiraComment(
    val comments: List<SprintComment>
)

@Serializable
data class SprintComment(
    val self: String? = null,
    val id: String? = null,
    val author: Assignee? = null,
    val body: String? = null,
)

@Serializable
data class IssueType(
    val self: String? = null,
    val id: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val name: String? = null,
    val subtask: Boolean? = null,
)

@Serializable
data class Assignee(
    val self: String? = null,
    val accountId: String? = null,
    val emailAddress: String? = null,
    val displayName: String? = null,
    val active: Boolean? = null,
    val timeZone: String? = null
)

@Serializable
data class Status(
    val name: String? = null,
    val id: String? = null,
    val description: String? = null,
    val self: String? = null
)

@Serializable
data class Epic(
    val id: Long,
    val self: String? = null,
    val key: String,
    val name: String,
    val storyPoints: Float = 0f,
    val issues: List<SprintIssue> = emptyList()
) {
    val bauIds: List<Long> = listOf(
        320140L, 282211L, 279135L, 280005L, 280701L, 278773L, 322271L, 320786L, 279447L,
        322275L, 278866L, 281888L, 279305L, 281379L, 282166L, 281481, 278788, 279612,
        278775, 281881, 323048, 440975, 281880, 321984, 323462, 320030, 321614, 321986,
        321628, 280124, 320490, 403904, 417821, 430134, 445926
    )
    val isBau: Boolean get() = bauIds.contains(id)
}

@Serializable
data class JiraResponse<T>(
    val message: String? = null,
    val `status-code`: Int? = null,
    val maxResults: Int? = null,
    val total: Int? = null,
    val startAt: Int? = null,
    val isLast: Boolean? = null,
    val values: List<T>? = null,
    val issues: List<T>? = null
)

@Serializable
data class SprintIssueWithDaysInProgress(
    val accountId: String?,
    val emailAddress: String?,
    val displayName: String?,
    val ticketNumber: String?,
    val ticketType: String?,
    val storyPoints: Int?,
    val workFinished: Long?,
    val daysInProgress: Int?,
    val status: String?
)

data class AssigneeIssues(
    val sprint: Sprint,
    val assignee: Assignee,
    val issues: List<SprintIssue>
)
