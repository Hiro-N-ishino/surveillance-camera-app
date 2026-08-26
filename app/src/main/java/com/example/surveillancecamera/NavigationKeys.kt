package com.example.surveillancecamera

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class Main(
    val fileName: String? = null
) : NavKey

@Serializable
data object ImageList : NavKey
