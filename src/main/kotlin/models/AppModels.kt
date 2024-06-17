package com.ruddell.models

import kotlinx.serialization.Serializable
import models.SprintIssue

enum class EngineeringLevel {
    SE1, SE2, Senior, LeadEngineer, TeamLead, Manager
}

@Serializable
data class User(
    val jiraEmail: String,
    val githubId: String,
    val level: EngineeringLevel,
    val jiraId: String? = null,
    val tickets: Map<String, List<SprintIssue>> = mapOf(),
    val reviewedPrs: Int = 0,
    val authoredPrs: Int = 0
)

@Serializable
data class EngineeringTeam(
    val name: String,
    val members: List<User>
)
