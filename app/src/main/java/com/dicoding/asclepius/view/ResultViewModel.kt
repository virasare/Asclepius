package com.dicoding.asclepius.view

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ResultViewModel : ViewModel() {

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val _resultText = MutableLiveData<String>()
    val resultText: LiveData<String> = _resultText

    private val _confidenceScore = MutableLiveData<Float>()
    val confidenceScore: LiveData<Float> = _confidenceScore

    fun setData(imageUriString: String?, result: String?, confidence: Float?) {
        _imageUri.value = Uri.parse(imageUriString)
        _resultText.value = result ?: "No result"
        _confidenceScore.value = confidence ?: -1f
    }

    fun getResultWithConfidence(): String {
        val confidence = _confidenceScore.value
        return if (confidence != null && confidence != -1f) {
            val confidencePercentage = (confidence * 100).toInt()
            "Result: ${_resultText.value}, Confidence: $confidencePercentage%"
        } else {
            _resultText.value ?: "No result"
        }
    }
}
