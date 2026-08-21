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

    fun fetchLatestImage() {
        viewModelScope.launch {
            _uiState.value = MainScreenUiState.Loading

            try {
                val result = cameraRepository.fetchLatestImage()

                _uiState.value = MainScreenUiState.Success(
                    image = result.image,
                    fileName = result.fileName
                )
            } catch (e: Exception) {
                _uiState.value = MainScreenUiState.Error(e)
            }
        }
    }
}

sealed interface MainScreenUiState {
    data object Loading : MainScreenUiState

    data class Success(
        val image: ByteArray,
        val fileName: String
    ) : MainScreenUiState

    data class Error(
        val throwable: Throwable
    ) : MainScreenUiState
}
