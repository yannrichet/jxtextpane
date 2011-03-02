/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.swing;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.math.array.DoubleArray;
import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;

/**
 *
 * @author richet
 */
public class DefaultSyntaxColorizerPerfTest {

    public static void main(String args[]) throws Exception {
        DefaultSyntaxColorizerPerfTest test = new DefaultSyntaxColorizerPerfTest();
        test.setUp();
        test.testTicTocs();

    }
    CodeEditorPane edit;
    String testsrc = "src/javax/swing/JXTextPane.java";

    public void setUp() throws Exception {
        final HashMap syntax = new HashMap();
        syntax.put("import", Color.BLUE);
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

        edit = new CodeEditorPane();
        edit.setVerticalLineAtPos(80);

        edit.setKeywordColor(syntax);

        String content = read(testsrc);
        edit.setText(content);

        JFrame frame = new JFrame("Syntax Highlighting");
        frame.getContentPane().add(edit.getContainerWithLines());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setVisible(true);

        tictocplot = new Plot2DPanel();
        new FrameView(tictocplot);
    }
    static double tic;
    Plot2DPanel tictocplot;

    static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder(n * s.length());
        for (int i = 0; i < n; i++) {
            sb.append(s);
            sb.append('\n');
        }
        return sb.toString();
    }

    public void testTicTocs() {
        String unit_content = read(testsrc);
        int unit = unit_content.split("\n").length;
        for (int i = 1; i <= 33; i++) {
            String content = repeat(unit_content, i);
            double[] s = test10TicTocs(content);
            tictocplot.addScatterPlot("", DoubleArray.buildXY(unit * i, unit * i, s));
        }
        try {
            tictocplot.toGraphicFile(new File("DefaultSyntaxColorizerPerfTest.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public double[] test10TicTocs(String content) {
        double[] sample = new double[10];
        for (int i = 0; i < 10; i++) {
            double s = testTicToc(content);
            //System.out.println("    time elapsed: " + s);
            sample[i] = s;
        }
        //double mean = sum(sample) / sample.length;
        //double sd = sqrt(sum(times(minus(sample, mean), minus(sample, mean))) / (sample.length - 1));
        //System.out.println("  mean time elapsed: " + mean + " +/- " + sd);
        return sample;
    }

    public double testTicToc(String content) {
        tic();
        try {
            edit.setText(content);
        } catch (Exception e) {
            return 0;
        }
        return ((toc() - tic) / 1000);
    }

    public static void tic() {
        tic = Calendar.getInstance().getTimeInMillis();
    }

    public static double toc() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String read(String file) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append('\n');
            }
            fr.close();
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
