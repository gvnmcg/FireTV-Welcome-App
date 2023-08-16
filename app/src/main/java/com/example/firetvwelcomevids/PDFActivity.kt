package com.example.firetvwelcomevids

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.github.barteksc.pdfviewer.PDFView
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PdfActivity : FragmentActivity() {

    private lateinit var pdfView: PDFView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.idPDFView, PDFFragment())
                .commitNow()
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)


    }

}
