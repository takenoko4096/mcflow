package io.github.takenoko4096.bcl.ast.command

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.Diagnostic
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandLineParseResult(sourceSpan: SourceSpan) : Node(sourceSpan) {
    class Successful(val command: Command, val destination: String, val arguments: Map<String, Argument<*>>, sourceSpan: SourceSpan) : CommandLineParseResult(sourceSpan)

    class Failure(val command: Command?, val destination: String, val diagnostics: List<Diagnostic>, sourceSpan: SourceSpan) : CommandLineParseResult(sourceSpan)
}
