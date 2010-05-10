package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;

/**
 * Top level class to handle code syntax and advanced edition features.
 * @author richet
 */
public class CodeEditorPane extends LineNumbersTextPane {

    boolean init = false;
    private int vertical_line = -1;
    Color vertical_line_color = new Color(1.0f, 0.0f, 0.0f, 0.4f);
    HashMap<String, String> help;
    JPopupMenu completionMenu;
    static int DEFAULT_FONT_SIZE = 10;

    public CodeEditorPane() {
        super();
        super.setFont(Font.decode(Font.MONOSPACED + " " + DEFAULT_FONT_SIZE));
        init = true;

        completionMenu = new JPopupMenu() {

            @Override
            protected void firePopupMenuCanceled() {
                super.firePopupMenuCanceled();
                completionMenu.setVisible(false);
            }
        };

        completionMenu.setBackground(Color.WHITE);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE) {

                    String base = "";
                    int i = 1;
                    char c;
                    try {
                        c = getText(getCaretPosition() - i, i).charAt(0);
                        while (Character.isLetterOrDigit(c)) {
                            base = c + base;
                            i++;
                            c = getText(getCaretPosition() - i, i).charAt(0);
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                    System.err.println(base);
                    if (base.length() <= 0) {
                        return;
                    }

                    completionMenu.removeAll();
                    int n = 0;
                    for (String k : help.keySet()) {
                        if (k.startsWith(base)) {
                            completionMenu.add(new KeyWordItem(k, completionMenu, i));
                            n++;
                        }
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

    class KeyWordItem extends JMenu {

        String name;
        MenuElement[] path = new MenuElement[2];
        int alreadywriten;

        public KeyWordItem(final String name, JPopupMenu parent, final int alreadywriten) {
            super(new AbstractAction(name) {

                public void actionPerformed(ActionEvent e) {
                    try {
                        getDocument().insertString(getCaretPosition(), name.substring(alreadywriten - 1), null);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                    completionMenu.setVisible(false);
                }
            });
            this.name = name;
            this.alreadywriten = alreadywriten;
            setText(name);
            if (alreadywriten == name.length()) {
                add(help.get(name));
            }
            path[0] = parent;
            path[1] = this;
            this.addMenuKeyListener(new MenuKeyListener() {

                public void menuKeyTyped(MenuKeyEvent e) {
                    MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
                    KeyWordItem item = (KeyWordItem) path[path.length - 1];
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

    public void setVerticalLineAtPos(int pos) {
        this.vertical_line = pos;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (vertical_line > 0) {
            int xline = getFontMetrics(getFont()).stringWidth("a") * vertical_line + 2;
            g.setColor(vertical_line_color);
            g.drawLine(getVisibleRect().x + xline, getVisibleRect().y, getVisibleRect().x + xline, getVisibleRect().y + getVisibleRect().height);
        }
    }

    public void setKeywordColor(HashMap<String, Color> keywords) {
        if (keywords == null) {
            ((AbstractDocument) this.getDocument()).setDocumentFilter(new BlockModeHandler(this));
        } else {
            ((AbstractDocument) this.getDocument()).setDocumentFilter(new DocumentFilterChain(new SyntaxColorizer(this.getStyledDocument(), keywords), new BlockModeHandler(this)));
        }
    }

    public void setKeywordHelp(HashMap<String, String> keywords) {
        help = keywords;
    }

    @Override
    protected EditorKit createDefaultEditorKit() {
        return new LineWrapEditorKit();
    }

    @Override
    public void setFont(Font font) {
        if (font == null || font.getFamily() == null) {
            return;
        }
        if (init && !font.getFamily().equals(Font.MONOSPACED)) {
            throw new IllegalArgumentException("Only " + Font.getFont(Font.MONOSPACED).getName() + " is authorized in such component.");
        } else {
            super.setFont(font);
        }
    }
}
