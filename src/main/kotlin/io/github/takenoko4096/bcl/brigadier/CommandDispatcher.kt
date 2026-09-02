package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.ast.command.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.TokenType

class CommandDispatcher {
    private val commands = mutableSetOf<Command>()

    fun getCommand(name: String): Command? {
        return commands.find { it.root.literal == name }
    }

    fun parse(reader: TokenSeqReader): CommandLineParseResult {
        val start = reader.getCurrentTokenStartPosition()

        val name = reader.read(TokenType.WORD)

        val command = getCommand(name)
            ?: run {
                val span = start..reader.getPreviousTokenEndPosition()

                return CommandLineParseResult.Failure(
                    listOf(Diagnostic("不明なコマンド名です: $name", span)),
                    span
                )
            }

        val arguments = mutableMapOf<String, Argument<*>>()
        val diagnostics = mutableListOf<Diagnostic>()
        dispatch(reader, arguments, diagnostics, command.root.children)

        val end = reader.getPreviousTokenEndPosition()

        return if (diagnostics.isEmpty()) {
            CommandLineParseResult.Successful(command, arguments, start..end)
        }
        else {
            CommandLineParseResult.Failure(diagnostics, start..end)
        }
    }

    private fun dispatch(reader: TokenSeqReader, arguments: MutableMap<String, Argument<*>>, diagnostics: MutableList<Diagnostic>, children: Set<CommandNode>) {
        val t = reader.peek()

        for (node in children.filterIsInstance<LiteralCommandNode>()) {
            if (t.type == TokenType.WORD && t.value == node.literal) {
                reader.next()
                return dispatch(reader, arguments, diagnostics, node.children)
            }
        }

        val currentCursor = reader.cursor
        var argument: Argument<*>
        for (node in children.filterIsInstance<ArgumentCommandNode<*>>()) {
            try {
                argument = node.type.parse(reader)
            }
            catch (_: BclTokenSeqReadException) {
                reader.cursor = currentCursor
                continue
            }

            arguments[node.name] = argument
            return dispatch(reader, arguments, diagnostics, node.children)
        }

        diagnostics.add(Diagnostic(
            "...(書き途中)"
        ))
    }
}
