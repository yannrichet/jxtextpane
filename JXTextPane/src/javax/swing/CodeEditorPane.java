package javax.swing;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.EditorKit;

/**
 * Top level class to handle code syntax and advanced edition features.
 * @author richet
 */
public class CodeEditorPane extends LineNumbersTextPane {

    public void setKeywordColor(HashMap<String, Color> keywords) {
        if (keywords == null) {
            ((AbstractDocument) this.getDocument()).setDocumentFilter(new BlockModeHandler(this));
        } else {
            ((AbstractDocument) this.getDocument()).setDocumentFilter(new DocumentFilterChain(new SyntaxColorizer(this.getStyledDocument(), keywords), new BlockModeHandler(this)));
        }
    }

    @Override
    protected EditorKit createDefaultEditorKit() {
        return new LineWrapEditorKit();

    }
}
