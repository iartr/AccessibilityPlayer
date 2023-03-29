package com.example.exoplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var epilepticButton: Button
    private lateinit var switchButton: SwitchCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        epilepticButton = view.findViewById<Button>(R.id.main_button_video1).apply {
            setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(activity().getContainerId(), VideoFragment.getInstance("output", switchButton.isChecked))
                    .addToBackStack(null)
                    .commit()
            }
        }
        switchButton = view.findViewById(R.id.main_toggle)
    }

    private fun activity(): MainActivity {
        return requireActivity() as MainActivity
    }
}