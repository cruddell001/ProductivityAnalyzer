package com.ruddell.extensions

import com.ruddell.models.EngineeringTeam
import com.ruddell.models.User
import java.io.File

fun Array<String>.getArg(arg: String): String? {
    val lowercased = this.map { it.lowercase() }
    val key = arg.lowercase()
    val index = lowercased.indexOf(key)
    return if (index >= 0 && index < this.size - 1) {
        this[index + 1]
    } else {
        null
    }
}

fun Array<String>.loadTeamFile(): EngineeringTeam? {
    val team = this.getArg("-team") ?: this.getArg("-t") ?: return null
    val file = File(team)
    if (!file.exists()) {
        println("File not found for $team")
        return null
    }
    return file.readText().fromJson()
}
