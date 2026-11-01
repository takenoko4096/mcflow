package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class MacroStatement(
    sourceSpan: SourceSpan,
    semanticTokens: List<BclSemanticToken>,
    val identifier: String,
    val parameters: List<String>,
    val result: CommandLineParseResult
) : Statement(sourceSpan, semanticTokens)
