package com.technitedminds.wallet.data.ocr

import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.ImageSide
import com.technitedminds.wallet.presentation.constants.AppConstants
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Specialized parser for extracting card information from OCR text.
 * Implements advanced parsing algorithms for different card types.
 */
@Singleton
class CardTextParser @Inject constructor() {
    
    companion object {
        // Words to exclude from name detection
        private val EXCLUDED_WORDS = AppConstants.CardProcessing.EXCLUDED_WORDS
        
        // Card number patterns - enhanced for better debit card detection
        private val CARD_NUMBER_PATTERNS = listOf(
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_16),
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_SPACES),
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_CONTINUOUS_16),
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_15),
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_CONTINUOUS_15),
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_13),
            Regex(AppConstants.CardProcessing.CARD_NUMBER_PATTERN_14)
        )
        
        // Expiry date patterns
        private val EXPIRY_PATTERNS = listOf(
            Regex(AppConstants.CardProcessing.EXPIRY_PATTERN_SHORT),
            Regex(AppConstants.CardProcessing.EXPIRY_PATTERN_LONG),
            Regex(AppConstants.CardProcessing.EXPIRY_PATTERN_VALID_THRU, RegexOption.IGNORE_CASE),
            Regex(AppConstants.CardProcessing.EXPIRY_PATTERN_EXP, RegexOption.IGNORE_CASE)
        )
    }
    
    fun parseCardText(rawText: String, cardType: CardType, imageSide: ImageSide): Map<String, String> {
        if (!cardType.supportsOCR()) return emptyMap()
        
        val lines = preprocessText(rawText)
        val extractedData = mutableMapOf<String, String>()
        
        when (imageSide) {
            ImageSide.FRONT -> extractFrontSideData(lines, extractedData)
            ImageSide.BACK -> extractBackSideData(lines, extractedData)
        }
        
        return extractedData.filterValues { it.isNotBlank() }
    }
    
    private fun preprocessText(rawText: String): List<String> {
        return rawText.lines()
            .map { it.trim().replace(Regex(AppConstants.CardProcessing.CLEAN_TEXT_PATTERN), " ").replace(Regex(AppConstants.CardProcessing.WHITESPACE_PATTERN), " ") }
            .filter { it.isNotBlank() && it.length > AppConstants.CardProcessing.MIN_LINE_LENGTH }
    }
    
    private fun extractFrontSideData(lines: List<String>, extractedData: MutableMap<String, String>) {
        extractCardNumber(lines)?.let { extractedData[AppConstants.CardProcessing.FIELD_CARD_NUMBER] = it }
        extractExpiryDate(lines)?.let { extractedData[AppConstants.CardProcessing.FIELD_EXPIRY_DATE] = it }
        extractCardholderName(lines)?.let { extractedData[AppConstants.CardProcessing.FIELD_CARDHOLDER_NAME] = it }
    }
    
    private fun extractBackSideData(lines: List<String>, extractedData: MutableMap<String, String>) {
        extractCVV(lines)?.let { extractedData[AppConstants.CardProcessing.FIELD_CVV] = it }
    }
    
    private fun extractCardNumber(lines: List<String>): String? {
        for (line in lines) {
            for (pattern in CARD_NUMBER_PATTERNS) {
                pattern.find(line)?.let { match ->
                    val cardNumber = match.value.replace(Regex(AppConstants.CardProcessing.CARD_NUMBER_CLEAN_PATTERN), "")
                    if (cardNumber.length in AppConstants.CardProcessing.MIN_CARD_NUMBER_LENGTH..AppConstants.CardProcessing.MAX_CARD_NUMBER_LENGTH && cardNumber.all { it.isDigit() }) {
                        return formatCardNumber(cardNumber)
                    }
                }
            }
        }
        return null
    }
    
    private fun extractExpiryDate(lines: List<String>): String? {
        for (line in lines) {
            for (pattern in EXPIRY_PATTERNS) {
                pattern.find(line)?.let { match ->
                    val dateText = match.value
                    val dateRegex = Regex(AppConstants.CardProcessing.DATE_EXTRACT_PATTERN)
                    dateRegex.find(dateText)?.let { dateMatch ->
                        return formatExpiryDate(dateMatch.value)
                    }
                }
            }
        }
        return null
    }
    
    private fun extractCardholderName(lines: List<String>): String? {
        val candidates = lines.filter { line ->
            val upperLine = line.uppercase()
            !EXCLUDED_WORDS.any { upperLine.contains(it) } &&
            line.count { it.isLetter() } > line.length * AppConstants.CardProcessing.NAME_LETTER_RATIO &&
            line.length in AppConstants.CardProcessing.MIN_NAME_LENGTH..AppConstants.CardProcessing.MAX_NAME_LENGTH
        }
        
        return candidates.find { candidate ->
            val words = candidate.split(Regex(AppConstants.CardProcessing.WHITESPACE_PATTERN)).filter { it.isNotBlank() }
            words.size in AppConstants.CardProcessing.MIN_NAME_WORDS..AppConstants.CardProcessing.MAX_NAME_WORDS && words.all { it.length >= AppConstants.CardProcessing.MIN_NAME_WORD_LENGTH && it.all { c -> c.isUpperCase() || !c.isLetter() } }
        }?.let { formatCardholderName(it) }
    }
    
    private fun extractCVV(lines: List<String>): String? {
        val cvvPattern = Regex(AppConstants.CardProcessing.CVV_PATTERN)
        for (line in lines) {
            cvvPattern.find(line)?.let { match ->
                val cvv = match.value
                if (cvv.length in 3..4) return cvv
            }
        }
        return null
    }
    
    private fun formatCardNumber(cardNumber: String): String {
        return when (cardNumber.length) {
            15 -> cardNumber.chunked(4).joinToString(" ")
            16 -> cardNumber.chunked(4).joinToString(" ")
            else -> cardNumber
        }
    }
    
    private fun formatExpiryDate(expiryDate: String): String {
        val parts = expiryDate.split("/")
        return if (parts.size == 2 && parts[1].length == AppConstants.CardProcessing.EXPIRY_YEAR_LENGTH) {
            "${parts[0]}/${parts[1].takeLast(AppConstants.CardProcessing.EXPIRY_SHORT_YEAR_LENGTH)}"
        } else {
            expiryDate
        }
    }
    
    private fun formatCardholderName(name: String): String {
        return name.trim().split(AppConstants.CardProcessing.WHITESPACE_PATTERN).joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}