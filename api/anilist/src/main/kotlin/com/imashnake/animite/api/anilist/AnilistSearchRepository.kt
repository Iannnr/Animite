package com.imashnake.animite.api.anilist

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.imashnake.animite.api.anilist.sanitize.search.Search
import com.imashnake.animite.api.anilist.type.MediaType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for fetching media search results (e.g., search bar).
 *
 * @param apolloClient Default apollo client.
 * @property fetchSearch Fetch a list of `search`es.
 */
class AnilistSearchRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {

    suspend fun fetchSearch(
        type: MediaType,
        search: String
    ): List<Search> {
        return apolloClient
            .query(
                SearchQuery(
                    type = Optional.presentIfNotNull(type),
                    perPage = Optional.presentIfNotNull(50),
                    search = Optional.presentIfNotNull(search)
                )
            )
            .fetchPolicy(FetchPolicy.CacheFirst)
            .execute()
            .dataAssertNoErrors
            .page
            ?.media
            ?.filterNotNull()
            ?.map { query -> Search(query) }
            .orEmpty()
    }
}
