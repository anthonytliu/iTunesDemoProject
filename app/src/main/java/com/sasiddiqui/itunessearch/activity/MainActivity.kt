package com.sasiddiqui.itunessearch.activity

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.BuildConfig
import butterknife.ButterKnife
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.sasiddiqui.itunessearch.R
import com.sasiddiqui.itunessearch.activity.MainActivity
import com.sasiddiqui.itunessearch.adapter.SearchResultRVAdapter
import com.sasiddiqui.itunessearch.network.ResultClickListener
import com.sasiddiqui.itunessearch.network.api.SearchService
import com.sasiddiqui.itunessearch.network.model.ResultModel
import com.sasiddiqui.itunessearch.network.model.SearchResultModel
import com.sasiddiqui.itunessearch.network.utils.RetrofitBuilder
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by shahrukhamd on 04/06/18.
 */
class MainActivity : AppCompatActivity(), Callback<SearchResultModel?>, ResultClickListener {
    @BindView(R.id.main_result_recycler_view)
    var resultRecyclerView: RecyclerView? = null

    private var player: SimpleExoPlayer? = null
    private var dataSourceFactory: DefaultHttpDataSourceFactory? = null
    private var playbackPosition: Long = 0
    private var currentMediaUrl: String? = null
    private var searchResultRVAdapter: SearchResultRVAdapter? = null
    private var queryObservable: PublishSubject<CharSequence>? = null
    private var searchService: SearchService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        init()
    }

    public override fun onStart() {
        super.onStart()
//        initExoPlayer()
    }

    override fun onStop() {
        super.onStop()
//        releasePlayer()
    }

    /**
     * Initializing the views and accompanying components.
     */
    private fun init() {
        searchService = RetrofitBuilder.getRetrofit().create(SearchService::class.java)

        // User query observer for RxJava
        queryObservable = PublishSubject.create()
        queryObservable?.debounce(SEARCH_TIMEOUT_MILLI.toLong(), TimeUnit.MILLISECONDS)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Observer<CharSequence> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onComplete() {}
                    override fun onNext(s: CharSequence) {
                        if (s.isNotEmpty()) {
                            main_progress_bar.visibility = View.VISIBLE
//                            player?.stop()
                            searchService?.getSearchResults(s, SearchService.ENTITY_TYPE_MUSIC_TRACK)
                                    ?.enqueue(this@MainActivity)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e)
//                        player!!.stop()
                    }
                })
        main_search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                queryObservable?.onNext(s)
            }
        })

        // Search result recycler view
        val layoutManager: StaggeredGridLayoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            StaggeredGridLayoutManager(SPAN_COUNT_PORT, StaggeredGridLayoutManager.VERTICAL)
        } else {
            StaggeredGridLayoutManager(SPAN_COUNT_LAND, StaggeredGridLayoutManager.VERTICAL)
        }
        searchResultRVAdapter = SearchResultRVAdapter(this)
        resultRecyclerView?.apply {
            setLayoutManager(layoutManager)
            adapter = searchResultRVAdapter
        }
    }

//    private fun initExoPlayer() {
//        dataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-" + BuildConfig.APPLICATION_ID)
//        player = ExoPlayerFactory.newSimpleInstance(
//                DefaultRenderersFactory(this),
//                DefaultTrackSelector(), DefaultLoadControl())
//        player?.playWhenReady = true
//        if (currentMediaUrl != null) {
//            player?.prepare(mediaSource)
//            player?.seekTo(playbackPosition)
//        }
//    }

    override fun onResponse(call: Call<SearchResultModel?>, response: Response<SearchResultModel?>) {
        main_progress_bar.visibility = View.GONE
        if (response.isSuccessful && response.body() != null) {
            val resultModelList = response.body()?.resultModels
            var output = StringBuilder()
            searchResultRVAdapter?.updateResults(resultModelList)
            if (resultModelList != null) {
                if (resultModelList.isNotEmpty()) {
//                    resultRecyclerView?.visibility = View.VISIBLE
                            main_help_text.visibility = View.VISIBLE
                            (resultModelList.indices).forEach{
                                output.append(resultModelList[it].artistName.toString() + "\n")
                            }
                            main_help_text.text = output.toString()
//                            main_help_text.text = response.body()?.resultModels!![0].artistName.toString()
//                            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()

                } else {
//                    resultRecyclerView?.visibility = View.GONE
                    main_help_text.visibility = View.VISIBLE
                }
            }
        } else {
            showError(R.string.message_some_error_occurred)
        }
    }

    override fun onFailure(call: Call<SearchResultModel?>, t: Throwable) {
        main_progress_bar.visibility = View.GONE
        showError(R.string.message_network_error)
    }

    /**
     * Show appropriate error message to user.
     */
    fun showError(error: Int) {
        resultRecyclerView!!.visibility = View.GONE
        main_help_text.visibility = View.VISIBLE
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onResultItemClick(resultModel: ResultModel?) {
//        player!!.stop() // Stop whatever is playing
        Toast.makeText(this, String.format(getString(R.string.message_playing_track), resultModel?.trackName), Toast.LENGTH_SHORT).show()
        currentMediaUrl = resultModel?.previewUrl
//        player!!.prepare(mediaSource, true, false)
        showTrackInfoDialog(resultModel!!)
    }

    private val mediaSource: MediaSource
        private get() = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(currentMediaUrl))

    private fun showTrackInfoDialog(resultModel: ResultModel) {
        AlertDialog.Builder(this)
                .setTitle(resultModel.trackName)
                .setMessage(resultModel.artistName)
                .setPositiveButton(R.string.text_close, null)
                .setOnDismissListener {
//                    player!!.stop()
                }.show()
    }

//    /**
//     * Release the resources used by Exo player.
//     */
//    private fun releasePlayer() {
//        if (player != null) {
//            playbackPosition = player!!.currentPosition // Storing the playback position for resume
//            player!!.release()
//            player = null
//        }
//    }

    companion object {
        private const val SEARCH_TIMEOUT_MILLI = 555
        private const val SPAN_COUNT_PORT = 3
        private const val SPAN_COUNT_LAND = 4
    }
    
}