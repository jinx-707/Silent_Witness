package com.silentwitness.presentation.photocapture

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PhotoCaptureUiState(
    val tempUri: Uri? = null,
    val capturedUri: Uri? = null
)

@HiltViewModel
class PhotoCaptureViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoCaptureUiState())
    val uiState: StateFlow<PhotoCaptureUiState> = _uiState.asStateFlow()

    fun createTempImageUri(context: Context): Uri {
        val dir = File(context.cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        _uiState.value = _uiState.value.copy(tempUri = uri)
        return uri
    }

    fun setCapturedUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(capturedUri = uri ?: _uiState.value.capturedUri)
    }

    fun clearImage() {
        _uiState.value = PhotoCaptureUiState()
    }
}
