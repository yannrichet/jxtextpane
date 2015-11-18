/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.swing;

import java.awt.Color;
import java.util.HashMap;

import org.irsn.javax.swing.DefaultSyntaxColorizer.RegExpHashMap;

import org.irsn.javax.swing.CodeEditorPane;

/**
 *
 * @author richet
 */
public class JeremyTest {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        HashMap<String, Color> syntax = new RegExpHashMap();
        syntax.put("begin", Color.YELLOW);
        CodeEditorPane editor = new CodeEditorPane();
        editor.setKeywordColor(syntax);
        frame.setContentPane(editor.getContainerWithLines());
        frame.setVisible(true);
        frame.setSize(400, 300);
    }
}
