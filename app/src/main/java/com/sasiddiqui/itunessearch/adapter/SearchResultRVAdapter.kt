package com.sasiddiqui.itunessearch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.sasiddiqui.itunessearch.R
import com.sasiddiqui.itunessearch.network.ResultClickListener
import com.sasiddiqui.itunessearch.network.model.ResultModel

/**
 * Created by shahrukhamd on 04/06/18.
 */
class SearchResultRVAdapter(resultClickListener: ResultClickListener?) : RecyclerView.Adapter<SearchResultRVAdapter.ViewHolder>() {
    private var resultModelList: List<ResultModel>? = null
    private val resultClickListener: ResultClickListener?

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        @BindView(R.id.music_item_image)
        var musicCoverImage: ImageView? = null

        @BindView(R.id.music_item_title_text)
        var musicTitleText: TextView? = null

        @BindView(R.id.music_item_author_text)
        var musicAuthorText: TextView? = null
        fun bindData(resultModel: ResultModel) {
            musicTitleText!!.text = resultModel.trackName
            musicAuthorText!!.text = resultModel.artistName
            Glide.with(musicCoverImage!!.context)
                    .load(resultModel.artworkUrl100)
                    .into(musicCoverImage!!)
        }

        override fun onClick(v: View) {
            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION && resultClickListener != null) {
                resultClickListener.onResultItemClick(resultModelList!![pos])
            }
        }

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_search_result_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(resultModelList!![position])
    }

    override fun getItemCount(): Int {
        return if (resultModelList != null) resultModelList!!.size else 0
    }

    fun updateResults(resultModelList: List<ResultModel>?) {
        this.resultModelList = resultModelList
        notifyDataSetChanged()
    }

    init {
        this.resultClickListener = resultClickListener
    }
}