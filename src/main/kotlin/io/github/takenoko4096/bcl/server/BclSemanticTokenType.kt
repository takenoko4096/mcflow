package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class BclSemanticTokenType : StringRepresentable {
    NUMBER,
    STRING,
    COMMAND,
    IDENTIFIER,
    BOOLEAN,
    PARAMETER,
    LITERAL,
    LABEL,
    SELECTOR_TYPE,
    SYMBOL,

    ERROR;

    override fun getString(): String {
        return name.lowercase()
    }
}
