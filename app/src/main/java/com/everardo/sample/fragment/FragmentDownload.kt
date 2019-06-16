package com.everardo.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.everardo.sample.DownloaderProvider
import com.everardo.sample.R
import com.everardo.sample.viewmodel.DownloadViewModel
import com.everardo.sample.viewmodel.DownloadViewModelFactory
import kotlinx.android.synthetic.main.fragment_download.*

class FragmentDownload: Fragment() {

    private lateinit var viewModel: DownloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = DownloadViewModelFactory(activity!!.application as DownloaderProvider)

        viewModel = ViewModelProviders.of(this, factory).get(DownloadViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        download_btn.setOnClickListener {
            viewModel.download(file_url_text.text.toString())
        }

        viewModel.getComplete().observe(this, Observer {
            it?.let {
                Toast.makeText(this@FragmentDownload.context, "status = ${it.status}", Toast.LENGTH_LONG)
            }
        })
    }
}