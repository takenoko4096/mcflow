package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.RootCommandNode
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
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

    fun parse(reader: TokenSeqReader): CommandLineParseResult {
        val semanticTokens = mutableListOf<BclSemanticToken>()

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
            semanticTokens,
            initialState,
            root.children
        )

        if (potentials.isNotEmpty()) {
            val bestRoutePotential = potentials.maxWith(
                compareBy<RoutePotential> { it.state.reader.isOver() }
                    .thenBy { it.diagnostics.isEmpty() }
                    .thenBy { it.state.reader.cursor }
                    .thenBy { it.state.literalCount }
            )

            return if (bestRoutePotential.diagnostics.isEmpty()) {
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
                        diagnostics + BclDiagnostic(
                            error.message ?: error::class.java.name,
                            reader.sourceCode,
                            bestRoutePotential.state.reader.getEntireSourceSpan()
                        ),
                        start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                        semanticTokens
                    )
                }

                CommandLineParseResult.Successful(
                    bestRoutePotential.state.route.first() as Command,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.literals,
                    bestRoutePotential.state.arguments,
                    start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                    semanticTokens,
                    resultValue
                )
            }
            else {
                CommandLineParseResult.Failure(
                    bestRoutePotential.state.route.firstOrNull() as Command?,
                    bestRoutePotential.state.route,
                    bestRoutePotential.state.literals,
                    bestRoutePotential.state.arguments,
                    bestRoutePotential.diagnostics,
                    start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
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
            listOf(
                BclDiagnostic(
                    "不明なコマンド名です: '${sourceSpan.getStringFromSourceCode(reader.sourceCode)}'",
                    reader.sourceCode,
                    sourceSpan
                )
            ),
            sourceSpan,
            semanticTokens
        )
    }

    /*private fun dispatch(text: String, state: DispatcherParseState, effectiveChildren: Map<String, CommandNode>): MutableList<RoutePotential> {
        val potentials = mutableListOf<RoutePotential>()

        for ((name, node) in effectiveChildren) {
            val candidateReader = state.reader.clone()
            val candidateLiterals = state.literals.toMutableList()
            val candidateArguments = state.arguments.toMutableList()

            val nextState = DispatcherParseState(
                candidateReader,
                candidateLiterals,
                candidateArguments,
                route = state.route + node,
                literalCount = state.literalCount + if (state.noUnfatal && node is LiteralCommandNode) 1 else 0,
                noUnfatal = true
            )

            var unfatalErrors = listOf<BclDiagnostic>()

            try {
                when (node) {
                    is LiteralCommandNode -> {
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.getOverNextSourceSpan(),
                                "${state.route.last()} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val cursor = candidateReader.cursor
                        val start = candidateReader.getCurrentTokenStartPosition()
                        val literal = candidateReader.readCommandString()
                        if (literal != name) {
                            candidateReader.cursor = cursor
                            continue
                        }
                        val end = candidateReader.getPreviousTokenEndPosition()
                        candidateLiterals.add(node to start..end)
                    }

                    is ArgumentCommandNode<*> -> {
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.getOverNextSourceSpan(),
                                "${state.route.last()} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val argument = node.type.parse(candidateReader)
                        candidateArguments.add(node to argument)

                        unfatalErrors = argument.unfatalErrors
                        nextState.noUnfatal = false
                    }

                    else -> throw IllegalStateException("NEVER HAPPENS")
                }
            }
            catch (error: BclTokenSeqReadException) {
                if (detailedOutput) error.printStackTrace(System.err)

                potentials += RoutePotential(
                    nextState,
                    listOf(
                        BclDiagnostic(
                            """
                            ${if (state.route.isEmpty()) "コマンド名" else "引数"}が正しくありません (${error.description})
                            ${
                                if (effectiveChildren.size == 1) {
                                    effectiveChildren.values.first().toString() + " が期待されています"
                                }
                                else {
                                    effectiveChildren.map { it.value.toString() }.joinToString(" | ", postfix = " のいずれかが期待されています")
                                }
                            }
                            """.trimIndent(),
                            text,
                            error.sourceSpan
                        )
                    ) + unfatalErrors
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
                        val efC = node.getEffectiveChildren()
                        val additional = if (efC.isEmpty()) ""
                        else if (efC.size == 1) {
                            "; ${efC.values.first()} が期待されています"
                        }
                        else {
                            efC.values.joinToString(prefix = "; ", separator = " | ", postfix = " のいずれかが期待されています")
                        }

                        listOf(
                            BclDiagnostic(
                                "予期しない引数です: '${candidateReader.getRemainingSourceSpan().getStringFromSourceCode(candidateReader.sourceCode)}'" + additional,
                                candidateReader.sourceCode,
                                candidateReader.getRemainingSourceSpan()
                            )
                        )
                    }
                )
            }

            // さらに子へ進む可能性
            if (node.getEffectiveChildren().isNotEmpty()) {
                val childrenAndRedirectsOfCurrent = node.getEffectiveChildren()

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
    }*/

    private fun dispatch(
        text: String,
        semanticTokens: MutableList<BclSemanticToken>,
        state: DispatcherParseState,
        effectiveChildren: Map<String, CommandNode>
    ): MutableList<RoutePotential> {
        val potentials = mutableListOf<RoutePotential>()

        for ((name, node) in effectiveChildren) {
            val candidateReader = state.reader.clone()
            val candidateLiterals = state.literals.toMutableList()
            val candidateArguments = state.arguments.toMutableList()

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

            try {
                when (node) {
                    is LiteralCommandNode -> {
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.getOverNextSourceSpan(),
                                "${state.route.last()} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val cursor = candidateReader.cursor
                        val start = candidateReader.getCurrentTokenStartPosition()
                        val literal = candidateReader.readCommandString()
                        val end = candidateReader.getPreviousTokenEndPosition()

                        semanticTokens.add(BclSemanticToken(
                            if (node is Command) BclSemanticTokenType.COMMAND else BclSemanticTokenType.LITERAL,
                            start..end
                        ))

                        if (literal != name) {
                            candidateReader.cursor = cursor
                            continue
                        }

                        candidateLiterals.add(node to start..end)
                    }

                    is ArgumentCommandNode<*> -> {
                        if (candidateReader.isOver()) {
                            throw candidateReader.exception(
                                candidateReader.getOverNextSourceSpan(),
                                "${state.route.last()} の次の引数が要求されていますが、引数が不足しています"
                            )
                        }

                        val argumentParseResult = node.type.parse(candidateReader, unfatalErrors, semanticTokens)
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
                if (detailedOutput) error.printStackTrace(System.err)

                // fatal err
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, candidateReader.getRemainingSourceSpan()))

                potentials += RoutePotential(
                    nextState,
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
                    )
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
                    }
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
                    semanticTokens,
                    nextState,
                    childrenAndRedirectsOfCurrent
                )

                if (childPotentials.isNotEmpty()) {
                    if (unfatalState == null) {
                        potentials += childPotentials
                    }
                    else {
                        /*
                         * 子の解析結果は「解析を最後まで進めた結果」だが、
                         * このルートでは既に非致命エラーが発生している。
                         *
                         * したがって、
                         *   - diagnostics は非致命エラーを先頭に追加
                         *   - state はエラー発生時点の state に戻す
                         *
                         * 子を解析したこと自体は「この先に構文上何があるか」
                         * を判定するためだけに利用する。
                         */
                        potentials += childPotentials.map { potential ->
                            potential.copy(
                                state = unfatalState,
                                diagnostics = unfatalErrors + potential.diagnostics
                            )
                        }
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
