package com.example.surveillancecamera.ui.imagelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surveillancecamera.data.CameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageListViewModel(
    private val cameraRepository: CameraRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ImageListUiState>(ImageListUiState.Loading)

    val uiState: StateFlow<ImageListUiState> =
        _uiState.asStateFlow()

    fun fetchImages() {
        viewModelScope.launch {
            _uiState.value = ImageListUiState.Loading

            try {
                val fileNames = cameraRepository.fetchImageList()

                _uiState.value = ImageListUiState.Success(
                    images = fileNames
                )
            } catch (e: Exception) {
                _uiState.value = ImageListUiState.Error(e)
            }
        }
    }
}

sealed interface ImageListUiState {

    data object Loading : ImageListUiState

    data class Success(
        val images: List<String>
    ) : ImageListUiState

    data class Error(
        val throwable: Throwable
    ) : ImageListUiState
}
