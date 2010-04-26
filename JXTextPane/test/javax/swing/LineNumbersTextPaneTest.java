package javax.swing;

import java.awt.Color;
import java.util.HashMap;

/**
 *
 * @author richet
 */
public class LineNumbersTextPaneTest {

    public static void main(String[] args) {
        JFrame f = new JFrame("TextEditor");
        HashMap<String, Color> kw = new HashMap<String, Color>();
        kw.put("AAAA", Color.BLUE);
        kw.put("BBBB", Color.RED);
        kw.put("CCCC", Color.GREEN);
        kw.put("DDDD", Color.YELLOW);
        LineNumbersTextPane te = new LineNumbersTextPane();
        te.setText("AAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\n");
        f.setContentPane(te.getContainerWithLines());
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
