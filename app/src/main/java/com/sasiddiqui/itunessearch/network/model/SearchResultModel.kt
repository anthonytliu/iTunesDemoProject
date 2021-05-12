package com.sasiddiqui.itunessearch.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by shahrukhamd on 04/06/18.
 */
class SearchResultModel {
    @SerializedName("resultCount")
    @Expose
    var resultCount = 0

    @SerializedName("results")
    @Expose
    var resultModels: List<ResultModel>? = null

}