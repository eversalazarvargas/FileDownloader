package com.everardo.sample.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.everardo.sample.R
import com.everardo.sample.fragment.FragmentDownload

class DemoActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, FragmentDownload())
                    .commit()
        }
    }
}