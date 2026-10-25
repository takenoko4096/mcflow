package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.RootCommandNode
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.completer.CompletionCandidate
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer

class CommandDispatcher(name: String, private val detailedOutput: Boolean = false) {
    private val root = RootCommandNode(name, mutableMapOf())

    fun getCommand(name: String): Command? {
        return root.commands[name]
    }

    fun getRoot(): RootCommandNode = root

    fun getCommands(): Set<Command> {
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

    private fun compare(a: RoutePotential, b: RoutePotential): Int {
        val aNoError = a.diagnostics.none { it.isError }
        val bNoError = b.diagnostics.none { it.isError }

        return when {
            aNoError != bNoError -> aNoError.compareTo(bNoError)
            aNoError -> compareValuesBy(
                a, b,
                { it.state.reader.isOver() },
                { it.state.reader.cursor },
                { it.state.literalCount }
            )
            else -> compareValuesBy(
                a, b,
                { it.furthestCursor },
                { it.state.reader.isOver() },
                { it.state.literalCount }
            )
        }
    }

    fun parse(reader: CommandLineReader): CommandLineParseResult {
        if (reader.isOver()) {
            return CommandLineParseResult.Failure(
                null,
                route = listOf(),
                emptyList(),
                emptyList(),
                getCommands()
                    .map {
                        CompletionCandidate(it, reader.getEntireSourceSpan().start, null)
                    },
                listOf(
                    BclDiagnostic(
                        "不明なコマンド名です: ''",
                        reader.sourceCode,
                        reader.getOverNextSourceSpan()
                    )
                ),
                reader.getEntireSourceSpan(),
                reader.lastSourcePosition,
                emptyList()
            )
        }

        val start = reader.getCurrentTokenStartPosition()

        val initialState = DispatcherParseState(
            reader = reader.clone(),
            literals = emptyList(),
            arguments = emptyList(),
            route = mutableListOf(),
            literalCount = 0,
            noUnfatal = true
        )

        val potentials = dispatch(
            reader.sourceCode,
            initialState,
            root.children
        )

        if (potentials.isNotEmpty()) {
            val bestRoutePotential = potentials.maxWith(::compare)

            val completionCandidates = potentials
                .filter { compare(it, bestRoutePotential) == 0 }
                .flatMap { it.completionCandidates }
                .distinct()

            return if (bestRoutePotential.diagnostics.none { it.isError }) {
                val diagnostics = mutableListOf<BclDiagnostic>()

                val context = CommandContext(
                    reader.sourceCode,
                    this,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.arguments,
                    diagnostics
                )

                val resultValue = try {
                    bestRoutePotential.state.route.last().executor!!(context)
                }
                catch (error: Throwable) {
                    return CommandLineParseResult.Failure(
                        bestRoutePotential.state.route.firstOrNull() as Command?,
                        bestRoutePotential.state.route,
                        bestRoutePotential.state.literals,
                        bestRoutePotential.state.arguments,
                        completionCandidates,
                        diagnostics + BclDiagnostic(
                            error.message ?: error::class.java.name,
                            reader.sourceCode,
                            // TODO linebreak 対策
                            bestRoutePotential.state.reader.getEntireSourceSpan()
                        ),
                        start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                        bestRoutePotential.state.reader.lastSourcePosition,
                        bestRoutePotential.semanticTokens
                    )
                }

                CommandLineParseResult.Successful(
                    bestRoutePotential.state.route.first() as Command,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.literals,
                    bestRoutePotential.state.arguments,
                    completionCandidates,
                    start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                    bestRoutePotential.state.reader.lastSourcePosition,
                    bestRoutePotential.semanticTokens,
                    resultValue
                )
            }
            else {
                val semanticTokens = bestRoutePotential.semanticTokens.toMutableList()

                val cloned = bestRoutePotential.state.reader.clone()
                cloned.cursor = bestRoutePotential.furthestCursor
                cloned.getSourceSpansOfRemainingLines().forEach {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, it))
                }

                CommandLineParseResult.Failure(
                    bestRoutePotential.state.route.firstOrNull() as Command?,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.literals,
                    bestRoutePotential.state.arguments,
                    completionCandidates,
                    bestRoutePotential.diagnostics,
                    start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                    bestRoutePotential.state.reader.lastSourcePosition,
                    semanticTokens
                )
            }
        }

