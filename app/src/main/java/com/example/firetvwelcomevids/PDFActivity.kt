package com.example.firetvwelcomevids
import android.os.Build
import android.os.Bundle
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
// ...



class PDFActivity : FragmentActivity() {
    var pdfView: PDFView? = null;
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        pdfView = findViewById(R.id.pdfView)
//        val movie = intent.getSerializableExtra(DetailsActivity.MOVIE, Movie::class.java)
        val movie = intent.getSerializableExtra(DetailsActivity.MOVIE) as Movie?
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
        val url = URL(pdfUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val inputStream = BufferedInputStream(connection.inputStream)
        return inputStream.readBytes()
    }
    private fun displayPdf(pdfBytes: ByteArray) {
        pdfView?.fromBytes(pdfBytes)?.load()
    }

    companion object {
        private const val TAG = "PdfActivity"
    }
}
