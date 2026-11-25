package com.propodcast.app.core.testing

import com.propodcast.app.core.model.Podcast

fun testPodcast(id: String, title: String = "Podcast $id") = Podcast(
    id = id,
    title = title,
    publisher = "Publisher",
    imageUrl = null,
    thumbnailUrl = null,
    description = "",
    totalEpisodes = 10,
    listennotesUrl = "https://www.listennotes.com/c/$id/",
)
