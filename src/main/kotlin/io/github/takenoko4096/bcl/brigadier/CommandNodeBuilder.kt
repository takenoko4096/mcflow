package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

sealed class CommandNodeBuilder(protected val name: String) {
    protected val children = mutableMapOf<String, CommandNode>()

    protected var executor: (CommandContext.() -> Any?)? = null

    protected lateinit var rootBuilder: CommandBuilder

    protected val redirects = mutableSetOf<String>()

    init {
        if (!name.matches(JAVA_IDENTIFIER_STRING_REGEX)) {
            throw IllegalArgumentException("コマンドノードの作成に失敗しました: 不正なコマンドノード名です: $name; $JAVA_IDENTIFIER_STRING_REGEX に従う必要があります")
        }
    }

    class LiteralCommandNodeBuilder(name: String, configurator: LiteralCommandNodeBuilder.() -> Unit, rootBuilder: CommandBuilder) : CommandNodeBuilder(name) {
        init {
            this.rootBuilder = rootBuilder
            configurator()
        }

        override fun build(): LiteralCommandNode {
            return LiteralCommandNode(name, executor, children, mutableListOf()).apply {
                rootBuilder.builders[this@apply] = this@LiteralCommandNodeBuilder
            }
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit, rootBuilder: CommandBuilder) : CommandNodeBuilder(name) {
        init {
            this.rootBuilder = rootBuilder
            configurator()
        }

        override fun build(): ArgumentCommandNode<T> {
            return ArgumentCommandNode(name, type, executor, children, mutableListOf()).apply {
                rootBuilder.builders[this@apply] = this@ArgumentCommandNodeBuilder
            }
        }
    }

    class CommandBuilder(name: String, configurator: CommandBuilder.() -> Unit) : CommandNodeBuilder(name) {
        init {
            this.rootBuilder = this
        }

        internal val builders = mutableMapOf<CommandNode, CommandNodeBuilder>()

        var description: String = "EMPTY DESCRIPTION"

        val aliases = mutableSetOf<String>()

        init {
            configurator()
        }

        override fun build(): Command {
            val command = Command(name, description, aliases, executor, children)

            for ((node, builder) in builders) {
                if (builder.redirects.isEmpty()) continue

                for (targetName in builder.redirects) {
                    val target = node.getNamedParent(command, targetName) ?: throw IllegalArgumentException()
                    (node.redirects as MutableList<CommandNode>).add(target)
                }
            }

            return command
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit): LiteralCommandNode {
        val builder = LiteralCommandNodeBuilder(this, configurator, rootBuilder)
        val node = builder.build()
        put(node)
        return node
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit): ArgumentCommandNode<T> {
        val builder = ArgumentCommandNodeBuilder(this, type, configurator, rootBuilder)
        val node = builder.build()
        put(node)
        return node
    }

    fun put(node: CommandNode) {
        if (node.name in children) {
            throw IllegalStateException("コマンドノード名 '$this' が重複しています")
        }

        children[node.name] = node
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

    abstract fun build(): CommandNode

    companion object {
        val JAVA_IDENTIFIER_STRING_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()
    }
}
