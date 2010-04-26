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
import javax.swing.text.EditorKit;

/**
 *
 * @author richet
 */
public class BlockEditTest {

    public static void main(String a[]) throws BadLocationException, Exception {
        final HashMap<String, Color> syntax = new HashMap<String, Color>();
        syntax.put("import", Color.RED);
        syntax.put("abstract", Color.BLUE);
        syntax.put("boolean", Color.BLUE);
        syntax.put("break", Color.BLUE);
        syntax.put("byte", Color.BLUE);
        syntax.put("byvalue", Color.BLUE);
        syntax.put("case", Color.BLUE);
        syntax.put("cast", Color.BLUE);
        syntax.put("catch", Color.BLUE);

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

        //((AbstractDocument) edit.getDocument()).setDocumentFilter(new SyntaxColorizer(edit.getStyledDocument(), syntax));
        ((AbstractDocument) edit.getDocument()).setDocumentFilter(new BlockModeHandler(edit));


        JFrame frame = new JFrame("Syntax Highlighting");
        frame.getContentPane().add(new JScrollPane(edit));
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
    }

    public static String read(String file) throws Exception {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s + "\n");
        }
        fr.close();
        return sb.toString();
    }
}
