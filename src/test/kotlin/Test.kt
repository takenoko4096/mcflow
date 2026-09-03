import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.argument.IntegerArgumentType
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import org.junit.jupiter.api.Test
import java.io.PrintStream

object Test {
    @Test
    fun test() {
        System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
        System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

        val dispatcher = CommandDispatcher()
        dispatcher.setCommand("return") {
            "value"(IntegerArgumentType()) {
                executes()
            }
        }

        val text =
            """
            return 42
            """

        println(
            BclParser(
                text,
                BclTokenizer(text).tokenize(),
                dispatcher
            ).parse()
        )
    }
}
