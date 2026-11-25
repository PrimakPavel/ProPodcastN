package com.propodcast.app.core.network.model

/** Listen Notes often sends "" instead of a missing value; models use null for "absent". */
internal fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
