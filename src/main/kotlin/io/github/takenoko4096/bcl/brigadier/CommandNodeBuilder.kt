package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.tokenizer.KeyChar

sealed class CommandNodeBuilder(protected val name: String) {
    protected val children = mutableMapOf<String, CommandNode>()

    protected var executable = false

    init {
        if (!name.matches(KeyChar.COMMAND_UNQUOTED_LITERAL)) {
            throw IllegalArgumentException("コマンドノードの作成に失敗しました: 不正なコマンドノード名です: $name; ${KeyChar.COMMAND_UNQUOTED_LITERAL} に従う必要があります")
        }
    }

    open class LiteralCommandNodeBuilder(name: String) : CommandNodeBuilder(name) {
        override fun build(): LiteralCommandNode {
            return LiteralCommandNode(name, executable, children)
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>) : CommandNodeBuilder(name) {
        override fun build(): ArgumentCommandNode<T> {
            return ArgumentCommandNode(name, type, executable, children)
        }
    }

    class RootCommandNodeBuilder(name: String) : LiteralCommandNodeBuilder(name) {
        private var description: String = ""

        private var aliases: Set<String> = setOf()

        fun description(text: String) {
            description = text
        }

        fun aliases(vararg names: String) {
            aliases = names.toSet()
        }

        fun command(): Command {
            return Command(name, description, aliases, build())
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit) {
        val literalCommandNodeBuilderBuilder = LiteralCommandNodeBuilder(this)
        literalCommandNodeBuilderBuilder.configurator()
        children[this] = literalCommandNodeBuilderBuilder.build()
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) {
        val argumentCommandNodeBuilderBuilder = ArgumentCommandNodeBuilder(this, type)
        argumentCommandNodeBuilderBuilder.configurator()
        children[this] = argumentCommandNodeBuilderBuilder.build()
    }

    fun executable() {
        executable = true
    }

    abstract fun build(): CommandNode
}
