package io.github.takenoko4096.bcl.brigadier.dispatcher

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * CommandSourceStack はないよ
 */
class CommandExecuteContext(
    val sourceCode: String,
    val dispatcher: CommandDispatcher,
    val route: List<CommandNode>,
    val arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
    val diagnostics: MutableList<BclDiagnostic>,
    val sender: CommandSender
) {
    fun diagnostic(sourceSpan: SourceSpan, message: String): BclDiagnostic {
        return BclDiagnostic(message, sourceCode, sourceSpan)
    }

    fun <T : Any> getArgument(name: String, clazz: KClass<T>): Argument<T> {
        val result = mutableListOf<Argument<T>>()

        for ((node, argument) in arguments) {
            if (argument !is Argument<*>) continue

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