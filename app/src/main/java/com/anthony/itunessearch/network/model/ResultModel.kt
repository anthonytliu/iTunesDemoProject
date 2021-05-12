package com.anthony.itunessearch.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ResultModel {
    @SerializedName("artistName")
    @Expose
    var artistName: String? = null

    @SerializedName("trackName")
    @Expose
    var trackName: String? = null

    @SerializedName("previewUrl")
    @Expose
    var previewUrl: String? = null

    @SerializedName("artworkUrl100")
    @Expose
    var artworkUrl100: String? = null

}