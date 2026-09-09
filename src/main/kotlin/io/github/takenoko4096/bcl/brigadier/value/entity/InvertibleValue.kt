package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.SpanHolder
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

/**
 * こいつの sourceSpan は ! を含まない あるいは別で保存か？
 */
class InvertibleValue<T>(val inverted: Boolean, val value: T, override val sourceSpan: SourceSpan) : SpanHolder {
    override fun toString(): String {
        return if (inverted) "!$value" else value.toString()
    }

    companion object {
        fun <T> inverted(value: T, span: SourceSpan) = InvertibleValue(true, value, span)

        fun <T> uninverted(value: T, span: SourceSpan) = InvertibleValue(false, value, span)
    }
}
