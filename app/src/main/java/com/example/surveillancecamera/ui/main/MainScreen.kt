package com.example.surveillancecamera.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.surveillancecamera.data.CameraRepository
import com.example.surveillancecamera.theme.SurveillanceCameraTheme
import androidx.compose.runtime.LaunchedEffect
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(CameraRepository()) },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
      viewModel.fetchLatestImage()
  }

  when (val currentState = state) {
    MainScreenUiState.Loading -> {
      Text("画像取得中...")
    }
    is MainScreenUiState.Success -> {
      val bitmap = BitmapFactory.decodeByteArray(
          currentState.image,
          0,
          currentState.image.size
      )

      Column(modifier){
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = "温室カメラ",
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentScale = ContentScale.Fit
        )

        val dateTime = LocalDateTime.parse(
            currentState.fileName.removeSuffix(".jpg"),
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        )

        Text(
            text = "撮影日時： ${dateTime.format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
            )}",
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = { viewModel.fetchLatestImage() },
            modifier = Modifier.padding(16.dp)
        ){
            Text("更新")
        }
      }
    }
    is MainScreenUiState.Error -> {
      Text(
          "Error: ${currentState.throwable::class.simpleName}\n" +
          "${currentState.throwable}"
      )
    }
  }
}

@Composable
internal fun MainScreen(data: List<String>, modifier: Modifier = Modifier) {
  Column(modifier) { data.forEach { Greeting(it) } }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  SurveillanceCameraTheme { MainScreen(listOf("Android")) }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
fun MainScreenPortraitPreview() {
  SurveillanceCameraTheme { MainScreen(listOf("Android")) }
}
