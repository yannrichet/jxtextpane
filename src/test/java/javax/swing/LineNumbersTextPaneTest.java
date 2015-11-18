package javax.swing;

import org.irsn.javax.swing.LineNumbersTextPane;

/**
 *
 * @author richet
 */
public class LineNumbersTextPaneTest {

    public static void main(String[] args) {
        JFrame f = new JFrame("LineNumbersTextPane");
        LineNumbersTextPane te = new LineNumbersTextPane();
        te.setText("AAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\nAAA\nAAAA\nBBBB 0.5\nCCCC 0.5 0.4\n* AAAA\n");
        f.setContentPane(te.getContainerWithLines());
        System.out.println(te.isDisplayLineNumbers());
        System.out.println(te.getNumberOfLines());
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
