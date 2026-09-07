package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import kotlin.reflect.KClass

/**
 * CommandSourceStack はないよ
 */
class CommandContext(private val sourceCode: String, private val arguments: Map<String, Argument<*>>) {
    fun exception(sourceSpan: SourceSpan, message: String, cause: Throwable? = null): BclCommandExecutionException {
        return BclCommandExecutionException(message, sourceCode, sourceSpan, cause)
    }

    fun <T : Any> getArgument(name: String, clazz: KClass<T>): Argument<T> {
        if (name !in arguments) {
            throw IllegalArgumentException("'$name' は不明な引数名です")
        }

        val argument = arguments[name]!!

        if (!clazz.isInstance(argument.value)) {
            throw IllegalArgumentException("クラス '${clazz.simpleName}' は引数 '$name' の型ではありません")
        }

        return argument as Argument<T>
    }

    inline fun <reified T : Any> getArgument(name: String): Argument<T> {
        return getArgument(name, T::class)
    }

    inline fun <reified T : Any> getValue(name: String): T {
        return getArgument(name, T::class).value
    }
}
