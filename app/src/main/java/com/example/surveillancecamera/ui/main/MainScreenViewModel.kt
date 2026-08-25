package com.example.surveillancecamera.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surveillancecamera.data.CameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val cameraRepository: CameraRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MainScreenUiState>(MainScreenUiState.Loading)

    val uiState: StateFlow<MainScreenUiState> =
        _uiState.asStateFlow()

    private var imageFileNames: List<String> = emptyList()

    private var currentIndex: Int = -1

    fun fetchLatestImage() {
        viewModelScope.launch {
            _uiState.value = MainScreenUiState.Loading

            try {
                imageFileNames = cameraRepository.fetchImageList()

                if (imageFileNames.isEmpty()) {
                    throw RuntimeException("画像がありません")
                }

                currentIndex = imageFileNames.lastIndex

                fetchCurrentImage()

            } catch (e: Exception) {
                _uiState.value = MainScreenUiState.Error(e)
            }
        }
    }

    fun showPreviousImage() {
        if (currentIndex <= 0) {
            return
        }

        currentIndex--

        viewModelScope.launch {
            try {
                fetchCurrentImage()
            } catch (e: Exception) {
                _uiState.value = MainScreenUiState.Error(e)
            }
        }
    }

    fun showNextImage() {
        if (currentIndex >= imageFileNames.lastIndex) {
            return
        }

        currentIndex++

        viewModelScope.launch {
            try {
                fetchCurrentImage()
            } catch (e: Exception) {
                _uiState.value = MainScreenUiState.Error(e)
            }
        }
    }

    private suspend fun fetchCurrentImage() {
        val fileName = imageFileNames[currentIndex]

        val result = cameraRepository.fetchImage(fileName)

        _uiState.value = MainScreenUiState.Success(
            image = result.image,
            fileName = result.fileName,
            currentIndex = currentIndex,
            imageCount = imageFileNames.size
        )
    }
}

sealed interface MainScreenUiState {

    data object Loading : MainScreenUiState

    data class Success(
        val image: ByteArray,
        val fileName: String,
        val currentIndex: Int,
        val imageCount: Int
    ) : MainScreenUiState

    data class Error(
        val throwable: Throwable
    ) : MainScreenUiState
}
