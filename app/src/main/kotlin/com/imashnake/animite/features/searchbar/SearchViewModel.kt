package com.imashnake.animite.features.searchbar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imashnake.animite.api.anilist.type.MediaType
import com.imashnake.animite.core.Constants
import com.imashnake.animite.core.data.Resource
import com.imashnake.animite.features.searchbar.domain.MergeSearchResultsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: MergeSearchResultsUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mediaType = savedStateHandle.getStateFlow<MediaType?>(Constants.MEDIA_TYPE, null)
    private val query = savedStateHandle.getStateFlow<String?>(QUERY, null)

    val searchList = mediaType
        .filterNotNull()
        .combine(query, ::Pair)
        .transformLatest { (media, query) ->
            if (!query.isNullOrEmpty() && query.length >= MIN_SEARCH_LENGTH) {
                emit(searchUseCase(media, query))
            } else {
                emit(Resource.loading())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), Resource.loading())

    fun setMediaType(mediaType: MediaType) {
        savedStateHandle[Constants.MEDIA_TYPE] = mediaType
    }

    fun setQuery(query: String?) {
        savedStateHandle[QUERY] = query
    }

    companion object {
        private const val QUERY = "query"
        private const val MIN_SEARCH_LENGTH = 3
    }
}
