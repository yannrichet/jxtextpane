package javax.swing;

import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

/** Component integrating scrollpane and left-side line numbers. Appears like a JXTextPane for easy replacement of former JXTextPane
@author richet (heavily inspired by many searches on the web)
 */
public class LineNumbersTextPane extends JXTextPane {

    private boolean displayLineNumbers = true;
    protected JSplitPane jSplitPane1;
    protected JScrollPane jScrollPane1;
    protected LineNumbersSidePane linenumbers;

    public Rectangle modelToScrollView(int pos) throws BadLocationException {
        Rectangle offset = super.modelToView(pos);
        offset.translate(-jScrollPane1.getViewport().getViewPosition().x, -jScrollPane1.getViewport().getViewPosition().y);
        //System.err.println("modelToView "+super.modelToView(pos)+"\n -> \n"+offset);
        return offset;
    }

    public int getNumberOfLines() {
        return ((LineWrapEditorKit) getEditorKit()).number_of_lines;
    }

    @Override
    public final void setEditorKit(EditorKit kit) {
        if (kit instanceof LineWrapEditorKit) {
            super.setEditorKit(kit);
            ((LineWrapEditorKit) kit).setWrap(false);
        } else {
            throw new IllegalArgumentException("Must be WrapEditorKit");
        }
    }

    @Override
    protected EditorKit createDefaultEditorKit() {
        LineWrapEditorKit kit = new LineWrapEditorKit();
        kit.setWrap(false);
        return kit;
    }

    public LineNumbersTextPane() {
        jScrollPane1 = new JScrollPane(this);

        jScrollPane1.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                updateLineNumberView();
            }
        });

        jSplitPane1 = new JSplitPane();

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerSize(0);
        jSplitPane1.setRightComponent(jScrollPane1);

        linenumbers = new LineNumbersSidePane(this);
        jSplitPane1.setLeftComponent(linenumbers);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                updateLineNumberDivider();
            }
        });

        addMouseWheelListener(new MouseAdapter() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() > 0) {
                        setFont(getFont().deriveFont(getFont().getSize2D() - 1.0f));
                    } else {
                        setFont(getFont().deriveFont(getFont().getSize2D() + 1.0f));
                    }
                    updateLineNumberDivider();
                } else {
                    for (MouseWheelListener m : jScrollPane1.getMouseWheelListeners()) {
                        m.mouseWheelMoved(e);
                    }
                }
            }
        });
        //System.err.println("  Init done.");
    }

    @Override
    public void setCaretPosition(int position) {
        super.setCaretPosition(position);
        updateLineNumberDivider();
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        updateLineNumberDivider();
        updateLineNumberView();
    }

    public void updateLineNumberDivider() {
        if (!isDisplayLineNumbers()) {
            return;
        }
        int width = linenumbers._editor.getFontMetrics(linenumbers._editor.getFont()).charWidth('0');
        int div = (int) (width * (Math.floor(Math.log10(((LineWrapEditorKit) getEditorKit()).number_of_lines + 1)) + 3));
        jSplitPane1.setDividerLocation(div);
    }

    public void updateLineNumberView() {
        // We need to properly convert the points to match the viewport
        // Read docs for viewport
        viewStart = viewToModel(jScrollPane1.getViewport().getViewPosition());
        // starting pos  in document
        viewEnd = viewToModel(new Point(jScrollPane1.getViewport().getViewPosition().x,
                jScrollPane1.getViewport().getViewPosition().y + jScrollPane1.getViewport().getExtentSize().height));
        // end pos in doc
        if (!isDisplayLineNumbers()) {
            return;
        }
        linenumbers.repaint();
        //System.err.println("updateLineNumberView");
    }

    public Container getContainerWithLines() {
        return jSplitPane1;
    }

    /**
     * @return the displayLineNumbers
     */
    public boolean isDisplayLineNumbers() {
        return displayLineNumbers;
    }

    /**
     * @param displayLineNumbers the displayLineNumbers to set
     */
    public void setDisplayLineNumbers(boolean displayLineNumbers) {
        this.displayLineNumbers = displayLineNumbers;
        if (!displayLineNumbers) {
            jSplitPane1.setDividerLocation(0);
        }
    }
    protected int viewStart;
    protected int viewEnd;

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (linenumbers != null) {
            linenumbers.fontChanged();
            linenumbers.repaint();
        }
    }

    public class LineNumbersSidePane extends JPanel {

        LineNumbersTextPane _editor;
        private boolean updateFont;
        private int fontHeight;
        private int fontDesc;
        private int starting_y;

        public LineNumbersSidePane(LineNumbersTextPane editor) {
            _editor = editor;
        }

        void fontChanged() {
            updateFont = true;
        }

        @Override
        public void paint(Graphics g) {
            //System.err.println("JLineNumberPane.paint");
            super.paint(g);

            if (!isDisplayLineNumbers()) {
                return;
            }

            // translate offsets to lines
            Document doc = _editor.getDocument();
            int startline = doc.getDefaultRootElement().getElementIndex(viewStart) + 1;
            int endline = doc.getDefaultRootElement().getElementIndex(viewEnd) + 1;

            g.setFont(_editor.getFont());
            if (updateFont) {
                fontHeight = g.getFontMetrics(_editor.getFont()).getHeight();
                fontDesc = g.getFontMetrics(_editor.getFont()).getDescent();
                updateFont = false;
            }
            try {
                starting_y = _editor.modelToView(viewStart).y - jScrollPane1.getViewport().getViewPosition().y + fontHeight - fontDesc;
            } catch (BadLocationException e1) {
                //e1.printStackTrace();
            } catch (NullPointerException e) {
            }

            for (int line = startline, y = starting_y; line <= endline; y += fontHeight, line++) {
                g.drawString(" " + Integer.toString(line), 0, y);
            }

        }
    }
}
