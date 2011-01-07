package javax.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author richet
 */
public class BraceCompletion extends DocumentFilter {

    private final JTextComponent component;
    private char[][] braces;
    public static char[][] DEFAULT_BRACES = {{'{', '}'}, {'(', ')'}, {'[', ']'}, {'\'', '\''}, {'"', '"'}};

    public BraceCompletion(JTextComponent component, char[]... braces) {
        this.component = component;
        this.braces = braces;
    }

    @Override
    public void insertString(FilterBypass b, int offset, String str, AttributeSet a) throws BadLocationException {
        int caret_offset = 0;
        if (str.length() == 1 && braces != null) {//to avoid when str does not come from user: it crashes if 4096 buffer starts with a '('
            for (char[] cs : braces) {
                if (str.charAt(0) == cs[0]) {
                    str = "" + cs[0] + cs[1];
                    caret_offset = -1;
                    break;
                }
            }
        }

        b.insertString(offset, str, a);

        if (caret_offset != 0) {
            component.setCaretPosition(component.getCaretPosition() + caret_offset);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        super.replace(fb, offset, length, text, attrs);
    }
}
