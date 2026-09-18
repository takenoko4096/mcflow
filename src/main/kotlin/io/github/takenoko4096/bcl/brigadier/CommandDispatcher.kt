package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.RootCommandNode
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.TokenType

class CommandDispatcher {
    private val root = RootCommandNode("root", mutableMapOf())

    fun getCommand(name: String): Command? {
        return root.commands[name]
    }

    fun getRoot(): RootCommandNode = root

    fun getKnownCommands(): Set<Command> {
        return root.commands.values.toSet()
    }

    fun register(builder: CommandNodeBuilder.CommandBuilder): Command {
        val command = builder.build(root)

        if (command.name in root.commands) {
            throw IllegalArgumentException("コマンド名 '${command.name}' は既に使用されているため登録できません")
        }

        root.commands[command.name] = command

        for (string in command.aliases) {
            if (string in root.commands) {
                throw IllegalArgumentException("コマンド名 '${command.name}' は既に使用されているためエイリアスを登録できません")
            }

            root.commands[string] = command
        }

        return command
    }

    fun register(name: String, configurator: CommandNodeBuilder.CommandBuilder.() -> Unit): Command {
        return register(Command.builder(name, configurator))
    }

    fun parse(reader: TokenSeqReader): CommandLineParseResult {
        val start = reader.getCurrentTokenStartPosition()

        val initialState = DispatcherParseState(
            reader = reader.clone(),
            arguments = emptyMap(),
            route = mutableListOf(),
            literalCount = 0
        )

        val potentials = dispatch(
            reader.sourceCode,
            initialState,
            root.children
        )

        if (potentials.isNotEmpty()) {
            val bestRoutePotential = potentials.maxWith(
                compareBy<RoutePotential> { it.state.reader.isOver() }
                    .thenBy { it.diagnostics.isEmpty() }
                    .thenBy { it.state.literalCount }
                    .thenBy { it.state.reader.cursor }
            )

            return if (bestRoutePotential.diagnostics.isEmpty()) {
                val context = CommandContext(reader.sourceCode, this, bestRoutePotential.state.route, bestRoutePotential.state.arguments)

                val resultValue = try {
                    bestRoutePotential.state.route.last().executor!!(context)
                }
                catch (error: BclCommandExecutionException) {
                    return CommandLineParseResult.Failure(
                        bestRoutePotential.state.route.firstOrNull() as Command?,
                        bestRoutePotential.state.route,
                        bestRoutePotential.state.arguments,
                        listOf(BclDiagnostic(
                            error.description,
                            reader.sourceCode,
                            error.sourceSpan
                        )),
                        start..bestRoutePotential.state.reader.getPreviousTokenEndPosition()
                    )
                }

                CommandLineParseResult.Successful(
                    bestRoutePotential.state.route.first() as Command,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.arguments,
                    start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                    resultValue
                )
            }
            else {
                CommandLineParseResult.Failure(
                    bestRoutePotential.state.route.firstOrNull() as Command?,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.arguments,
                    bestRoutePotential.diagnostics,
                    start..bestRoutePotential.state.reader.getPreviousTokenEndPosition()
                )
            }
        }

        val end = reader.peekSafely(-1).sourceSpan.end
        val sourceSpan = start..end

        return CommandLineParseResult.Failure(
            null,
            route = listOf(),
            emptyMap(),
            listOf(
                BclDiagnostic(
                    "不明なコマンド名です: '${sourceSpan.getStringFromSourceCode(reader.sourceCode)}'",
                    reader.sourceCode,
                    sourceSpan
                )
            ),
            sourceSpan
        )
    }

    private fun dispatch(text: String, state: DispatcherParseState, childrenAndRedirects: Map<String, CommandNode>): MutableList<RoutePotential> {
        val potentials = mutableListOf<RoutePotential>()

        for ((name, node) in childrenAndRedirects) {
            val candidateReader = state.reader.clone()
            val candidateArguments = state.arguments.toMutableMap()

            val nextState = DispatcherParseState(
                candidateReader,
                candidateArguments,
                state.route + node,
                state.literalCount + if (node is LiteralCommandNode) 1 else 0
            )

            try {
                when (node) {
                    is LiteralCommandNode -> {
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.peekSafely(-1).sourceSpan,
                                "${state.route} の次の引数が要求されていますが、引数が不足しています"
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
                                "${state.route} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val argument = node.type.parse(candidateReader)
                        candidateArguments[name] = argument
                    }

                    else -> throw IllegalStateException("NEVER HAPPENS")
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
                                if (childrenAndRedirects.size == 1) {
                                    childrenAndRedirects.values.first().toString() + " が期待されています"
                                }
                                else {
                                    childrenAndRedirects.map { it.value.toString() }.joinToString(" | ", postfix = " のいずれかが期待されています")
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
            if (node.getChildrenAndRedirects().isNotEmpty()) {
                val childrenAndRedirectsOfCurrent = node.getChildrenAndRedirects()

                val childPotentials = dispatch(
                    text,
                    nextState,
                    childrenAndRedirectsOfCurrent
                )

                if (childPotentials.isNotEmpty()) {
                    potentials += childPotentials
                }
                else {
                    potentials += RoutePotential(
                        nextState,
                        listOf(
                            BclDiagnostic(
                                if (childrenAndRedirectsOfCurrent.size == 1) {
                                    "予期しない引数です: ${childrenAndRedirectsOfCurrent.values.first()} が期待されています"
                                }
                                else {
                                    childrenAndRedirectsOfCurrent.values.joinToString(prefix = "予期しない引数です: ", separator = " | ", postfix = " のいずれかが期待されています")
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
