package io.github.takenoko4096.bcl.ast.command

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandLineParseResult(
    sourceSpan: SourceSpan,
    val name: String,
    val arguments: Map<String, Argument<*>>
) : Node(sourceSpan)
