package com.aiwriter.free

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TextProcessorActivity : AppCompatActivity() {
    
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var aiEngine: LocalAIEngine
    private var progressBar: ProgressBar? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        aiEngine = (application as AIWriterApplication).aiEngine
        
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
        
        if (selectedText.isNullOrEmpty()) {
            finish()
            return
        }
        
        if (!aiEngine.isModelDownloaded()) {
            showModelNotDownloadedDialog()
            return
        }
        
        showTaskSelectionDialog(selectedText, readonly)
    }
    
    private fun showTaskSelectionDialog(text: String, readonly: Boolean) {
        val categories = arrayOf(
            "AI Writing",
            "AI with Web Search",
            "Formatting & Style",
            "Case & Size",
            "Fancy Fonts"
        )
        
        AlertDialog.Builder(this)
            .setTitle("AI Writer - Select Category")
            .setItems(categories) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> showAIWritingMenu(text, readonly)
                    1 -> showWebSearchMenu(text, readonly)
                    2 -> showFormattingMenu(text, readonly)
                    3 -> showCaseSizeMenu(text, readonly)
                    4 -> showFancyFontsMenu(text, readonly)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
    
    private fun showAIWritingMenu(text: String, readonly: Boolean) {
        val tasks = arrayOf(
            "Fix Grammar",
            "Rewrite - Casual",
            "Rewrite - Formal",
            "Rewrite - Professional",
            "Summarize",
            "Expand",
            "Simplify"
        )
        
        AlertDialog.Builder(this)
            .setTitle("AI Writing (Offline)")
            .setItems(tasks) { dialog, which ->
                val task = when (which) {
                    0 -> TextTask.GRAMMAR
                    1 -> TextTask.REWRITE_CASUAL
                    2 -> TextTask.REWRITE_FORMAL
                    3 -> TextTask.REWRITE_PROFESSIONAL
                    4 -> TextTask.SUMMARIZE
                    5 -> TextTask.EXPAND
                    6 -> TextTask.SIMPLIFY
                    else -> TextTask.GRAMMAR
                }
                processText(text, task, readonly)
                dialog.dismiss()
            }
            .setNegativeButton("Back") { _, _ ->
                showTaskSelectionDialog(text, readonly)
            }
            .show()
    }
    
    private fun showWebSearchMenu(text: String, readonly: Boolean) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Web search requires internet connection", Toast.LENGTH_SHORT).show()
            showTaskSelectionDialog(text, readonly)
            return
        }
        
        val tasks = arrayOf(
            "Rewrite with Research",
            "Expand with Facts",
            "Update with Current Info",
            "Fact Check",
            "Add Citations"
        )
        
        AlertDialog.Builder(this)
            .setTitle("AI with Web Search")
            .setItems(tasks) { dialog, which ->
                val task = when (which) {
                    0 -> TextTask.REWRITE_WITH_RESEARCH
                    1 -> TextTask.EXPAND_WITH_FACTS
                    2 -> TextTask.UPDATE_WITH_CURRENT
                    3 -> TextTask.FACT_CHECK
                    4 -> TextTask.ADD_CITATIONS
                    else -> TextTask.REWRITE_WITH_RESEARCH
                }
                processText(text, task, readonly)
                dialog.dismiss()
            }
            .setNegativeButton("Back") { _, _ ->
                showTaskSelectionDialog(text, readonly)
            }
            .show()
    }
    
    private fun showFormattingMenu(text: String, readonly: Boolean) {
        val tasks = arrayOf(
            "Bold",
            "Italic",
            "Underline",
            "Heading 1",
            "Heading 2",
            "Bullet List",
            "Numbered List",
            "Quote Block",
            "✨ Sparkle",
            "🔥 Fire",
            "⭐ Star",
            "🎉 Celebrate"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Formatting & Style")
            .setItems(tasks) { dialog, which ->
                val task = when (which) {
                    0 -> TextTask.FORMAT_BOLD
                    1 -> TextTask.FORMAT_ITALIC
                    2 -> TextTask.FORMAT_UNDERLINE
                    3 -> TextTask.FORMAT_HEADING_1
                    4 -> TextTask.FORMAT_HEADING_2
                    5 -> TextTask.FORMAT_BULLET_LIST
                    6 -> TextTask.FORMAT_NUMBERED_LIST
                    7 -> TextTask.FORMAT_QUOTE
                    8 -> TextTask.EMOJI_SPARKLE
                    9 -> TextTask.EMOJI_FIRE
                    10 -> TextTask.EMOJI_STAR
                    11 -> TextTask.EMOJI_CELEBRATE
                    else -> TextTask.FORMAT_BOLD
                }
                processText(text, task, readonly)
                dialog.dismiss()
            }
            .setNegativeButton("Back") { _, _ ->
                showTaskSelectionDialog(text, readonly)
            }
            .show()
    }
    
    private fun showCaseSizeMenu(text: String, readonly: Boolean) {
        val tasks = arrayOf(
            "UPPERCASE",
            "lowercase",
            "Title Case",
            "Sentence case",
            "Tiny Size",
            "Small Size",
            "Large Size",
            "Huge Size"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Case & Size")
            .setItems(tasks) { dialog, which ->
                val task = when (which) {
                    0 -> TextTask.CASE_UPPER
                    1 -> TextTask.CASE_LOWER
                    2 -> TextTask.CASE_TITLE
                    3 -> TextTask.CASE_SENTENCE
                    4 -> TextTask.SIZE_TINY
                    5 -> TextTask.SIZE_SMALL
                    6 -> TextTask.SIZE_LARGE
                    7 -> TextTask.SIZE_HUGE
                    else -> TextTask.CASE_TITLE
                }
                processText(text, task, readonly)
                dialog.dismiss()
            }
            .setNegativeButton("Back") { _, _ ->
                showTaskSelectionDialog(text, readonly)
            }
            .show()
    }
    
    private fun showFancyFontsMenu(text: String, readonly: Boolean) {
        val tasks = arrayOf(
            "𝗕𝗼𝗹𝗱 𝗦𝗮𝗻𝘀",
            "𝘐𝘵𝘢𝘭𝘪𝘤",
            "𝓢𝓬𝓻𝓲𝓹𝓽",
            "𝙼𝚘𝚗𝚘𝚜𝚙𝚊𝚌𝚎",
            "Ⓒⓘⓡⓒⓛⓔⓓ",
            "uǝpᴉsuI"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Fancy Fonts")
            .setItems(tasks) { dialog, which ->
                val task = when (which) {
                    0 -> TextTask.FONT_BOLD_SANS
                    1 -> TextTask.FONT_ITALIC
                    2 -> TextTask.FONT_SCRIPT
                    3 -> TextTask.FONT_MONOSPACE
                    4 -> TextTask.FONT_CIRCLED
                    5 -> TextTask.FONT_INVERTED
                    else -> TextTask.FONT_BOLD_SANS
                }
                processText(text, task, readonly)
                dialog.dismiss()
            }
            .setNegativeButton("Back") { _, _ ->
                showTaskSelectionDialog(text, readonly)
            }
            .show()
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
    
    private fun processText(text: String, task: TextTask, readonly: Boolean) {
        showProgress()
        
        scope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    aiEngine.processText(text, task, useWebSearch = true)
                }
                
                hideProgress()
                
                // Check if result indicates an error
                if (result.text.startsWith("Error:")) {
                    Toast.makeText(
                        this@TextProcessorActivity,
                        result.text,
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@launch
                }
                
                if (!readonly) {
                    // Return the processed text to replace selection
                    val resultIntent = Intent()
                    resultIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, result.text)
                    setResult(Activity.RESULT_OK, resultIntent)
                    
                    // If sources were used, show notification
                    if (result.usedWebSearch && result.sources.isNotEmpty()) {
                        Toast.makeText(
                            this@TextProcessorActivity,
                            "Used ${result.sources.size} web sources",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    finish()
                } else {
                    // Show result in dialog for readonly text
                    showResultDialog(result)
                }
            } catch (e: Exception) {
                hideProgress()
                Toast.makeText(this@TextProcessorActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun showResultDialog(result: LocalAIEngine.ProcessResult) {
        val message = buildString {
            append(result.text)
            
            if (result.usedWebSearch && result.sources.isNotEmpty()) {
                append("\n\n─── Sources ───\n")
                result.sources.forEachIndexed { index, source ->
                    append("${index + 1}. ${source}\n")
                }
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle(if (result.usedWebSearch) "Result (with web research)" else "Result")
            .setMessage(message)
            .setPositiveButton("Copy") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("AI Writer Result", result.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
    
    private fun showModelNotDownloadedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Model Not Downloaded")
            .setMessage("The AI model needs to be downloaded first. Open AI Writer app to download.")
            .setPositiveButton("Open App") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
    
    private fun showProgress() {
        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }
        setContentView(progressBar)
    }
    
    private fun hideProgress() {
        progressBar?.visibility = View.GONE
    }
    
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
