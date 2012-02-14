package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Utilities;
import javax.swing.text.View;

/** Top level class to handle code syntax and advanced edition features.
 * @author richet
 */
public class CodeEditorPane extends LineNumbersTextPane {

    /** Here is a way to handle regexp on keywords.*/
    public static class RegExpKeywordMap extends HashMap {
        
        public boolean keyAsRegexp = true;
        
        @Override
        public boolean containsKey(Object o) {
            if (keyAsRegexp) {
                for (Object regexp_key : super.keySet()) {
                    if (o.toString().matches(regexp_key.toString())) {
                        return true;
                    }
                }
                return false;
            } else {
                for (Object regexp_key : super.keySet()) {
                    if (o.toString().equals(regexp_key.toString())) {
                        return true;
                    }
                }
                return false;
            }
        }
        
        @Override
        public Object get(Object o) {
            if (keyAsRegexp) {
                for (Object regexp_key : super.keySet()) {
                    if (o.toString().matches(regexp_key.toString())) {
                        Object found = super.get(regexp_key.toString());
                        return found;
                    }
                }
                return null;
            } else {
                for (Object regexp_key : super.keySet()) {
                    if (o.toString().equals(regexp_key.toString())) {
                        Object found = super.get(regexp_key.toString());
                        return found;
                    }
                }
                return null;
            }
        }
    }
    boolean init = false;
    protected int vertical_line = -1;
    private boolean caret_line = true;
    protected int caret_line_start = -1;
    protected int caret_line_end = -1;
    private boolean caret_brace = true;
    protected int caret_brace_start = -1;
    protected int caret_brace_end = -1;
    protected Color vertical_line_color = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    protected Color caret_line_color = new Color(0.0f, 0.0f, 1.0f, 0.1f);
    protected Color caret_brace_color = new Color(1.0f, 0.0f, 0.0f, 0.2f);
    protected HashMap<String, String> help;
    protected JPopupMenu completionMenu;
    public static int DEFAULT_FONT_SIZE = 10;
    KeyAdapter keyAdapter;
    int maxCompletionMenuItems = 10;
    public static char[][] OpenCloseBrace = {{'(', ')'}, {'[', ']'}, {'{', '}'}/*, {'<', '>'}, {'\'', '\''}, {'"', '"'}*/};
    
    public class CodeHighlighter implements CaretListener {
        
