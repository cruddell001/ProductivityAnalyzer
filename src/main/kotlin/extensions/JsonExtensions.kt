package com.ruddell.extensions

import com.ruddell.utils.JiraHelper
import kotlinx.serialization.encodeToString

inline fun <reified T>String.fromJson(): T? = try { JiraHelper.serializer.decodeFromString(this) } catch (e: Exception) { e.printStackTrace(); null }
inline fun <reified T>T.toJson(): String? = try { JiraHelper.serializer.encodeToString(this) } catch (e: Exception) { null }
