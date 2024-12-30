package com.imashnake.animite.features.searchbar.domain.model

import com.imashnake.animite.api.anilist.sanitize.search.Search
import com.imashnake.animite.api.anilist.sanitize.search.Search.Format
import com.imashnake.animite.api.mal.data.model.PagingData

data class SearchResultUiModel(
    val id: Int,
    val resultType: SearchResultType,
    val image: String?,
    val title: String?,
    val seasonYear: Int?,
    val studios: List<String>,
    val format: Format,
    val episodes: Int?,
) {

    constructor(anilist: Search, mal: PagingData?) : this(
        id = anilist.id,
        resultType = SearchResultType.ANIMITE,
        image = takeLongest(anilist.coverImage, mal?.node?.mainPicture?.medium),
        title = takeLongest(anilist.title, mal?.node?.title),
        seasonYear = anilist.seasonYear ?: mal?.node?.startSeason?.year,
        studios = takeLongest(anilist.studios, mal?.node?.studios?.map { it.name }),
        format = anilist.format,
        episodes = anilist.episodes ?: mal?.node?.numEpisodes
    )

    constructor(mal: PagingData, anilist: Search?) : this(
        id = mal.node.id,
        resultType = SearchResultType.MAL,
        image = takeLongest(anilist?.coverImage, mal.node.mainPicture?.medium),
        title = takeLongest(anilist?.title, mal.node.title),
        seasonYear = anilist?.seasonYear ?: mal.node.startSeason?.year,
        studios = takeLongest(anilist?.studios, mal.node.studios.map { it.name }),
        format = anilist?.format ?: Format.UNKNOWN,
        episodes = anilist?.episodes ?: mal.node.numEpisodes
    )

    enum class SearchResultType {
        ANIMITE,
        MAL
    }
}

private fun takeLongest(vararg values: String?): String? {
    return values.filterNotNull().maxByOrNull { it.length }
}

private fun takeLongest(anilist: List<String>?, mal: List<String?>?): List<String> {
    return maxOf(anilist, mal, compareBy { it?.size ?: 0 }).orEmpty().filterNotNull()
}
