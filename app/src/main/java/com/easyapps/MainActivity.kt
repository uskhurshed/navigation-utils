package com.easyapps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.easyapps.NavigationUtils.setDefaultFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setDefaultFragment(R.id.fragment_container,BlankFragment1())

    }
}