package com.example.firetvwelcomevids
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class PDFActivity : FragmentActivity() {
    private lateinit var pdfView: PDFView;
    private var zoomed = false;
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        pdfView = findViewById(R.id.pdfView)

        pdfView.minZoom = 0.47f
        pdfView.maxZoom = 1.3f

        Toast.makeText(this,
            "Zoom: Center Button \n" +
                    "Page: Left & Right  \n" +
                    "Scroll: Up & Down ",
            Toast.LENGTH_LONG)
            .show()

        val movie = intent.getSerializableExtra(MainActivity.MOVIE) as Movie?
        Log.i(TAG, "onCreate: video slug ${movie?.videoUrl}")
        movie?.videoUrl?.let { loadPdfFromUrl(it) }
    }
    private fun loadPdfFromUrl(pdfUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val pdfBytes = fetchPdfBytes(pdfUrl)
            withContext(Dispatchers.Main) {
                displayPdf(pdfBytes)
            }
        }
    }
    private fun fetchPdfBytes(pdfUrl: String): ByteArray {

        val url = URL("$serverURL$vidsDir$pdfUrl")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val inputStream = BufferedInputStream(connection.inputStream)
        return inputStream.readBytes()
    }
    private fun displayPdf(pdfBytes: ByteArray) {
        pdfView.useBestQuality(true)
        pdfView.fromBytes(pdfBytes)?.load()
        pdfView.zoomTo(pdfView.minZoom)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //Back
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }

        var handled = false;
        //Zoom
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (zoomed) {
                pdfView.fitToWidth(pdfView.currentPage)
                pdfView.jumpTo(pdfView.currentPage)
                zoomed = false
            } else {
                pdfView.zoomTo(pdfView.minZoom)
                pdfView.jumpTo(pdfView.currentPage)
                zoomed = true
            }
            handled = true
        }

        //Pages
        if ((keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) ) {
            pdfView.jumpTo(pdfView.currentPage.plus(1))
            pdfView.scrollY = 0
            handled = true
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            pdfView.jumpTo(pdfView.currentPage.minus(1))
            pdfView.scrollY = 0
            handled = true
        }

        //Scroll
        if ( keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            pdfView.scrollY = pdfView.scrollY.plus(50) as Int
            handled = true
        }
        if ( keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            pdfView.scrollY = pdfView.scrollY.minus(50) as Int
            handled = true
        }

        if (handled)
            Toast.makeText(this,
                        "Page: ${pdfView.currentPage + 1} / ${pdfView.pageCount}" ,
                Toast.LENGTH_SHORT)
                .show()

        return handled
    }

    companion object {
        private const val TAG = "PdfActivity"
    }
}
