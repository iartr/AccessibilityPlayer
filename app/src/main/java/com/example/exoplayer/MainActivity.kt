package com.example.exoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView

class MainActivity : AppCompatActivity() {
    private lateinit var fragmentContainer: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentContainer = findViewById(R.id.fragment_container_v)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, MainFragment())
                .commit()
        }
    }

    fun getContainerId() = fragmentContainer.id
}