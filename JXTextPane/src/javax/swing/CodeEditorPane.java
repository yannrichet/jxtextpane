package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;

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
    protected Color vertical_line_color = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    protected HashMap<String, String> help;
    protected JPopupMenu completionMenu;
    public static int DEFAULT_FONT_SIZE = 10;
    KeyAdapter keyAdapter;
    int maxCompletionMenuItems = 10;

    public CodeEditorPane() {
        super();
        init = true;
        setFont(Font.decode(Font.MONOSPACED + " " + DEFAULT_FONT_SIZE));

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
                completionMenu.setVisible(false);
                completionMenu.removeAll();
                if (from > 0) {
                    completionMenu.add(new JMenuItem(new AbstractAction("...") {

                        public void actionPerformed(ActionEvent e) {
                            fillCompletionMenu(items, Math.min(0, from - maxCompletionMenuItems));
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

                    if (visible_completion_keywords.size() > 0) {
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
        String base = "";
        int i = 1;
        if (beforeCaret.length() > 0) {
            char c;
            c = beforeCaret.charAt(beforeCaret.length() - i);
            while (!syntaxDocumentFilter.isDelimiter(c)) {
                base = c + base;
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
            if (k.startsWith(base)) {
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

        public KeyWordItem(final String name, HashMap<String, String> dictionnary, JPopupMenu parent, final int alreadywriten) {
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

            createHelpMenu(name, dictionnary);

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

        }

        protected void createHelpMenu(String name, HashMap<String, String> dictionnary) {
            if (dictionnary != null && name != null) {
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

    @Override
    public void paint(Graphics g) {
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
    BlockModeHandler blockDocumentFilter;
    SyntaxColorizer syntaxDocumentFilter;

    public void setKeywordColor(HashMap<String, Color> keywords) {
        if (keywords == null) {
            blockDocumentFilter = new BlockModeHandler(this);
            ((AbstractDocument) this.getDocument()).setDocumentFilter(blockDocumentFilter);
        } else {
            blockDocumentFilter = new BlockModeHandler(this);
            syntaxDocumentFilter = new SyntaxColorizer(this, keywords);
            ((AbstractDocument) this.getDocument()).setDocumentFilter(new DocumentFilterChain(syntaxDocumentFilter, blockDocumentFilter));
        }
    }

    public void setKeywordHelp(HashMap<String, String> keywords) {
        help = keywords;
    }

    @Override
    protected EditorKit createDefaultEditorKit() {
        return new LineWrapEditorKit();
    }
    protected int font_width = 0;
    protected String font_width_ex = "a";
    protected boolean reset_font_width = true;

    @Override
    public void setFont(Font font) {
        if (font == null || font.getFamily() == null) {
            return;
        }
        if (init && !font.getFamily().equals(Font.MONOSPACED)) {
            throw new IllegalArgumentException("Only " + Font.getFont(Font.MONOSPACED).getName() + " is authorized in such component.");
        } else {
            super.setFont(font);
            reset_font_width = true;
        }
    }
}