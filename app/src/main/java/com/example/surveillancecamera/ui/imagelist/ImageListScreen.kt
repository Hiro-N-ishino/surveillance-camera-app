package com.example.surveillancecamera.ui.imagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.surveillancecamera.data.CameraRepository
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

@Composable
fun ImageListScreen(
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImageListViewModel = viewModel {
        ImageListViewModel(CameraRepository())
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchImages()
    }

    when (val currentState = state) {
        ImageListUiState.Loading -> {
            Text(
                text = "画像一覧を取得中...",
                modifier = modifier.padding(16.dp)
            )
        }

        is ImageListUiState.Success -> {
            ImageListContent(
                fileNames = currentState.images,
                onImageClick = onImageClick,
                modifier = modifier
            )
        }

        is ImageListUiState.Error -> {
            Text(
                text = "Error: ${currentState.throwable::class.simpleName}\n" +
                    currentState.throwable,
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ImageListContent(
    fileNames: List<String>,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedImages = fileNames
        .map { fileName ->
            val dateTime = LocalDateTime.parse(
                fileName.removeSuffix(".jpg"),
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            )

            fileName to dateTime
        }
        .groupBy { (_, dateTime) ->
            dateTime.toLocalDate()
        }
        .toSortedMap(reverseOrder())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedImages.forEach { (date, images) ->
            item {
                Text(
                    text = date.format(
                        DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                    ),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        )
                )
            }

            items(
                items = images.sortedByDescending { it.second },
                key = { it.first }
            ) { (fileName, dateTime) ->

                Text(
                    text = dateTime.format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onImageClick(fileName)
                        }
                        .padding(
                            horizontal = 8.dp,
                            vertical = 12.dp
                        )
                )
            }
        }
    }
}
