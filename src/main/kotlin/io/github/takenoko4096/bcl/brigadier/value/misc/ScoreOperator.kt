package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.tokenizer.Symbol

enum class ScoreOperator(val symbol: Symbol) : StringRepresentable {
    ASSIGN(Symbol.ASSIGN),
    PLUS(Symbol.PLUS_ASSIGN),
    MINUS(Symbol.MINUS_ASSIGN),
    MULTIPLY(Symbol.MULTIPLY_ASSIGN),
    DIVIDE(Symbol.DIVIDE_ASSIGN),
    MOD(Symbol.MOD_ASSIGN),
    MIN(Symbol.LOGICAL_LESS),
    MAX(Symbol.LOGICAL_MORE),
    SWAP(Symbol.SWAP);

    override fun getString(): String {
        return symbol.value
    }
}
