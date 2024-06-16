package com.example.capstone.ui.analyze.detail

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.capstone.R
import com.example.capstone.data.ResultState
import com.example.capstone.data.api.response.DataPredict
import com.example.capstone.data.local.entity.AnalyzeHistory
import com.example.capstone.data.local.room.AnalyzeDatabase
import com.example.capstone.databinding.ActivityDetailAnalyzeBinding
import com.example.capstone.ui.SharedViewModel
import com.example.capstone.ui.ViewModelFactory
import com.example.capstone.ui.analyze.AnalyzeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailAnalyzeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailAnalyzeBinding
    private lateinit var database: AnalyzeDatabase

    private val viewModel by viewModels<AnalyzeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val sharedViewModel by viewModels<SharedViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = Room.databaseBuilder(applicationContext, AnalyzeDatabase::class.java, "riwayat_database").build()

        val analyzeResult = intent.getStringExtra("PREDICTION_RESULT")
        val nutrition = intent.getStringExtra("NUTRITION")
        val confidenceScore = intent.getStringExtra("CONFIDENCE_SCORE") ?: "0"
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        if (analyzeResult != null && imageUriString != null) {
            binding.titleTextView.text = analyzeResult
            binding.descResultTextView.text = nutrition
            binding.descScoreTextView.text = confidenceScore
            binding.previewImageView.setImageURI(Uri.parse(imageUriString))

            if (nutrition != null) {
                saveAnalyzeToDatabase(imageUriString, analyzeResult, nutrition, confidenceScore)
            }
        } else {
            showToast("Informasi tidak lengkap untuk disimpan.")
        }

        val imageUri = Uri.parse(imageUriString)
        if (imageUri != null) {
            binding.previewImageView.setImageURI(imageUri)
        } else {
            showToast("Gagal memuat gambar.")
        }

        setupAction()

    }

    private fun setupAction() {
        val detailData = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(DATA_DETAIL, DataPredict::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<DataPredict>(DATA_DETAIL)
        }

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        val nameFood = detailData?.predictedClassName?.capitalize().toString()
        imageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
        binding.descResultTextView.text = nameFood
        binding.titleTextView.text = detailData?.recommendation.toString()
        binding.descScoreTextView.text = detailData?.confidenceScore.toString()
        viewModel.getNutrition(nameFood).observe(this) { result ->
            if (result != null) {
                when (result) {
                    is ResultState.Loading -> {
                        showLoading(true)
                    }

                    is ResultState.Success -> {
                        val calories = result.data.data?.calories!!.toFloat()
                        binding.descNutritionTextView.text = calories.toString()
                        sharedViewModel.setCalories(calories)
                        showLoading(false)
                    }

                    is ResultState.Error -> {
                        showToast(result.error)
                        showLoading(false)
                    }
                }
            }

        }
    }

    private fun saveAnalyzeToDatabase(imageUri: String, analyzeResult: String, nutrition: String, confidenceScore : String) {
        val history = AnalyzeHistory(
            imageUri = imageUri,
            analyzeResult = analyzeResult,
            nutrition = nutrition,
            confidenceScore = confidenceScore.replace("%", "").toFloatOrNull() ?: 0f
        )

        CoroutineScope(Dispatchers.IO).launch {
            database.analyzeHistoryDao().insert(history)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val DATA_DETAIL = "detail"
    }
}