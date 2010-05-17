package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
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
    private int vertical_line = -1;
    protected Color vertical_line_color = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    protected HashMap<String, String> help;
    protected JPopupMenu completionMenu;
    public static int DEFAULT_FONT_SIZE = 10;

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

                }
                super.keyPressed(e);
            }
        });

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (isCompletionKeyEvent(e)) {
                    String txt = getText();
                    String before = txt.substring(0, getCaretPosition());
                    String after = txt.substring(getCaretPosition());

                    LinkedList<JMenu> items = buildCompletionMenu(before, after);
                    if (items == null || items.size() == 0) {
                        return;
                    }

                    completionMenu.removeAll();
                    int n = 0;
                    for (JMenu k : items) {
                        completionMenu.add(k);
                        n++;
                    }
                    if (n > 0) {
                        try {
                            Rectangle r = modelToView(getCaretPosition());
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
        });
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
    LinkedList<JMenu> buildCompletionMenu(String beforeCaret, String afterCaret) {
        if (beforeCaret.length() == 0 || help == null || help.isEmpty()) {
            return null;
        }
        String base = "";
        int i = 1;
        char c;
        c = beforeCaret.charAt(beforeCaret.length() - i);
        while (Character.isLetterOrDigit(c)) {
            base = c + base;
            i++;
            if (((getCaretPosition() - i) >= 0)) {
                c = beforeCaret.charAt(beforeCaret.length() - i);
            } else {
                break;
            }
        }

        if (base.length() <= 0) {
            return null;
        }

        LinkedList<JMenu> items = new LinkedList<JMenu>();
        for (String k : help.keySet()) {
            if (k.startsWith(base)) {
                items.add(new KeyWordItem(k, completionMenu, i));
            }
        }
        return items;
    }

    /**Method to override for changing completion key*/
    boolean isCompletionKeyEvent(KeyEvent e) {
        return e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE;
    }

    class KeyWordItem extends JMenu {//TODO: help content may be displayed automatically when this menu is selected...

        String name;
        MenuElement[] path = new MenuElement[2];
        int alreadywriten;

        public KeyWordItem(final String name, JPopupMenu parent, final int alreadywriten) {
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

            if (help.get(name).length() > 0) {
                add(buildHelpMenu(help.get(name)));
            }

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
    int font_width = 0;
    String font_width_ex = "a";
    boolean reset_font_width = true;

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
