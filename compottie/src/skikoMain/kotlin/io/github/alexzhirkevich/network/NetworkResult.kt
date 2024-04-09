package io.github.alexzhirkevich.network

sealed interface NetworkResult {
    data class Success(val data: String) : NetworkResult
    data class Error(val exception: Exception) : NetworkResult
}