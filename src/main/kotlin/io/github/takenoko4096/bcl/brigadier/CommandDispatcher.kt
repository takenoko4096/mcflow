package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.CommandNodeBuilder.CommandBuilder
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.TokenType

class CommandDispatcher {
    private val commands = mutableMapOf<String, Command>()

    fun getCommand(name: String): Command? {
        return commands[name]
    }

    fun register(name: String, configurator: CommandBuilder.() -> Unit): Command {
        val command = CommandBuilder(name, configurator).build()

        if (name in commands) {
            throw IllegalArgumentException("コマンド名 '$name' は既に使用されているため登録できません")
        }

        commands[name] = command

        for (string in command.aliases) {
            if (string in commands) {
                throw IllegalArgumentException("コマンド名 '$name' は既に使用されているためエイリアスを登録できません")
            }

            commands[string] = command
        }

        return command
    }

    fun parse(reader: TokenSeqReader): CommandLineParseResult {
        val start = reader.getCurrentTokenStartPosition()

        val token = reader.peek()
        if (token.type != TokenType.WORD) {
            return CommandLineParseResult.Failure(
                null,
                null,
                emptyMap(),
                listOf(
                    BclDiagnostic(
                        "コマンド名の読み取りに失敗しました: ${TokenType.WORD} のトークンが期待されましたが、 ${token.type} のトークン '${token.value}' が見つかりました",
                        reader.sourceCode,
                        token.sourceSpan
                    )
                ),
                reader.getRemainingSourceSpan()
            )
        }

        reader.next()
        val name = token.value

        val root = getCommand(name)
            ?: return CommandLineParseResult.Failure(
                null,
                null,
                emptyMap(),
                listOf(
                    BclDiagnostic(
                        "不明なコマンド名です: $name",
                        reader.sourceCode,
                        start..reader.getPreviousTokenEndPosition()
                    )
                ),
                start..reader.getPreviousTokenEndPosition()
            )

        val initialState = DispatcherParseState(
            reader = reader.clone(),
            arguments = emptyMap(),
            destination = root,
            literalCount = 1
        )

        val potentials = dispatch(
            reader.sourceCode,
            initialState,
            root.children
        )

        if (potentials.isNotEmpty()) {
            val best = selectBest(potentials)

            return if (best.diagnostics.isEmpty()) {
                CommandLineParseResult.Successful(
                    root,
                    best.state.destination,
                    best.state.arguments,
                    start..best.state.reader.getPreviousTokenEndPosition()
                )
            }
            else {
                CommandLineParseResult.Failure(
                    root,
                    best.state.destination,
                    best.state.arguments,
                    best.diagnostics,
                    start..best.state.reader.getPreviousTokenEndPosition()
                )
            }
        }

        val diagnostics = potentials.flatMap { it.diagnostics }

        return CommandLineParseResult.Failure(
            root,
            destination = null,
            emptyMap(),
            diagnostics,
            start..diagnostics.maxBy { it.sourceSpan.end.index }.sourceSpan.end
        )
    }

    private fun selectBest(potentials: List<RoutePotential>): RoutePotential {
        return potentials.maxWith(
            compareBy<RoutePotential> { it.state.reader.isOver() }
                .thenBy { it.diagnostics.isEmpty() }
                .thenBy { it.state.literalCount }
                .thenBy { it.state.reader.cursor }
        )
    }

    private fun dispatch(text: String, state: DispatcherParseState, children: Map<String, CommandNode>): List<RoutePotential> {
        val potentials = mutableListOf<RoutePotential>()

        for ((name, node) in children) {
            val candidateReader = state.reader.clone()
            val candidateArguments = state.arguments.toMutableMap()

            val nextState = DispatcherParseState(
                candidateReader,
                candidateArguments,
                node,
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
                potentials += RoutePotential(
                    nextState,
                    listOf(
                        BclDiagnostic(
                            """
                            引数が正しくありません (${e.description})
                            ${
                                if (children.size == 1) {
                                    children.values.first().toString() + " が期待されています"
                                }
                                else {
                                    children.map { it.value.toString() }.joinToString(" | ", postfix = " のいずれかが期待されています")
                                }
                            }
                            """.trimIndent(),
                            text,
                            e.span
                        )
                    )
                )
                continue
            }

            // node自身で終了する可能性
            if (node.executable) {
                potentials += RoutePotential(
                    nextState,
                    if (candidateReader.isOver()) {
                        emptyList()
                    }
                    else {
                        listOf(
                            BclDiagnostic(
                                "予期しない引数です: ${candidateReader.peek()}",
                                candidateReader.sourceCode,
                                candidateReader.getRemainingSourceSpan()
                            )
                        )
                    }
                )
            }

            // さらに子へ進む可能性
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
