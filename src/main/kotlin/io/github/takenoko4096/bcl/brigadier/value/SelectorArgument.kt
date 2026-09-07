package io.github.takenoko4096.bcl.brigadier.value

import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class SelectorArgument<T>(val name: String) {
    abstract fun parse(reader: TokenSeqReader): Value<T>

    sealed class Value<T>(val argument: SelectorArgument<T>, val sourceSpan: SourceSpan)
    class Data<T>(argument: SelectorArgument<T>, sourceSpan: SourceSpan, val value: T) : Value<T>(argument, sourceSpan)
    class Filter<T>(argument: SelectorArgument<T>, sourceSpan: SourceSpan, val inclusive: Set<T>, val exclusive: List<T>) : Value<T>(argument, sourceSpan)

    sealed class SinglePropertyFilter<T>(name: String) : SelectorArgument<T>(name) {

    }

    sealed class SetPropertyFilter<T>(name: String) : SelectorArgument<T>(name) {

    }
}
