Integration of several features lacking in JTextPane for code edition purpose.

All classes implemented are highly inspired from web search results.
The main value added of this project is to integrate all of these search results in one consistent API.

Following classes are available:
  * SyntaxColorizer (extends DocumentFilter) providing **keywords and comments colors**
  * BlockModeHandler (extends DocumentFilter) providing column or **block mode** for select, insert, replace and remove text,
  * LineWrapEditorKit (extends StyledEditorKit) providing **lines wrapping** and unwrapping mode,
These _background_ components are integrated in following Swing components (extends JEditorPane):
  * JXTextPane (extends JXEditorPane from SwingX) providing **undo/redo**, **search**, **font zoom** (Ctrl+Mouse wheel),
  * LineNumbersTextPane (extends JXTextPane) providing **line numbers**,
  * CodeEditorPane (extends LineNumbersTextPane) providing **code completion** (Ctrl+Space), **code help**, **vertical line**.

Commercial compatible license.

Here is an example to build a basic code editor panel:
![http://jxtextpane.googlecode.com/svn/trunk/JXTextPane/example.png](http://jxtextpane.googlecode.com/svn/trunk/JXTextPane/example.png)
```
package javax.swing;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.SyntaxColorizer.RegExpHashMap;
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
```