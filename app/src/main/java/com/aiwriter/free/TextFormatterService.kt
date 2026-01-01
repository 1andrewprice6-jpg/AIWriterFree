package com.aiwriter.free

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*

class TextFormatterService {
    
    enum class FontStyle {
        BOLD,
        ITALIC,
        UNDERLINE,
        STRIKETHROUGH,
        MONOSPACE,
        SMALL_CAPS
    }
    
    enum class FormatType {
        HEADING_1,
        HEADING_2,
        HEADING_3,
        BULLET_LIST,
        NUMBERED_LIST,
        QUOTE,
        CODE_BLOCK
    }
    
    data class FontConfig(
        val family: String = "default",  // default, serif, sans-serif, monospace, cursive
        val size: Int = 16,              // 8-72
        val color: String? = null,       // hex color like "#FF0000"
        val styles: Set<FontStyle> = emptySet()
    )
    
    fun applyFontStyle(text: String, config: FontConfig): String {
        var result = text
        
        // Apply text styles using markup that most apps support
        if (FontStyle.BOLD in config.styles) {
            result = "**$result**"  // Markdown bold
        }
        if (FontStyle.ITALIC in config.styles) {
            result = "_${result}_"  // Markdown italic
        }
        if (FontStyle.UNDERLINE in config.styles) {
            result = "<u>$result</u>"  // HTML underline
        }
        if (FontStyle.STRIKETHROUGH in config.styles) {
            result = "~~$result~~"  // Markdown strikethrough
        }
        if (FontStyle.MONOSPACE in config.styles) {
            result = "`$result`"  // Markdown code
        }
        
        return result
    }
    
    fun applyFormat(text: String, format: FormatType): String {
        return when (format) {
            FormatType.HEADING_1 -> {
                "# $text"  // Markdown H1
            }
            FormatType.HEADING_2 -> {
                "## $text"  // Markdown H2
            }
            FormatType.HEADING_3 -> {
                "### $text"  // Markdown H3
            }
            FormatType.BULLET_LIST -> {
                // Convert lines to bullet points
                text.split("\n")
                    .filter { it.isNotBlank() }
                    .joinToString("\n") { "• $it" }
            }
            FormatType.NUMBERED_LIST -> {
                // Convert lines to numbered list
                text.split("\n")
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, line -> "${index + 1}. $line" }
                    .joinToString("\n")
            }
            FormatType.QUOTE -> {
                // Block quote format
                text.split("\n")
                    .joinToString("\n") { "> $it" }
            }
            FormatType.CODE_BLOCK -> {
                "```\n$text\n```"
            }
        }
    }
    
    fun changeFontSize(text: String, size: FontSize): String {
        return when (size) {
            FontSize.TINY -> "<small><small>$text</small></small>"
            FontSize.SMALL -> "<small>$text</small>"
            FontSize.NORMAL -> text
            FontSize.LARGE -> "<big>$text</big>"
            FontSize.HUGE -> "<big><big>$text</big></big>"
        }
    }
    
    enum class FontSize {
        TINY,      // 10pt
        SMALL,     // 12pt
        NORMAL,    // 14pt
        LARGE,     // 18pt
        HUGE       // 24pt
    }
    
    fun applyCase(text: String, caseType: CaseType): String {
        return when (caseType) {
            CaseType.UPPERCASE -> text.uppercase()
            CaseType.LOWERCASE -> text.lowercase()
            CaseType.TITLE_CASE -> {
                text.split(" ")
                    .joinToString(" ") { word ->
                        word.lowercase().replaceFirstChar { it.uppercase() }
                    }
            }
            CaseType.SENTENCE_CASE -> {
                text.lowercase().replaceFirstChar { it.uppercase() }
            }
            CaseType.ALTERNATING -> {
                text.mapIndexed { index, char ->
                    if (index % 2 == 0) char.uppercase() else char.lowercase()
                }.joinToString("")
            }
        }
    }
    
    enum class CaseType {
        UPPERCASE,
        LOWERCASE,
        TITLE_CASE,
        SENTENCE_CASE,
        ALTERNATING
    }
    
    fun applyEmoji(text: String, emojiStyle: EmojiStyle): String {
        return when (emojiStyle) {
            EmojiStyle.SPARKLE -> "✨ $text ✨"
            EmojiStyle.FIRE -> "🔥 $text 🔥"
            EmojiStyle.STAR -> "⭐ $text ⭐"
            EmojiStyle.HEART -> "❤️ $text ❤️"
            EmojiStyle.CHECK -> "✅ $text"
            EmojiStyle.WARN -> "⚠️ $text"
            EmojiStyle.INFO -> "ℹ️ $text"
            EmojiStyle.CELEBRATE -> "🎉 $text 🎊"
        }
    }
    
    enum class EmojiStyle {
        SPARKLE,
        FIRE,
        STAR,
        HEART,
        CHECK,
        WARN,
        INFO,
        CELEBRATE
    }
    
    fun applyFancyFont(text: String, style: FancyFontStyle): String {
        return when (style) {
            FancyFontStyle.BOLD_SANS -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1D5D4 + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x1D5EE + (char - 'a')).toChar()
                    in '0'..'9' -> (0x1D7EC + (char - '0')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.ITALIC -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1D608 + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x1D622 + (char - 'a')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.BOLD_ITALIC -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1D63C + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x1D656 + (char - 'a')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.SCRIPT -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1D49C + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x1D4B6 + (char - 'a')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.DOUBLE_STRUCK -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1D538 + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x1D552 + (char - 'a')).toChar()
                    in '0'..'9' -> (0x1D7D8 + (char - '0')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.MONOSPACE -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1D670 + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x1D68A + (char - 'a')).toChar()
                    in '0'..'9' -> (0x1D7F6 + (char - '0')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.CIRCLED -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x24B6 + (char - 'A')).toChar()
                    in 'a'..'z' -> (0x24D0 + (char - 'a')).toChar()
                    in '0'..'9' -> (0x2460 + (char - '1')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.SQUARED -> text.map { char ->
                when (char) {
                    in 'A'..'Z' -> (0x1F130 + (char - 'A')).toChar()
                    else -> char
                }
            }.joinToString("")
            
            FancyFontStyle.INVERTED -> text.map { char ->
                val inverted = mapOf(
                    'a' to 'ɐ', 'b' to 'q', 'c' to 'ɔ', 'd' to 'p', 'e' to 'ǝ',
                    'f' to 'ɟ', 'g' to 'ƃ', 'h' to 'ɥ', 'i' to 'ᴉ', 'j' to 'ɾ',
                    'k' to 'ʞ', 'l' to 'ן', 'm' to 'ɯ', 'n' to 'u', 'o' to 'o',
                    'p' to 'd', 'q' to 'b', 'r' to 'ɹ', 's' to 's', 't' to 'ʇ',
                    'u' to 'n', 'v' to 'ʌ', 'w' to 'ʍ', 'x' to 'x', 'y' to 'ʎ',
                    'z' to 'z'
                )
                inverted[char.lowercaseChar()] ?: char
            }.joinToString("")
        }
    }
    
    enum class FancyFontStyle {
        BOLD_SANS,
        ITALIC,
        BOLD_ITALIC,
        SCRIPT,
        DOUBLE_STRUCK,
        MONOSPACE,
        CIRCLED,
        SQUARED,
        INVERTED
    }
}
