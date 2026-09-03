package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

sealed class CommandNodeBuilder {
    protected val children = mutableMapOf<String, CommandNode>()

    protected var executable = false

    open class LiteralCommandNodeBuilder : CommandNodeBuilder() {
        override fun build(): LiteralCommandNode {
            return LiteralCommandNode(executable, children)
        }
    }

    class ArgumentCommandNodeBuilder<T>(private val type: ArgumentType<T>) : CommandNodeBuilder() {
        override fun build(): ArgumentCommandNode<T> {
            return ArgumentCommandNode(type, executable, children)
        }
    }

    class RootCommandNodeBuilder : LiteralCommandNodeBuilder() {
        private var description: String = ""

        private var aliases: Set<String> = setOf()

        fun description(text: String) {
            description = text
        }

        fun aliases(vararg names: String) {
            aliases = names.toSet()
        }

        fun command(): Command {
            return Command(description, aliases, build())
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit) {
        val literalCommandNodeBuilderBuilder = LiteralCommandNodeBuilder()
        literalCommandNodeBuilderBuilder.configurator()
        children[this] = literalCommandNodeBuilderBuilder.build()
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) {
        val argumentCommandNodeBuilderBuilder = ArgumentCommandNodeBuilder(type)
        argumentCommandNodeBuilderBuilder.configurator()
        children[this] = argumentCommandNodeBuilderBuilder.build()
    }

    fun executes() {
        executable = true
    }

    abstract fun build(): CommandNode
}
