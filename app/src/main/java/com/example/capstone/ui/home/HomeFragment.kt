package com.example.capstone.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.capstone.databinding.FragmentHomeBinding
import com.example.capstone.ui.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var progressCircularBar: Float = 0F
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("home_fragment_prefs", Context.MODE_PRIVATE)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndResetProgress()
        setupObservers()
        setupCircularProgressBar()
    }

    private fun setupObservers() {
        sharedViewModel.calories.observe(viewLifecycleOwner) { newProgress ->
            updateProgress(newProgress)
        }
    }

    private fun setupCircularProgressBar() {
        binding.circularProgressBar.apply {
            setProgressWithAnimation(progressCircularBar, 1000)
            progressMax = 300f
        }
        binding.numberCalories.text = "${progressCircularBar.toInt()}/300"
    }

    private fun checkAndResetProgress() {
        val lastUpdatedDate = sharedPreferences.getString("last_updated_date", null)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastUpdatedDate == null || lastUpdatedDate != currentDate) {
            progressCircularBar = 0f
            sharedPreferences.edit().putString("last_updated_date", currentDate).apply()
        }
    }

    fun updateProgress(newProgress: Float) {
        progressCircularBar = newProgress
        binding.circularProgressBar.setProgressWithAnimation(progressCircularBar, 1000)
        binding.numberCalories.text = "${progressCircularBar.toInt()}/300"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}