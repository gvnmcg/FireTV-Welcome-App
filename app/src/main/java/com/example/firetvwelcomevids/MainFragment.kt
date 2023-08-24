package com.example.firetvwelcomevids

import java.util.Timer
import java.util.TimerTask

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    private val mHandler = Handler(Looper.myLooper()!!)
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = null
//    private val serverReader = ServerReader(resources)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        prepareBackgroundManager()

        setupUIElements()

        loadRows()

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
        mBackgroundTimer?.cancel()
    }

    private fun prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(activity)
        mBackgroundManager.attach(activity!!.window)
        mDefaultBackground = ContextCompat.getDrawable(activity!!, R.drawable.default_background)
        mMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUIElements() {
        Log.i(TAG, "setupUIElements: ")
        title = getString(R.string.browse_title)
        // over title
        headersState = BrowseSupportFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(activity!!, R.color.fastlane_background)
        // set search icon color
//        searchAffordanceColor = ContextCompat.getColor(activity!!, R.color.search_opaque)
    }

    private fun loadRows() {
        Log.i(TAG, "loadRows: ")
        runBlocking {

            val categorizedMap: Map<String, MutableList<Movie>> =
                movieMapSFTP(resources.getString(R.string.house_number),
                            resources.getString(R.string.REMOTE_USER),
                            resources.getString(R.string.REMOTE_PASSWORD),
                            resources.getString(R.string.REMOTE_HOST),
                            resources.getString(R.string.REMOTE_PORT).toInt()
                    )

            GlobalScope.launch(Dispatchers.Main) {

                if (categorizedMap.isEmpty()) {
                    Log.e(TAG, "loadRows: EMPTY MAP", )
                } else {
                    Log.i(TAG, "loadRows: $categorizedMap")

                    var MOVIE_CATEGORY = arrayOf(
                        "welcome",
                        "indoor",
                        "outdoor",
                        "pool",
                        "hot_tub",
                        "help"
                    )

                    val fullRowsAdapter = ArrayObjectAdapter(ListRowPresenter())

                    var id = 0
                    for (cat in MOVIE_CATEGORY) {
                        id++
                        val mediaList = categorizedMap.get(cat)

                        val header = HeaderItem(
                            id.toLong(), cat.replaceFirstChar { it.uppercase() }
                                .replace("_", " "))

//                        val cardPresenter = CardPresenter()
                        val movieListAdapter = ArrayObjectAdapter(CardPresenter())
                        mediaList?.forEach { movieListAdapter.add(it) }
                        fullRowsAdapter.add(ListRow(header, movieListAdapter))
                        id++
                    }
                    Log.i(TAG, "loadRows: $fullRowsAdapter")

                    adapter = fullRowsAdapter
                }
            }
        }
    }

//    private fun setupEventListeners() {
//        Log.i(TAG, "setupEventListeners: ")
//        setOnSearchClickedListener {
//            Toast.makeText(activity!!, "Implement your own in-app search", Toast.LENGTH_LONG)
//                    .show()
//        }
//
//        onItemViewClickedListener = ItemViewClickedListener()
//        onItemViewSelectedListener = ItemViewSelectedListener()
//    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {

            if (item is Movie) {
                Log.d(TAG, "Item: $item")
                if (item.studio == "pdf") {
                    val intent = Intent(activity!!, PDFActivity::class.java)
                    intent.putExtra(MainActivity.MOVIE, item)
                    startActivity(intent)
                } else if (item.studio == "png") {
                    val intent = Intent(activity!!, ImageActivity::class.java)
                    intent.putExtra(MainActivity.MOVIE, item)
                    startActivity(intent)
                } else {
                    val intent = Intent(activity!!, PlaybackActivity::class.java)
                    intent.putExtra(MainActivity.MOVIE, item)
                    startActivity(intent)
                }

            }
            else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(activity!!, BrowseErrorActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(activity!!, item, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Movie) {
                mBackgroundUri = item.backgroundImageUrl
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(activity!!)
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into<SimpleTarget<Drawable>>(
                        object : SimpleTarget<Drawable>(width, height) {
                            override fun onResourceReady(drawable: Drawable,
                                                         transition: Transition<in Drawable>?) {
                                mBackgroundManager.drawable = drawable
                            }
                        })
        mBackgroundTimer?.cancel()
    }

    private fun startBackgroundTimer() {
        mBackgroundTimer?.cancel()
        mBackgroundTimer = Timer()
        mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            mHandler.post { updateBackground(mBackgroundUri) }
        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    companion object {
        val TAG = "MainFragment"

        private val BACKGROUND_UPDATE_DELAY = 300
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
    }
}