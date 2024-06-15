package models

import kotlinx.serialization.Serializable

@Serializable
data class PullRequest(
    val url: String = "",
    val html_url: String = "",
    val id: Long? = null,
    val number: Int? = null,
    val state: String = "",
    val closed_at: String = "",
    val assignee: GithubAssignee? = null,
    val review_comments_url: String? = null,
    val reviews: List<PullRequestReview>? = null,
    val timeline: List<GithubTimeline> = emptyList(),
    val repo: String? = null,
)

@Serializable
data class PrsResponse(
    val total_count: Int? = null,
    val incomplete_results: Boolean? = null,
    val items: List<PullRequest>? = null
)

@Serializable
data class GithubAssignee(
    val login: String,
    val id: Long,
    val node_id: String? = null,
)

@Serializable
data class PullRequestReview(
    val pull_request_review_id: Long,
    val id: Long,
    val user: GithubAssignee,
    val body: String,
    val pr: PullRequest? = null
)

@Serializable
data class GithubTimeline(
    val sha: String? = null,
    val author: GithubAuthor? = null,
    val message: String? = null,
    val body: String? = null,
    val event: String? = null,
    val user: GithubAssignee? = null,
    val actor: GithubAssignee? = null,
)

@Serializable
data class GithubAuthor(
    val name: String,
    val email: String,
    val date: String
)

