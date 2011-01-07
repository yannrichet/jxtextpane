package javax.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.text.*;

/** DocumentFilter to support block mode selection. Selection highlighting is overloaded with custom one.
 * @author richet (heavily inspired by many searches on the web)
 */
public class BlockModeHandler extends DocumentFilter implements ClipboardOwner {

    private boolean blockMode = false;
    JTextComponent component;
    Caret normal;
    BlockModeCaret block;
    Color block_color = new Color(0.3f, .8f, 0.3f, 0.4f);

    public BlockModeHandler(JTextComponent component) {
        setTextComponent(component);
        normal = component.getCaret();
        block = new BlockModeCaret(block_color);
    }

    void setTextComponent(JTextComponent c) {
        if (c == this.component) {
            return;
        }
        this.component = c;

        component.setFont(Font.getFont(Font.MONOSPACED));

        resetCut();
        resetCopy();
        resetPaste();

        component.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (isBlockModeKeyEvent(e)) {
                    setBlockMode(!isBlockMode());
                    if (isBlockMode()) {
                        component.getCaret().setSelectionVisible(false);
                        try {
                            int pos = component.getCaretPosition();
                            int start = component.getSelectionStart();
                            Point startPoint = component.modelToView(start).getLocation();
                            int end = component.getSelectionEnd();
                            Point endPoint = component.modelToView(end).getLocation();
                            endPoint.setLocation(endPoint.x, endPoint.y + 1);
                            component.setCaret(block);
                            component.setCaretPosition(pos);
                            block.customHighlight(startPoint, endPoint);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        int pos = component.getCaretPosition();
                        component.setCaret(normal);
                        component.setCaretPosition(pos);
                        int start = 0;
                        int end = 0;
                        if (component.getHighlighter().getHighlights().length > 0) {
                            start = component.getHighlighter().getHighlights()[0].getStartOffset();
                            end = component.getHighlighter().getHighlights()[component.getHighlighter().getHighlights().length - 1].getEndOffset();
                            component.getHighlighter().removeAllHighlights();
                        } else {
                            start = component.getCaretPosition();
                            end = start;
                        }
                        component.setSelectionStart(start);
                        component.setSelectionEnd(end);
                    }
                }
                super.keyPressed(e);
            }
        });
    }

    /**Method to override for changing block mode key*/
    boolean isBlockModeKeyEvent(KeyEvent e) {
        return e.getKeyChar() == 'c' && e.isAltDown() && !e.isAltGraphDown() && !e.isControlDown();
    }

    void resetCopy() {
        final Action copy = component.getActionMap().get("copy");
        //printActions();
        Action newcopy = new Action() {

            public Object getValue(String key) {
                return copy.getValue(key);
            }

            public void putValue(String key, Object value) {
                copy.putValue(key, value);
            }

            public void setEnabled(boolean b) {
                copy.setEnabled(b);
            }

            public boolean isEnabled() {
                return copy.isEnabled();
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                copy.addPropertyChangeListener(listener);
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                copy.removePropertyChangeListener(listener);
            }

            public void actionPerformed(ActionEvent e) {
                //System.err.println("COPY !");
                if (isBlockMode()) {
                    setClipboardContents(getSelectedBlock());
                } else {
                    copy.actionPerformed(e);
                }
                resetCopy();
            }
        };
        component.getActionMap().put("copy", newcopy);
        //printActions();
    }

    /*void printActions() {
    System.err.println("ACTIONS:");
    for (Object object : component.getActionMap().keys()) {
    System.err.println(object + " " + component.getActionMap().get(object));
    }
    System.err.println("=======================");
    }*/
    void resetPaste() {
        final Action paste = component.getActionMap().get("paste");
        //printActions();
        Action newpaste = new Action() {

            public Object getValue(String key) {
                return paste.getValue(key);
            }

            public void putValue(String key, Object value) {
                paste.putValue(key, value);
            }

            public void setEnabled(boolean b) {
                paste.setEnabled(b);
            }

            public boolean isEnabled() {
                return paste.isEnabled();
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                paste.addPropertyChangeListener(listener);
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                paste.removePropertyChangeListener(listener);
            }

            public void actionPerformed(ActionEvent e) {
                //System.err.println("PASTE !");
                if (isBlockMode()) {
                    pasteAsBlock(getClipboardContents());
                } else {
                    paste.actionPerformed(e);
                }
                resetPaste();
            }
        };
        component.getActionMap().put("paste", newpaste);
        //printActions();
    }

    void resetCut() {
        final Action cut = component.getActionMap().get("cut");
        //printActions();
        Action newcut = new Action() {

            public Object getValue(String key) {
                return cut.getValue(key);
            }

            public void putValue(String key, Object value) {
                cut.putValue(key, value);
            }

            public void setEnabled(boolean b) {
                cut.setEnabled(b);
            }

            public boolean isEnabled() {
                return cut.isEnabled();
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                cut.addPropertyChangeListener(listener);
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                cut.removePropertyChangeListener(listener);
            }

            public void actionPerformed(ActionEvent e) {
                //System.err.println("CUT !");
                if (isBlockMode()) {
                    setClipboardContents(getSelectedBlock());
                    clearSelectedBlock();
                } else {
                    cut.actionPerformed(e);
                }
                resetCut();
            }
        };
        component.getActionMap().put("cut", newcut);
        //printActions();
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the Clipboard's contents.
     */
    void setClipboardContents(String aString) {
        Transferable stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);

        TransferHandler transferHandler = component.getTransferHandler();
        transferHandler.exportToClipboard(component, clipboard, TransferHandler.COPY);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an
     * empty String.
     */
    String getClipboardContents() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(this);
        String data = "";
        try {
            data = (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return data;
    }

    public void lostOwnership(Clipboard clpbrd, Transferable t) {
        //System.err.println("lostOwnership");
    }
    int tailHighlights = 0;

    public void setTailHighlights(int n) {
        tailHighlights = n;
    }

    public String getSelectedBlock() {
        if (component == null) {
            return null;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (isBlockMode() && (selections != null && selections.length > 1)) {
            StringBuilder sb = new StringBuilder();
            try {
                int cnt = selections.length;
                for (int i = 0; i < cnt - tailHighlights; i++) {//do not use the last highlight dedicated to highlight current line !!! Ok, that's not clean :)
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }
                    sb.append(component.getText(start, end - start));
                    sb.append('\n');
                }
                return sb.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void clearSelectedBlock() {
        if (component == null) {
            return;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (isBlockMode() && (selections != null && selections.length > 1)) {
            try {
                int cnt = selections.length;
                for (int i = 0; i < cnt - tailHighlights; i++) {//do not use the last highlight dedicated to highlight current line !!! Ok, that's not clean :)
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }
                    component.getDocument().remove(start, end - start);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void pasteAsBlock(String s) {
        if (component == null) {
            return;
        }
        if (isBlockMode()) {
            Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
            if (selections != null && selections.length > 1) {
                setBlockMode(false);//needed to not apply block mode insert which duplicate string for each selections
                String[] lines = s.split("\n");
                try {
                    int cnt = selections.length;
                    for (int i = 0; i < cnt - tailHighlights; i++) {//do not use the last highlight dedicated to highlight current line !!! Ok, that's not clean :)
                        int start = selections[i].getStartOffset();
                        int end = selections[i].getEndOffset();
                        if (start > end) {
                            int tmp = start;
                            start = end;
                            end = tmp;
                        }
                        if (start != end) {
                            component.getDocument().remove(start, end - start);
                        }
                        component.getDocument().insertString(start, lines[i % lines.length], null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                setBlockMode(true);//restore previous state
            } else {
                try {
                    component.getDocument().insertString(component.getCaretPosition(), s, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass b, int offs, String str, AttributeSet a) throws BadLocationException {
        if (component == null) {
            return;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (!(offs == 0 && 0 == component.getDocument().getLength()) && isBlockMode() && (selections != null && selections.length > 1)) {
            try {
                int cnt = selections.length;
                int start[] = new int[cnt - tailHighlights];
                boolean selectInsert = false;
                for (int i = 0; i < cnt - tailHighlights; i++) {//do not use the last highlight dedicated to highlight current line !!! Ok, that's not clean :)
                    start[i] = selections[i].getStartOffset();
                    if (start[i] - 1 == offs || selections[i].getEndOffset() - 1 == offs) {
                        selectInsert = true;
                    }
                }
                if (selectInsert) {// if not, it means that insert is not related to selection... (like in insrtString( O, ...))
                    Arrays.sort(start);
                    for (int i = start.length - 1; i >= 0; i--) {
                        b.insertString(start[i], str, a);
                    }
                } else {
                    b.insertString(offs, str, a);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            b.insertString(offs, str, a);
        }
    }

    @Override
    public void remove(DocumentFilter.FilterBypass b, int offs, int len) throws BadLocationException {
        if (component == null) {
            return;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (!(offs == 0 && len == component.getDocument().getLength()) && isBlockMode() && (selections != null && selections.length > 1)) {
            try {
                int cnt = selections.length;
                int start[] = new int[cnt - tailHighlights];
                int end[] = new int[cnt - tailHighlights];
                boolean selectRemove = false;
                for (int i = 0; i < cnt - tailHighlights; i++) {//do not use the last highlight dedicated to highlight current line !!! Ok, that's not clean :)
                    start[i] = selections[i].getStartOffset();
                    end[i] = selections[i].getEndOffset();
                    //System.err.println(offs + " " + start[i] + " " + end[i]);
                    if (start[i] - 1 == offs || end[i] - 1 == offs) {
                        selectRemove = true;
                    }
                }
                if (selectRemove) {// if not, it means that insert is not related to selection... (like in insrtString( O, ...))
                    Arrays.sort(start);
                    Arrays.sort(end);
                    for (int i = start.length - 1; i >= 0; i--) {
                        //System.err.println(start[i] + " .. " + (end[i] - start[i]));
                        b.remove(start[i], end[i] - start[i]);
                    }
                } else {
                    //System.err.println("!!!");
                    b.remove(offs, len);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            b.remove(offs, len);
        }
    }

    @Override
    public void replace(DocumentFilter.FilterBypass b, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (component == null) {
            return;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (isBlockMode() && (selections != null && selections.length > 0)) {
            try {
                int cnt = selections.length;
                for (int i = 0; i < cnt - tailHighlights; i++) {//do not use the last highlight dedicated to highlight current line !!! Ok, that's not clean :)
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }
                    super_replace(b, start, end - start, text, attrs);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            super_replace(b, offset, length, text, attrs);
        }
    }

    private void super_replace(DocumentFilter.FilterBypass b, int offset, int length, String text,
                               AttributeSet attrs) throws BadLocationException {
        if (component == null) {
            return;
        }
        if (length == 0 && (text == null || text.length() == 0)) {
            return;
        }
        //DocumentFilter filter = getDocumentFilter();

        //writeLock();
        //try {
            /*if (filter != null) {
        filter.replace(getFilterBypass(), offset, length, text,
        attrs);
        }*/
        //else {
        if (length > 0) {
            b.remove(offset, length);
        }
        if (text != null && text.length() > 0) {
            b.insertString(offset, text, attrs);
        }
        //}
        //} finally {
        //writeUnlock();
        //}
    }

    /**
     * @return the BlockMode
     */
    public boolean isBlockMode() {
        return blockMode;
    }

    /**
     * @param BlockMode true for block mode selection, false for original multiline selection.
     */
    public void setBlockMode(boolean BlockMode) {
        this.blockMode = BlockMode;
    }

    public class BlockModeCaret extends DefaultCaret {

        Point lastPoint = new Point(0, 0);
        LayeredHighlighter.LayerPainter painter;
        Color color;

        public BlockModeCaret(Color c) {
            color = c;
            this.painter = new BlockHighlightPainter(c);
        }

        @Override
        public void paint(Graphics g) {
            JTextComponent comp = getComponent();
            if (comp == null) {
                return;
            }

            int dot = getDot();
            Rectangle r = null;
            try {
                r = comp.modelToView(dot);
            } catch (BadLocationException e) {
                return;
            }
            if (r == null) {
                return;
            }

            if ((x != r.x) || (y != r.y)) {
                x = r.x; // set new values for x,y,width,height
                y = r.y;
                width = 8;
                height = 8;
                repaint(); // erase previous location of caretrepain
            }

            if (isVisible()) {
                g.setColor(color);
                g.drawLine(r.x, r.y, r.x, r.y + 7);
                g.drawLine(r.x, r.y, r.x + 7, r.y);
                g.drawLine(r.x + 1, r.y, r.x + 1, r.y + 7);
                g.drawLine(r.x, r.y + 1, r.x + 7, r.y + 1);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            lastPoint = new Point(e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            getComponent().getHighlighter().removeAllHighlights();
        }

        @Override
        protected void moveCaret(MouseEvent e) {
            super.moveCaret(e);
            Point pt = new Point(e.getX(), e.getY());
            Position.Bias[] biasRet = new Position.Bias[1];
            int pos = getComponent().getUI().viewToModel(getComponent(), pt, biasRet);
            if (biasRet[0] == null) {
                biasRet[0] = Position.Bias.Forward;
            }
            if (pos >= 0) {
                setDot(pos);
                Point start = new Point(Math.min(lastPoint.x, pt.x), Math.min(lastPoint.y, pt.y));
                Point end = new Point(Math.max(lastPoint.x, pt.x), Math.max(lastPoint.y, pt.y));
                customHighlight(start, end);
            }
        }

        public void customHighlight(Point start, Point end) {
            getComponent().getHighlighter().removeAllHighlights();
            int y0 = start.y;
            int firstX = start.x;
            int lastX = end.x;
            int pos1 = getComponent().getUI().viewToModel(getComponent(), new Point(firstX, y0));
            int pos2 = getComponent().getUI().viewToModel(getComponent(), new Point(lastX, y0));
            try {
                getComponent().getHighlighter().addHighlight(pos1, pos2, painter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            y0++;
            while (y0 < end.y) {
                int pos1new = getComponent().getUI().viewToModel(getComponent(), new Point(firstX, y0));
                int pos2new = getComponent().getUI().viewToModel(getComponent(), new Point(lastX, y0));
                if (pos1 != pos1new) {
                    pos1 = pos1new;
                    pos2 = pos2new;
                    try {
                        getComponent().getHighlighter().addHighlight(pos1, pos2, painter);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                y0++;
            }
        }
    }

    // Painter for underlined highlights
    public class BlockHighlightPainter extends LayeredHighlighter.LayerPainter {

        public BlockHighlightPainter(Color c) {
            color = c;
        }

        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            // Do nothing: this method will never be called
        }

        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
            g.setColor(color == null ? c.getSelectionColor() : color);

            Rectangle alloc = null;
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                if (bounds instanceof Rectangle) {
                    alloc = (Rectangle) bounds;
                } else {
                    alloc = bounds.getBounds();
                }
            } else {
                //System.err.println(offs0 + " " + view.getStartOffset() + " " + offs1 + " " + view.getEndOffset());
                try {
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                    alloc = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
                } catch (BadLocationException e) {
                    //System.err.println("!");
                    return null;
                }
            }

            g.fillRect(alloc.x, alloc.y, alloc.width + 2, alloc.height);
            try {
                int luoffset = c.viewToModel(new Point(Math.min(c.modelToView(offs0).x, c.modelToView(offs1).x), Math.min(c.modelToView(offs0).y, c.modelToView(offs1).y)));
                int rloffset = c.viewToModel(new Point(Math.max(c.modelToView(offs0).x, c.modelToView(offs1).x), Math.max(c.modelToView(offs0).y, c.modelToView(offs1).y)));
                return view.modelToView(luoffset, Position.Bias.Forward, rloffset + 1, Position.Bias.Backward, bounds);
            } catch (BadLocationException ex) {
                return alloc;
            }
        }
        protected Color color; // The color for the underline
    }
}
