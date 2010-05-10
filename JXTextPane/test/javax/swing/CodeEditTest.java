package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import javax.swing.SyntaxColorizer.RegExpHashMap;
import javax.swing.text.BadLocationException;
import static javax.swing.Common.*;

/**
 *
 * @author richet
 */
public class CodeEditTest {

    public static void main(String a[]) throws BadLocationException, Exception {
        final HashMap<String, Color> syntax = new RegExpHashMap();
        syntax.put("imp(\\w{3})", Color.RED);
        syntax.put("abstract", Color.BLUE);
        syntax.put("boolean", Color.BLUE);
        syntax.put("break", Color.BLUE);
        syntax.put("byte", Color.BLUE);
        syntax.put("byvalue", Color.BLUE);
        syntax.put("case", Color.BLUE);
        syntax.put("cast", Color.BLUE);
        syntax.put("catch", Color.BLUE);

        final CodeEditorPane edit = new CodeEditorPane();

        edit.setKeywordColor(syntax);

        HashMap<String, String> help = new HashMap<String, String>();
        help.put("boolean", "???");
        help.put("booleeeaaa", "???");
        edit.setKeywordHelp(help);

        edit.setText(read("src/javax/swing/JXTextPane.java"));

        edit.setVerticalLineAtPos(80);

        JFrame frame = new JFrame("Code editor");
        frame.getContentPane().add(edit.getContainerWithLines());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
    }
}
