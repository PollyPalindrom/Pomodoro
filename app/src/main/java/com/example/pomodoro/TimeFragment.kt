package com.example.pomodoro

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.viewbinding.ViewBinding
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.TimeFragmentBinding

class TimeFragment : Fragment() {

    private lateinit var binding: TimeFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TimeFragmentBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        mainActivity.makeButtonInvisible()
        binding.timePicker.setIs24HourView(true)
        binding.setButton.setOnClickListener {
            mainActivity.getAdapter()?.addStopwatch(
                Stopwatch(
                    0,
                    (binding.timePicker.hour * 60 + binding.timePicker.minute).toLong() * 60L * 1000L,
                    isStarted = false,
                    shouldBeRestarted = false
                )
            )
            mainActivity.makeButtonVisible()
            mainActivity.supportFragmentManager.popBackStack()

        }
        mainActivity.onBackPressedDispatcher.addCallback(mainActivity,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                mainActivity.makeButtonVisible()
                mainActivity.supportFragmentManager.popBackStack()
            }
        })
    }
}