        public void updateHighlights() {
            int nh = 0;
            if (caret_line) {
                if (caret_line_start >= 0) {
                    if (reset_font_height) {
                        font_height = getFontMetrics(getFont()).getHeight();
                        reset_font_height = false;
                    }
                    if (highlight_width != null) {
                        getHighlighter().removeHighlight(highlight_width);
                        highlight_width = null;
                    }
                    try {
                        highlight_width = getHighlighter().addHighlight(caret_line_start, caret_line_end, painter_width);
                        nh++;
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (caret_brace) {
                if (caret_brace_start >= 0) {
                    if (reset_font_height) {
                        font_height = getFontMetrics(getFont()).getHeight();
                        reset_font_height = false;
                    }
                    if (highlight_brace != null) {
                        getHighlighter().removeHighlight(highlight_brace);
                        highlight_brace = null;
                    }
                    try {
                        highlight_brace = getHighlighter().addHighlight(caret_brace_start, caret_brace_end, painter_brace);
                        nh++;
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    if (reset_font_height) {
                        font_height = getFontMetrics(getFont()).getHeight();
                        reset_font_height = false;
                    }
                    if (highlight_brace != null) {
                        getHighlighter().removeHighlight(highlight_brace);
                        highlight_brace = null;
                    }
                }
            }
            if (blockDocumentFilter != null) {
                blockDocumentFilter.setTailHighlights(nh);
            }
        }
        
        public void caretUpdate(CaretEvent e) {
            JTextComponent comp = (JTextComponent) e.getSource();
            int pos = comp.getCaretPosition();
            if (isCaretBrace() && pos > 1 && pos < comp.getText().length() - 1) {
                char c = comp.getText().charAt(pos);
                char cp = comp.getText().charAt(pos - 1);
                caret_brace_start = -1;
                for (char[] cs : OpenCloseBrace) {
                    char openBrace = cs[0];
                    char closeBrace = cs[1];
                    if (cp == openBrace) {
                        int offset = 0;
                        int open = 0;
                        char t = comp.getText().charAt(pos + offset);
                        while ((open != 0 || t != closeBrace) && (pos + offset) < comp.getText().length() - 1) {
                            if (t == openBrace) {
                                open++;
                            } else if (t == closeBrace) {
                                open--;
                            }
                            offset++;
                            if ((pos + offset) > viewEnd) {
                                offset = 0;
                                break;
                            }
                            t = comp.getText().charAt(pos + offset);
                        }
                        caret_brace_start = pos;
                        caret_brace_end = pos + offset;
                        updateHighlights();
                        break;
                    } else if (c == closeBrace) {
                        int offset = -1;
                        int open = 0;
                        char t = comp.getText().charAt(pos + offset);
                        while ((open != 0 || t != openBrace) && (pos + offset) > 0) {
                            if (t == openBrace) {
                                open++;
                            } else if (t == closeBrace) {
                                open--;
                            }
                            offset--;
                            if ((pos + offset) < viewStart) {
                                offset = 0;
                                break;
                            }
                            t = comp.getText().charAt(pos + offset);
                        }
                        caret_brace_start = pos + offset + 1;
                        caret_brace_end = pos;
                        updateHighlights();
                        break;
                    }
                }
            }
            if (isCaretLine()) {
                Element elem = Utilities.getParagraphElement(comp, pos);
                caret_line_start = elem.getStartOffset();
                caret_line_end = elem.getEndOffset();
                updateHighlights();
            }
        }
    }
    
    public CodeEditorPane() {
        super();
        init = true;
        setFont(Font.decode(Font.MONOSPACED + " " + DEFAULT_FONT_SIZE));
        
        setTabSize(4);
        
        completionMenu = new JPopupMenu() {
            
            @Override
            protected void firePopupMenuCanceled() {
                super.firePopupMenuCanceled();
                completionMenu.setVisible(false);
            }
        };
        
        completionMenu.setBackground(Color.WHITE);
        completionMenu.setFont(getFont());
        
        completionMenu.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent e) {//To handle cycling of suggestions with completion key
                if (isCompletionKeyEvent(e)) {
                    MenuElement[] el = MenuSelectionManager.defaultManager().getSelectedPath();
                    KeyWordItem selected = (KeyWordItem) el[el.length - 1];
                    int sel = completionMenu.getComponentIndex(selected);
                    MenuSelectionManager.defaultManager().setSelectedPath(((KeyWordItem) completionMenu.getComponent((sel + 1) % completionMenu.getComponentCount())).path);
                } else {
                    /*if (completionMenu.isVisible() && Character.isLetterOrDigit(e.getKeyChar())) {
                    System.err.println("" + e.getKeyChar());
                    LinkedList<KeyWordItem> toremove = new LinkedList<KeyWordItem>();
                    for (KeyWordItem keyWordItem : visible_completion_keywords) {
                    if (!keyWordItem.name.startsWith("" + e.getKeyChar(), keyWordItem.alreadywriten - 1)) {
                    toremove.add(keyWordItem);
                    }
                    }
                    System.err.println("To remove:");
                    for (KeyWordItem keyWordItem : toremove) {
                    System.err.println(keyWordItem.name);
                    visible_completion_keywords.remove(keyWordItem);
                    completionMenu.remove(keyWordItem);
                    completionMenu.updateUI();
                    }
                    }*/
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        if (isEditable()) {
                            try {
                                getDocument().remove(getCaretPosition() - 1, 1);
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                        }
                        keyAdapter.keyPressed(e);//TODO rebuild all completionMenu, which may be optimized...
                    } else if (Character.isLetterOrDigit(e.getKeyCode())) {
                        if (isEditable()) {
                            try {
                                getDocument().insertString(getCaretPosition(), "" + e.getKeyChar(), null);
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                        }
                        keyAdapter.keyPressed(e);//TODO rebuild all completionMenu, which may be optimized...
                    }
                }
            }
        });
        
        keyAdapter = new KeyAdapter() {
            
            void fillCompletionMenu(final LinkedList<KeyWordItem> items, final int from) {
                if (items == null) {
                    return;
                }
                completionMenu.setVisible(false);
                completionMenu.removeAll();
                if (from > 0) {
                    completionMenu.add(new JMenuItem(new AbstractAction("...") {
                        
                        public void actionPerformed(ActionEvent e) {
                            fillCompletionMenu(items, Math.max(0, from - maxCompletionMenuItems));
                        }
                    }));
                }
                int i = 0;
                for (i = from; i < Math.min(from + maxCompletionMenuItems, items.size()); i++) {
                    KeyWordItem k = items.get(i);
                    completionMenu.add(k);
                }
                if (i < items.size()) {
                    completionMenu.add(new JMenuItem(new AbstractAction("...") {
                        
                        public void actionPerformed(ActionEvent e) {
                            fillCompletionMenu(items, from + maxCompletionMenuItems);
                        }
                    }));
                }
                completionMenu.setVisible(true);
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (isCompletionKeyEvent(e) || completionMenu.isVisible()) {
                    String txt = getText();
                    String before = txt.substring(0, getCaretPosition());
                    String after = txt.substring(getCaretPosition());
                    
                    LinkedList<KeyWordItem> visible_completion_keywords = buildCompletionMenu(before, after);
                    
                    fillCompletionMenu(visible_completion_keywords, 0);
                    
                    if (visible_completion_keywords != null && visible_completion_keywords.size() > 0) {
                        try {
                            Rectangle r = modelToScrollView(getCaretPosition());
                            completionMenu.show(getParent(), (int) r.getX(), (int) r.getY() + getFont().getSize() + 2);
                            MenuSelectionManager.defaultManager().setSelectedPath(((KeyWordItem) completionMenu.getComponents()[0]).path);
                            completionMenu.requestFocus();
                        } catch (BadLocationException ex) {
                            completionMenu.setVisible(false);
                        }
                    } else {
                        completionMenu.setVisible(false);
                    }
                }
            }
        };
        addKeyListener(keyAdapter);
        
        addCaretListener(new CodeHighlighter());
        
        blockDocumentFilter = new BlockModeHandler(this);
        blockDocumentFilter.setTailHighlights(1);
        
        braceCompleter = new BraceCompletion(this, bracesToComplete);
    }
    
    @Override
    public String getSelectedText() {
        if (blockDocumentFilter != null && blockDocumentFilter.isBlockMode()) {
            return blockDocumentFilter.getSelectedBlock();
        } else {
            return super.getSelectedText();
        }
    }

    /** Method to override for more flexible (and clever:) completion strategy.
    This impl. is just default: suggest complete word with matching begining.*/
    public LinkedList<KeyWordItem> buildCompletionMenu(String beforeCaret, String afterCaret) {
        if (help == null || help.isEmpty()) {
            return null;
        }
        StringBuilder base = new StringBuilder();
        int i = 1;
        if (beforeCaret.length() > 0) {
            char c;
            c = beforeCaret.charAt(beforeCaret.length() - i);
            while (!syntaxDocumentFilter.isTokenSeparator(c)) {
                base.insert(0, c);
                i++;
                if (((getCaretPosition() - i) >= 0)) {
                    c = beforeCaret.charAt(beforeCaret.length() - i);
                } else {
                    break;
                }
            }
        }
        
        LinkedList<KeyWordItem> newitems = new LinkedList<KeyWordItem>();
        for (String k : help.keySet()) {
            if (k.startsWith(base.toString())) {
                newitems.add(new KeyWordItem(k, help, completionMenu, i));
            }
        }
        Collections.sort(newitems);
        return newitems;
    }

    /**Method to override for changing completion key*/
    boolean isCompletionKeyEvent(KeyEvent e) {
        return e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE;
    }
    
    public class KeyWordItem extends JMenu implements Comparable {//TODO: help content may be displayed automatically when this menu is selected...

        String name;
        MenuElement[] path = new MenuElement[2];
        int alreadywriten;
        
        public KeyWordItem(final String name, final Map<String, String> dictionnary, JPopupMenu parent, final int alreadywriten) {
            super(new AbstractAction(name) {
                
                public void actionPerformed(ActionEvent e) {
                    if (isEditable()) {
                        try {
                            getDocument().insertString(getCaretPosition(), name.substring(alreadywriten - 1), null);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                    completionMenu.setVisible(false);
                }
            });
            this.name = name;
            this.alreadywriten = alreadywriten;
            setText(name);
            setFont(completionMenu.getFont());
            
            path[0] = parent;
            path[1] = this;
            this.addMenuKeyListener(new MenuKeyListener() {
                
                public void menuKeyTyped(MenuKeyEvent e) {
                    MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
                    KeyWordItem item = (KeyWordItem) path[1];
                    int code = e.getKeyChar();
                    if (code == KeyEvent.VK_ENTER && item.name.equals(name)) {
                        getAction().actionPerformed(null);
                    }
                }
                
                public void menuKeyPressed(MenuKeyEvent e) {
                }
                
                public void menuKeyReleased(MenuKeyEvent e) {
                }
            });
            this.addMenuListener(new MenuListener() {
                
                public void menuSelected(MenuEvent e) {
                    createHelpMenu(name, dictionnary);
                }
                
                public void menuDeselected(MenuEvent e) {
                }
                
                public void menuCanceled(MenuEvent e) {
                }
            });
        }
        
        protected void createHelpMenu(String name, Map<String, String> dictionnary) {
            if (dictionnary != null && name != null && dictionnary.containsKey(name)) {
                if (dictionnary.get(name) != null && dictionnary.get(name).length() > 0) {
                    add(buildHelpMenu(dictionnary.get(name)));
                }
            }
        }
        
        @Override
        public String toString() {
            return "[" + name.substring(0, alreadywriten) + "]" + name.substring(alreadywriten) + "\n" + super.toString();
        }
        
        public int compareTo(Object o) {
            return name.compareTo(((KeyWordItem) o).name);
        }
    }
    
    public JComponent buildHelpMenu(String help) {
        return new JMenuItem(help);
    }
    
    public void setVerticalLineAtPos(int pos) {
        this.vertical_line = pos;
    }

    /**
     * @return the caret_line
     */
    public boolean isCaretLine() {
        return caret_line;
    }

    /**
     * @param caret_line the caret_line to set
     */
    public void setCaretLine(boolean caret_line) {
        this.caret_line = caret_line;
        
    }

    /**
     * @return the caret_brace
     */
    public boolean isCaretBrace() {
        return caret_brace;
    }

    /**
     * @param caret_brace the caret_brace to set
     */
    public void setCaretBrace(boolean caret_brace) {
        this.caret_brace = caret_brace;
    }
    public Highlighter.HighlightPainter painter_width = new ComponentWidthHighlightPainter(caret_line_color);
    public Object highlight_width;
    
    public class ComponentWidthHighlightPainter extends LayeredHighlighter.LayerPainter {
        
        public ComponentWidthHighlightPainter(Color c) {
            color = c;
        }
        
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            // Do nothing: this method will never be called
        }
        
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
            g.setColor(color);
            Rectangle alloc = null;
            if (bounds instanceof Rectangle) {
                alloc = (Rectangle) bounds;
            } else {
                alloc = bounds.getBounds();
            }
            if (alloc.x <= 5) {// To only apply on layer applied to full line
                alloc.width = getVisibleRect().width;
                g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
            }
            return alloc;
        }
        protected Color color; // The color for the underline
    }
    public Highlighter.HighlightPainter painter_brace = new DefaultHighlightPainter(caret_brace_color);
    public Object highlight_brace;
    //int p = 1;

    @Override
    public void paint(Graphics g) {
        //System.err.println("paint CodeEditorPane " + (p++));
        super.paint(g);
        if (vertical_line > 0) {
            if (reset_font_width) {
                font_width = getFontMetrics(getFont()).stringWidth(font_width_ex);
                reset_font_width = false;
            }
            int xline = font_width * vertical_line + 2;
            g.setColor(vertical_line_color);
            g.drawLine(getVisibleRect().x + xline, getVisibleRect().y, getVisibleRect().x + xline, getVisibleRect().y + getVisibleRect().height);
        }
    }
    public BlockModeHandler blockDocumentFilter;
    public SyntaxColorizer syntaxDocumentFilter;
    public BraceCompletion braceCompleter;
    public char[][] bracesToComplete = BraceCompletion.DEFAULT_BRACES;
    
    public void setBracesToComplete(char[][] bracesToComplete) {
        this.bracesToComplete = bracesToComplete;
        braceCompleter = new BraceCompletion(this, bracesToComplete);
        updateDocumentFilter();
    }
    
    protected SyntaxColorizer buildSyntaxColorizer(HashMap<String, Color> keywords) {
        return new DefaultSyntaxColorizer(this, keywords);
    }
    
    public void setKeywordColor(HashMap<String, Color> keywords) {
        if (keywords == null) {
            syntaxDocumentFilter = null;
        } else {
            syntaxDocumentFilter = buildSyntaxColorizer(keywords);
        }
        updateDocumentFilter();
    }
    
    protected void updateDocumentFilter() {
        ((AbstractDocument) this.getDocument()).setDocumentFilter(new DocumentFilterChain(braceCompleter, syntaxDocumentFilter, blockDocumentFilter));
    }
    
    public void setKeywordHelp(HashMap<String, String> keywords) {
        help = keywords;
    }
    
    @Override
    protected EditorKit createDefaultEditorKit() {
        return new LineWrapEditorKit();
    }
    protected int font_width = 0;
    protected int font_height = 0;
    protected String font_width_ex = "a";
    protected boolean reset_font_width = true;
    protected boolean reset_font_height = true;
    
    @Override
    public void setFont(Font font) {
        if (font == null || font.getFamily() == null) {
            return;
        }
        if (init && !font.getFamily().contains("Mono")) {
            throw new IllegalArgumentException("Only monospaced font is authorized in such component.");
        } else {
            super.setFont(font);
            reset_font_width = true;
            reset_font_height = true;
        }
    }
}
