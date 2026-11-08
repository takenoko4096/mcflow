package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.argument.type.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class NoValuedArgument<T>(
    type: ArgumentType<T>,
    sourceSpan: SourceSpan?,
    val diagnostics: List<BclDiagnostic>
) : ArgumentParseResult<T>(type, sourceSpan)
