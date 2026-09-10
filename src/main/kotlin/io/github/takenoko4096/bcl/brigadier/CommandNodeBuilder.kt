package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

sealed class CommandNodeBuilder(protected val name: String) {
    protected val children = mutableMapOf<String, CommandNode>()

    protected var executor: (CommandContext.() -> Any?)? = null

    protected open var description: String? = null

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
            return LiteralCommandNode(name, description, executor, children)
        }
    }

    class ArgumentCommandNodeBuilder<T>(name: String, private val type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) : CommandNodeBuilder(name) {
        init {
            configurator()
        }

        override fun build(): ArgumentCommandNode<T> {
            return ArgumentCommandNode(name, description, type, executor, children)
        }
    }

    class CommandBuilder(name: String, configurator: CommandBuilder.() -> Unit) : CommandNodeBuilder(name) {
        public override var description: String? = null

        private var aliases: Set<String> = setOf()

        init {
            configurator()
        }

        fun aliases(vararg names: String) {
            aliases = names.toSet()
        }

        override fun build(): Command {
            if (description == null) {
                // TODO temp throw IllegalStateException("コマンド '$name' の description が設定されていません")
                description = "empty description"
            }

            return Command(name, description!!, aliases, executor, children)
        }
    }

    operator fun String.invoke(configurator: LiteralCommandNodeBuilder.() -> Unit) {
        children[this] = LiteralCommandNodeBuilder(this, configurator).build()
    }

    operator fun <T> String.invoke(type: ArgumentType<T>, configurator: ArgumentCommandNodeBuilder<T>.() -> Unit) {
        children[this] = ArgumentCommandNodeBuilder(this, type, configurator).build()
    }

    // TODO temp description = ""
    fun executes(description: String = "empty description", callback: CommandContext.() -> Any? = {}) {
        if (executor == null) {
            executor = callback
            this.description = description
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
