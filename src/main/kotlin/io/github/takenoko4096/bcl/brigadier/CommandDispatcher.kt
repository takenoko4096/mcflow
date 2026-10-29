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
import io.github.takenoko4096.bcl.tokenizer.SourcePosition

class CommandDispatcher(name: String, private val detailedOutput: Boolean = true) {
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

    fun parse(reader: CommandLineReader, sender: CommandSender): CommandLineParseResult {
        if (reader.isOver()) {
            return CommandLineParseResult.Failure(
                null,
                route = listOf(),
                emptyList(),
                emptyList(),
                getCommands()
                    .map {
                        CompletionCandidate(it, reader.lastSourcePosition, null)
                    },
                listOf(
                    BclDiagnostic(
                        "コマンド名を入力してください",
                        reader.sourceCode,
                        reader.lastSourcePosition..reader.lastSourcePosition
                    )
                ),
                reader.lastSourcePosition..reader.lastSourcePosition,
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
            sender,
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

                if (diagnostics.isEmpty()) {
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
                    CommandLineParseResult.Failure(
                        bestRoutePotential.state.route.firstOrNull() as Command?,
                        bestRoutePotential.state.route,
                        bestRoutePotential.state.literals,
                        bestRoutePotential.state.arguments,
                        completionCandidates,
                        diagnostics,
                        start..bestRoutePotential.state.reader.getPreviousTokenEndPosition(),
                        bestRoutePotential.state.reader.lastSourcePosition,
                        bestRoutePotential.semanticTokens
                    )
                }
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

    /*private fun dispatch(text: String, sender: CommandSender, state: DispatcherParseState, effectiveChildren: Map<String, CommandNode>): MutableList<RoutePotential> {
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

            val diagnosticsUnfatal = mutableListOf<BclDiagnostic>()

            // 非致命エラーが発生した直後の状態。
            // 発生しなければ null。
            var unfatalState: DispatcherParseState? = null

            val completionCandidates = mutableListOf<CompletionCandidate>()

            fun permission(start: SourcePosition, end: SourcePosition) {
                val context = CommandPermissionCheckContext(text, node, start..end, sender)
                val result = node.predicate?.invoke(context)
                if (result is CommandPermissionCheckContext.Failure) {
                    diagnosticsUnfatal.add(result.diagnostic)
                }
            }

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

                        permission(start, end)
                        if (diagnosticsUnfatal.isNotEmpty()) {
                            unfatalState = nextState.copy(
                                reader = candidateReader.clone(),
                                literals = candidateLiterals.toList(),
                                arguments = candidateArguments.toList()
                            )
                        }

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
                                diagnostics = diagnosticsUnfatal + listOf(
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
                            node.type.parse(candidateReader, diagnosticsUnfatal, candidateSemanticTokens)
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

                            permission(start, end)

                            completionCandidates.add(CompletionCandidate(node, start, start..end))

                            throw error
                        }

                        var end = candidateReader.getPreviousTokenEndPosition()

                        if (end.index < start.index) {
                            end = start
                        }

                        permission(start, end)

                        if (argumentParseResult is NoValuedArgument) {
                            completionCandidates.add(CompletionCandidate(node, start, start..end))
                        }

                        candidateArguments.add(node to argumentParseResult)

                        if (diagnosticsUnfatal.isNotEmpty()) {
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
                    diagnosticsUnfatal + listOf(
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
                    diagnosticsUnfatal
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
                    sender,
                    nextState,
                    childrenAndRedirectsOfCurrent
                )

                if (childPotentials.isNotEmpty()) {
                    potentials += childPotentials.map { potential ->
                        potential.copy(
                            state = unfatalState ?: potential.state,
                            diagnostics =
                                if (unfatalState != null) {
                                    diagnosticsUnfatal + potential.diagnostics
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
                        diagnosticsUnfatal
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
    }*/

    fun evaluate(sourceCode: String): List<CommandLineParseResult> {
        val tokens = BclTokenizer(sourceCode, extension = false).tokenize()
        val block = BclParser(sourceCode, tokens, this, CommandSender(extension = false)).parse()
        return block.nodes.filterIsInstance<CommandLineParseResult>()
    }

    private data class NodeParseContext(
        val sourceCode: String,
        val sender: CommandSender,
        val parentState: DispatcherParseState,
        val current: CommandNode,
        val nodeCandidates: Map<String, CommandNode>
    ) {
        val commandLineReader: CommandLineReader = parentState.reader.clone()
        val literals = parentState.literals.toMutableList()
        val arguments = parentState.arguments.toMutableList()
        val semanticTokens = mutableListOf<BclSemanticToken>()
        val diagnostics = mutableListOf<BclDiagnostic>()
        val completionCandidates = mutableListOf<CompletionCandidate>()

        /*
         * 非致命エラーが発生した直後の state
         */
        var unfatalState: DispatcherParseState? = null

        val nextState: DispatcherParseState
            get() = DispatcherParseState(
                reader = commandLineReader,
                literals = literals,
                arguments = arguments,
                route = parentState.route + current,
                literalCount = parentState.literalCount + if (parentState.noUnfatal && current is LiteralCommandNode) 1 else 0,
                noUnfatal = true
            )

        /*
         * 非致命エラーが発生した時点の state を保存する
         */
        fun saveUnfatalState() {
            unfatalState = nextState.copy(
                reader = commandLineReader.clone(),
                literals = literals.toList(),
                arguments = arguments.toList()
            )
        }

        /*
         * 最終的な RoutePotential に使う state
         */
        fun resultState(): DispatcherParseState {
            return unfatalState ?: nextState
        }

        /*
         * 権限チェック。
         *
         * 元コードの permission() と同じ処理。
         */
        fun checkPermission(start: SourcePosition, end: SourcePosition) {
            val context = CommandPermissionCheckContext(
                sourceCode,
                current,
                start..end,
                sender
            )

            val result = current.predicate?.invoke(context)

            if (result is CommandPermissionCheckContext.Failure) {
                diagnostics += result.diagnostic
            }
        }

        /*
         * 引数不足時の completion candidate を追加する
         */
        fun addMissingArgumentCompletion() {
            if (commandLineReader.sourceCode.getOrNull(commandLineReader.getEntireSourceSpan().end.index + 1)?.isWhitespace() == true) {
                completionCandidates += CompletionCandidate(
                    current,
                    commandLineReader.getOverNextSourceSpan().start,
                    null
                )
            }
        }
    }

    /*
     * node の解析結果。
     *
     * 通常は Success。
     *
     * Literal が一致しなかった場合だけ Rejected になり、
     * その時点で RoutePotential を生成して dispatch() に返す。
     *
     * これは元コードの
     *
     * potentials += RoutePotential(...)
     * continue
     *
     * を、例外などを使わずにそのまま表現するためのもの。
     */
    private sealed interface NodeParseResult {
        data object Successful : NodeParseResult
        data class Failure(val potential: RoutePotential) : NodeParseResult
    }

    private fun dispatch(sourceCode: String, sender: CommandSender, state: DispatcherParseState, children: Map<String, CommandNode>): MutableList<RoutePotential> {
        val potentials = mutableListOf<RoutePotential>()

        for ((name, node) in children) {
            val context = NodeParseContext(
                sourceCode = sourceCode,
                sender = sender,
                parentState = state,
                current = node,
                nodeCandidates = children
            )

            val nodeParseResult = try {
                parseNode(context, name)
            }
            catch (error: BclTokenSeqReadException) {
                potentials += createParseErrorPotential(context, error)
                continue
            }

            if (nodeParseResult is NodeParseResult.Failure) {
                potentials += nodeParseResult.potential
                continue
            }

            // node 自身で終了できる場合
            val terminalPotential = createTerminalPotential(context)
            if (terminalPotential != null) {
                potentials.add(terminalPotential)
            }

            // 子へ進める場合
            if (node.getEffectiveChildren().isNotEmpty()) {
                potentials += createChildPotentials(context)
            }
        }

        return potentials
    }


    /*
     * Literal / Argument の実際の解析。
     */
    private fun parseNode(context: NodeParseContext, name: String): NodeParseResult {
        return when (val node = context.current) {
            is LiteralCommandNode -> {
                parseLiteral(context, node, name)
            }

            is ArgumentCommandNode<*> -> {
                parseArgument(context, node)

                NodeParseResult.Successful
            }

            else -> {
                throw IllegalStateException("NEVER HAPPENS")
            }
        }
    }


    /*
     * LiteralCommandNode の解析。
     */
    private fun parseLiteral(context: NodeParseContext, node: LiteralCommandNode, name: String): NodeParseResult {
        val reader = context.commandLineReader

        if (reader.isOver()) {
            context.addMissingArgumentCompletion()

            throw reader.exception(
                reader.getOverNextSourceSpan(),
                "${context.parentState.route.last()} の次の引数が要求されていますが、引数が不足しています"
            )
        }

        /*
         * Literal の読み取り前の cursor を保存
         * 不一致時にここへ戻す
         */
        val cursor = reader.cursor
        val start = reader.getCurrentTokenStartPosition()
        val literal = reader.readCommandString()
        val end = reader.getPreviousTokenEndPosition()

        context.semanticTokens += BclSemanticToken(
            if (node is Command) BclSemanticTokenType.COMMAND else BclSemanticTokenType.LITERAL,
            start..end
        )

        /*
         * permission は Literal が一致しているかどうかに関係なく実行する
         */
        context.checkPermission(start, end)
        if (context.diagnostics.isNotEmpty()) {
            context.saveUnfatalState()
        }

        /*
         * Literal 不一致。
         */
        if (literal != name) {
            context.completionCandidates += CompletionCandidate(
                node,
                start,
                start..end
            )

            /*
             * 元コードと同じく、
             * Literal を読み終わった位置を furthestCursor とする。
             */
            val furthestCursor = reader.cursor

            /*
             * 元コードと同じく、RoutePotential に渡す前に
             * reader.cursor を Literal 読み取り前へ戻す。
             *
             * nextState.reader も同じ reader オブジェクトを参照しているため、
             * nextState の cursor もここで戻る。
             */
            reader.cursor = cursor

            // TODO
            return NodeParseResult.Failure(
                RoutePotential(
                    state = context.nextState.copy(
                        literalCount = context.parentState.literalCount,
                        route = context.parentState.route
                    ),
                    diagnostics =
                        context.diagnostics + BclDiagnostic(
                            """
                                ${if (context.parentState.route.isEmpty()) "コマンド名" else "引数"}が正しくありません
                                ${getExpectationMessage(context.nodeCandidates.values)}
                                """.trimIndent(),
                            context.sourceCode,
                            start..end
                        ),
                    completionCandidates =
                        context.completionCandidates.toList(),
                    semanticTokens =
                        context.semanticTokens.toList(),
                    furthestCursor = furthestCursor
                )
            )
        }

        /*
         * Literal が一致した場合だけ literals に追加する。
         */
        context.literals += node to (start..end)

        return NodeParseResult.Successful
    }


    /*
     * ArgumentCommandNode の解析。
     */
    private fun parseArgument(
        context: NodeParseContext,
        node: ArgumentCommandNode<*>
    ) {
        val reader = context.commandLineReader

        /*
         * 元コードと同じく、入力が既に終わっている場合。
         */
        if (reader.isOver()) {
            context.addMissingArgumentCompletion()

            throw reader.exception(
                reader.getOverNextSourceSpan(),
                "${context.parentState.route.last()} の次の引数が要求されていますが、引数が不足しています"
            )
        }

        val start = reader.getCurrentTokenStartPosition()

        /*
         * Argument の parse。
         *
         * parse() 内で発生した BclTokenSeqReadException は
         * 元コードと同じくここで一度処理してから再 throw する。
         */
        val argumentParseResult = try {
            node.type.parse(
                reader,
                context.diagnostics,
                context.semanticTokens
            )
        }
        catch (error: BclTokenSeqReadException) {
            var end = reader.getPreviousTokenEndPosition()

            if (start.index > end.index) {
                end = start
            }

            /*
             * 元コードと同じく、
             * Argument の parse に失敗した場合でも permission を実行する。
             */
            context.checkPermission(start, end)

            context.completionCandidates += CompletionCandidate(
                node,
                start,
                start..end
            )

            throw error
        }

        var end = reader.getPreviousTokenEndPosition()

        if (end.index < start.index) {
            end = start
        }

        context.checkPermission(start, end)

        /*
         * NoValuedArgument の場合のみ completion candidate を追加。
         */
        if (argumentParseResult is NoValuedArgument) {
            context.completionCandidates += CompletionCandidate(
                node,
                start,
                start..end
            )
        }

        /*
         * Argument の解析結果を保存。
         */
        context.arguments += node to argumentParseResult

        /*
         * parse() または permission() で非致命エラーが発生していれば、
         * その時点の state を保存。
         */
        if (context.diagnostics.isNotEmpty()) {
            context.saveUnfatalState()
        }
    }

    /*
     * BclTokenSeqReadException が発生した場合の RoutePotential。
     * 元 dispatch() の catch ブロックに対応する。
     */
    private fun createParseErrorPotential(context: NodeParseContext, error: BclTokenSeqReadException): RoutePotential {
        if (detailedOutput) {
            error.printStackTrace(System.err)
        }

        return RoutePotential(
            state = context.nextState.copy(
                literalCount = context.parentState.literalCount,
                route = context.parentState.route
            ),
            diagnostics = context.diagnostics +
                BclDiagnostic(
                    """
                    ${if (context.parentState.route.isEmpty()) "コマンド名" else "引数"}が正しくありません (${error.description})
                    ${getExpectationMessage(context.nodeCandidates.values)}
                    """.trimIndent(),
                    context.sourceCode,
                    error.sourceSpan
                ),
            completionCandidates = context.completionCandidates.toList(),
            semanticTokens = context.semanticTokens.toList()
        )
    }

    /*
     * node 自身に executor がある場合
     */
    private fun createTerminalPotential(context: NodeParseContext): RoutePotential? {
        if (context.current.executor == null) {
            return null
        }

        val diagnostics = if (context.unfatalState == null) emptyList() else context.diagnostics
        val reader = context.commandLineReader

        val diagnostic =
            if (reader.isOver()) {
                null
            }
            else {
                BclDiagnostic(
                    "予期しない引数です: '${reader.getRemainingSourceSpan().getStringFromSourceCode(reader.sourceCode)}'"
                        + getExpectationMessage(context.current.getEffectiveChildren().values),
                    reader.sourceCode,
                    reader.getRemainingSourceSpan()
                )
            }

        val children = context.current.getEffectiveChildren()

        val completions = context.completionCandidates +
            if (children.isEmpty()) {
                emptyList()
            }
            else {
                children.map { (_, value) ->
                    CompletionCandidate(
                        value,
                        reader.getOverNextSourceSpan().start,
                        null
                    )
                }
            }

        return RoutePotential(
            state = context.resultState(),
            diagnostics = diagnostics + listOfNotNull(diagnostic),
            completionCandidates = completions,
            semanticTokens = context.semanticTokens.toList()
        )
    }

    /*
     * 現在の node からさらに子へ進む場合
     */
    private fun createChildPotentials(context: NodeParseContext): List<RoutePotential> {
        val children = context.current.getEffectiveChildren()

        val childPotentials = dispatch(
            sourceCode = context.sourceCode,
            sender = context.sender,
            state = context.nextState,
            children = children
        )

        /*
         * 子が1つも有効な RoutePotential を生成しなかった場合
         */
        if (childPotentials.isEmpty()) {
            val diagnostics = if (context.unfatalState == null) emptyList() else context.diagnostics

            return listOf(RoutePotential(
                state = context.resultState(),
                diagnostics = diagnostics +
                    BclDiagnostic(
                        "予期しない引数です: ${getExpectationMessage(children.values)}",
                        context.sourceCode,
                        context.commandLineReader.peek().sourceSpan
                    ),
                completionCandidates = context.completionCandidates.toList(),
                semanticTokens = context.semanticTokens.toList()
            ))
        }

        // 子側で生成された RoutePotential を現在の node の情報と結合する
        return childPotentials.map { potential ->
            potential.copy(
                /*
                 * 非致命エラーがあった場合は、
                 * 最終的な state を unfatalState に戻す。
                 *
                 * ただし、再帰呼び出しには nextState を渡しているので、
                 * 子の解析自体はそのまま継続されている。
                 */
                state = context.unfatalState ?: potential.state,
                diagnostics =
                    if (context.unfatalState == null) {
                        potential.diagnostics
                    }
                    else {
                        context.diagnostics + potential.diagnostics
                    },
                completionCandidates = context.completionCandidates + potential.completionCandidates,
                semanticTokens = context.semanticTokens + potential.semanticTokens
            )
        }
    }

    private fun getExpectationMessage(candidates: Collection<CommandNode>): String {
        return when (candidates.size) {
            0 -> ""
            1 -> "\n${candidates.first()} が期待されています"
            else -> candidates.joinToString(separator = " | ", prefix = "\n", postfix = " のいずれかが期待されています")
        }
    }
}
