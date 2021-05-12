package com.anthony.itunessearch.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.sasiddiqui.itunessearch.R
import com.anthony.itunessearch.network.api.SearchService
import com.anthony.itunessearch.network.model.SearchResultModel
import com.anthony.itunessearch.network.utils.RetrofitBuilder
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

class MainActivity : AppCompatActivity(), Callback<SearchResultModel?> {

    private var queryObservable: PublishSubject<CharSequence>? = null
    private var searchService: SearchService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
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
                            searchService?.getSearchResults(s, SearchService.ENTITY_TYPE_MUSIC_TRACK)
                                    ?.enqueue(this@MainActivity)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e)
                    }
                })
        main_search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                queryObservable?.onNext(s)
            }
        })
    }

    override fun onResponse(call: Call<SearchResultModel?>, response: Response<SearchResultModel?>) {
        main_progress_bar.visibility = View.GONE
        if (response.isSuccessful && response.body() != null) {
            val resultModelList = response.body()?.resultModels
            val output = StringBuilder()
            if (resultModelList != null) {
                if (resultModelList.isNotEmpty()) {
                            main_help_text.visibility = View.VISIBLE
                            (resultModelList.indices).forEach{
                                output.append(resultModelList[it].artistName.toString() + "\n")
                            }
                            main_help_text.text = output.toString()
                } else {
                    main_help_text.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onFailure(call: Call<SearchResultModel?>, t: Throwable) {
        main_progress_bar.visibility = View.GONE
        showError(R.string.message_network_error)
    }

    private fun showError(error: Int) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val SEARCH_TIMEOUT_MILLI = 555
    }

}