package com.ruddell

import com.ruddell.extensions.getArg
import com.ruddell.utils.GithubHelper
import com.ruddell.utils.JiraHelper
import extensions.bcprint
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    println("BC Performance Utility")
    println()
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
        val closedIssues = JiraHelper.getClosedIssuesForUser(jiraUser.userId, daysSince = days)
        val storyPoints = closedIssues.sumOf { it.fields.storyPoints?.toInt() ?: 0 }
        val reviewed: Int = GithubHelper.prsReviewedBy(githubUser)
        val authored: Int = GithubHelper.prsClosedBy(githubUser)

        println("Stats for ${jiraUser.firstName} ${jiraUser.lastName} since $firstDate:")
        val jiraClosedLabel = "Tickets Closed:"
        val storyPointsLabel = "Story Points:"
        val prReviewedLabel = "PRs Reviewed:"
        val prAuthoredLable = "PRs Authored:"
        val labelLength = maxOf(jiraClosedLabel.length, storyPointsLabel.length, prReviewedLabel.length, prAuthoredLable.length)
        val labelPadding = 10
        println(jiraClosedLabel.padEnd(labelLength + labelPadding) + closedIssues.size)
        println(storyPointsLabel.padEnd(labelLength + labelPadding) + storyPoints)
        println(prReviewedLabel.padEnd(labelLength + labelPadding) + reviewed)
        println(prAuthoredLable.padEnd(labelLength + labelPadding) + authored)
    }
}

