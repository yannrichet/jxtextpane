package javax.swing;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.DefaultSyntaxColorizer.RegExpHashMap;
import javax.swing.text.BadLocationException;

/**
 *
 * @author richet
 */
public class Example {

    public static void main(String a[]) throws BadLocationException, Exception {
        final HashMap<String, Color> syntax = new RegExpHashMap();
        syntax.put("public", Color.BLUE);
        syntax.put("static", Color.BLUE);
        syntax.put("void", Color.MAGENTA);
        syntax.put("main", Color.PINK);
        syntax.put("System", Color.ORANGE);
        syntax.put("out", Color.GREEN);
        syntax.put("err", Color.RED);
        syntax.put("print(\\w{2})", Color.CYAN);

        final CodeEditorPane edit = new CodeEditorPane();

        edit.setKeywordColor(syntax);

        HashMap<String, String> help = new HashMap<String, String>();
        help.put("System", "The System class contains several useful class fields and methods. It cannot be instantiated.");
        help.put("out", "The \"standard\" output stream. This stream is already open and ready to accept output data.");
        help.put("err", "The \"standard\" error output stream. This stream is already open and ready to accept output data.");
        edit.setKeywordHelp(help);

        edit.setText("public static void main(String args[]) {\n  System.out.println(\"this is an example\");\n  //This is a comment\n}");

        edit.setVerticalLineAtPos(80);

        JFrame frame = new JFrame("Code editor");
        frame.getContentPane().add(edit.getContainerWithLines());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
    }
}
