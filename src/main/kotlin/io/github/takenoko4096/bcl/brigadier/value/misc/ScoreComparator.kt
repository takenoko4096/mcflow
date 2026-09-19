package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.tokenizer.Symbol

enum class ScoreComparator(val symbol: Symbol) : StringRepresentable {
    EQUALS(Symbol.ASSIGN),
    LESS(Symbol.LOGICAL_LESS),
    MORE(Symbol.LOGICAL_MORE),
    LESS_OR_EQUALS(Symbol.LOGICAL_LESS_OR_EQUALS),
    MORE_OR_EQUALS(Symbol.LOGICAL_MORE_OR_EQUALS);

    override fun getString(): String {
        return symbol.value
    }
}
