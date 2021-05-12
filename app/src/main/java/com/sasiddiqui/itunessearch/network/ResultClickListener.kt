package com.sasiddiqui.itunessearch.network

import com.sasiddiqui.itunessearch.network.model.ResultModel

/**
 * Created by shahrukhamd on 05/06/18.
 */
interface ResultClickListener {
    fun onResultItemClick(resultModel: ResultModel?)
}