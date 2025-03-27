package com.easyapps.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.easyapps.navigation.NavigationUtils.setDefaultFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setDefaultFragment(BlankFragment1())

    }
}