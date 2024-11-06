package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val resultText = intent.getStringExtra(EXTRA_RESULT)

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            binding.resultImage.setImageURI(imageUri)
        }

        val displayResultText = resultText ?: getString(R.string.no_result)

        val confidenceScore = intent.getFloatExtra(EXTRA_CONFIDENCE_SCORE, -1f)

        if (confidenceScore != -1f) {
            val resultWithConfidence = getString(R.string.result_with_confidence, displayResultText, confidenceScore)
            binding.resultText.text = resultWithConfidence
        } else {
            binding.resultText.text = displayResultText
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_CONFIDENCE_SCORE = "extra_confidence_score"
    }
}
