package com.aiwriter.free

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.LongBuffer

class LocalAIEngine(private val context: Context) {
    
    private var ortEnvironment: OrtEnvironment? = null
    private var session: OrtSession? = null
    private val ragEngine = RAGEngine(context)
    private val formatter = TextFormatterService()
    private val modelPath: String
        get() = "${context.filesDir}/qwen2.5-1.5b-q4.onnx"
    
    companion object {
        private const val TAG = "LocalAIEngine"
        private const val MAX_LENGTH = 512
        private const val MODEL_URL = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-ONNX/resolve/main/qwen2.5-1.5b-instruct-q4.onnx"
    }
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!File(modelPath).exists()) {
                Log.d(TAG, "Model not found, needs download")
                return@withContext false
            }
            
            ortEnvironment = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.addNnapi()  // Use Android NNAPI for hardware acceleration
            
            session = ortEnvironment?.createSession(modelPath, sessionOptions)
            Log.d(TAG, "Model initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model", e)
            false
        }
    }
    
    suspend fun processText(
        text: String,
        task: TextTask,
        useWebSearch: Boolean = true
    ): ProcessResult = withContext(Dispatchers.Default) {
        try {
            // Check if this is a formatting task (doesn't need AI)
            if (isFormattingTask(task)) {
                val formatted = applyFormatting(text, task)
                return@withContext ProcessResult(formatted, emptyList(), false)
            }
            
            // Check if model is loaded for AI tasks
            if (session == null || ortEnvironment == null) {
                // Try to initialize if not already done
                val initialized = initialize()
                if (!initialized) {
                    return@withContext ProcessResult(
                        "Error: AI model not loaded. Please download the model first from the main app.",
                        emptyList(),
                        false
                    )
                }
            }
            
            // Determine if task requires current knowledge
            val requiresCurrent = when (task) {
                TextTask.REWRITE_WITH_RESEARCH,
                TextTask.EXPAND_WITH_FACTS,
                TextTask.UPDATE_WITH_CURRENT,
                TextTask.FACT_CHECK,
                TextTask.ADD_CITATIONS -> true
                else -> false
            }
            
            // Enhance with web context if needed
            val enhancedContext = if (requiresCurrent && useWebSearch) {
                ragEngine.enhanceWithCurrentKnowledge(text, requiresCurrent = true)
            } else {
                RAGEngine.EnhancedContext(text, "", emptyList())
            }
            
            // Build prompt with optional web context
            val prompt = buildPrompt(text, task, enhancedContext.webContext)
            val response = generateText(prompt)
            val result = extractResponse(response, task)
            
            ProcessResult(
                text = result,
                sources = enhancedContext.sources,
                usedWebSearch = enhancedContext.webContext.isNotEmpty()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing text", e)
            ProcessResult("Error: ${e.message}", emptyList(), false)
        }
    }
    
    private fun isFormattingTask(task: TextTask): Boolean {
        return when (task) {
            TextTask.FORMAT_BOLD, TextTask.FORMAT_ITALIC, TextTask.FORMAT_UNDERLINE,
            TextTask.FORMAT_HEADING_1, TextTask.FORMAT_HEADING_2,
            TextTask.FORMAT_BULLET_LIST, TextTask.FORMAT_NUMBERED_LIST, TextTask.FORMAT_QUOTE,
            TextTask.CASE_UPPER, TextTask.CASE_LOWER, TextTask.CASE_TITLE, TextTask.CASE_SENTENCE,
            TextTask.SIZE_TINY, TextTask.SIZE_SMALL, TextTask.SIZE_LARGE, TextTask.SIZE_HUGE,
            TextTask.FONT_BOLD_SANS, TextTask.FONT_ITALIC, TextTask.FONT_SCRIPT,
            TextTask.FONT_MONOSPACE, TextTask.FONT_CIRCLED, TextTask.FONT_INVERTED,
            TextTask.EMOJI_SPARKLE, TextTask.EMOJI_FIRE, TextTask.EMOJI_STAR, TextTask.EMOJI_CELEBRATE -> true
            else -> false
        }
    }
    
    private fun applyFormatting(text: String, task: TextTask): String {
        return when (task) {
            // Basic formatting
            TextTask.FORMAT_BOLD -> formatter.applyFontStyle(text, 
                TextFormatterService.FontConfig(styles = setOf(TextFormatterService.FontStyle.BOLD)))
            TextTask.FORMAT_ITALIC -> formatter.applyFontStyle(text,
                TextFormatterService.FontConfig(styles = setOf(TextFormatterService.FontStyle.ITALIC)))
            TextTask.FORMAT_UNDERLINE -> formatter.applyFontStyle(text,
                TextFormatterService.FontConfig(styles = setOf(TextFormatterService.FontStyle.UNDERLINE)))
            
            // Structural formatting
            TextTask.FORMAT_HEADING_1 -> formatter.applyFormat(text, TextFormatterService.FormatType.HEADING_1)
            TextTask.FORMAT_HEADING_2 -> formatter.applyFormat(text, TextFormatterService.FormatType.HEADING_2)
            TextTask.FORMAT_BULLET_LIST -> formatter.applyFormat(text, TextFormatterService.FormatType.BULLET_LIST)
            TextTask.FORMAT_NUMBERED_LIST -> formatter.applyFormat(text, TextFormatterService.FormatType.NUMBERED_LIST)
            TextTask.FORMAT_QUOTE -> formatter.applyFormat(text, TextFormatterService.FormatType.QUOTE)
            
            // Case changes
            TextTask.CASE_UPPER -> formatter.applyCase(text, TextFormatterService.CaseType.UPPERCASE)
            TextTask.CASE_LOWER -> formatter.applyCase(text, TextFormatterService.CaseType.LOWERCASE)
            TextTask.CASE_TITLE -> formatter.applyCase(text, TextFormatterService.CaseType.TITLE_CASE)
            TextTask.CASE_SENTENCE -> formatter.applyCase(text, TextFormatterService.CaseType.SENTENCE_CASE)
            
            // Size changes
            TextTask.SIZE_TINY -> formatter.changeFontSize(text, TextFormatterService.FontSize.TINY)
            TextTask.SIZE_SMALL -> formatter.changeFontSize(text, TextFormatterService.FontSize.SMALL)
            TextTask.SIZE_LARGE -> formatter.changeFontSize(text, TextFormatterService.FontSize.LARGE)
            TextTask.SIZE_HUGE -> formatter.changeFontSize(text, TextFormatterService.FontSize.HUGE)
            
            // Fancy fonts
            TextTask.FONT_BOLD_SANS -> formatter.applyFancyFont(text, TextFormatterService.FancyFontStyle.BOLD_SANS)
            TextTask.FONT_ITALIC -> formatter.applyFancyFont(text, TextFormatterService.FancyFontStyle.ITALIC)
            TextTask.FONT_SCRIPT -> formatter.applyFancyFont(text, TextFormatterService.FancyFontStyle.SCRIPT)
            TextTask.FONT_MONOSPACE -> formatter.applyFancyFont(text, TextFormatterService.FancyFontStyle.MONOSPACE)
            TextTask.FONT_CIRCLED -> formatter.applyFancyFont(text, TextFormatterService.FancyFontStyle.CIRCLED)
            TextTask.FONT_INVERTED -> formatter.applyFancyFont(text, TextFormatterService.FancyFontStyle.INVERTED)
            
            // Emoji decoration
            TextTask.EMOJI_SPARKLE -> formatter.applyEmoji(text, TextFormatterService.EmojiStyle.SPARKLE)
            TextTask.EMOJI_FIRE -> formatter.applyEmoji(text, TextFormatterService.EmojiStyle.FIRE)
            TextTask.EMOJI_STAR -> formatter.applyEmoji(text, TextFormatterService.EmojiStyle.STAR)
            TextTask.EMOJI_CELEBRATE -> formatter.applyEmoji(text, TextFormatterService.EmojiStyle.CELEBRATE)
            
            else -> text
        }
    }
    
    data class ProcessResult(
        val text: String,
        val sources: List<String>,
        val usedWebSearch: Boolean
    )
    
    private fun buildPrompt(text: String, task: TextTask, webContext: String = ""): String {
        val contextPrefix = if (webContext.isNotEmpty()) {
            """Current Information from Web:
$webContext

---

"""
        } else {
            ""
        }
        
        return when (task) {
            TextTask.GRAMMAR -> """${contextPrefix}Fix grammar and spelling errors in the following text. Return ONLY the corrected text without explanations:

$text"""
            
            TextTask.REWRITE_CASUAL -> """${contextPrefix}Rewrite the following text in a casual, friendly tone. Return ONLY the rewritten text:

$text"""
            
            TextTask.REWRITE_FORMAL -> """${contextPrefix}Rewrite the following text in a formal, professional tone. Return ONLY the rewritten text:

$text"""
            
            TextTask.REWRITE_PROFESSIONAL -> """${contextPrefix}Rewrite the following text in a clear, professional business tone. Return ONLY the rewritten text:

$text"""
            
            TextTask.SUMMARIZE -> """${contextPrefix}Summarize the following text concisely in 2-3 sentences. Return ONLY the summary:

$text"""
            
            TextTask.EXPAND -> """${contextPrefix}Expand the following text with more details and context. Return ONLY the expanded text:

$text"""
            
            TextTask.SIMPLIFY -> """${contextPrefix}Simplify the following text to make it easier to understand. Return ONLY the simplified text:

$text"""
            
            TextTask.REWRITE_WITH_RESEARCH -> """${contextPrefix}Rewrite the following text incorporating accurate, current information from the sources above. Return ONLY the rewritten text:

$text"""
            
            TextTask.EXPAND_WITH_FACTS -> """${contextPrefix}Expand the following text by adding verified facts and current information from the sources above. Return ONLY the expanded text:

$text"""
            
            TextTask.UPDATE_WITH_CURRENT -> """${contextPrefix}Update the following text with current information from the sources above, replacing any outdated information. Return ONLY the updated text:

$text"""
            
            TextTask.FACT_CHECK -> """${contextPrefix}Check the claims in the following text against the current information above. Identify what is accurate and what needs correction. Return your analysis:

$text"""
            
            TextTask.ADD_CITATIONS -> """${contextPrefix}Add proper citations to claims in the following text using the sources provided above. Return the text with [Source N] citations:

$text"""
        }
    }
    
    private suspend fun generateText(prompt: String): String = withContext(Dispatchers.Default) {
        val currentSession = session
        val env = ortEnvironment
        
        if (currentSession == null || env == null) {
            Log.e(TAG, "Model not loaded - session or environment is null")
            return@withContext "Error: Model not loaded"
        }
        
        try {
            // Simple tokenization (in production, use proper tokenizer)
            val tokens = tokenize(prompt)
            val inputIds = LongBuffer.wrap(tokens.toLongArray())
            
            val inputTensor = OnnxTensor.createTensor(env, inputIds, longArrayOf(1, tokens.size.toLong()))
            val inputs = mapOf("input_ids" to inputTensor)
            
            val outputs = currentSession.run(inputs)
            val output = outputs[0].value as Array<LongArray>
            
            // Decode tokens back to text
            val generated = decode(output[0].toList())
            
            inputTensor.close()
            outputs.close()
            
            generated
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed", e)
            "Error: ${e.message ?: "Text generation failed"}"
        }
    }
    
    private fun tokenize(text: String): List<Long> {
        // Simplified tokenization - in production use proper tokenizer
        return text.split(" ").map { it.hashCode().toLong() and 0xFFFF }
    }
    
    private fun decode(tokens: List<Long>): String {
        // Simplified decoding - in production use proper tokenizer
        return tokens.joinToString(" ") { (it and 0xFFFF).toString() }
    }
    
    private fun extractResponse(response: String, task: TextTask): String {
        // Extract just the generated text, removing any prompt echoes
        return response.trim()
    }
    
    fun release() {
        session?.close()
        ortEnvironment?.close()
        session = null
        ortEnvironment = null
    }
    
    fun isModelDownloaded(): Boolean {
        return File(modelPath).exists()
    }
}

