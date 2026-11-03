package io.github.takenoko4096.bcl.brigadier.dispatcher

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.Command
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNodeBuilder
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.RootCommandNode
import io.github.takenoko4096.bcl.completer.CompletionCandidate
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.reader.BclTokenSeqReadException
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import kotlin.collections.iterator
import kotlin.collections.plus
import kotlin.collections.plusAssign

class CommandDispatcher(
    name: String,
    private val commandSemanticTokenType: BclSemanticTokenType = BclSemanticTokenType.COMMAND
) {
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

    fun evaluate(sourceCode: String): List<CommandLineParseResult> {
        val tokens = BclTokenizer(sourceCode, extension = false).tokenize()
        val block = BclParser(
            sourceCode,
            tokens,
            this,
            CommandDispatcher("root"),
            CommandSender(extension = false)
        ).parse()
        return block.nodes.filterIsInstance<CommandLineParseResult>()
    }

    fun parse(reader: CommandLineReader, sender: CommandSender): CommandLineParseResult {
        if (reader.isOver()) {
            return CommandLineParseResult.Failure(
                null,
                sender,
                this,
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
                reader.preprocessedSemanticTokens
            )
        }

        val start = reader.getCurrentTokenStartPosition()

        val potentials = parsePotentials(reader, sender)

        if (potentials.isNotEmpty()) {
            val best = potentials.maxWith(::comparePotentials)

            val completionCandidates = potentials
                .filter { comparePotentials(it, best) == 0 }
                .flatMap { it.completionCandidates }
                .distinct()

            return if (best.diagnostics.none { it.isError }) {
                val diagnosticsOnExecution = mutableListOf<BclDiagnostic>()

                val context = CommandExecuteContext(
                    reader.sourceCode,
                    this,
                    best.state.route,
                    best.state.arguments,
                    diagnosticsOnExecution,
                    sender
                )

                val executor = best.state.route.last().executor ?: throw IllegalStateException(
                    best.toString()
                )

                val resultValue = try {
                    context.executor()
                }
                catch (error: Throwable) {
                    return CommandLineParseResult.Failure(
                        best.state.route.firstOrNull() as Command?,
                        sender,
                        this,
                        best.state.route,
                        best.state.literals,
                        best.state.arguments,
                        completionCandidates,
                        diagnosticsOnExecution + BclDiagnostic(
                            error.message ?: "This is probably a bug. An error occurred in execution phase (caused by: ${best.state.route})\n${error.stackTraceToString()}",
                            reader.sourceCode,
                            // TODO linebreak 対策
                            best.state.reader.getEntireSourceSpan()
                        ),
                        start..best.state.reader.getPreviousTokenEndPosition(),
                        best.state.reader.lastSourcePosition,
                        reader.preprocessedSemanticTokens + best.semanticTokens
                    )
                }

                if (diagnosticsOnExecution.none { it.isError }) {
                    CommandLineParseResult.Successful(
                        best.state.route.first() as Command,
                        sender,
                        this,
                        best.state.route,
                        best.state.literals,
                        best.state.arguments,
                        completionCandidates,
                        best.diagnostics + diagnosticsOnExecution,
                        start..best.state.reader.getPreviousTokenEndPosition(),
                        best.state.reader.lastSourcePosition,
                        reader.preprocessedSemanticTokens + best.semanticTokens,
                        resultValue
                    )
                }
                else {
                    CommandLineParseResult.Failure(
                        best.state.route.firstOrNull() as Command?,
                        sender,
                        this,
                        best.state.route,
                        best.state.literals,
                        best.state.arguments,
                        completionCandidates,
                        diagnosticsOnExecution,
                        start..best.state.reader.getPreviousTokenEndPosition(),
                        best.state.reader.lastSourcePosition,
                        reader.preprocessedSemanticTokens + best.semanticTokens
                    )
                }
            }
            else {
                val semanticTokens = (reader.preprocessedSemanticTokens + best.semanticTokens).toMutableList()

                val furthestReader = best.state.reader
                    .clone()
                    .apply { cursor = best.furthestCursor }

                furthestReader.getSourceSpansOfRemainingLines().forEach {
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ERROR, it))
                }

                CommandLineParseResult.Failure(
                    best.state.route.firstOrNull() as Command?,
                    sender,
                    this,
                    best.state.route,
                    best.state.literals,
                    best.state.arguments,
                    completionCandidates,
                    best.diagnostics,
                    start..furthestReader.getPreviousTokenEndPosition(),
                    best.state.reader.lastSourcePosition,
                    semanticTokens
                )
            }
        }

        val end = reader.peekSafely(-1).sourceSpan.end
        val sourceSpan = start..end

        return CommandLineParseResult.Failure(
            null,
            sender,
            this,
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
            reader.preprocessedSemanticTokens
        )
    }

    fun parsePotentials(reader: CommandLineReader, sender: CommandSender): List<RoutePotential> {
        if (reader.isOver()) {
            return emptyList()
        }

        val initialState = DispatcherParseState(
            reader = reader.clone(),
            literals = emptyList(),
            arguments = emptyList(),
            route = mutableListOf(),
            literalCount = 0
        )

        return dispatch(
            reader.sourceCode,
            sender,
            initialState,
            root.getEffectiveChildren()
        )
    }

    private fun comparePotentials(a: RoutePotential, b: RoutePotential): Int {
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
                { it.errorCursor ?: it.furthestCursor },
                { -it.diagnostics.count { diagnostic -> diagnostic.isError } },
                { it.state.reader.isOver() },
                { it.state.reader.cursor },
                { it.state.literalCount }
            )
        }
    }

    /*
     * node の解析結果
     * 通常は Success
     * Literal が一致しなかった場合だけ Failure になり、
     * その時点で RoutePotential を生成して dispatch() に返す
     */
    private sealed interface LiteralParseResult {
        data object Matched : LiteralParseResult
        data class Mismatched(val potential: RoutePotential) : LiteralParseResult
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
                when (node) {
                    is LiteralCommandNode -> {
                        parseLiteral(context, node, name)
                    }
                    is ArgumentCommandNode<*> -> {
                        parseArgument(context, node)
                        null
                    }
                    else -> throw IllegalStateException("NEVER HAPPENS")
                }
            }
            catch (error: BclTokenSeqReadException) {
                potentials += createParseErrorPotential(context, error)
                continue
            }

            if (nodeParseResult is LiteralParseResult.Mismatched) {
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
     * LiteralCommandNode の解析。
     */
    private fun parseLiteral(context: NodeParseContext, node: LiteralCommandNode, name: String): LiteralParseResult {
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

        context.semanticTokens.add(BclSemanticToken(
            if (node is Command) {
                commandSemanticTokenType
            } else {
                BclSemanticTokenType.LITERAL
            },
            start..end
        ))

        /*
         * permission は Literal が一致しているかどうかに関係なく実行する
         */
        context.checkPermission(start, end)

        /*
         * Literal 不一致。
         */
        if (literal != name) {
            context.completionCandidates.add(CompletionCandidate(
                node,
                start,
                start..end
            ))

            /*
             * 元コードと同じく、
             * Literal を読み終わった位置を furthestCursor とする。
             */
            // TODO: furthestCursor と reader.cursor の役割を入れ替える？
            val furthestCursor = reader.cursor

            /*
             * 元コードと同じく、RoutePotential に渡す前に
             * reader.cursor を Literal 読み取り前へ戻す。
             *
             * nextState.reader も同じ reader オブジェクトを参照しているため、
             * nextState の cursor もここで戻る。
             */
            reader.cursor = cursor

            return LiteralParseResult.Mismatched(
                RoutePotential(
                    state = context.nextState.copy(
                        literalCount = context.parentState.literalCount,
                        route = context.parentState.route
                    ),
                    diagnostics =
                        context.diagnostics + BclDiagnostic(
                            """
                            ${if (context.parentState.route.isEmpty()) "コマンド名" else "引数"}が正しくありません: '$literal'
                            ${getExpectationMessage(context.nodeCandidates.values)}
                            """.trimIndent(),
                            context.sourceCode,
                            start..end
                        ),
                    completionCandidates = context.completionCandidates.toList(),
                    semanticTokens = context.semanticTokens.toList(),
                    furthestCursor = furthestCursor
                )
            )
        }

        context.literals += node to (start..end)

        return LiteralParseResult.Matched
    }

    /*
     * ArgumentCommandNode の解析。
     */
    private fun parseArgument(context: NodeParseContext, node: ArgumentCommandNode<*>) {
        val reader = context.commandLineReader

        if (reader.isOver()) {
            context.addMissingArgumentCompletion()

            throw reader.exception(
                reader.getOverNextSourceSpan(),
                "${context.parentState.route.last()} の次の引数が要求されていますが、引数が不足しています"
            )
        }

        val start = reader.getCurrentTokenStartPosition()

        // ArgumentType.parse() が追加した diagnostic のみを検出するために保存
        val diagnosticCountBeforeParse = context.diagnostics.count { it.isError }

        val argumentParseResult = try {
            node.type.parse(
                ArgumentParseContext(
                    reader,
                    context.sender,
                    this,
                    context.diagnostics,
                    context.semanticTokens
                )
            )
        }
        catch (error: BclTokenSeqReadException) {
            var end = reader.getPreviousTokenEndPosition()

            if (start.index > end.index) {
                end = start
            }

            // permission failure は state rollback の対象ではない
            context.checkPermission(start, end)

            context.completionCandidates.add(CompletionCandidate(
                node,
                start,
                start..end
            ))

            throw error
        }

        var end = reader.getPreviousTokenEndPosition()

        if (end.index < start.index) {
            end = start
        }

        if (argumentParseResult is NoValuedArgument) {
            context.completionCandidates.add(CompletionCandidate(
                node,
                start,
                start..end
            ))
        }

        context.arguments += node to argumentParseResult

        /*
         * ArgumentType.parse() 自身が非致命 diagnostic を発生させた場合だけ
         * その時点の state を保存する。
         *
         * permission failure はここでは stateAtDiagnostic にしない。
         */
        val newErrors = context.diagnostics
            .drop(diagnosticCountBeforeParse)
            .filter { it.isError }

        // permission failure は diagnostic として記録するだけ
        context.checkPermission(start, end)

        if (newErrors.isNotEmpty()) {
            context.saveStateAtParseError()

            if (context.errorCursor == null) {
                context.errorCursor = newErrors.minOf { it.sourceSpan.start.index }
            }
        }
    }

    /*
     * BclTokenSeqReadException が発生した場合の RoutePotential。
     * 元 dispatch() の catch ブロックに対応する。
     */
    private fun createParseErrorPotential(context: NodeParseContext, error: BclTokenSeqReadException): RoutePotential {
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
            errorCursor = error.sourceSpan.start.index,
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

        val diagnostics = context.diagnostics
        val reader = context.commandLineReader

        val diagnostic =
            if (reader.isOver()) {
                null
            }
            else {
                BclDiagnostic(
                    "予期しない引数です: '${
                        reader.getRemainingSourceSpan().getStringFromSourceCode(reader.sourceCode)
                    }'"
                        + if (context.current.getEffectiveChildren().isNotEmpty()) {
                        '\n' + getExpectationMessage(context.current.getEffectiveChildren().values)
                    } else "",
                    reader.sourceCode,
                    reader.getRemainingSourceSpan()
                )
            }

        val children = context.current.getEffectiveChildren()

        val completions = context.completionCandidates +
            if (children.isEmpty() || !reader.hasSeparatorBeforeCursor()) {
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
            val diagnostics = if (context.stateAtParseError == null) emptyList() else context.diagnostics

            return listOf(
                RoutePotential(
                    state = context.resultState(),
                    diagnostics = diagnostics +
                        BclDiagnostic(
                            "予期しない引数です: ${getExpectationMessage(children.values)}",
                            context.sourceCode,
                            context.commandLineReader.peek().sourceSpan
                        ),
                    errorCursor = context.errorCursor,
                    completionCandidates = context.completionCandidates.toList(),
                    semanticTokens = context.semanticTokens.toList()
                )
            )
        }

        return childPotentials.map { potential ->
            potential.copy(
                state = context.stateAtParseError ?: potential.state,
                errorCursor = context.errorCursor ?: potential.errorCursor,
                diagnostics = context.diagnostics + potential.diagnostics,
                completionCandidates = context.completionCandidates + potential.completionCandidates,
                semanticTokens = context.semanticTokens + potential.semanticTokens
            )
        }
    }

    private fun getExpectationMessage(candidates: Collection<CommandNode>): String {
        return when (candidates.size) {
            0 -> ""
            1 -> "${candidates.first()} が期待されています"
            else -> candidates.joinToString(separator = " | ", postfix = " のいずれかが期待されています")
        }
    }
}