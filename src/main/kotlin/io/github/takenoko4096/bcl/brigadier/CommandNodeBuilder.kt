package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.brigadier.node.RootCommandNode
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import org.eclipse.lsp4j.CompletionItem

sealed class CommandNodeBuilder(protected val name: String) {
    protected val children = mutableMapOf<String, CommandNodeBuilder>()

    protected var executor: (CommandContext.() -> Any?)? = null

    protected val redirects = mutableSetOf<String>()

    protected var completerOverride: ((BclCompletionContext) -> List<CompletionItem>)? = null

    init {
        if (!name.matches(COMMAND_NODE_NAME_REGEX)) {
            throw IllegalArgumentException("コマンドノードの作成に失敗しました: 不正なコマンドノード名です: $name; $COMMAND_NODE_NAME_REGEX に従う必要があります")
        }
    }

    class LiteralCommandNodeBuilder(name: String, configurator: LiteralCommandNodeBuilder.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(nodes: List<CommandNode>): LiteralCommandNode {
            val map = mutableMapOf<String, CommandNode>()
            val list = mutableListOf<CommandNode>()

           val literalNode = LiteralCommandNode(
                name,
                executor,
                map,
                list,
               completerOverride
           )

            list.addAll(redirects.map { name ->
                nodes.findLast { it.name == name }
                    ?: throw IllegalStateException("ノード '${this.name}' のビルドに失敗しました: 親ノードに '$name' が見つかりません")
            })

            map.putAll(children.mapValues {
                it.value.build(nodes + literalNode)
            })

            return literalNode
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(nodes: List<CommandNode>): ArgumentCommandNode<T> {
            val map = mutableMapOf<String, CommandNode>()
            val list = mutableListOf<CommandNode>()

            val argumentNode = ArgumentCommandNode(
                name,
                type,
                executor,
                map,
                list,
                completerOverride
            )

            list.addAll(redirects.map { name ->
                nodes.findLast { it.name == name }
                    ?: throw IllegalStateException("ノード '${this.name}' のビルドに失敗しました: 親ノードに '$name' が見つかりません")
            })

            map.putAll(children.mapValues {
                it.value.build(nodes + argumentNode)
            })

            return argumentNode
        }
    }

    class CommandBuilder(name: String, configurator: CommandBuilder.() -> Unit) : CommandNodeBuilder(name) {
        var description: String = "EMPTY DESCRIPTION"

        val aliases = mutableSetOf<String>()

        init {
            configurator()
        }

        override fun build(nodes: List<CommandNode>): Command {
            val map = mutableMapOf<String, CommandNode>()
            val list = mutableListOf<CommandNode>()

            val command = Command(
                name,
                description,
                aliases,
                executor,
                map,
                list
            )

            list.addAll(redirects.map { name ->
                nodes.findLast { it.name == name }
                    ?: throw IllegalStateException("ノード '${this.name}' のビルドに失敗しました: 親ノードに '$name' が見つかりません")
            })

            map.putAll(children.mapValues {
                it.value.build(nodes + command)
            })

            return command
        }

        fun build(root: RootCommandNode): Command = build(listOf(root))
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit) {
        val builder = LiteralCommandNodeBuilder(this, configurator)
        put(builder)
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) {
        val builder = ArgumentCommandNodeBuilder(this, type, configurator)
        put(builder)
    }

    fun inline(vararg strings: String, configurator: LiteralCommandNodeBuilder.() -> Unit) {
        for (string in strings) {
            val builder = LiteralCommandNodeBuilder(string, configurator)
            put(builder)
        }
    }

    fun put(builder: CommandNodeBuilder) {
        if (builder.name in children) {
            throw IllegalStateException("コマンドノード名 '$this' が重複しています")
        }

        children[builder.name] = builder
    }

    fun redirect(name: String) {
        redirects.add(name)
    }

    fun executes(callback: CommandContext.() -> Any? = {}) {
        if (executor == null) {
            executor = callback
        }
        else {
            throw IllegalArgumentException("実行処理を複数書くことはできません")
        }
    }

    fun complete(callback: (BclCompletionContext) -> List<CompletionItem>) {
        if (completerOverride == null) {
            completerOverride = callback
        }
        else {
            throw IllegalArgumentException("補完処理を複数書くことはできません")
        }
    }

    // 最初に渡すのは listOf(root)
    protected abstract fun build(nodes: List<CommandNode>): CommandNode

    companion object {
        val COMMAND_NODE_NAME_REGEX = "^[a-zA-Z_.][a-zA-Z0-9_\\-.]*$".toRegex()
    }
}
