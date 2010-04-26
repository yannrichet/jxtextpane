package javax.swing;

import java.awt.Color;
import java.util.HashMap;

/**
 *
 * @author richet
 */
public class JXTextPaneTest {

    public static void main(String[] args) {
        JFrame f = new JFrame("TextEditor");
        final HashMap<String, Color> kw = new HashMap<String, Color>();
        kw.put("AAAA", Color.BLUE);
        kw.put("BBBB", Color.RED);
        kw.put("CCCC", Color.GREEN);
        kw.put("DDDD", Color.YELLOW);

        JXTextPane te = new JXTextPane();
        te.setText("AAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\n");
        f.setContentPane(te);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        /*used to test for memory usage... no problem seen between wrap lines and no wrap.
        static String randText(int lines, int columns) {
        StringBuffer txt = new StringBuffer();
        for (int i = 0; i < lines; i++) {
        for (int j = 0; j < columns; j++) {
        txt.append((int) (Math.random() * 10));
        }
        txt.append("\n");
        }
        return txt.toString();
        }

        public static void main(String[] args) throws InterruptedException {

        TextEditor editor = new TextEditor();

        JFrame container = new JFrame("Test for TextEditor");
        container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        container.add(editor);
        editor.setText(randText(10, 100));
        container.pack();
        container.setVisible(true);

        Thread.sleep(10000);
        for (int i = 0; i < 100; i++) {
        System.out.println("reset");
        editor.setText(randText(10, 100));
        Thread.sleep(3000);
        }
        }*/
    }
}
