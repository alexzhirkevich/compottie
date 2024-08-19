package io.github.alexzhirkevich.compottie.network

public interface HttpClient {
    public suspend fun get(url: String) : ByteArray
}