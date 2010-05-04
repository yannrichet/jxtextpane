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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.text.*;

/**TODO change BlockMode Caret color, copy/paste
 * DocumentFilter to support block mode selection. Selection highlighting is overloaded with custom one.
 * @author richet (heavily inspired by many searches on the web)
 */
public class BlockModeHandler extends DocumentFilter implements ClipboardOwner {

    private boolean blockMode = false;
    JTextComponent component;
    Caret normal;
    Caret block;

    public BlockModeHandler(JTextComponent component) {
        setTextComponent(component);
        normal = component.getCaret();
        block = new BlockModeCaret(Color.RED);
    }

    void setTextComponent(JTextComponent c) {
        if (c == this.component) {
            return;
        }
        this.component = c;

        component.setFont(Font.getFont(Font.MONOSPACED));

        component.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isAltDown()) {
                    setBlockMode(!isBlockMode());
                }

                component.getCaret().setSelectionVisible(false);
                int start = component.getSelectionStart();
                int end = component.getSelectionEnd();
                if (isBlockMode()) {
                    component.setCaret(block);
                } else {
                    component.getHighlighter().removeAllHighlights();
                    component.setCaret(normal);
                }
                component.setSelectionStart(start);
                component.setSelectionEnd(end);

                if (isBlockMode() && e.isControlDown()) {// To support copy/paste keyboard actions... Maybe a better handling should exists
                    if (e.getKeyCode() == KeyEvent.VK_C) {
                        System.out.println("copy");
                        setClipboardContents(getSelectedBlock());
                        return; // needed to not avoerload action with super.keyPressed(e);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_V) {
                        System.out.println("paste");
                        pasteAsBlock(getClipboardContents());
                        return;// needed to not avoerload action with super.keyPressed(e);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_X) {
                        System.out.println("cut");
                        setClipboardContents(getSelectedBlock());
                        clearSelectedBlock();
                        return;// needed to not avoerload action with super.keyPressed(e);
                    }
                }

                super.keyPressed(e);
            }
        });
    }

    /*private void copy()
    {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    TransferHandler transferHandler = jTextComponent.getTransferHandler();
    transferHandler.exportToClipboard(jTextComponent, clipboard, TransferHandler.COPY);
    }

    private void paste()
    {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    TransferHandler transferHandler = jTextComponent.getTransferHandler();
    transferHandler.importData(jTextComponent, clipboard.getContents(null));
    }*/
    /**
     * Place a String on the clipboard, and make this class the
     * owner of the Clipboard's contents.
     */
    void setClipboardContents(String aString) {
        System.err.println("setClipboardContents");
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
        System.err.println("getClipboardContents");
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        TransferHandler transferHandler = component.getTransferHandler();
        Transferable contents = clipboard.getContents(this);
        transferHandler.importData(component, contents);
        try {
            System.err.println(contents.getTransferData(DataFlavor.stringFlavor));
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //    boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        // if (hasTransferableText) {
        try {
            result = (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ex) {
            //highly unlikely since we are using a standard DataFlavor
            //System.err.println(ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            //System.err.println(ex);
            ex.printStackTrace();
        }
        //  }
        return result;
    }

    public void lostOwnership(Clipboard clpbrd, Transferable t) {
        System.err.println("lostOwnership");
    }

    public String getSelectedBlock() {
        if (component == null) {
            return null;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (isBlockMode() && (selections != null && selections.length > 0)) {
            StringBuilder sb = new StringBuilder();
            try {
                int cnt = selections.length;
                for (int i = 0; i < cnt; i++) {
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
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
        if (isBlockMode() && (selections != null && selections.length > 0)) {
            try {
                int cnt = selections.length;
                for (int i = cnt - 1; i >= 0; i--) {
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
                    component.getDocument().remove(start, end - start);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void pasteAsBlock(String s) {//TODO choose strategy and implement
        if (component == null) {
            return;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (isBlockMode() && (selections != null && selections.length > 0)) {
            try {
                int cnt = selections.length;
                for (int i = cnt - 1; i >= 0; i--) {
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
                    component.getDocument().insertString(start, s, null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass b, int offs, String str, AttributeSet a) throws BadLocationException {
        if (component == null) {
            return;
        }
        Highlighter.Highlight[] selections = component.getHighlighter().getHighlights();
        if (isBlockMode() && (selections != null && selections.length > 0)) {
            try {
                int cnt = selections.length;
                for (int i = cnt - 1; i >= 0; i--) {
                    int start = selections[i].getStartOffset();
                    b.insertString(start, str, a);
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
        if (isBlockMode() && (selections != null && selections.length > 0)) {
            try {
                int cnt = selections.length;
                for (int i = cnt - 1; i >= 0; i--) {
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
                    b.remove(start, end - start);
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
                for (int i = cnt - 1; i >= 0; i--) {
                    int start = selections[i].getStartOffset();
                    int end = selections[i].getEndOffset();
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
                repaint(); // erase previous location of caret
                x = r.x; // set new values for x,y,width,height
                y = r.y;
                width = 8;
                height = 8;
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

        protected void customHighlight(Point start, Point end) {
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
                System.err.println(offs0 + " " + view.getStartOffset() + " " + offs1 + " " + view.getEndOffset());
                try {
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                    alloc = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
                } catch (BadLocationException e) {
                    System.err.println("!");
                    return null;
                }
            }
            g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
            return alloc;
        }
        protected Color color; // The color for the underline
    }
}
