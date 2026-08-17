package com.example.parser

import com.example.data.model.DealerSettings
import com.example.data.model.ParseResult
import com.example.data.model.ParsedBetItem

object TwoDParser {

    private val MYANMAR_DIGITS = mapOf(
        '၀' to '0', '၁' to '1', '၂' to '2', '၃' to '3', '၄' to '4',
        '၅' to '5', '၆' to '6', '၇' to '7', '၈' to '8', '၉' to '9'
    )

    val TWINS = listOf("00", "11", "22", "33", "44", "55", "66", "77", "88", "99")
    val POWERS = listOf("05", "50", "16", "61", "27", "72", "38", "83", "49", "94")
    val NAKHATS = listOf("07", "70", "18", "81", "24", "42", "35", "53", "69", "96")
    val BROTHERS = listOf(
        "01", "10", "12", "21", "23", "32", "34", "43", "45", "54",
        "56", "65", "67", "76", "78", "87", "89", "98", "90", "09"
    )

    fun normalizeMyanmarNumbers(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(MYANMAR_DIGITS[ch] ?: ch)
        }
        return sb.toString()
    }

    /**
     * Parses 2D bet text containing various formulas and formats.
     * Supports:
     * - Direct: 12-500, 12/500, 12 500, 12=500, 12,34,56-500
     * - Reverse: 12 R 500, 12r500, 12-500R, 12 ပြန် 500
     * - Twins: ပူး 500, အပူး 500, double 500
     * - Power: ပါဝါ 500, pw 500, power 500
     * - Nakhat: နက္ခတ် 500, nk 500, nakhat 500
     * - Brothers: ညီကို 500, brother 500
     * - Combinations: 123 ခွေ 500, 1234ခွေ 500, 123 ခွေပူး 500
     * - Round/Pat: 1 ပတ် 500, 1ပတ် 500, 1 round 500
     * - Head: 1 ထိပ် 500, 1ထိပ် 500, 1 head 500
     * - Tail: 1 ပိတ် 500, 1ပိတ် 500, 1 tail 500
     * - Break: 5 ဘရိတ် 500, 5ဘရိတ် 500, 5 break 500
     */
    fun parseBetText(rawInput: String, settings: DealerSettings): ParseResult {
        val normalized = normalizeMyanmarNumbers(rawInput.trim())
        if (normalized.isBlank()) {
            return ParseResult(emptyList(), 0.0, 0, emptyList(), emptyList(), rawInput)
        }

        val items = mutableListOf<ParsedBetItem>()
        val unrecognized = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Split by lines or semicolons
        val lines = normalized.split(Regex("[\\n;]+")).map { it.trim() }.filter { it.isNotBlank() }

        for (line in lines) {
            val parsedFromLine = parseSingleLine(line, settings)
            if (parsedFromLine.isNotEmpty()) {
                items.addAll(parsedFromLine)
            } else {
                // Try splitting by comma if single line contains multiple expressions
                val commaTokens = line.split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (commaTokens.size > 1) {
                    var lineParsedAny = false
                    for (token in commaTokens) {
                        val subParsed = parseSingleLine(token, settings)
                        if (subParsed.isNotEmpty()) {
                            items.addAll(subParsed)
                            lineParsedAny = true
                        } else {
                            unrecognized.add(token)
                        }
                    }
                    if (!lineParsedAny) {
                        unrecognized.add(line)
                    }
                } else {
                    unrecognized.add(line)
                }
            }
        }

        val totalAmount = items.sumOf { it.amount }
        val belowMinCount = items.count { it.isBelowMin }
        val aboveMaxCount = items.count { it.isAboveMaxSingle }

        if (belowMinCount > 0) {
            warnings.add("အနည်းဆုံး ထိုးကြေး (${settings.minBetAmount.toInt()} ${settings.currency}) အောက်ရောက်နေသော ဂဏန်း ($belowMinCount) ခု ရှိပါသည်။")
        }
        if (aboveMaxCount > 0) {
            warnings.add("သတ်မှတ် Limit (${settings.maxBetAmount.toInt()} ${settings.currency}) ကျော်လွန်သော တစ်ကွက်ထိုးကြေး ($aboveMaxCount) ခု ပါဝင်ပါသည်။")
        }

        return ParseResult(
            items = items,
            totalAmount = totalAmount,
            itemCount = items.size,
            unrecognizedTokens = unrecognized,
            warnings = warnings,
            rawText = rawInput
        )
    }

    private fun parseSingleLine(line: String, settings: DealerSettings): List<ParsedBetItem> {
        val clean = line.replace("+", " ").replace("=", " ").replace(":", " ").replace("-", " ").replace("/", " ")
        val tokens = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()

        val lowerLine = line.lowercase()

        // 1. Twins (အပူး / ပူး / double)
        if (lowerLine.contains("အပူး") || lowerLine.contains("ပူး") || lowerLine.contains("double") || lowerLine.contains("twins")) {
            val amount = extractTrailingAmount(clean)
            if (amount != null && amount > 0) {
                return TWINS.map { createParsedItem(it, amount, "အပူး", settings) }
            }
        }

        // 2. Power (ပါဝါ / power / pw)
        if (lowerLine.contains("ပါဝါ") || lowerLine.contains("power") || lowerLine.contains("pw")) {
            val amount = extractTrailingAmount(clean)
            if (amount != null && amount > 0) {
                return POWERS.map { createParsedItem(it, amount, "ပါဝါ", settings) }
            }
        }

        // 3. Nakhat (နက္ခတ် / nakhat / nk)
        if (lowerLine.contains("နက္ခတ်") || lowerLine.contains("nakhat") || lowerLine.contains("nk")) {
            val amount = extractTrailingAmount(clean)
            if (amount != null && amount > 0) {
                return NAKHATS.map { createParsedItem(it, amount, "နက္ခတ်", settings) }
            }
        }

        // 4. Brothers (ညီကို / brother / nk / bk)
        if (lowerLine.contains("ညီကို") || lowerLine.contains("brother")) {
            val amount = extractTrailingAmount(clean)
            if (amount != null && amount > 0) {
                return BROTHERS.map { createParsedItem(it, amount, "ညီကို", settings) }
            }
        }

        // 5. Permutation / Khway (အခွေ / ခွေ) e.g. 123 ခွေ 500, 123ခွေ500, 1234 ခွေ 200, 123 ခွေပူး 500
        val khwayRegex = Regex("([0-9]{3,6})\\s*(အခွေ|ခွေပူး|ခွေ|khway)\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        val khwayMatch = khwayRegex.find(line)
        if (khwayMatch != null) {
            val digits = khwayMatch.groupValues[1]
            val typeStr = khwayMatch.groupValues[2]
            val amount = khwayMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val includeTwins = typeStr.contains("ပူး")
            val numbers = generateCombinations(digits, includeTwins)
            return numbers.map { createParsedItem(it, amount, if (includeTwins) "အခွေပူး" else "အခွေ", settings) }
        }

        // 6. Round / Pat (အပတ် / ပတ် / round / pat) e.g. 1 ပတ် 500, 1ပတ် 500
        val patRegex = Regex("([0-9])\\s*(အပတ်|ပတ်|round|pat)\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        val patMatch = patRegex.find(line)
        if (patMatch != null) {
            val digit = patMatch.groupValues[1][0]
            val amount = patMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val numbers = generatePat(digit)
            return numbers.map { createParsedItem(it, amount, "$digit ပတ်", settings) }
        }

        // 7. Head (ထိပ် / head / front) e.g. 1 ထိပ် 500
        val headRegex = Regex("([0-9])\\s*(ထိပ်|head|front)\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        val headMatch = headRegex.find(line)
        if (headMatch != null) {
            val digit = headMatch.groupValues[1][0]
            val amount = headMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val numbers = generateHead(digit)
            return numbers.map { createParsedItem(it, amount, "$digit ထိပ်", settings) }
        }

        // 8. Tail (ပိတ် / tail / back) e.g. 1 ပိတ် 500
        val tailRegex = Regex("([0-9])\\s*(ပိတ်|tail|back)\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        val tailMatch = tailRegex.find(line)
        if (tailMatch != null) {
            val digit = tailMatch.groupValues[1][0]
            val amount = tailMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val numbers = generateTail(digit)
            return numbers.map { createParsedItem(it, amount, "$digit ပိတ်", settings) }
        }

        // 9. Break (ဘရိတ် / break / br) e.g. 5 ဘရိတ် 500
        val breakRegex = Regex("([0-9])\\s*(ဘရိတ်|break|br)\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        val breakMatch = breakRegex.find(line)
        if (breakMatch != null) {
            val digit = breakMatch.groupValues[1].toIntOrNull() ?: 0
            val amount = breakMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val numbers = generateBreak(digit)
            return numbers.map { createParsedItem(it, amount, "$digit ဘရိတ်", settings) }
        }

        // 10. Reverse R formats (e.g. "12 R 500", "12r500", "12-500R", "12 ပြန် 500", "12, 34, 56 R 500")
        val isReverse = lowerLine.contains("r") || lowerLine.contains("ပြန်") || lowerLine.contains("အပြန်")
        if (isReverse) {
            val parsedReverse = parseReverseLine(line, settings)
            if (parsedReverse.isNotEmpty()) return parsedReverse
        }

        // 11. Multiple numbers with one amount (e.g. "12 34 56 - 500" or "12.34.56 500" or "12,34,56=500")
        val multiNumParsed = parseMultiNumberLine(line, settings)
        if (multiNumParsed.isNotEmpty()) return multiNumParsed

        // 12. Standard single number: e.g. "23 500" or "23-500" or "23/500"
        val singleNumRegex = Regex("^([0-9]{2})\\s*[-/=: ]*\\s*([0-9]+)$")
        val singleMatch = singleNumRegex.find(clean.trim())
        if (singleMatch != null) {
            val num = singleMatch.groupValues[1]
            val amount = singleMatch.groupValues[2].toDoubleOrNull() ?: 0.0
            return listOf(createParsedItem(num, amount, "တိုက်ရိုက်", settings))
        }

        return emptyList()
    }

    private fun parseReverseLine(line: String, settings: DealerSettings): List<ParsedBetItem> {
        val clean = line.replace("r", " R ")
            .replace("ပြန်", " R ")
            .replace("အပြန်", " R ")
            .replace("+", " ")
            .replace("=", " ")
            .replace(":", " ")
            .replace("-", " ")
            .replace("/", " ")

        val tokens = clean.split(Regex("[\\s,.]+")).filter { it.isNotBlank() }
        val rIndex = tokens.indexOfFirst { it.equals("R", ignoreCase = true) }
        if (rIndex == -1) return emptyList()

        // Amount is usually after R or at the end
        val amount = tokens.lastOrNull()?.toDoubleOrNull() ?: return emptyList()

        // Numbers are before R (or all 2-digit tokens other than amount and R)
        val result = mutableListOf<ParsedBetItem>()
        for (i in 0 until tokens.size - 1) {
            val token = tokens[i]
            if (token.equals("R", ignoreCase = true)) continue
            if (token.length == 2 && token.all { it.isDigit() }) {
                result.add(createParsedItem(token, amount, "R (တိုက်ရိုက်)", settings))
                val rev = token.reversed()
                if (rev != token) {
                    result.add(createParsedItem(rev, amount, "R (အပြန်)", settings))
                }
            }
        }
        return result
    }

    private fun parseMultiNumberLine(line: String, settings: DealerSettings): List<ParsedBetItem> {
        // Find trailing amount
        val clean = line.replace("+", " ")
            .replace("=", " ")
            .replace(":", " ")
            .replace("-", " ")
            .replace("/", " ")
            .replace(",", " ")

        val tokens = clean.split(Regex("[\\s.]+")).filter { it.isNotBlank() }
        if (tokens.size < 2) return emptyList()

        val amountStr = tokens.last()
        val amount = amountStr.toDoubleOrNull() ?: return emptyList()

        val numbers = mutableListOf<String>()
        for (i in 0 until tokens.size - 1) {
            val token = tokens[i]
            if (token.length == 2 && token.all { it.isDigit() }) {
                numbers.add(token)
            }
        }

        if (numbers.isNotEmpty()) {
            return numbers.map { createParsedItem(it, amount, "တိုက်ရိုက်", settings) }
        }
        return emptyList()
    }

    private fun extractTrailingAmount(cleanLine: String): Double? {
        val tokens = cleanLine.split(Regex("\\s+")).filter { it.isNotBlank() }
        for (token in tokens.reversed()) {
            val num = token.toDoubleOrNull()
            if (num != null && num > 0) return num
        }
        return null
    }

    fun generateCombinations(digits: String, includeTwins: Boolean): List<String> {
        val uniqueDigits = digits.toSet().toList()
        val result = mutableListOf<String>()
        for (i in uniqueDigits.indices) {
            for (j in uniqueDigits.indices) {
                if (i != j) {
                    result.add("${uniqueDigits[i]}${uniqueDigits[j]}")
                } else if (includeTwins) {
                    result.add("${uniqueDigits[i]}${uniqueDigits[j]}")
                }
            }
        }
        return result.sorted()
    }

    fun generatePat(digit: Char): List<String> {
        val result = mutableSetOf<String>()
        for (i in 0..9) {
            result.add("$digit$i")
            result.add("$i$digit")
        }
        return result.sorted()
    }

    fun generateHead(digit: Char): List<String> {
        val result = mutableListOf<String>()
        for (i in 0..9) {
            result.add("$digit$i")
        }
        return result
    }

    fun generateTail(digit: Char): List<String> {
        val result = mutableListOf<String>()
        for (i in 0..9) {
            result.add("$i$digit")
        }
        return result
    }

    fun generateBreak(breakDigit: Int): List<String> {
        val result = mutableListOf<String>()
        for (i in 0..99) {
            val formatted = String.format("%02d", i)
            val sum = (formatted[0] - '0') + (formatted[1] - '0')
            if (sum % 10 == breakDigit % 10) {
                result.add(formatted)
            }
        }
        return result
    }

    private fun createParsedItem(
        number: String,
        amount: Double,
        formulaType: String,
        settings: DealerSettings
    ): ParsedBetItem {
        val formattedNum = if (number.length == 1) "0$number" else number
        val isBelow = amount < settings.minBetAmount
        val isAbove = amount > settings.maxBetAmount
        return ParsedBetItem(
            number = formattedNum,
            amount = amount,
            formulaType = formulaType,
            isBelowMin = isBelow,
            isAboveMaxSingle = isAbove
        )
    }
}
