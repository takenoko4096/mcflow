package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.brigadier.CommandNodeBuilder.RootCommandNodeBuilder
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.TokenType
import kotlin.collections.contains
import kotlin.collections.set

class CommandDispatcher {
    private val commands = mutableMapOf<String, Command>()

    fun getCommand(name: String): Command? {
        return commands[name]
    }

    fun register(name: String, configurator: RootCommandNodeBuilder.() -> Unit): Command {
        val builder = RootCommandNodeBuilder(name)
        builder.configurator()
        val cmd = builder.command()
        if (name in commands) {
            throw IllegalArgumentException()
        }
        commands[name] = cmd
        for (string in cmd.aliases) {
            if (string in commands) {
                throw IllegalArgumentException()
            }
            commands[string] = cmd
        }
        return cmd
    }

    private data class ParseState(
        val reader: TokenSeqReader,
        val arguments: Map<String, Argument<*>>,
        val destination: String,
        val literalCount: Int
    )

    private data class Potential(
        val state: ParseState,
        val diagnostics: List<Diagnostic>
    )

    fun parse(reader: TokenSeqReader): CommandLineParseResult {
        val start = reader.getCurrentTokenStartPosition()

        val name = reader.read(TokenType.WORD)

        val command = getCommand(name)
            ?: return CommandLineParseResult.Failure(
                null,
                name,
                listOf(
                    Diagnostic(
                        "不明なコマンド名です: $name",
                        reader.sourceCode,
                        start..reader.getPreviousTokenEndPosition()
                    )
                ),
                start..reader.getPreviousTokenEndPosition()
            )

        val initialState = ParseState(
            reader = reader.clone(),
            arguments = emptyMap(),
            destination = name,
            literalCount = 1
        )

        val potentials = dispatch(
            reader.sourceCode,
            initialState,
            command.root.children
        )

        if (potentials.isNotEmpty()) {
            val best = selectBest(potentials)

            return if (best.diagnostics.isEmpty()) {
                CommandLineParseResult.Successful(
                    command,
                    best.state.destination,
                    best.state.arguments,
                    start..best.state.reader.getPreviousTokenEndPosition()
                )
            }
            else {
                CommandLineParseResult.Failure(
                    command,
                    best.state.destination,
                    best.diagnostics,
                    start..best.state.reader.getPreviousTokenEndPosition()
                )
            }
        }

        val diagnostics = potentials.flatMap { it.diagnostics }

        return CommandLineParseResult.Failure(
            command,
            destination = "?",
            diagnostics,
            start..diagnostics.maxBy { it.sourceSpan.end.index }.sourceSpan.end
        )
    }

    private fun selectBest(potentials: List<Potential>): Potential {
        return potentials.maxWith(
            compareBy<Potential> { it.state.reader.isOver() }
                .thenBy { it.diagnostics.isEmpty() }
                .thenBy { it.state.literalCount }
                .thenBy { it.state.reader.cursor }
        )
    }

    private fun dispatch(text: String, state: ParseState, children: Map<String, CommandNode>): List<Potential> {
        val potentials = mutableListOf<Potential>()

        for ((name, node) in children) {
            val candidateReader = state.reader.clone()
            val candidateArguments = state.arguments.toMutableMap()

            val nextState = ParseState(
                candidateReader,
                candidateArguments,
                name,
                state.literalCount + if (node is LiteralCommandNode) 1 else 0
            )

            try {
                when (node) {
                    is LiteralCommandNode -> {
                        val token = candidateReader.peek()

                        if (token.type != TokenType.WORD || token.value != name) {
                            continue
                        }

                        candidateReader.next()
                    }

                    is ArgumentCommandNode<*> -> {
                        val argument = node.type.parse(candidateReader)
                        candidateArguments[name] = argument
                    }
                }
            }
            catch (e: BclTokenSeqReadException) {
                potentials += Potential(
                    nextState,
                    listOf(
                        Diagnostic("引数が正しくありません (${e.description})", text, e.span)
                    )
                )
                continue
            }

            // 2. node自身で終了する可能性
            if (node.executable) {
                potentials += Potential(
                    nextState,
                    if (candidateReader.isOver()) {
                        emptyList()
                    }
                    else {
                        listOf(
                            Diagnostic(
                                "予期しない引数です: ${candidateReader.peek()}",
                                candidateReader.sourceCode,
                                candidateReader.getRemainingSourceSpan()
                            )
                        )
                    }
                )
            }

            // 3. さらに子へ進む可能性
            if (node.children.isNotEmpty()) {
                potentials += dispatch(
                    text,
                    nextState,
                    node.children
                )
            }

            /*if (node.children.isEmpty()) {
                if (node.executable) {
                    potentials += if (!candidateReader.isOver()) {
                        Potential(
                            nextState,
                            listOf(
                                Diagnostic("予期しない引数です", text, candidateReader.peek().sourceSpan)
                            )
                        )
                    } else Potential(
                        nextState,
                        listOf()
                    )
                }
                else if (candidateReader.isOver()) {
                    potentials += Potential(
                        nextState,
                        listOf(
                            Diagnostic("引数が不足しています", text, candidateReader.peekSafely(offset = -1).sourceSpan)
                        )
                    )
                }
                else {
                    potentials += Potential(
                        nextState,
                        listOf(
                            Diagnostic("予期しない引数です", text, candidateReader.peek().sourceSpan)
                        )
                    )
                }
            }
            else {
                val nextPotentials = dispatch(text, nextState, node.children)
                potentials += nextPotentials
            }*/
        }

        return potentials
    }
}
