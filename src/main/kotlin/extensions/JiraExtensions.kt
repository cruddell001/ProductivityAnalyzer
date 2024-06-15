package extensions

import com.ruddell.extensions.toJson
import models.*
import com.ruddell.utils.TeamMember
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

data class DayInProgressCalc(
    val days: Int?,
    val workBegan: JiraHistory?,
    val workEnded: JiraHistory?
)

fun SprintIssue.daysInProgress(): DayInProgressCalc? {
    val histories = changelog?.histories?.reversed() ?: return null
    var usedAltMethod = false
    val indexMovedToCodeReview =
        histories
            .indexOfFirst { it.items?.any { it.inCodeReview() } ?: false }
            .takeIf { it >= 0 }
        ?: histories
            .indexOfLast { it.items?.any { it.endedStatus("In Progress") } ?: false }
            .takeIf { it >= 0 }
            ?.also { usedAltMethod = true }
        ?: return null

    val movedToCodeReview: JiraHistory = histories[indexMovedToCodeReview]

    val previousStatus = movedToCodeReview.items?.firstOrNull { it.field == "status" }?.fromString ?: "Open"
    var startedWorkHistoryItem: JiraHistory? = null
    histories.take(indexMovedToCodeReview + 1).reversed().forEach {
        if (it.items?.any { it.beganStatus(previousStatus) } == true) {
            startedWorkHistoryItem = startedWorkHistoryItem ?: it
            return@forEach
        }
    }
    if (usedAltMethod) {
        bcprint("Issue $key has an indexMovedToCodeReview of $indexMovedToCodeReview, corresponding to ${movedToCodeReview.toJson()}")
        bcprint("Isssue $key has a previousStatus of $previousStatus from ${startedWorkHistoryItem.toJson()}")
    }

    if (startedWorkHistoryItem == null) {
        bcprint("****Unable to find start date for $key, so looking for date it moved into the sprint")
        histories.firstOrNull { history ->
            true == history.items?.any { it.field == "Sprint" && true == it.toString?.contains("MOB: ") }
        }?.let {
            startedWorkHistoryItem = it
        }
    }
    val startedWork = startedWorkHistoryItem?.created?.parseJiraDate()
    if (startedWork == null) {
        bcprint("*******No start date found for $key (${fields.title ?: ""}) - previousStatus: $previousStatus - indexMovedToCodeReview: $indexMovedToCodeReview - histories: ${histories.size}")
        return null
    }
    val endedWork = movedToCodeReview.created?.parseJiraDate()
    if (endedWork == null) {
        bcprint("*******No end date found for $key (${fields.title ?: ""})")
        return null
    }
    val diff = endedWork.time - startedWork.time
    val daysInProgress = (diff / (1000 * 60 * 60 * 24).toDouble()).let {
        ceil(it).roundToInt()
    }
    if (usedAltMethod) {
        bcprint("Issue $key has a diff of $diff, which is $daysInProgress days, calculated from $startedWork to $endedWork")
    }
    if (daysInProgress <= 0 || daysInProgress > 30) {
        bcprint("*******Weird calculation alert: $daysInProgress days in progress for $key (${fields.title ?: ""}), startedWork: $startedWork, endedWork: $endedWork, diff:$diff")
        bcprint(startedWorkHistoryItem?.toJson() ?: "")
        bcprint("-->")
        bcprint(movedToCodeReview.toJson() ?: "")
        bcprint("Histories backward from code review:")
        histories.take(indexMovedToCodeReview + 1).reversed().forEach {
            bcprint("History: ${it.created}: ${it.items?.firstOrNull { it.field == "status" }?.let { "${it.fromString} -> ${it.toString}" }}: ${it.items?.any { it.beganStatus(previousStatus) } == true}")
        }
    }
    return (1 + daysInProgress).takeIf { it > 0 }?.let {
        DayInProgressCalc(it, startedWorkHistoryItem, movedToCodeReview)
    }
}

fun JiraHistoryItem.inCodeReview(): Boolean = field == "status" && toString == "Code Review"
fun JiraHistoryItem.beganStatus(status: String): Boolean = field == "status" && toString == status
fun JiraHistoryItem.endedStatus(status: String): Boolean = field == "status" && fromString == status

fun JiraHistory.created(): Date? = created?.parseWithFormats()

fun String.jiraDate(): Date? = try { SimpleDateFormat("yyyy-MM-dd").parse(this.split("T").first()) }
catch (e: Exception) { e.printStackTrace(); null }

