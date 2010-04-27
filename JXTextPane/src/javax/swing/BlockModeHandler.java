package javax.swing;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.text.*;

/**TODO font monospaced only
 * DocumentFilter to support block mode selection. Selection highlighting is overloaded with custom one.
 */
public class BlockModeHandler extends DocumentFilter {

    private boolean blockMode = false;
    JTextComponent component;
    Caret normal;
    Caret block;

    public BlockModeHandler(JTextComponent component) {
        setTextComponent(component);
        normal = component.getCaret();
        block = new BlockModeCaret();
    }

    void setTextComponent(JTextComponent c) {
        if (c == this.component) {
            return;
        }
        this.component = c;
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
                super.keyPressed(e);
            }
        });
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
                    int end = selections[i].getEndOffset();
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
                getComponent().getHighlighter().addHighlight(pos1, pos2, DefaultHighlighter.DefaultPainter);
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
                        getComponent().getHighlighter().addHighlight(pos1, pos2, DefaultHighlighter.DefaultPainter);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                y0++;
            }
        }
    }
}
