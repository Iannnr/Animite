package com.imashnake.animite.features.searchbar.domain

import com.imashnake.animite.api.anilist.AnilistSearchRepository
import com.imashnake.animite.api.anilist.type.MediaType
import com.imashnake.animite.api.mal.data.MyAnimeListSearchRepository
import com.imashnake.animite.core.data.Resource
import com.imashnake.animite.features.searchbar.domain.model.SearchResultUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class MergeSearchResultsUseCase @Inject constructor(
    private val myAnimeListSearchRepository: MyAnimeListSearchRepository,
    private val anilistSearchRepository: AnilistSearchRepository
) {

    suspend operator fun invoke(
        mediaType: MediaType,
        query: String
    ): Resource<List<SearchResultUiModel>> = coroutineScope {
        try {
            val anilistSearch = runSuspendCatching {
                async { anilistSearchRepository.fetchSearch(mediaType, query) }
                    .await()
            }.getOrDefault(listOf())

            val malSearch = runSuspendCatching {
                async { myAnimeListSearchRepository.searchAnime(query).data }
                    .await()
            }.getOrDefault(listOf())

            val merged = malSearch
                .filter { it.node.mediaType == "tv" || it.node.mediaType == "ova"  } // add more?
                .map { mal ->
                    var result = SearchResultUiModel(mal, null)

                    anilistSearch.forEach { anilist ->
                        if (!mal.node.alternativeTitles?.ja.isNullOrEmpty() && mal.node.alternativeTitles?.ja == anilist.native) {
                            result = SearchResultUiModel(anilist, mal)
                        } else if (!mal.node.title.isNullOrEmpty() && mal.node.title == anilist.title) {
                            result = SearchResultUiModel(anilist, mal)
                        }
                    }

                    return@map result
                }.distinctBy {
                    it.id
                }

            Resource.success(merged)
        } catch (e: Throwable) {
            Resource.error(e.message!!)
        }
    }

    private inline fun <T> runSuspendCatching(block: () -> List<T>): Result<List<T>> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}