package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.CommandNodeBuilder.CommandBuilder
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.TokenType

class CommandDispatcher {
    private val commands = mutableMapOf<String, Command>()

    fun getCommand(name: String): Command? {
        return commands[name]
    }

    fun getKnownCommands(): Set<Command> {
        return commands.values.toSet()
    }

    fun register(command: Command): Command {
        if (command.name in commands) {
            throw IllegalArgumentException("コマンド名 '${command.name}' は既に使用されているため登録できません")
        }

        commands[command.name] = command

        for (string in command.aliases) {
            if (string in commands) {
                throw IllegalArgumentException("コマンド名 '${command.name}' は既に使用されているためエイリアスを登録できません")
            }

            commands[string] = command
        }

        return command
    }

    fun register(name: String, configurator: CommandBuilder.() -> Unit): Command {
        return register(Command.create(name, configurator))
    }

    fun parse(reader: TokenSeqReader): CommandLineParseResult {
        val start = reader.getCurrentTokenStartPosition()

        val name = try {
            reader.readCommandString()
        }
        catch (error: BclTokenSeqReadException) {
            return CommandLineParseResult.Failure(
                null,
                null,
                emptyMap(),
                listOf(
                    BclDiagnostic(
                        "コマンド名の読み取りに失敗しました: ${error.description}",
                        reader.sourceCode,
                        error.sourceSpan
                    )
                ),
                start..reader.getPreviousTokenEndPosition()
            )
        }

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

        if (root.executor != null) {
            potentials += RoutePotential(
                initialState,
                if (reader.isOver()) {
                    emptyList()
                }
                else {
                    listOf(
                        BclDiagnostic(
                            "予期しない引数です: ${reader.peek()}",
                            reader.sourceCode,
                            reader.peek().sourceSpan
                        )
                    )
                }
            )
        }

        if (potentials.isNotEmpty()) {
            val best = selectBest(potentials)

            return if (best.diagnostics.isEmpty()) {
                val context = CommandContext(reader.sourceCode, this, best.state.arguments)

                val resultValue = try {
                    best.state.destination.executor!!(context)
                }
                catch (error: BclCommandExecutionException) {
                    return CommandLineParseResult.Failure(
                        root,
                        best.state.destination,
                        best.state.arguments,
                        listOf(BclDiagnostic(
                            error.description,
                            reader.sourceCode,
                            error.sourceSpan
                        )),
                        start..best.state.reader.getPreviousTokenEndPosition()
                    )
                }

                CommandLineParseResult.Successful(
                    root,
                    best.state.destination,
                    best.state.arguments,
                    start..best.state.reader.getPreviousTokenEndPosition(),
                    resultValue
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

        val end = reader.peekSafely(-1).sourceSpan.end
        val sourceSpan = start..end

        return CommandLineParseResult.Failure(
            root,
            destination = null,
            emptyMap(),
            listOf(
                BclDiagnostic(
                    "?",
                    reader.sourceCode,
                    sourceSpan
                )
            ),
            sourceSpan
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

    private fun dispatch(text: String, state: DispatcherParseState, children: Map<String, CommandNode>): MutableList<RoutePotential> {
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
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.peekSafely(-1).sourceSpan,
                                "${state.destination} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val token = candidateReader.peek()

                        if (token.type != TokenType.WORD || token.value != name) {
                            continue
                        }

                        candidateReader.next()
                    }

                    is ArgumentCommandNode<*> -> {
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.peekSafely(-1).sourceSpan,
                                "${state.destination} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val argument = node.type.parse(candidateReader)
                        candidateArguments[name] = argument
                    }
                }
            }
            catch (error: BclTokenSeqReadException) {
                potentials += RoutePotential(
                    nextState,
                    listOf(
                        BclDiagnostic(
                            """
                            引数が正しくありません (${error.description})
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
                            error.sourceSpan
                        )
                    )
                )
                continue
            }

            // node自身で終了する可能性
            if (node.executor != null) {
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
                                candidateReader.peek().sourceSpan
                            )
                        )
                    }
                )
            }

            // さらに子へ進む可能性
            if (node.children.isNotEmpty()) {
                val childPotentials = dispatch(
                    text,
                    nextState,
                    node.children
                )

                if (childPotentials.isNotEmpty()) {
                    potentials += childPotentials
                }
                else {
                    potentials += RoutePotential(
                        nextState,
                        listOf(
                            BclDiagnostic(
                                if (node.children.size == 1) {
                                    "予期しない引数です: ${node.children.values.first()} が期待されています"
                                }
                                else {
                                    node.children.values.joinToString(prefix = "予期しない引数です: ", separator = " | ", postfix = " のいずれかが期待されています")
                                },
                                text,
                                candidateReader.peek().sourceSpan
                            )
                        )
                    )
                }
            }
        }

        return potentials
    }

    fun evaluate(sourceCode: String): List<CommandLineParseResult> {
        val tokens = BclTokenizer(sourceCode, interpretComments = false).tokenize()
        val block = BclParser(sourceCode, tokens, this, extension = false).parse()
        return block.nodes.filterIsInstance<CommandLineParseResult>()
    }
}
