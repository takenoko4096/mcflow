import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import org.junit.jupiter.api.Test
import java.io.PrintStream

object Test {
    @Test
    fun test() {
        System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
        System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

        val text =
            """
            execute as @e[type=player, name=Steve, hasitem={item=apple, data=1, amount = 2..5}] at @s run {
                say a
                say b
                kill
            }
            
            @return 0
            
            @continue #a
            
            set main() {
                /say hello, world!
            }
            """

        println(
            BclParser(
                text,
                BclTokenizer(text).tokenize()
            ).parse()
        )
    }
}
