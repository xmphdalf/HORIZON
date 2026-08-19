package dev.horizon.buildlogic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BangBangScannerTest {

    @Test
    fun `finds the operator`() {
        val source = """
            fun f(v: String?): String = v!!
        """.trimIndent()
        assertEquals(listOf(1), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `finds the operator in a chained call`() {
        val source = "fun f(v: String?): Int = v!!.length"
        assertEquals(listOf(1), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `ignores it inside a string literal`() {
        // An error message is not an operator. A gate that rejects this would
        // block valid code, which is worse than the gap it closes.
        val source = """const val SHOUT = "Careful!!" """
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `ignores it inside a raw string`() {
        val source = "val s = \"\"\"wow!!\"\"\""
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `ignores it inside a line comment`() {
        val source = "// never write v!! here\nval x = 1"
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `ignores it inside a block comment`() {
        val source = "/*\n * v!! is forbidden\n */\nval x = 1"
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `ignores it inside a KDoc block`() {
        val source = "/** Do not use v!! anywhere. */\nval x = 1"
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `survives an escaped quote inside a string`() {
        val source = """val s = "she said \"careful!!\" loudly" """
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `reports the right line number after a multi-line comment`() {
        val source = "/*\n *\n */\nval a = 1\nfun f(v: String?) = v!!"
        assertEquals(listOf(5), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `reports every offending line`() {
        val source = "fun a(v: String?) = v!!\nval b = 1\nfun c(v: String?) = v!!"
        assertEquals(listOf(1, 3), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `does not confuse referential inequality for the operator`() {
        val source = "val same = a !== b"
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }

    @Test
    fun `accepts the project's own value class unchanged`() {
        val source = """
            @JvmInline
            public value class Kelvin(public val value: Int) {
                init {
                    require(value in MIN..MAX) { "Kelvin must be in ${'$'}MIN..${'$'}MAX, was ${'$'}value" }
                }
            }
        """.trimIndent()
        assertEquals(emptyList<Int>(), BangBangScanner.offendingLines(source))
    }
}
