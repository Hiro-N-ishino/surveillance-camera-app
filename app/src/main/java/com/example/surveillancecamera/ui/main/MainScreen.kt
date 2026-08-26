package com.example.surveillancecamera.ui.main

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.surveillancecamera.data.CameraRepository
import com.example.surveillancecamera.theme.SurveillanceCameraTheme
import com.example.surveillancecamera.ImageList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    selectedFileName: String?,
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(CameraRepository())
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    var hasResumedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFileName) {
        if (selectedFileName == null) {
            viewModel.fetchLatestImage()
        } else {
            viewModel.showImage(selectedFileName)
        }
    }

    DisposableEffect(lifecycleOwner, selectedFileName) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_RESUME &&
                hasResumedOnce &&
                selectedFileName == null
            ) {
                viewModel.fetchLatestImage()
            }

            if (event == Lifecycle.Event.ON_RESUME) {
                hasResumedOnce = true
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (val currentState = state) {
        MainScreenUiState.Loading -> {
            Text(
                text = "画像取得中...",
                modifier = modifier.padding(16.dp)
            )
        }

        is MainScreenUiState.Success -> {
            val bitmap = BitmapFactory.decodeByteArray(
                currentState.image,
                0,
                currentState.image.size
            )

            var scale by remember(currentState.fileName) {
                mutableFloatStateOf(1f)
            }

            var offset by remember(currentState.fileName) {
                mutableStateOf(Offset.Zero)
            }

            val dateTime = LocalDateTime.parse(
                currentState.fileName.removeSuffix(".jpg"),
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            )

            Column(
                modifier = modifier.fillMaxSize()
            ) {
                // タイトル
                Text(
                    text = "温室カメラ",
                    modifier = Modifier.padding(16.dp)
                )

                // 画像表示領域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "温室カメラ",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                            .pointerInput(currentState.fileName) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)

                                    if (scale > 1f) {
                                        offset += pan
                                    } else {
                                        offset = Offset.Zero
                                    }
                                }
                            }
                    )
                }

                // 撮影日時
                Text(
                    text = "撮影日時：${
                        dateTime.format(
                            DateTimeFormatter.ofPattern(
                                "yyyy年MM月dd日 HH:mm"
                            )
                        )
                    }",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // 現在位置
                Text(
                    text = "${currentState.currentIndex + 1} / ${currentState.imageCount}",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp)
                )

                // 最新画像を取得
                Button(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                        viewModel.fetchLatestImage()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Text("更新(最新画像)")
                }

                // 前へ / 次へ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                            viewModel.showPreviousImage()
                        },
                        enabled = currentState.currentIndex > 0
                    ) {
                        Text("前へ")
                    }

                    Button(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                            viewModel.showNextImage()
                        },
                        enabled = currentState.currentIndex <
                            currentState.imageCount - 1
                    ) {
                        Text("次へ")
                    }
                }

                Button(
                    onClick = {
                        onItemClick(ImageList)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                ) {
                    Text("画像一覧")
                }
            }
        }

        is MainScreenUiState.Error -> {
            Text(
                text = "Error: ${currentState.throwable::class.simpleName}\n" +
                    currentState.throwable,
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Composable
internal fun MainScreen(
    data: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        data.forEach { Greeting(it) }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SurveillanceCameraTheme {
        MainScreen(listOf("Android"))
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun MainScreenPortraitPreview() {
    SurveillanceCameraTheme {
        MainScreen(listOf("Android"))
    }
}
