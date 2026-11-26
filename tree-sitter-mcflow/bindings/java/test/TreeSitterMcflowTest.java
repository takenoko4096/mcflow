import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.mcflow.TreeSitterMcflow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TreeSitterMcflowTest {
    @Test
    public void testCanLoadLanguage() {
        assertDoesNotThrow(() -> new Language(TreeSitterMcflow.language()));
    }
}
