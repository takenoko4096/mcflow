package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

sealed class CommandNodeBuilderX(protected val name: String) {
    protected val children = mutableMapOf<String, CommandNodeBuilderX>()

    protected var executor: (CommandContext.() -> Any?)? = null

    protected val redirects = mutableSetOf<String>()

    init {
        if (!name.matches(JAVA_IDENTIFIER_STRING_REGEX)) {
            throw IllegalArgumentException("コマンドノードの作成に失敗しました: 不正なコマンドノード名です: $name; $JAVA_IDENTIFIER_STRING_REGEX に従う必要があります")
        }
    }

    class LiteralCommandNodeBuilder(name: String, configurator: LiteralCommandNodeBuilder.() -> Unit) : CommandNodeBuilderX(name) {
        init {
            configurator()
        }

        override fun build(dispatcher: CommandDispatcher): LiteralCommandNode {
            return LiteralCommandNode(
                name,
                executor,
                children.mapValues { it.value.build(dispatcher) },
                mutableListOf()
            )
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) : CommandNodeBuilderX(name) {
        init {
            configurator()
        }

        override fun build(dispatcher: CommandDispatcher): ArgumentCommandNode<T> {
            return ArgumentCommandNode(
                name,
                type,
                executor,
                children.mapValues { it.value.build(dispatcher) },
                mutableListOf()
            )
        }
    }

    class CommandBuilder(name: String, configurator: CommandBuilder.() -> Unit) : CommandNodeBuilderX(name) {
        var description: String = "EMPTY DESCRIPTION"

        val aliases = mutableSetOf<String>()

        init {
            configurator()
        }

        override fun build(dispatcher: CommandDispatcher): Command {
            val command = Command(
                name,
                description,
                aliases,
                executor,
                children.mapValues {
                    it.value.build(dispatcher, /*ここにList<CommandNode>を渡すんね*/)
                }
            )

            /*for (redirect in children.redirects) {
                val target = node.getNamedParent(command, redirect) ?: throw IllegalArgumentException()
                (node.redirects as MutableList<CommandNode>).add(target)
            }*/

            return command
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit) {
        val builder = LiteralCommandNodeBuilder(this, configurator)
        put(builder)
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) {
        val builder = ArgumentCommandNodeBuilder(this, type, configurator)
        put(builder)
    }

    fun put(builder: CommandNodeBuilderX) {
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

    abstract fun build(dispatcher: CommandDispatcher): CommandNode

    companion object {
        val JAVA_IDENTIFIER_STRING_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()
    }
}
