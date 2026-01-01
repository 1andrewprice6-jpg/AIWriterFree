package com.aiwriter.free

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WebSearchService {
    
    companion object {
        private const val TAG = "WebSearchService"
        private const val SEARCH_API = "https://api.duckduckgo.com/"
        private const val USER_AGENT = "Mozilla/5.0 (Android) AIWriterFree/1.0"
    }
    
    data class SearchResult(
        val title: String,
        val snippet: String,
        val url: String
    )
    
    suspend fun search(query: String, maxResults: Int = 5): List<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "${SEARCH_API}?q=$encodedQuery&format=json&no_redirect=1"
            
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                parseSearchResults(response, maxResults)
            } else {
                Log.e(TAG, "Search failed with code: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search error", e)
            emptyList()
        }
    }
    
    private fun parseSearchResults(jsonResponse: String, maxResults: Int): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            val json = JSONObject(jsonResponse)
            
            // DuckDuckGo Instant Answer
            val abstract = json.optString("AbstractText", "")
            val abstractUrl = json.optString("AbstractURL", "")
            if (abstract.isNotEmpty()) {
                results.add(SearchResult(
                    title = json.optString("Heading", ""),
                    snippet = abstract,
                    url = abstractUrl
                ))
            }
            
            // Related topics
            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null) {
                for (i in 0 until minOf(relatedTopics.length(), maxResults - results.size)) {
                    val topic = relatedTopics.getJSONObject(i)
                    val text = topic.optString("Text", "")
                    val firstURL = topic.optString("FirstURL", "")
                    
                    if (text.isNotEmpty()) {
                        results.add(SearchResult(
                            title = text.take(60),
                            snippet = text,
                            url = firstURL
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error", e)
        }
        
        return results.take(maxResults)
    }
    
    suspend fun fetchPageContent(url: String): String = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val content = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                extractTextFromHtml(content)
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch error", e)
            ""
        }
    }
    
    private fun extractTextFromHtml(html: String): String {
        // Simple HTML tag removal - in production use jsoup
        return html
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(2000) // Limit to 2000 chars
    }
    
    fun extractKeywords(text: String): List<String> {
        // Simple keyword extraction - finds capitalized words and important terms
        val words = text.split(Regex("\\s+"))
        return words
            .filter { it.length > 3 }
            .filter { word -> 
                word[0].isUpperCase() || 
                word.matches(Regex("\\d{4}")) || // Years
                word.contains("@") // Emails/handles
            }
            .distinct()
            .take(5)
    }
}
