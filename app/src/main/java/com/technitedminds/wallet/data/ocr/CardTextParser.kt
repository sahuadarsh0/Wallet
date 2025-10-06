package com.technitedminds.wallet.data.ocr

import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.usecase.ocr.ImageSide
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
        // Common bank names for validation
        private val BANK_NAMES = setOf(
            "BANK", "CHASE", "WELLS", "FARGO", "CITI", "CAPITAL", "ONE", "AMERICAN", "EXPRESS",
            "DISCOVER", "MASTERCARD", "VISA", "UNION", "CREDIT", "FEDERAL", "TRUST", "SAVINGS"
        )
        
        // Words to exclude from name detection
        private val EXCLUDED_WORDS = setOf(
            "BANK", "CARD", "CREDIT", "DEBIT", "VALID", "THRU", "EXPIRES", "EXP", "MEMBER", "SINCE",
            "VISA", "MASTERCARD", "AMERICAN", "EXPRESS", "DISCOVER", "PLATINUM", "GOLD", "CLASSIC",
            "REWARDS", "CASH", "BACK", "MILES", "POINTS", "SIGNATURE", "WORLD", "ELITE", "PREFERRED",
            "BUSINESS", "CORPORATE", "STUDENT", "SECURED", "PREPAID", "GIFT", "TRAVEL", "DINING",
            "ENTERTAINMENT", "GAS", "GROCERY", "DEPARTMENT", "STORE", "ONLINE", "SHOPPING", "MOBILE",
            "CONTACTLESS", "CHIP", "PIN", "MAGNETIC", "STRIPE", "SECURITY", "CODE", "CVV", "CVC",
            "CUSTOMER", "SERVICE", "PHONE", "NUMBER", "WEBSITE", "ADDRESS", "ZIP", "CODE", "STATE"
        )
        
        // Card number patterns - enhanced for better debit card detection
        private val CARD_NUMBER_PATTERNS = listOf(
            // Standard 16-digit patterns with various separators
            Regex("""(\d{4}[\s\-\.]?\d{4}[\s\-\.]?\d{4}[\s\-\.]?\d{4})"""),
            // More flexible 16-digit pattern
            Regex("""(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{4}\b)"""),
            // 16 digits with no separators
            Regex("""(\b\d{16}\b)"""),
            // Amex 15-digit patterns
            Regex("""(\d{4}[\s\-\.]?\d{6}[\s\-\.]?\d{5})"""),
            Regex("""(\b\d{4}\s*\d{6}\s*\d{5}\b)"""),
            Regex("""(\b\d{15}\b)"""),
            // Other 15-digit variants
            Regex("""(\d{4}[\s\-\.]?\d{4}[\s\-\.]?\d{4}[\s\-\.]?\d{3})"""),
            // 13-digit cards (some Visa cards)
            Regex("""(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{1}\b)"""),
            Regex("""(\b\d{13}\b)"""),
            // 14-digit cards (some Diners Club)
            Regex("""(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{2}\b)"""),
            Regex("""(\b\d{14}\b)"""),
            // 17-digit cards (some regional cards)
            Regex("""(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{4}\s*\d{1}\b)"""),
            // 18-digit cards (some regional cards)
            Regex("""(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{4}\s*\d{2}\b)"""),
            // 19-digit cards (some regional cards)
            Regex("""(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{4}\s*\d{3}\b)""")
        )
        
        // Expiry date patterns
        private val EXPIRY_PATTERNS = listOf(
            Regex("""(\d{2}/\d{2})"""), // MM/YY
            Regex("""(\d{2}/\d{4})"""), // MM/YYYY
            Regex("""(\d{2}-\d{2})"""), // MM-YY
            Regex("""(\d{2}\s+\d{2})"""), // MM YY
            Regex("""(VALID\s+THRU\s+\d{2}/\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""(EXP\s*:?\s*\d{2}/\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""(EXPIRES\s+\d{2}/\d{2})""", RegexOption.IGNORE_CASE)
        )
    }
    
    /**
     * Parses OCR text to extract card information
     */
    fun parseCardText(rawText: String, cardType: CardType, imageSide: ImageSide): Map<String, String> {
        if (!cardType.supportsOCR()) {
            return emptyMap()
        }
        
        // Debug logging - in production this would use proper logging
        println("CardTextParser: Processing ${cardType.getDisplayName()} ${imageSide.name} side")
        println("CardTextParser: Raw text: $rawText")
        
        val lines = preprocessText(rawText)
        println("CardTextParser: Preprocessed lines: $lines")
        
        val extractedData = mutableMapOf<String, String>()
        
        when (imageSide) {
            ImageSide.FRONT -> {
                extractFrontSideData(lines, extractedData)
            }
            ImageSide.BACK -> {
                extractBackSideData(lines, extractedData)
            }
        }
        
        println("CardTextParser: Extracted data: $extractedData")
        
        return validateAndFormatData(extractedData)
    }
    
    /**
     * Preprocesses raw OCR text for better parsing
     */
    private fun preprocessText(rawText: String): List<String> {
        return rawText
            .lines()
            .map { line ->
                line.trim()
                    // Keep more characters that might be relevant for card numbers
                    .replace(Regex("""[^\w\s/\-.*]"""), " ") // Keep dots and asterisks
                    .replace(Regex("""\s+"""), " ") // Normalize whitespace
                    // Handle common OCR misreads
                    .replace("O", "0") // O -> 0
                    .replace("l", "1") // lowercase l -> 1
                    .replace("I", "1") // uppercase I -> 1
                    .replace("S", "5") // S -> 5 (common misread)
            }
            .filter { it.isNotBlank() && it.length > 1 }
    }
    
    /**
     * Extracts data from front side of card - only essential fields
     */
    private fun extractFrontSideData(lines: List<String>, extractedData: MutableMap<String, String>) {
        // Extract card number
        extractCardNumber(lines)?.let { cardNumber ->
            extractedData["cardNumber"] = cardNumber
        }
        
        // Extract expiry date
        extractExpiryDate(lines)?.let { expiryDate ->
            extractedData["expiryDate"] = expiryDate
        }
        
        // Extract cardholder name
        extractCardholderName(lines)?.let { name ->
            extractedData["cardholderName"] = name
        }
    }
    
    /**
     * Extracts data from back side of card - only CVV
     */
    private fun extractBackSideData(lines: List<String>, extractedData: MutableMap<String, String>) {
        // Extract CVV only
        extractCVV(lines)?.let { cvv ->
            extractedData["cvv"] = cvv
        }
    }
    
    /**
     * Extracts card number using multiple patterns and validation
     */
    private fun extractCardNumber(lines: List<String>): String? {
        val candidates = mutableListOf<String>()
        
        // First pass: collect all potential card numbers
        for (line in lines) {
            for (pattern in CARD_NUMBER_PATTERNS) {
                val matches = pattern.findAll(line)
                for (match in matches) {
                    val cardNumber = match.value.replace(Regex("""[\s\-\.]"""), "")
                    if (cardNumber.length in 13..19 && cardNumber.all { it.isDigit() }) {
                        candidates.add(cardNumber)
                    }
                }
            }
        }
        
        // Second pass: validate candidates and return the best one
        val validCandidates = candidates.filter { isValidCardNumber(it) }
        
        return when {
            validCandidates.isNotEmpty() -> {
                // Prefer 16-digit cards (most common), then by position in text
                val preferred = validCandidates.find { it.length == 16 } ?: validCandidates.first()
                formatCardNumber(preferred)
            }
            candidates.isNotEmpty() -> {
                // If no valid Luhn candidates, try the most likely one anyway
                val mostLikely = candidates.find { it.length == 16 } ?: candidates.first()
                formatCardNumber(mostLikely)
            }
            else -> null
        }
    }
    
    /**
     * Extracts expiry date with comprehensive pattern matching
     */
    private fun extractExpiryDate(lines: List<String>): String? {
        for (line in lines) {
            for (pattern in EXPIRY_PATTERNS) {
                val match = pattern.find(line)
                if (match != null) {
                    val expiryText = match.value
                    val extractedDate = extractDateFromText(expiryText)
                    if (extractedDate != null && isValidExpiryDate(extractedDate)) {
                        return formatExpiryDate(extractedDate)
                    }
                }
            }
        }
        return null
    }
    
    /**
     * Extracts cardholder name using advanced heuristics
     * Focuses on capital letter names in First Last format
     */
    private fun extractCardholderName(lines: List<String>): String? {
        println("CardTextParser: extractCardholderName called with ${lines.size} lines")
        val candidates = mutableListOf<String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            val upperLine = trimmedLine.uppercase()
            
            println("CardTextParser: Processing line: '$trimmedLine'")
            
            // Skip lines with excluded words
            if (EXCLUDED_WORDS.any { upperLine.contains(it) }) {
                println("CardTextParser: Skipping line with excluded words: '$trimmedLine'")
                continue
            }
            
            // Skip lines with numbers (except Roman numerals like II, III)
            if (trimmedLine.any { it.isDigit() } && !trimmedLine.matches(Regex(""".*\b(II|III|IV|V|JR|SR)\b.*"""))) {
                println("CardTextParser: Skipping line with numbers: '$trimmedLine'")
                continue
            }
            
            // Look for lines that are likely cardholder names
            if (isCardholderName(trimmedLine)) {
                println("CardTextParser: Found potential cardholder name: '$trimmedLine'")
                candidates.add(trimmedLine)
            } else {
                println("CardTextParser: Line not recognized as cardholder name: '$trimmedLine'")
            }
        }
        
        println("CardTextParser: Found ${candidates.size} cardholder name candidates: $candidates")
        
        // Return the best candidate based on cardholder name scoring
        val filteredCandidates = candidates.filter { it.split(" ").size in 2..4 }
        println("CardTextParser: Filtered candidates (2-4 words): $filteredCandidates")
        
        val bestCandidate = filteredCandidates.maxByOrNull { calculateCardholderNameScore(it) }
        println("CardTextParser: Best candidate: '$bestCandidate'")
        
        return bestCandidate?.let { formatCardholderName(it) }
    }
    
    /**
     * Extracts CVV from back side text
     */
    private fun extractCVV(lines: List<String>): String? {
        val cvvPattern = Regex("""(\b\d{3,4}\b)""")
        
        for (line in lines) {
            val matches = cvvPattern.findAll(line)
            for (match in matches) {
                val cvv = match.value
                if (cvv.length in 3..4 && !isLikelyCardNumber(cvv)) {
                    return cvv
                }
            }
        }
        return null
    }
    

    
    /**
     * Validates card number using Luhn algorithm
     */
    private fun isValidCardNumber(cardNumber: String): Boolean {
        if (cardNumber.length !in 13..19) return false
        if (!cardNumber.all { it.isDigit() }) return false
        
        // Luhn algorithm
        var sum = 0
        var alternate = false
        
        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].digitToInt()
            
            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = (digit % 10) + 1
                }
            }
            
            sum += digit
            alternate = !alternate
        }
        
        return sum % 10 == 0
    }
    
    /**
     * Validates expiry date format and ensures it's not expired
     */
    private fun isValidExpiryDate(expiryDate: String): Boolean {
        val dateRegex = Regex("""(\d{2})/(\d{2,4})""")
        val match = dateRegex.find(expiryDate) ?: return false
        
        val month = match.groupValues[1].toIntOrNull() ?: return false
        val year = match.groupValues[2].toIntOrNull() ?: return false
        
        if (month !in 1..12) return false
        
        // Convert 2-digit year to 4-digit
        val fullYear = if (year < 100) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentCentury = (currentYear / 100) * 100
            if (year < (currentYear % 100)) {
                currentCentury + 100 + year
            } else {
                currentCentury + year
            }
        } else {
            year
        }
        
        // Check if not expired
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        
        return fullYear > currentYear || (fullYear == currentYear && month >= currentMonth)
    }
    
    /**
     * Checks if a line is likely to be a cardholder name
     * Focuses on capital letter names typical on credit/debit cards
     */
    private fun isCardholderName(line: String): Boolean {
        val trimmedLine = line.trim()
        
        println("CardTextParser: isCardholderName checking: '$trimmedLine'")
        
        // Must have reasonable length for a name
        if (trimmedLine.length < 4 || trimmedLine.length > 30) {
            println("CardTextParser: Length check failed: ${trimmedLine.length}")
            return false
        }
        
        // Must be mostly letters and spaces
        val letterSpaceCount = trimmedLine.count { it.isLetter() || it.isWhitespace() }
        val letterSpaceRatio = letterSpaceCount.toFloat() / trimmedLine.length
        if (letterSpaceRatio < 0.9) {
            println("CardTextParser: Letter/space ratio check failed: $letterSpaceRatio")
            return false
        }
        
        // Split into words
        val words = trimmedLine.split(Regex("""\s+""")).filter { it.isNotEmpty() }
        println("CardTextParser: Words: $words (count: ${words.size})")
        
        // Must have 2-4 words (First Last, First Middle Last, etc.)
        if (words.size !in 2..4) {
            println("CardTextParser: Word count check failed: ${words.size}")
            return false
        }
        
        // Each word should be 2+ characters and mostly uppercase letters
        val allWordsValid = words.all { word ->
            val lengthOk = word.length >= 2
            val allLetters = word.all { it.isLetter() }
            val uppercaseCount = word.count { it.isUpperCase() }
            val uppercaseRatio = uppercaseCount.toFloat() / word.length
            val uppercaseOk = uppercaseRatio >= 0.8
            
            println("CardTextParser: Word '$word' - length: $lengthOk, allLetters: $allLetters, uppercase ratio: $uppercaseRatio ($uppercaseOk)")
            
            lengthOk && allLetters && uppercaseOk
        }
        
        println("CardTextParser: All words valid: $allWordsValid")
        return allWordsValid
    }
    
    /**
     * Checks if a line is likely to be a person's name (legacy method)
     */
    private fun isLikelyName(line: String): Boolean {
        val upperLine = line.uppercase()
        
        // Must be mostly letters
        if (line.count { it.isLetter() } < line.length * 0.8) return false
        
        // Must have reasonable length
        if (line.length < 3 || line.length > 50) return false
        
        // Must not contain numbers (except maybe Jr, Sr, III, etc.)
        if (line.any { it.isDigit() } && !line.matches(Regex(""".*\b(JR|SR|III|IV|V)\b.*"""))) {
            return false
        }
        
        // Should be in title case or all caps
        val words = line.split(" ")
        return words.all { word ->
            word.isNotEmpty() && (
                word[0].isUpperCase() || // Title case
                word.all { it.isUpperCase() } // All caps
            )
        }
    }
    
    /**
     * Calculates a score for how likely a string is to be a cardholder name
     * Prioritizes capital letter names typical on cards
     */
    private fun calculateCardholderNameScore(name: String): Int {
        var score = 0
        val words = name.split(Regex("""\s+""")).filter { it.isNotEmpty() }
        
        // Prefer 2-3 words (First Last, First Middle Last)
        score += when (words.size) {
            2 -> 15  // First Last - most common
            3 -> 12  // First Middle Last
            4 -> 8   // First Middle Middle Last
            else -> 1
        }
        
        // Prefer reasonable total length
        when (name.length) {
            in 8..20 -> score += 10  // Optimal length
            in 6..25 -> score += 5   // Acceptable length
            else -> score -= 5       // Too short or too long
        }
        
        // Strongly prefer all uppercase (typical on cards)
        if (words.all { it.all { char -> char.isUpperCase() } }) {
            score += 20
        }
        
        // Each word should be reasonable length for a name
        words.forEach { word ->
            when (word.length) {
                in 2..12 -> score += 3  // Good name length
                in 1..1 -> score -= 5   // Too short for a name
                else -> score -= 2      // Too long
            }
        }
        
        // Bonus for common name patterns
        if (words.size == 2 && words.all { it.length in 3..10 }) {
            score += 5  // Classic First Last pattern
        }
        
        return score
    }
    
    /**
     * Calculates a score for how likely a string is to be a name (legacy method)
     */
    private fun calculateNameScore(name: String): Int {
        var score = 0
        
        val words = name.split(" ")
        
        // Prefer 2-3 words (first, middle, last)
        score += when (words.size) {
            2 -> 10
            3 -> 8
            1 -> 3
            else -> 1
        }
        
        // Prefer reasonable length
        if (name.length in 10..30) score += 5
        
        // Prefer title case
        if (words.all { it.isNotEmpty() && it[0].isUpperCase() && it.drop(1).all { c -> c.isLowerCase() } }) {
            score += 5
        }
        
        return score
    }
    
    /**
     * Checks if a number is likely a card number (to avoid confusing with CVV)
     */
    private fun isLikelyCardNumber(number: String): Boolean {
        return number.length >= 13
    }
    
    /**
     * Extracts date from text containing expiry information
     */
    private fun extractDateFromText(text: String): String? {
        val dateRegex = Regex("""(\d{2}/\d{2,4})""")
        return dateRegex.find(text)?.value
    }
    
    /**
     * Formats card number with spaces for readability
     */
    private fun formatCardNumber(cardNumber: String): String {
        return when (cardNumber.length) {
            15 -> cardNumber.chunked(4).joinToString(" ") // Amex format
            16 -> cardNumber.chunked(4).joinToString(" ") // Standard format
            else -> cardNumber
        }
    }
    
    /**
     * Formats expiry date to MM/YY format
     */
    private fun formatExpiryDate(expiryDate: String): String {
        val dateRegex = Regex("""(\d{2})/(\d{2,4})""")
        val match = dateRegex.find(expiryDate) ?: return expiryDate
        
        val month = match.groupValues[1]
        val year = match.groupValues[2]
        
        // Convert to MM/YY format
        val shortYear = if (year.length == 4) year.takeLast(2) else year
        return "$month/$shortYear"
    }
    
    /**
     * Formats cardholder name to proper case
     */
    private fun formatCardholderName(name: String): String {
        return name.trim().split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
    

    
    /**
     * Validates and formats all extracted data
     */
    private fun validateAndFormatData(data: Map<String, String>): Map<String, String> {
        val validatedData = mutableMapOf<String, String>()
        
        data.forEach { (key, value) ->
            val trimmedValue = value.trim()
            if (trimmedValue.isNotEmpty()) {
                validatedData[key] = trimmedValue
            }
        }
        
        return validatedData
    }
}