fun Sprint.inflate() = SprintWithDates(
    id, self, state, name, startDate?.jiraDate(), endDate?.jiraDate(), completeDate?.jiraDate()
)

val testableIssueTypes = listOf("Bug", "Task", "Story")
fun SprintIssue.testable(): Boolean = testableIssueTypes.contains(this.fields.issuetype?.name ?: "") && testers().isNotEmpty()

fun SprintIssue.testers(): List<String> {
    val author = this.fields.assignee?.emailAddress ?: ""
    val history = this
        .changelog
        ?.histories
        ?.filter { (it.items?.any {
            it.field == "status" && (it.toString == "Demo" || it.toString == "In Testing")
        } ?: false) && it.author?.emailAddress != author }
    val commenters = this.fields.comment?.comments?.mapNotNull { comment ->
        comment.author?.emailAddress?.takeIf { it != author && it != "it+svc-engineering@bigcommerce.com" }
    } ?: emptyList()
    val testers = history?.mapNotNull { it.author?.emailAddress }?.distinct() ?: emptyList()
    return (testers + commenters).distinct()
}

fun SprintIssue.lastUpdate(): Date? = changelog?.histories?.firstOrNull {
    it.items?.any { it.inCodeReview() } ?: false
} ?.created()

fun SprintIssueWithDaysInProgress.weekOfWorkFinished() =
    (this.workFinished?.let { Date(it) } ?: Date(0L))
        .beginningOfWeek()

fun SprintIssue.withProgress() = with(daysInProgress()) { SprintIssueWithDaysInProgress(
    accountId = fields.assignee?.accountId,
    emailAddress = fields.assignee?.emailAddress,
    displayName = fields.assignee?.displayName,
    ticketNumber = key,
    ticketType = fields.issuetype?.name,
    storyPoints = fields.storyPoints?.toInt(),
    workFinished = this?.workEnded?.created()?.time ?: lastUpdate()?.time,
    daysInProgress = this?.days,
    status = fields.status?.name
) }

fun TeamMember.assignee() = Assignee(
    emailAddress = this.user.email,
    displayName = "${user.firstName} ${user.lastName}",
    accountId = user.userId,
)

fun SprintIssueWithDaysInProgress.sprintIssue(): SprintIssue = SprintIssue(
    id = 0,
    key = ticketNumber ?: "",
    fields = IssueFields(
        assignee = Assignee(
            accountId = accountId,
            emailAddress = emailAddress,
            displayName = displayName
        ),
        issuetype = IssueType(name = ticketType ?: ""),
        storyPoints = storyPoints?.toFloat(),
        title = ticketNumber ?: "",
        updated = workFinished?.toString(),
        comment = JiraComment(
            comments = listOf(
                SprintComment(
                    author = Assignee(emailAddress = emailAddress, displayName = displayName),
                    body = "Days in progress: $daysInProgress"
                )
            )
        )
    )

)

fun List<SprintIssueWithDaysInProgress>.assigned(): AssigneeIssues {
    val week = this.firstOrNull()?.workFinished?.let { Date(it).weekString() } ?: ""
    val firstIssue = this.firstOrNull()
    val sprint = Sprint(id = 0, name = week)
    val assignee = TeamMember.values().firstOrNull { it.user.userId == firstIssue?.accountId }?.assignee() ?: Assignee()
    val issues = this.map {
        it.sprintIssue()
    }
    return AssigneeIssues(sprint, assignee, issues)
}

fun List<SprintIssueWithDaysInProgress>.averageDays(): Float =
    this.mapNotNull { it.daysInProgress?.toFloat() }
        .average()
        .toFloat()
        .roundToDigits()

fun List<SprintIssueWithDaysInProgress>.issuesFor(teamMember: TeamMember): List<SprintIssueWithDaysInProgress> =
    this.filter { it.accountId == teamMember.user.userId }

fun List<SprintIssueWithDaysInProgress>.averageDaysFor(teamMember: TeamMember): Float =
    this.issuesFor(teamMember).averageDays()

fun List<SprintIssueWithDaysInProgress>.averageDaysAcrossTeam() =
    TeamMember.entries.map {
        this.averageDaysFor(it)
    }.average().toFloat().roundToDigits()

fun List<SprintIssueWithDaysInProgress>.weeks(): List<Date> = this.map { it.weekOfWorkFinished() }.distinctBy { it.weekString() }
fun List<SprintIssueWithDaysInProgress>.weekStringss(): List<String> = this.map { it.weekOfWorkFinished().weekString() }.distinct()