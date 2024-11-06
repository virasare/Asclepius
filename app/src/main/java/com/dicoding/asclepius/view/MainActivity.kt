package com.dicoding.asclepius.view

import ImageClassifierHelper
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import android.graphics.Color
import androidx.activity.viewModels
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private var currentImageUri: Uri? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            currentImageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI)
            val result = savedInstanceState.getString(KEY_CLASSIFICATION_RESULT)
            val confidence = savedInstanceState.getFloat(KEY_CLASSIFICATION_CONFIDENCE, -1f)

            result?.let {
                viewModel.classificationResult(it, confidence)
            }
        }


        viewModel.currentImageUri.observe(this) { uri ->
            currentImageUri = uri
            showImage()
        }

        viewModel.progressVisibility.observe(this) { visibility ->
            binding.progressIndicator.visibility = visibility
        }

        viewModel.classificationResult.observe(this) { result ->
            val detectedResult = result.first
            val confidenceScore = result.second

            if (confidenceScore != -1f) {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
                    putExtra(ResultActivity.EXTRA_RESULT, detectedResult)
                    putExtra(ResultActivity.EXTRA_CONFIDENCE_SCORE, confidenceScore)
                }
                startActivity(intent)
            } else {
                showToast(detectedResult)
            }
        }

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
            imageCrop(uri)
            viewModel.setCurrentImageUri(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun imageCrop(uri: Uri){
        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.blue))
            setActiveControlsWidgetColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.blue_dark
                )
            )
            setStatusBarColor(ContextCompat.getColor(this@MainActivity, R.color.blue))
            setToolbarWidgetColor(Color.WHITE)
        }

        val imageCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, "cropped_image")))
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .withOptions(options)

        imageCrop.start(this)
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageCrop = UCrop.getOutput(data!!)
            imageCrop?.let {
                currentImageUri = it
                showImage()
                viewModel.setCurrentImageUri(it)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(data!!)
            error?.let {
                Log.e("Image Cropping Error", "Failed to crop image: $error")
            }
        }
    }

    private fun analyzeImage(uri: Uri) {
        binding.progressIndicator.visibility = View.VISIBLE
        imageClassifierHelper.classifyStaticImage(uri, contentResolver)
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        binding.progressIndicator.visibility = View.GONE

        if (results.isNullOrEmpty()) {
            showToast(getString(R.string.image_classifier_failed))
            return
        }

        val topResult = results[0].categories.maxByOrNull { it.score }
        val detectedResult = topResult?.label ?: getString(R.string.image_classifier_failed)
        val confidenceScore = topResult?.score ?: -1f

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
            putExtra(ResultActivity.EXTRA_RESULT, detectedResult)
            putExtra(ResultActivity.EXTRA_CONFIDENCE_SCORE, confidenceScore)
        }
        startActivity(intent)
    }

    override fun onError(error: String) {
        binding.progressIndicator.visibility = View.GONE
        showToast(error)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_IMAGE_URI, currentImageUri)
        val classification = viewModel.classificationResult.value
        if (classification != null) {
            outState.putString(KEY_CLASSIFICATION_RESULT, classification.first)
            outState.putFloat(KEY_CLASSIFICATION_CONFIDENCE, classification.second)
        }
    }


    companion object {
        const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
        const val KEY_CLASSIFICATION_RESULT = "KEY_CLASSIFICATION_RESULT"
        const val KEY_CLASSIFICATION_CONFIDENCE = "KEY_CLASSIFICATION_CONFIDENCE"
    }

}