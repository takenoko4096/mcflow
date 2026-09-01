package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Parameter
import io.github.takenoko4096.bcl.ast.command.CommandLine
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class MacroStatement(
    sourceSpan: SourceSpan,
    val identifier: String,
    val parameters: List<Parameter>,
    val commandLine: CommandLine
) : Statement(sourceSpan)