        val end = reader.peekSafely(-1).sourceSpan.end
        val sourceSpan = start..end

        return CommandLineParseResult.Failure(
            null,
            route = listOf(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                BclDiagnostic(
                    "不明なコマンド名です: '${sourceSpan.getStringFromSourceCode(reader.sourceCode)}'",
                    reader.sourceCode,
                    sourceSpan
                )
            ),
            sourceSpan,
            reader.lastSourcePosition,
            emptyList()
        )
    }

    private fun dispatch(text: String, state: DispatcherParseState, effectiveChildren: Map<String, CommandNode>): MutableList<RoutePotential> {
        val potentials = mutableListOf<RoutePotential>()

        for ((name, node) in effectiveChildren) {
            val candidateReader = state.reader.clone()
            val candidateLiterals = state.literals.toMutableList()
            val candidateArguments = state.arguments.toMutableList()
            val candidateSemanticTokens = mutableListOf<BclSemanticToken>()

            val nextState = DispatcherParseState(
                reader = candidateReader,
                literals = candidateLiterals,
                arguments = candidateArguments,
                route = state.route + node,
                literalCount = state.literalCount + if (state.noUnfatal && node is LiteralCommandNode) 1 else 0,
                noUnfatal = true
            )

            val unfatalErrors = mutableListOf<BclDiagnostic>()

            // 非致命エラーが発生した直後の状態。
            // 発生しなければ null。
            var unfatalState: DispatcherParseState? = null

            val completionCandidates = mutableListOf<CompletionCandidate>()

            try {
                when (node) {
                    is LiteralCommandNode -> {
                        if (candidateReader.isOver()) {
                            if (candidateReader.sourceCode.getOrNull(candidateReader.getEntireSourceSpan().end.index + 1)?.isWhitespace() == true) {
                                completionCandidates.add(
                                    CompletionCandidate(
                                        node,
                                        candidateReader.getOverNextSourceSpan().start,
                                        null
                                    )
                                )
                            }

                            throw candidateReader.exception(
                                candidateReader.getOverNextSourceSpan(),
                                "${state.route.last()} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val cursor = candidateReader.cursor
                        val start = candidateReader.getCurrentTokenStartPosition()
                        val literal = candidateReader.readCommandString()
                        val end = candidateReader.getPreviousTokenEndPosition()

                        candidateSemanticTokens.add(
                            BclSemanticToken(
                                if (node is Command) {
                                    BclSemanticTokenType.COMMAND
                                }
                                else {
                                    BclSemanticTokenType.LITERAL
                                },
                                start..end
                            )
                        )

                        if (literal != name) {
                            completionCandidates.add(
                                CompletionCandidate(
                                    node,
                                    start,
                                    start..end
                                )
                            )

                            val furthestCursor = candidateReader.cursor

                            candidateReader.cursor = cursor

                            potentials += RoutePotential(
                                state = nextState.copy(
                                    literalCount = state.literalCount,
                                    route = state.route
                                ),
                                diagnostics = unfatalErrors + listOf(
                                    BclDiagnostic(
                                        """
                                        ${if (state.route.isEmpty()) "コマンド名" else "引数"}が正しくありません
                                        ${
                                                if (effectiveChildren.size == 1) {
                                                    effectiveChildren.values.first()
                                                        .toString() + " が期待されています"
                                                }
                                                else {
                                                    effectiveChildren.values.joinToString(
                                                        " | ",
                                                        postfix = " のいずれかが期待されています"
                                                    )
                                                }
                                            }
                                        """.trimIndent(),
                                        text,
                                        start..end
                                    )
                                ),
                                completionCandidates = completionCandidates.toList(),
                                semanticTokens = candidateSemanticTokens.toList(),
                                furthestCursor = furthestCursor
                            )

                            continue
                        }

                        candidateLiterals.add(node to start..end)
                    }

                    is ArgumentCommandNode<*> -> {
                        if (candidateReader.isOver()) {
                            if (candidateReader.sourceCode.getOrNull(candidateReader.getEntireSourceSpan().end.index + 1)?.isWhitespace() == true) {
                                completionCandidates.add(
                                    CompletionCandidate(
                                        node,
                                        candidateReader.getOverNextSourceSpan().start,
                                        null
                                    )
                                )
                            }

                            throw candidateReader.exception(
                                candidateReader.getOverNextSourceSpan(),
                                "${state.route.last()} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val start = candidateReader.getCurrentTokenStartPosition()

                        val argumentParseResult = try {
                            node.type.parse(candidateReader, unfatalErrors, candidateSemanticTokens)
                        }
                        catch (error: BclTokenSeqReadException) {
                            var end = candidateReader.getPreviousTokenEndPosition()

                            if (start.index > end.index) {
                                end = start
                            }

                            // TODO これやるか迷い
                            /*candidateSemanticTokens.add(BclSemanticToken(
                                BclSemanticTokenType.ERROR,
                                start..end
                            ))*/

                            completionCandidates.add(CompletionCandidate(node, start, start..end))

                            throw error
                        }

                        var end = candidateReader.getPreviousTokenEndPosition()

                        if (end.index < start.index) {
                            end = start
                        }

                        if (argumentParseResult is NoValuedArgument) {
                            completionCandidates.add(CompletionCandidate(node, start, start..end))
                        }

                        candidateArguments.add(node to argumentParseResult)

                        if (unfatalErrors.isNotEmpty()) {
                            /*
                             * parse() が非致命エラーを返した時点の
                             * candidateReader.cursor を保存する。
                             *
                             * route には node が既に追加されているので、
                             * [foo, ArgumentNode<Int>] になる。
                             *
                             * その後の解析では candidateReader をそのまま
                             * 使ってよいが、最終的な RoutePotential には
                             * この状態を使う。
                             */
                            unfatalState = nextState.copy(
                                reader = candidateReader.clone(),
                                literals = candidateLiterals.toList(),
                                arguments = candidateArguments.toList()
                            )
                        }
                    }

                    else -> throw IllegalStateException("NEVER HAPPENS")
                }
            }
            catch (error: BclTokenSeqReadException) {
                if (detailedOutput) {
                    error.printStackTrace(System.err)
                }

                potentials += RoutePotential(
                    nextState.copy(literalCount = state.literalCount, route = state.route),
                    unfatalErrors + listOf(
                        BclDiagnostic(
                            """
                            ${if (state.route.isEmpty()) "コマンド名" else "引数"}が正しくありません (${error.description})
                            ${
                                    if (effectiveChildren.size == 1) {
                                        effectiveChildren.values.first().toString() +
                                            " が期待されています"
                                    }
                                    else {
                                        effectiveChildren.values.joinToString(
                                            " | ",
                                            postfix = " のいずれかが期待されています"
                                        )
                                    }
                                }
                            """.trimIndent(),
                            text,
                            error.sourceSpan
                        )
                    ),
                    completionCandidates = completionCandidates.toList(),
                    semanticTokens = candidateSemanticTokens.toList()
                )

                continue
            }

            /*
             * 非致命エラーがなければ、今まで通り nextState が
             * 「ここまで正常に解析できた状態」。
             *
             * 非致命エラーがあれば、解析を続けるための state は
             * nextState のまま、最終候補として使う state は
             * unfatalState にする。
             */
            val resultState = unfatalState ?: nextState

            // node自身で終了する可能性
            if (node.executor != null) {
                val diagnostics = if (unfatalState != null) {
                    unfatalErrors
                }
                else {
                    emptyList()
                }

                potentials += RoutePotential(
                    resultState,
                    if (candidateReader.isOver()) {
                        diagnostics
                    }
                    else {
                        val efC = node.getEffectiveChildren()

                        val additional = if (efC.isEmpty()) {
                            ""
                        }
                        else if (efC.size == 1) {
                            "; ${efC.values.first()} が期待されています"
                        }
                        else {
                            efC.values.joinToString(
                                prefix = "; ",
                                separator = " | ",
                                postfix = " のいずれかが期待されています"
                            )
                        }

                        diagnostics + BclDiagnostic(
                            "予期しない引数です: " +
                                "'${candidateReader.getRemainingSourceSpan()
                                    .getStringFromSourceCode(candidateReader.sourceCode)}'" +
                                additional,
                            candidateReader.sourceCode,
                            candidateReader.getRemainingSourceSpan()
                        )
                    },
                    completionCandidates = completionCandidates.toList() + if (node.getEffectiveChildren().isEmpty()) {
                        emptyList()
                    }
                    else {
                        node.getEffectiveChildren().map { (_, value) ->
                            CompletionCandidate(value, candidateReader.getOverNextSourceSpan().start, null)
                        }
                    },
                    semanticTokens = candidateSemanticTokens.toList()
                )
            }

            // さらに子へ進む可能性
            if (node.getEffectiveChildren().isNotEmpty()) {
                val childrenAndRedirectsOfCurrent = node.getEffectiveChildren()

                /*
                 * ここでは必ず nextState を渡す。
                 *
                 * つまり非致命エラーがあっても、
                 * candidateReader の現在位置から先を解析する。
                 */
                val childPotentials = dispatch(
                    text,
                    nextState,
                    childrenAndRedirectsOfCurrent
                )

                if (childPotentials.isNotEmpty()) {
                    potentials += childPotentials.map { potential ->
                        potential.copy(
                            state = unfatalState ?: potential.state,
                            diagnostics =
                                if (unfatalState != null) {
                                    unfatalErrors + potential.diagnostics
                                }
                                else {
                                    potential.diagnostics
                                },
                            furthestCursor = potential.furthestCursor,
                            completionCandidates = completionCandidates + potential.completionCandidates,

                            /*
                             * 現在の node が生成した token と、
                             * 子側の route が生成した token を結合する。
                             *
                             * 別 route の token はここには入ってこない。
                             */
                            semanticTokens = candidateSemanticTokens + potential.semanticTokens
                        )
                    }
                }
                else {
                    val diagnostics = if (unfatalState != null) {
                        unfatalErrors
                    }
                    else {
                        emptyList()
                    }

                    potentials += RoutePotential(
                        resultState,
                        diagnostics + BclDiagnostic(
                            if (childrenAndRedirectsOfCurrent.size == 1) {
                                "予期しない引数です: " +
                                    "${childrenAndRedirectsOfCurrent.values.first()} が期待されています"
                            }
                            else {
                                childrenAndRedirectsOfCurrent.values.joinToString(
                                    prefix = "予期しない引数です: ",
                                    separator = " | ",
                                    postfix = " のいずれかが期待されています"
                                )
                            },
                            text,
                            candidateReader.peek().sourceSpan
                        ),
                        completionCandidates = completionCandidates.toList(),
                        semanticTokens = candidateSemanticTokens.toList()
                    )
                }
            }
        }

        return potentials
    }

    fun evaluate(sourceCode: String): List<CommandLineParseResult> {
        val tokens = BclTokenizer(sourceCode, extension = false).tokenize()
        val block = BclParser(sourceCode, tokens, this, extension = false).parse()
        return block.nodes.filterIsInstance<CommandLineParseResult>()
    }
}
