package com.sasiddiqui.itunessearch.network.api

import com.sasiddiqui.itunessearch.network.model.SearchResultModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by shahrukhamd on 04/06/18.
 */
interface SearchService {
    @GET("search")
    fun getSearchResults(
            @Query("term") searchTerm: CharSequence?,
            @Query("entity") entityType: String?
    ): Call<SearchResultModel?>?

    companion object {
        const val ENTITY_TYPE_MUSIC_TRACK = "musicTrack"
    }
}