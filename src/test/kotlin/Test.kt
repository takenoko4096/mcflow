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
            set main() {
                /say hello
                /kill @e[type=!player]
                
                @while (x == 0) {
                    @break
                }
            }
            
            macro mul2(int x) -> int = x * 2;
            
            scoreboard players set #X mul2(1)
            """

        println(
            BclParser(
                text,
                BclTokenizer(text).tokenize()
            ).parse()
        )
    }
}
