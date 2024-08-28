package io.github.alexzhirkevich.compottie

public interface HttpClient {
    public suspend fun get(url: String) : ByteArray
}