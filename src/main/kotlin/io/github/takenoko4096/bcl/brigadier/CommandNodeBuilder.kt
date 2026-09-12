package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

sealed class CommandNodeBuilder(protected val name: String) {
    protected val children = mutableMapOf<String, CommandNode>()

    protected var executor: (CommandContext.() -> Any?)? = null

    init {
        if (!name.matches(JAVA_IDENTIFIER_STRING_REGEX)) {
            throw IllegalArgumentException("コマンドノードの作成に失敗しました: 不正なコマンドノード名です: $name; $JAVA_IDENTIFIER_STRING_REGEX に従う必要があります")
        }
    }

    class LiteralCommandNodeBuilder(name: String, configurator: LiteralCommandNodeBuilder.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(): LiteralCommandNode {
            return LiteralCommandNode(name, executor, children)
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(): ArgumentCommandNode<T> {
            return ArgumentCommandNode(name, type, executor, children)
        }
    }

    class CommandBuilder(name: String, configurator: CommandBuilder.() -> Unit) : CommandNodeBuilder(name) {
        var description: String = "EMPTY DESCRIPTION"

        val aliases = mutableSetOf<String>()

        init {
            configurator()
        }

        override fun build(): Command {
            return Command(name, description, aliases, executor, children)
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit): LiteralCommandNode {
        val node = LiteralCommandNodeBuilder(this, configurator).build()
        put(node)
        return node
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit): ArgumentCommandNode<T> {
        val node = ArgumentCommandNodeBuilder(this, type, configurator).build()
        put(node)
        return node
    }

    fun put(node: CommandNode) {
        if (node.name in children) {
            throw IllegalStateException("コマンドノード名 '$this' が重複しています")
        }

        children[node.name] = node
    }

    // TODO fun lazy()

    fun executes(callback: CommandContext.() -> Any? = {}) {
        if (executor == null) {
            executor = callback
        }
        else {
            throw IllegalArgumentException("実行処理を複数書くことはできません")
        }
    }

    abstract fun build(): CommandNode

    companion object {
        val JAVA_IDENTIFIER_STRING_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()
    }
}
