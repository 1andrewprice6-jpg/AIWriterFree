package com.aiwriter.free

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RAGEngine(private val context: Context) {
    
    private val webSearch = WebSearchService()
    private val knowledgeCache = mutableMapOf<String, CachedKnowledge>()
    
    companion object {
        private const val TAG = "RAGEngine"
        private const val CACHE_EXPIRY_MS = 3600000 // 1 hour
    }
    
    data class CachedKnowledge(
        val content: String,
        val timestamp: Long,
        val sources: List<String>
    )
    
    data class EnhancedContext(
        val originalText: String,
        val webContext: String,
        val sources: List<String>
    )
    
    suspend fun enhanceWithCurrentKnowledge(
        text: String,
        requiresCurrent: Boolean = false
    ): EnhancedContext = withContext(Dispatchers.Default) {
        
        if (!requiresCurrent) {
            // If current info not required, return original
            return@withContext EnhancedContext(text, "", emptyList())
        }
        
        try {
            // Extract search query from text
            val searchQuery = extractSearchQuery(text)
            
            // Check cache first
            val cached = knowledgeCache[searchQuery]
            if (cached != null && !isCacheExpired(cached)) {
                Log.d(TAG, "Using cached knowledge for: $searchQuery")
                return@withContext EnhancedContext(text, cached.content, cached.sources)
            }
            
            // Perform web search
            val results = webSearch.search(searchQuery, maxResults = 3)
            
            if (results.isEmpty()) {
                return@withContext EnhancedContext(text, "", emptyList())
            }
            
            // Build context from search results
            val contextBuilder = StringBuilder()
            val sources = mutableListOf<String>()
            
            results.forEachIndexed { index, result ->
                contextBuilder.append("Source ${index + 1}: ${result.title}\n")
                contextBuilder.append("${result.snippet}\n\n")
                sources.add(result.url)
            }
            
            val webContext = contextBuilder.toString()
            
            // Cache the results
            knowledgeCache[searchQuery] = CachedKnowledge(
                content = webContext,
                timestamp = System.currentTimeMillis(),
                sources = sources
            )
            
            EnhancedContext(text, webContext, sources)
            
        } catch (e: Exception) {
            Log.e(TAG, "RAG enhancement failed", e)
            EnhancedContext(text, "", emptyList())
        }
    }
    
    private fun extractSearchQuery(text: String): String {
        // Extract key terms for search
        val keywords = webSearch.extractKeywords(text)
        
        // If we have good keywords, use them
        if (keywords.isNotEmpty()) {
            return keywords.joinToString(" ")
        }
        
        // Otherwise, use first meaningful sentence
        val sentences = text.split(Regex("[.!?]"))
        return sentences.firstOrNull { it.length > 10 }?.trim() ?: text.take(100)
    }
    
    private fun isCacheExpired(cached: CachedKnowledge): Boolean {
        return System.currentTimeMillis() - cached.timestamp > CACHE_EXPIRY_MS
    }
    
    fun clearCache() {
        knowledgeCache.clear()
    }
    
    fun getCacheStats(): String {
        val validCacheCount = knowledgeCache.values.count { !isCacheExpired(it) }
        return "Cached knowledge: $validCacheCount entries"
    }
}
