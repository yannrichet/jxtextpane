package javax.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.EditorKit;
import static javax.swing.Common.*;

/**
 *
 * @author richet
 */
public class DocumentFilterChainTest {

    public static void main(String a[]) throws BadLocationException, Exception {
        final HashMap<String, Color> syntax1 = new HashMap<String, Color>();
        syntax1.put("import", Color.RED);
        final HashMap<String, Color> syntax2 = new HashMap<String, Color>();
        syntax2.put("import", Color.BLUE);
        syntax2.put("package", Color.BLUE);


        final LineWrapEditorKit kit = new LineWrapEditorKit();

        final JXTextPane edit = new JXTextPane() {

            @Override
            protected EditorKit createDefaultEditorKit() {
                return kit;
            }
        };

        JButton button = new JButton("Load ...");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    edit.setText(read("src/javax/swing/JXTextPane.java"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        //DocumentFilterChain df = new DocumentFilterChain(new SyntaxColorizer(edit.getStyledDocument(), syntax2),new SyntaxColorizer(edit.getStyledDocument(), syntax1));
        DocumentFilterChain df = new DocumentFilterChain(new SyntaxColorizer(edit.getStyledDocument(), syntax2),new BlockModeHandler(edit));
        ((AbstractDocument) edit.getDocument()).setDocumentFilter(df);

        //((AbstractDocument) edit.getDocument()).setDocumentFilter(new SyntaxColorizer(edit.getStyledDocument(), syntax));
        //((AbstractDocument) edit.getDocument()).setDocumentFilter(new BlockModeHandler(edit));


        JFrame frame = new JFrame("Syntax Highlighting");
        frame.getContentPane().add(new JScrollPane(edit));
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
    }
}
