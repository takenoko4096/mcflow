package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class SelectorArgument<T>(val name: String, val limitation: Limitation) {
    abstract fun parse(reader: TokenSeqReader, arguments: Set<Value<*>>): Value<T>

    protected fun limit() {

    }

    sealed class Value<T>(val argument: SelectorArgument<T>, val sourceSpan: SourceSpan)
    class Data<T>(argument: SelectorArgument<T>, sourceSpan: SourceSpan, val value: T) : Value<T>(argument, sourceSpan)
    class Filter<T>(argument: SelectorArgument<T>, sourceSpan: SourceSpan, val inclusive: Set<T>, val exclusive: List<T>) : Value<T>(argument, sourceSpan)

    enum class Limitation {
        ASSIGNABLE_ONCE,
        EXCLUDABLE,
        NONE
    }
}
