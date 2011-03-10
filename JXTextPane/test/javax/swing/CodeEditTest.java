package javax.swing;

import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.DefaultSyntaxColorizer.RegExpHashMap;
import javax.swing.text.BadLocationException;
import static javax.swing.Common.*;

/**
 *
 * @author richet
 */
public class CodeEditTest {

    static double tic;

    public static void tic() {
        tic = Calendar.getInstance().getTimeInMillis();
    }

    public static void toc() {
        double toc = Calendar.getInstance().getTimeInMillis();
        System.err.println("time elapsed: " + (toc - tic) / 1000.0);
    }

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
        tic();
        final CodeEditorPane edit = new CodeEditorPane();

        edit.setKeywordColor(syntax);

        HashMap<String, String> help = new HashMap<String, String>();
        help.put("boolea", "???");
        help.put("booleeeaaa", "???");
        help.put("booleeeaaannn", "");
        help.put("abstract", "abstract");
        help.put("boolean", "boolean");
        help.put("break", "break");
        help.put("byte", "byte");
        help.put("byvalue", "byvalue");
        help.put("case", "case");
        help.put("cast", "cast");
        help.put("catch", "catch");
        help.put("zz", "zzZZzz");
        help.put("a", "a");
        help.put("b", "b");
        help.put("c", "c");
        help.put("d", "d");
        help.put("e", "e");
        help.put("f", "f");
        help.put("g", "g");
        help.put("h", "h");
        help.put("i", "i");
        help.put("j", "j");
        help.put("k", "k");
        help.put("l", "l");

        edit.setKeywordHelp(help);

        JButton button = new JButton("Load ...");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    String content = read("/home/richet/tictoctest.txt");
                    //content = content+content;
                    //content = content+content;
                    tic();
                    edit.setText(content);
                    toc();
                    //edit.setText(read("src/javax/swing/JXTextPane.java"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        edit.setText(".654654654\n0.65465465\nzdzertert.6544654\n654654.zefzer\n 32132.654\n1E10\n-6546.645\n+dffds\n-sdfsdf\n-65465sdfgdfg\n-654654 sdfgsdfg\n654654-654654\n\n" + read("src/javax/swing/JXTextPane.java"));

        edit.setVerticalLineAtPos(80);

        JFrame frame = new JFrame("Code editor");
        frame.getContentPane().add(edit.getContainerWithLines());
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300);
        frame.setVisible(true);
        toc();
    }
}
