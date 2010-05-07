package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.EditorKit;

/**
 * Top level class to handle code syntax and advanced edition features.
 * @author richet
 */
public class CodeEditorPane extends LineNumbersTextPane {

    boolean init = false;
    private int vertical_line = -1;
    Color vertical_line_color = new Color(1.0f, 0.0f, 0.0f, 0.4f);

    public CodeEditorPane() {
        super();
        super.setFont(Font.decode(Font.MONOSPACED+" 10"));
        init = true;
    }

    public void setVerticalLineAtPos(int pos) {
        this.vertical_line = pos;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (vertical_line > 0) {
            int xline = getFontMetrics(getFont()).stringWidth("a") * vertical_line + 2;
            g.setColor(vertical_line_color);
            g.drawLine(getVisibleRect().x + xline, getVisibleRect().y, getVisibleRect().x + xline, getVisibleRect().y + getVisibleRect().height);
        }
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
            throw new IllegalArgumentException("Only " + Font.getFont(Font.MONOSPACED).getName() + " is authorized in such component.");
        } else {
            super.setFont(font);
        }
    }
}
