package javax.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import static javax.swing.Common.*;

/**
 *
 * @author richet
 */
public class SyntaxColorizerViewTest {

    public static void main(String a[]) throws BadLocationException {
        final HashMap syntax = new DefaultSyntaxColorizer.RegExpHashMap();
        //syntax.put("abstract", Color.BLUE);
        syntax.put("abs(\\w+)", Color.BLUE);
        syntax.put("boolean", Color.BLUE);
        syntax.put("break", Color.BLUE);
        syntax.put("byte", Color.BLUE);
        syntax.put("byvalue", Color.BLUE);
        syntax.put("case", Color.BLUE);
        syntax.put("cast", Color.BLUE);
        syntax.put("catch", Color.BLUE);
        syntax.put("char", Color.BLUE);
        syntax.put("class", Color.BLUE);
        syntax.put("const", Color.BLUE);
        syntax.put("continue", Color.BLUE);
        syntax.put("default", Color.BLUE);
        syntax.put("do", Color.BLUE);
        syntax.put("double", Color.BLUE);
        syntax.put("else", Color.BLUE);
        syntax.put("extends", Color.BLUE);
        syntax.put("false", Color.BLUE);
        syntax.put("final", Color.RED);
        syntax.put("finally", Color.RED);
        syntax.put("float", Color.RED);
        syntax.put("for", Color.RED);
        syntax.put("future", Color.RED);
        syntax.put("generic", Color.RED);
        syntax.put("goto", Color.RED);
        syntax.put("if", Color.RED);
        syntax.put("implements", Color.RED);
        syntax.put("import", Color.RED);
        syntax.put("inner", Color.RED);
        syntax.put("instanceof", Color.RED);
        syntax.put("int", Color.RED);
        syntax.put("interface", Color.RED);
        syntax.put("long", Color.RED);
        syntax.put("native", Color.RED);
        syntax.put("new", Color.RED);
        syntax.put("null", Color.RED);
        syntax.put("operator", Color.RED);
        syntax.put("outer", Color.RED);
        syntax.put("package", Color.RED);
        syntax.put("private", Color.GREEN);
        syntax.put("protected", Color.GREEN);
        syntax.put("public", Color.GREEN);
        syntax.put("rest", Color.GREEN);
        syntax.put("return", Color.GREEN);
        syntax.put("short", Color.GREEN);
        syntax.put("static", Color.GREEN);
        syntax.put("super", Color.GREEN);
        syntax.put("switch", Color.GREEN);
        syntax.put("synchronized", Color.GREEN);
        syntax.put("this", Color.GREEN);
        syntax.put("throw", Color.GREEN);
        syntax.put("throws", Color.GREEN);
        syntax.put("transient", Color.GREEN);
        syntax.put("true", Color.GREEN);
        syntax.put("try", Color.GREEN);
        syntax.put("var", Color.GREEN);
        syntax.put("void", Color.GREEN);
        syntax.put("volatile", Color.GREEN);
        syntax.put("while", Color.GREEN);
        syntax.put("\\$(\\w+)", Color.YELLOW);

        final CodeEditorPane edit = new CodeEditorPane();
        edit.setKeywordColor(syntax);

        JButton button = new JButton("Load ...");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    edit.setText(read("src/javax/swing/JXTextPane.java"));

                    /*try {
                        Writer s = new StringWriter();
                        SyntaxHTMLWriter w = new SyntaxHTMLWriter(s, edit.getStyledDocument());
                        w.write();
                        s.close();
                        System.out.println(s.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }*/
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        JFrame frame = new JFrame("Syntax Highlighting");
        frame.getContentPane().add(new JScrollPane(edit));
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
    }
}
