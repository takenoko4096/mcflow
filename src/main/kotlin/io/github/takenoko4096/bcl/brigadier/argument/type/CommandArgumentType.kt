package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandRequireContext
import io.github.takenoko4096.bcl.brigadier.node.Command
import io.github.takenoko4096.bcl.completer.BclCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind

class CommandArgumentType : ArgumentType<Command>("command") {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<Command> {
        val start = context.reader.getCurrentTokenStartPosition()
        val name = context.reader.readCommandString()
        val end = context.reader.getPreviousTokenEndPosition()

        context.semanticTokens.add(BclSemanticToken(
            BclSemanticTokenType.ENUM,
            start..end
        ))

        val command = context.dispatcher.getCommand(name)
            ?: return NoValuedArgument(
                this,
                start..end,
                context.diagnostics.apply {
                    add(context.reader.diagnostic(
                        start..end,
                        "不明なコマンド名です: '$name'"
                    ))
                }
            )

        val requireContext = CommandRequireContext(
            sourceCode = context.reader.sourceCode,
            command,
            start..end,
            context.sender
        )

        if (command.predicate == null || command.predicate(requireContext) is CommandRequireContext.Successful) {
            return Argument(this, command, start..end)
        }

        return NoValuedArgument(
            this,
            start..end,
            context.diagnostics.apply {
                add(context.reader.diagnostic(
                    start..end,
                    "コマンド '$name' を参照する権限がありません"
                ))
            }
        )
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        val commands = context.dispatcher.getRoot()
            .getEffectiveChildren()
            .filter { (_, value) ->
                val requireContext = CommandRequireContext(
                    sourceCode = context.sourceCode,
                    value,
                    SourceSpan.NULL,
                    context.sender
                )
                value.predicate == null || value.predicate(requireContext) is CommandRequireContext.Successful
            }

        val result = context.newCompleter().completeUnquotedString(
            commands.map { it.key },
            CompletionItemKind.EnumMember
        )

        if (result is BclCompleter.ShouldComplete) {
            return result.items
        }

        return emptyList()
    }
}
