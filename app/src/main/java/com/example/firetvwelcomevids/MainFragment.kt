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
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
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
import java.net.Inet4Address
import kotlin.concurrent.timerTask

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    private val mHandler = Handler(Looper.myLooper()!!)
    private lateinit var mBackgroundManager: BackgroundManager
    private var mDefaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
//    private var mMetrics: DisplayMetrics = DisplayMetrics()
    private var mBackgroundTimer: Timer? = null
    private var mBackgroundUri: String? = "h1-bg.png"
    private var propertyId = "h1"
    private var propertyPrefs = false
//    private val serverReader = ServerReader(resources)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        // get property id from preferences
        propertyId = resources.getString(R.string.house_number)
        if (propertyPrefs){
            val prefs = requireActivity().getSharedPreferences("com.example.firetvwelcomevids", 0)
            propertyId = prefs.getString("propertyId", propertyId)!!
        }
        mBackgroundUri = "$propertyId-bg.png"

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
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
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
        searchAffordanceColor = ContextCompat.getColor(activity!!, R.color.search_opaque)
    }

    private fun loadRows() {
        Log.i(TAG, "loadRows: ")
        runBlocking {

            val browserAdapter = ArrayObjectAdapter(ListRowPresenter())
            val mediaMap: Map<String,List<Movie>> =
                getMediaMapFromList(csvMediaList(
//                    resources.getString(R.string.house_number),
                    propertyId,
                    resources.getString(R.string.REMOTE_USER),
                    resources.getString(R.string.REMOTE_PASSWORD),
                    resources.getString(R.string.REMOTE_HOST),
                    resources.getString(R.string.REMOTE_PORT).toInt()))


            GlobalScope.launch(Dispatchers.Main) {

                if (mediaMap.isEmpty()) {
                    Log.e(TAG, "loadRows: EMPTY MAP", )
                } else {
                    Log.i(TAG, "loadRows: $mediaMap")

                    var id = 0
                    for (cat in mediaMap.keys) {
                        val mediaList = mediaMap[cat]
                        Log.i(TAG, "media list:$cat -> $mediaList")

                        val cardPresenter = CardPresenter()
                        val mediaListAdapter = ArrayObjectAdapter(cardPresenter)
                        mediaList?.forEach { mediaListAdapter.add(it) }
                        
                        browserAdapter.add(ListRow(
                            HeaderItem(id.toLong(), cat),
                            mediaListAdapter))
                        id++
                    }
                    Log.i(TAG, "loadRows: $browserAdapter")
                }

                if (propertyPrefs){

                    val gridHeader = HeaderItem(mediaMap.keys.size.toLong(), "Properties")

                    val mGridPresenter = GridItemPresenter()
                    val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
                    gridRowAdapter.add(getPropertyAddress("h1"))
                    gridRowAdapter.add(getPropertyAddress("h2"))
                    gridRowAdapter.add(getPropertyAddress("h3"))
                    gridRowAdapter.add(getPropertyAddress("h4"))
                    browserAdapter.add(ListRow(gridHeader, gridRowAdapter))
                }

                adapter = browserAdapter
            }
        }
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {

            if (item is Movie) {
                Log.d(TAG, "Item: $item")

                when (item.studio)
                {
                    "pdf" -> {
                        val intent = Intent(activity!!, PDFActivity::class.java)
                        intent.putExtra(MainActivity.MOVIE, item)
                        startActivity(intent)
                    }
                    "png" -> {
                        val intent = Intent(activity!!, ImageActivity::class.java)
                        intent.putExtra(MainActivity.MOVIE, item)
                        startActivity(intent)
                    }
                    "jpg" -> {
                        val intent = Intent(activity!!, ImageActivity::class.java)
                        intent.putExtra(MainActivity.MOVIE, item)
                        startActivity(intent)
                    }
                    "youtube" -> {
                        val intent = Intent(activity!!, YoutubePlaybackActivity::class.java)
                        intent.putExtra(MainActivity.MOVIE, item)
                        startActivity(intent)
                    }
                    "mp4" -> {
                        val intent = Intent(activity!!, PlaybackActivity::class.java)
                        intent.putExtra(MainActivity.MOVIE, item)
                        startActivity(intent)
                    }
                    "back" -> {
                        requireActivity().finish()
                    }
//                    "web" -> {
//                        val intent = Intent(activity!!, WebViewActivity::class.java)
//                        intent.putExtra(MainActivity.MOVIE, item)
//                        startActivity(intent)
//                    }
                    else -> {
                        Toast.makeText(activity!!, item.title, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (item is String) {

                if (propertyPrefs){

                    val propertyCode = getPropertyCode(item)
                    val prefs = activity!!.getSharedPreferences("com.example.firetvwelcomevids", 0)
                    val editor = prefs.edit()
                    editor.putString("propertyId", propertyCode)
                    editor.apply()
                    requireActivity().finish()
                    Toast.makeText(activity!!, propertyCode, Toast.LENGTH_SHORT).show()
                }

                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(activity!!, BrowseErrorActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Movie) {
                mBackgroundUri = "${item.backgroundImageUrl}"
                startBackgroundTimer()
            }
        }
    }

    private fun updateBackground(uri: String?) {
        val width = mMetrics.widthPixels
        val height = mMetrics.heightPixels
        Glide.with(requireActivity())
                .load("$serverURL$imagesDir$uri")
                .centerCrop()
                .error(mDefaultBackground)
                .into<SimpleTarget<Drawable>>(
                        object : SimpleTarget<Drawable>(width, height) {
                            override fun onResourceReady(drawable: Drawable,
                                                         transition: Transition<in Drawable>?) {
                                mBackgroundManager.drawable = drawable
                            }
                        })
//        mBackgroundTimer?.cancel()
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
//            view.setTextColor(Color.BLACK)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
    }

    fun getPropertyAddress(code:String):String{
        return when (code) {
            "h1" -> "12804 Piru Bld SE"
            "h2" -> "12612 Piru Bld SE"
            "h3" -> "12708 Piru Bld SE"
            "h4" -> "12608 Piru Bld SE"
            else -> "Piru Bld SE"
        }
    }

    fun getPropertyCode(address: String) :String {
        return when (address) {
            "12804 Piru Bld SE" -> "h1"
            "12612 Piru Bld SE" -> "h2"
            "12708 Piru Bld SE" -> "h3"
            "12608 Piru Bld SE" -> "h4"
            else -> "h1"
        }
    }

    companion object {
        const val TAG = "MainFragment"

        private val BACKGROUND_UPDATE_DELAY = 300
        private val GRID_ITEM_WIDTH = 200
        private val GRID_ITEM_HEIGHT = 200
    }
}