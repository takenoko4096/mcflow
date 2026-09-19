package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import kotlin.reflect.KClass

/**
 * CommandSourceStack はないよ
 */
class CommandContext(
    private val sourceCode: String,
    private val dispatcher: CommandDispatcher,
    private val route: List<CommandNode>,
    private val arguments: List<Pair<ArgumentCommandNode<*>, Argument<*>>>
) {
    fun exception(sourceSpan: SourceSpan, message: String, cause: Throwable? = null): BclCommandExecutionException {
        return BclCommandExecutionException(message, sourceCode, sourceSpan, cause)
    }

    fun getDispatcher() = dispatcher

    fun getRoute(): List<CommandNode> {
        return route
    }

    fun getArguments() = arguments.toList()

    fun <T : Any> getArgument(name: String, clazz: KClass<T>): Argument<T> {
        val result = mutableListOf<Argument<T>>()
        for ((node, argument) in arguments) {
            if (node.name == name && clazz.isInstance(argument.value)) {
                result.add(argument as Argument<T>)
            }
        }

        if (result.size == 1) {
            return result.first()
        }
        else if (result.size > 1) {
            throw IllegalArgumentException("引数 ${clazz.simpleName} $name が複数見つかりました")
        }
        else {
            throw IllegalArgumentException("引数 ${clazz.simpleName} $name が見つかりません")
        }
    }

    inline fun <reified T : Any> getArgument(name: String): Argument<T> {
        return getArgument(name, T::class)
    }

    inline fun <reified T : Any> getValue(name: String): T {
        return getArgument(name, T::class).value
    }
}
