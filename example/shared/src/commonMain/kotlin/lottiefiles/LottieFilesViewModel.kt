package lottiefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.ioDispatcher
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class LottieFilesViewModel() : ViewModel() {

    private val httpClient = HttpClient {
        expectSuccess = true
        install(HttpRequestRetry){
            maxRetries = 3
            exponentialDelay()
        }
    }

    private val _page = MutableStateFlow(1)
    val page = _page.asStateFlow()

    private val _pageCount = MutableStateFlow(1)
    val pageCount = _pageCount.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.Popular)
    val sortOrder = _sortOrder.asStateFlow()

    private val _search = MutableStateFlow("")
    val search = _search.asStateFlow()

    private val _files = MutableStateFlow<List<LottieFile>>(emptyList())
    val files: StateFlow<List<LottieFile>> = _files.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions.asStateFlow()

    private val _selectedFile = MutableStateFlow<LottieFile?>(null)
    val selectedFile: StateFlow<LottieFile?> = _selectedFile.asStateFlow()

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private var prevSearch : String? = null

    init {
        @OptIn(InternalCompottieApi::class)
        viewModelScope.launch(ioDispatcher()) {
            combine(search.debounce(1000), sortOrder, page) { q, s, p ->
                Triple(q, s, p)
            }.collectLatest { (q, s, p) ->
                try {
                    val resp = httpClient.get(
                        "https://lottiefiles.com/api/search/get-animations"
                    ) {
                        parameter("query", q)
                        parameter("type", "free")
                        parameter("sort", s.name.lowercase())
                        parameter("page", p)
                    }.bodyAsText().let {
                        json.decodeFromString<JsonObject>(it)
                    }

                    val files = resp
                        .get("data")!!
                        .jsonObject
                        .get("data")!!


                    _files.value = json.decodeFromJsonElement<List<LottieFile>>(files)
                    _pageCount.value = resp.get("originalPageCount")?.jsonPrimitive?.intOrNull ?: 0
                    _page.value = if (prevSearch == q) p else 1
                    prevSearch = q
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }

        @OptIn(InternalCompottieApi::class)
        viewModelScope.launch(ioDispatcher()) {
            search.debounce(1000)
                .collectLatest { s ->
                    if (s.isBlank()){
                        _suggestions.value = emptyList()
                    } else {
                        try {
                            val resp = httpClient.get(
                                "https://lottiefiles.com/api/search"
                            ) {
                                parameter("query", s)
                            }.bodyAsText()

                            _suggestions.value = json.decodeFromString<List<Suggestion>>(resp)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                }
        }
    }

    fun onSortOrderChanged(sortOrder: SortOrder){
        _sortOrder.value = sortOrder
    }

    fun onSearch(query: String) {
        _search.value = query
        if (query.isBlank()){
            _files.value = emptyList()
            _pageCount.value = 1
        }
    }

    fun onPageSelected(page : Int) {
        _page.value = page
    }

    fun onFileSelected(file: LottieFile?){
        _selectedFile.value = file
    }
}


