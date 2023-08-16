package com.example.firetvwelcomevids

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.PDFView

class PDFFragment : Fragment() {
    private lateinit var someView: View // Replace with your actual view reference
    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_pdf, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views

        pdfView = pdfView.findViewById(R.id.idPDFView)

        var pdfUrl: String? = null;

        val movie = requireActivity().intent.getSerializableExtra(DetailsActivity.MOVIE, Movie::class.java) as Movie
        if (movie.studio == "pdf") {
            pdfView = pdfView.findViewById(R.id.idPDFView)
            pdfUrl = movie.videoUrl
            pdfView.fromAsset(pdfUrl)
                .load()
        }
//        someView = view.findViewById(R.id.someView) // Replace with your actual view ID

        // Now you can interact with someView or other views
    }

}