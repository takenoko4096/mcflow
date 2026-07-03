package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ScoreAliasStatement(
    sourceSpan: SourceSpan,
    val isReadonly: Boolean,
    val identifier: String,
    val holder: ScoreHolderExpression,
    val objective: String
)

// score f = @s money
