package com.example.firetvwelcomevids

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageActivity : FragmentActivity() {
    var imageView: ImageView? = null;
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        imageView = findViewById(R.id.imageView)
        val movie = intent.getSerializableExtra(MainActivity.MOVIE) as Movie?
        movie?.videoUrl?.let { loadImageFromUrl(it) }
    }
    private fun loadImageFromUrl(imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val imageBytes = fetchImageBytes(imageUrl)
            withContext(Dispatchers.Main) {
                displayImage(imageBytes)
            }
        }
    }
    private fun fetchImageBytes(imageUrl: String): ByteArray {
        val url = URL("$serverURL$vidsDir$imageUrl")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val inputStream = BufferedInputStream(connection.inputStream)
        return inputStream.readBytes()
    }
    private fun displayImage(imageBytes: ByteArray) {
        val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageView?.setImageBitmap(bmp)
//        .fromBytes(imageBytes)?.load()
    }

    companion object {
        private const val TAG = "ImageActivity"
    }
}
