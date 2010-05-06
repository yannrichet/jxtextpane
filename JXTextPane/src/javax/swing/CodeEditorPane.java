package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.EditorKit;

/**
 * Top level class to handle code syntax and advanced edition features.
 * @author richet
 */
public class CodeEditorPane extends LineNumbersTextPane {

    boolean init = false;

    public CodeEditorPane() {
        super();
        super.setFont(Font.getFont(Font.MONOSPACED));
        init = true;
    }

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

    @Override
    public void setFont(Font font) {
        if (font == null || font.getFamily() == null) {
            return;
        }
        if (init && !font.getFamily().equals(Font.MONOSPACED)) {
            throw new IllegalArgumentException("Only " + Font.MONOSPACED + " is authorized in such component.");
        } else {
            super.setFont(font);
        }
    }
}
