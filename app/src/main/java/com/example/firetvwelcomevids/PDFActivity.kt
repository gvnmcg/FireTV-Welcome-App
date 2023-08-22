package com.example.firetvwelcomevids
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.github.barteksc.pdfviewer.PDFView


class PDFViewerActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        val pdfView: PDFView = findViewById(R.id.pdfView)

        val movie = intent.getSerializableExtra(DetailsActivity.MOVIE, Movie::class.java)
        // Load a PDF file from a file path, URL, or other sources
        pdfView.fromAsset(movie?.videoUrl) // Change to your PDF file source
            .defaultPage(0)
            .spacing(10)
            .load()
    }
}
