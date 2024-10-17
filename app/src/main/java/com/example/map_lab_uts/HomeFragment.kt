package com.example.map_lab_uts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dayDateView: TextView
    private lateinit var clockView: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dayDateView = view.findViewById(R.id.day_date_view)
        clockView = view.findViewById(R.id.clock_view)

        updateDateTime()

        val bigRoundButton = view.findViewById<Button>(R.id.big_round_button)
        bigRoundButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateDateTime() {
        val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val currentDate = Date()
        dayDateView.text = dateFormat.format(currentDate)
        clockView.text = timeFormat.format(currentDate)

        // Update the time every second
        handler.postDelayed({ updateDateTime() }, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}