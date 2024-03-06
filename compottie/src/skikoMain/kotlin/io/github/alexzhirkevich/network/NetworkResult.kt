package io.github.alexzhirkevich.network

sealed interface NetworkResult {
    data class Success<T>(val data: T) : NetworkResult
    data class Error(val exception: Exception) : NetworkResult
}