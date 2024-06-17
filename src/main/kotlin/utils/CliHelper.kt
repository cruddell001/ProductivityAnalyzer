package utils

import com.ruddell.models.EngineeringLevel
import com.ruddell.models.User
import com.ruddell.utils.GithubHelper
import com.ruddell.utils.JiraHelper
import kotlinx.coroutines.runBlocking
import models.SprintIssue

object CliHelper {


    fun runForMultipleUsers(users: List<User>, days: Int = 90) {
        println("BC Performance Utility")
        println()

        println()
        val firstDate = GithubHelper.dateFormat(days)
        val issueTypes: List<String> = listOf("Story", "Task", "Bug")

        suspend fun getDetails(user: User): User {
            val jiraUser = JiraHelper.getUsersByEmail(listOf(user.jiraEmail)).firstOrNull()
            val jiraId = jiraUser?.userId
            val tickets: Map<String, List<SprintIssue>> = if (jiraId != null) {
                issueTypes.map {
                    it to JiraHelper.getClosedIssuesForUser(jiraId, issueTypes = listOf(it), daysSince = days)
                }.toMap()
            } else {
                mapOf()
            }
            val reviewed: Int = GithubHelper.prsReviewedBy(user.githubId, daysSince = days)
            val authored: Int = GithubHelper.prsClosedBy(user.githubId, daysSince = days)
            return user.copy(
                jiraId = jiraId,
                tickets = tickets,
                reviewedPrs = reviewed,
                authoredPrs = authored
            )
        }

        val labelPadding = 10

        fun columnHeader(label: String, padding: Int = labelPadding, minLength: Int = 0): Int {
            val columnSize = label.length.coerceAtLeast(minLength)
            print(label.padEnd(columnSize + padding))
            return columnSize
        }

        fun columnData(data: String, padding: Int = labelPadding, minLength: Int = 0): Int {
            val columnSize = data.length.coerceAtLeast(minLength)
            print(data.padEnd(columnSize + padding))
            return columnSize
        }

        runBlocking {
            val usersWithDetail = users.map {
                getDetails(it)
            }



            // print out jira tickets table in format:
            // Email | Level | Story | Task | Bug | Total
            println("Jira tickets closed since $firstDate:")
            println()
            var c1 = columnHeader("Email", minLength = usersWithDetail.maxOf { it.jiraEmail.length })
            var c2 = columnHeader("Level", minLength = EngineeringLevel.entries.toTypedArray().maxOf { it.name.length })
            var c3 = columnHeader("Story")
            var c4 = columnHeader("Task")
            var c5 = columnHeader("Bug")
            var c6 = columnHeader("Total")
            println()
            var tableLength = c1 + c2 + c3 + c4 + c5 + c6
            println("-".repeat(tableLength + labelPadding*6))
            usersWithDetail.forEach { user ->
                val totalTickets = user.tickets.values.flatten().size
                columnHeader(user.jiraEmail, minLength = c1)
                columnHeader(user.level.toString(), minLength = c2)
                val closedStories = user.tickets["Story"]?.size ?: 0
                columnHeader(closedStories.toString(), minLength = c3)
                val closedTasks = user.tickets["Task"]?.size ?: 0
                columnHeader(closedTasks.toString(), minLength = c4)
                val closedBugs = user.tickets["Bug"]?.size ?: 0
                columnHeader(closedBugs.toString(), minLength = c5)
                columnHeader(totalTickets.toString(), minLength = c6)
                println()
            }
            println()
            println()

            // print out story points table in format:
            // Email | Level | Story | Task | Bug | Total
            println("Story points completed since $firstDate:")
            println()
            c1 = columnHeader("Email", minLength = usersWithDetail.maxOf { it.jiraEmail.length })
            c2 = columnHeader("Level", minLength = EngineeringLevel.entries.toTypedArray().maxOf { it.name.length })
            c3 = columnHeader("Story")
            c4 = columnHeader("Task")
            c5 = columnHeader("Bug")
            c6 = columnHeader("Total")
            println()
            tableLength = c1 + c2 + c3 + c4 + c5 + c6
            println("-".repeat(tableLength + labelPadding*6))
            usersWithDetail.forEach { user ->
                val totalPoints = user.tickets.values.flatten().sumOf { it.fields.storyPoints?.toInt() ?: 0 }
                columnData(user.jiraEmail, minLength = c1)
                columnData(user.level.toString(), minLength = c2)
                val storyPoints = user.tickets["Story"]?.sumOf { it.fields.storyPoints?.toInt() ?: 0 } ?: 0
                columnData(storyPoints.toString(), minLength = c3)
                val taskPoints = user.tickets["Task"]?.sumOf { it.fields.storyPoints?.toInt() ?: 0 } ?: 0
                columnData(taskPoints.toString(), minLength = c4)
                val bugPoints = user.tickets["Bug"]?.sumOf { it.fields.storyPoints?.toInt() ?: 0 } ?: 0
                columnData(bugPoints.toString(), minLength = c5)
                columnData(totalPoints.toString(), minLength = c6)
                println()
            }




            println()
            println()
            println("Github PRs since $firstDate:")
            // print out Github table in format:
            // Email | Level | PRs Reviewed | PRs Authored
            c1 = columnHeader("Email", minLength = usersWithDetail.maxOf { it.jiraEmail.length })
            c2 = columnHeader("Level", minLength = EngineeringLevel.entries.toTypedArray().maxOf { it.name.length })
            c3 = columnHeader("PRs Reviewed")
            c4 = columnHeader("PRs Authored")
            println()
            tableLength = c1 + c2 + c3 + c4
            println("-".repeat(tableLength + labelPadding*4))

            usersWithDetail.forEach { user ->
                columnData(user.jiraEmail, minLength = c1)
                columnData(user.level.toString(), minLength = c2)
                columnData(user.reviewedPrs.toString(), minLength = c3)
                columnData(user.authoredPrs.toString(), minLength = c4)
                println()
            }
        }
    }
}