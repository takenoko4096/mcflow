package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

/**
 * [String], [Int], [Boolean], `enum class` みたいな [SourceSpan] を付けられないオブジェクトに使うもの
 */
data class WithSpan<T>(val value: T, val sourceSpan: SourceSpan) {
    companion object {
        fun <T> noSpan(value: T): WithSpan<T> {
            return WithSpan(value, SourceSpan.NULL)
        }
    }
}
