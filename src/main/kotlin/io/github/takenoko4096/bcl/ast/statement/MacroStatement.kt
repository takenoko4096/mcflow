package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class MacroStatement(
    sourceSpan: SourceSpan,
    val identifier: String,
    val parameters: List<String>,
    val result: CommandLineParseResult
) : Statement(sourceSpan, null)
