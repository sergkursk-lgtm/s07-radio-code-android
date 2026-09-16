package com.soueast.s07code

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CodeGeneratorTest {

    @Test
    fun `known input produces known code`() {
        // 09/05/07 -> "090507" * 240830 % 1000000 = 800810
        assertEquals("800810", generateCode(9, 5, 7))
    }

    @Test
    fun `code is always six ascii digits even on locales with non-ascii digits`() {
        val defaultLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale("ar", "EG"))
            for (month in 1..12) {
                for (day in 1..28) {
                    for (hour in 0..23) {
                        val code = generateCode(month, day, hour)
                        assertTrue(
                            "Код '$code' для $month/$day/$hour не из ASCII-цифр",
                            code.matches(Regex("^[0-9]{6}$"))
                        )
                    }
                }
            }
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }
}
