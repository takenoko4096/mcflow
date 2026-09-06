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
        if (!name.matches(JAVA_IDENTIFIER_STRING_REGEX)) {
            throw IllegalArgumentException("コマンドノードの作成に失敗しました: 不正なコマンドノード名です: $name; $JAVA_IDENTIFIER_STRING_REGEX に従う必要があります")
        }
    }

    class LiteralCommandNodeBuilder(name: String, configurator: LiteralCommandNodeBuilder.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(): LiteralCommandNode {
            return LiteralCommandNode(name, executable, children)
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(): ArgumentCommandNode<T> {
            return ArgumentCommandNode(name, type, executable, children)
        }
    }

    class CommandBuilder(name: String, configurator: CommandBuilder.() -> Unit) : CommandNodeBuilder(name) {
        var description: String = ""

        private var aliases: Set<String> = setOf()

        init {
            configurator()
        }

        fun aliases(vararg names: String) {
            aliases = names.toSet()
        }

        override fun build(): Command {
            return Command(name, description, aliases, executable, children)
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit) {
        children[this] = LiteralCommandNodeBuilder(this, configurator).build()
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) {
        children[this] = ArgumentCommandNodeBuilder(this, type, configurator).build()
    }

    fun executable() {
        executable = true
    }

    abstract fun build(): CommandNode

    companion object {
        val JAVA_IDENTIFIER_STRING_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()
    }
}