enum class TextTask {
    // Original offline tasks
    GRAMMAR,
    REWRITE_CASUAL,
    REWRITE_FORMAL,
    REWRITE_PROFESSIONAL,
    SUMMARIZE,
    EXPAND,
    SIMPLIFY,
    
    // Enhanced tasks with current knowledge
    REWRITE_WITH_RESEARCH,
    EXPAND_WITH_FACTS,
    UPDATE_WITH_CURRENT,
    FACT_CHECK,
    ADD_CITATIONS,
    
    // Formatting tasks
    FORMAT_BOLD,
    FORMAT_ITALIC,
    FORMAT_UNDERLINE,
    FORMAT_HEADING_1,
    FORMAT_HEADING_2,
    FORMAT_BULLET_LIST,
    FORMAT_NUMBERED_LIST,
    FORMAT_QUOTE,
    
    // Case formatting
    CASE_UPPER,
    CASE_LOWER,
    CASE_TITLE,
    CASE_SENTENCE,
    
    // Font sizes
    SIZE_TINY,
    SIZE_SMALL,
    SIZE_LARGE,
    SIZE_HUGE,
    
    // Fancy fonts
    FONT_BOLD_SANS,
    FONT_ITALIC,
    FONT_SCRIPT,
    FONT_MONOSPACE,
    FONT_CIRCLED,
    FONT_INVERTED,
    
    // Emoji decoration
    EMOJI_SPARKLE,
    EMOJI_FIRE,
    EMOJI_STAR,
    EMOJI_CELEBRATE
}
