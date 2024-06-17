package com.ruddell

import com.ruddell.extensions.getArg
import com.ruddell.extensions.loadTeamFile
import com.ruddell.utils.GithubHelper
import com.ruddell.utils.JiraHelper
import extensions.bcprint
import kotlinx.coroutines.runBlocking
import models.SprintIssue
import utils.CliHelper
import java.io.File


fun main(args: Array<String>) {
    println("BC Performance Utility")
    println()
    val teamFile = args.getArg("-team") ?: args.getArg("-t")
    if (teamFile != null) {
        bcprint("Processing team file: $teamFile")
        if (!File(teamFile).exists()) {
            bcprint("File not found for $teamFile")
            return
        }
        val team = args.loadTeamFile()
        if (team == null) {
            bcprint("Failed to process team file.  Please make sure it is in the correct format.")
            println()
            println("Example JSON:")
            println(
                """
{
  "name": "Mobile Apps",
  "members": [
    {
      "jiraEmail": "chris.ruddell@bigcommerce.com",
      "githubId": "cruddell001",
      "level": "TeamLead"
    }
  ]
}
                """.trimIndent()
            )
            println()
            println("Note: level must be one of: SE1, SE2, Senior, LeadEngineer, TeamLead, Manager")
            return
        }

        bcprint("Running for ${team.name} Team")
        val days: Int = args.getArg("-days")?.toIntOrNull() ?: 90
        CliHelper.runForMultipleUsers(team.members, days = days)
        return
    }
    // get some user input from console or args:
    val jiraEmail: String = args.getArg("-jira")
        ?: args.getArg("-j")
        ?: Unit.let {
            println("Pro tip: use -jira <email> to skip this prompt next time")
            print("Enter email of user to search for:")
            readLine() ?: ""
        }

    val githubUser: String = args.getArg("-gh")
        ?: args.getArg("-g")
        ?: args.getArg("-GitHub")
        ?: Unit.let {
            println("Pro tip: use -gh <username> to skip this prompt next time")
            print("Enter GitHub username to search for:")
            readLine() ?: ""
        }

    val days: Int = args.getArg("-days")?.toIntOrNull()
        ?: args.getArg("-d")?.toIntOrNull()
        ?: Unit.let {
            println("Pro tip: use -days <days> to skip this prompt next time")
            print("How many days of history to evaluate? (default 90): ")
            readLine()?.toIntOrNull() ?: 90
        }

    println()
    val firstDate = GithubHelper.dateFormat(days)

    runBlocking {
        val jiraUser = JiraHelper.getUsersByEmail(listOf(jiraEmail)).firstOrNull()
        if (jiraUser == null) {
            bcprint("No Jira user found for $jiraEmail")
            return@runBlocking
        }
        val issueTypes: List<String> = listOf("Story", "Task", "Bug")
        val tickets: Map<String, List<SprintIssue>> = issueTypes.map {
            it to JiraHelper.getClosedIssuesForUser(jiraUser.userId, issueTypes = listOf(it), daysSince = days)
        }.toMap()
        val reviewed: Int = GithubHelper.prsReviewedBy(githubUser, daysSince = days)
        val authored: Int = GithubHelper.prsClosedBy(githubUser, daysSince = days)

        println("Jira Stats for ${jiraUser.firstName} ${jiraUser.lastName} since $firstDate:")
        println()

        val labelPadding = 10


        // print out jira table in format:
        // IssueType | Closed | Story Points
        val columnLength = "Story Points".length + labelPadding
        print("Issue Type".padEnd(columnLength))
        print("Closed".padEnd(columnLength))
        println("Story Points".padEnd(columnLength))
        println("-".repeat(columnLength * 3))
        issueTypes.forEach {
            val closedIssues: List<SprintIssue> = tickets[it] ?: emptyList()
            val storyPoints = closedIssues.sumOf { it.fields.storyPoints?.toInt() ?: 0 }
            print(it.padEnd(columnLength))
            print(closedIssues.size.toString().padEnd(columnLength))
            println(storyPoints.toString().padEnd(columnLength))
        }
        val totalTickets = tickets.values.flatten().size
        val totalStoryPoints = tickets.values.flatten().sumOf { it.fields.storyPoints?.toInt() ?: 0 }
        println("-".repeat(columnLength * 3))
        print("Total".padEnd(columnLength))
        print(totalTickets.toString().padEnd(columnLength))
        println(totalStoryPoints.toString().padEnd(columnLength))

        println()
        // print out Github table in format:
        // PRs Reviewed | PRs Authored
        print("PRs Reviewed".padEnd(columnLength))
        println("PRs Authored".padEnd(columnLength))
        print(reviewed.toString().padEnd(columnLength))
        println(authored.toString().padEnd(columnLength))
    }
}

