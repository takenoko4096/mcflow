package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class BclSemanticTokenType : StringRepresentable {
    LITERAL,
    NUMERIC,
    STRING,
    COMMAND,
    ENTITY_SELECTOR,
    IDENTIFIER,
    BOOLEAN,
    PROPERTY,
    KEYWORD,
    AXES,
    LABEL;

    override fun getString(): String {
        return name.lowercase()
    }
}
