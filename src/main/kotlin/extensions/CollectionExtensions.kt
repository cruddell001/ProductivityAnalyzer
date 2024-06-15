package com.ruddell.extensions

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
