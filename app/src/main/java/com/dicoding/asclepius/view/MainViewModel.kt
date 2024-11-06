package com.dicoding.asclepius.view

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> = _currentImageUri

    private val _progressVisibility = MutableLiveData<Int>()
    val progressVisibility: LiveData<Int> = _progressVisibility

    private val _classificationResult = MutableLiveData<Pair<String, Float>>()
    val classificationResult: LiveData<Pair<String, Float>> = _classificationResult

    fun classificationResult(result: String, confidence: Float) {
        _classificationResult.value = Pair(result, confidence)
    }

    fun setProgressVisibility(visibility: Int) {
        _progressVisibility.value = visibility
    }

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }
}