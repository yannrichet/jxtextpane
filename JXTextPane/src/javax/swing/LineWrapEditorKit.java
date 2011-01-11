package javax.swing;

import java.io.IOException;
import java.io.Reader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
@author richet (heavily inspired by many searches on the web)*/
public class LineWrapEditorKit extends StyledEditorKit {

    private static final char EOL_char = '\n';
    private static final String EOL = "\n";
    private static final String EOL_CR = "\r";
    private static final String EOL_CRLF = "\r\n";
    private boolean wrap = false;
    int number_of_lines;
    protected static ViewFactory defaultFactory;

    public LineWrapEditorKit() {
        super();
        number_of_lines = 0;
    }

    @Override
    public ViewFactory getViewFactory() {
        if (defaultFactory == null) {
            defaultFactory = new WrapViewFactory(super.getViewFactory());
        }
        return defaultFactory;
    }

    public boolean isWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public class WrapViewFactory implements ViewFactory {

        private ViewFactory parentViewFactory;

        WrapViewFactory(ViewFactory parentViewFactory) {
            super();
            this.parentViewFactory = parentViewFactory;
        }

        public View create(Element elem) {
            //System.out.print(".");
            if (AbstractDocument.ParagraphElementName.equals(elem.getName())) {
                return new WrapParagraphView(elem);
            }
            return parentViewFactory.create(elem);
        }
        /*public View create(Element elem) {
        System.out.println("create");
        String kind = elem.getName();
        if (kind != null) {
        if (kind.equals(AbstractDocument.ContentElementName)) {
        return new NoWrapView(elem);
        } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
        return new ParagraphView(elem);
        } else if (kind.equals(AbstractDocument.SectionElementName)) {
        return new BoxView(elem, View.Y_AXIS);
        } else if (kind.equals(StyleConstants.ComponentElementName)) {
        return new ComponentView(elem);
        } else if (kind.equals(StyleConstants.IconElementName)) {
        return new IconView(elem);
        }
        }

        // default to text display
        return new NoWrapView(elem);
        }*/
    }

    public class WrapParagraphView extends ParagraphView {

        WrapParagraphView(Element elem) {
            super(elem);
        }

        @Override
        protected void layout(int width, int height) {
            if (!wrap) {
                super.layout(Short.MAX_VALUE, height);
            } else {
                super.layout(width, height);
            }
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (!wrap) {
                return super.getPreferredSpan(axis);
            } else {
                return super.getMinimumSpan(axis);
            }
        }
    }
    /*public static class NoWrapView extends LabelView {

    public NoWrapView(Element e) {
    super(e);
    }

    public float getTabbedSpan(float x, TabExpander e) {
    float result = super.getTabbedSpan(x, e);
    this.preferenceChanged(this, true, false);
    return result;
    }
    }*/

    /**
     * Inserts content from the given stream, which will be
     * treated as plain text.
     *
     * @param in  The stream to read from
     * @param doc The destination for the insertion.
     * @param pos The location in the document to place the
     *   content >= 0.
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document.
     */
    @Override
    public void read(Reader in, Document doc, int pos)
            throws IOException, BadLocationException {

        if (pos == 0) {
            number_of_lines = 0;
        }

        char[] buff = new char[4096];
        int nch;
        boolean lastWasCR = false;
        boolean isCRLF = false;
        boolean isCR = false;
        int last;
        boolean wasEmpty = (doc.getLength() == 0);
        AttributeSet attr = getInputAttributes();

        // Read in a block at a time, mapping \r\n to \n, as well as single
        // \r's to \n's. If a \r\n is encountered, \r\n will be set as the
        // newline string for the document, if \r is encountered it will
        // be set as the newline character, otherwise the newline property
        // for the document will be removed.
        while ((nch = in.read(buff, 0, buff.length)) != -1) {
            last = 0;
            for (int counter = 0; counter < nch; counter++) {
                switch (buff[counter]) {
                    case '\r':
                        number_of_lines++;
                        if (lastWasCR) {
                            isCR = true;
                            if (counter == 0) {
                                doc.insertString(pos, EOL, attr);
                                pos++;
                            } else {
                                buff[counter - 1] = EOL_char;
                            }
                        } else {
                            lastWasCR = true;
                        }
                        break;
                    case '\n':
                        number_of_lines++;
                        if (lastWasCR) {
                            if (counter > (last + 1)) {
                                doc.insertString(pos, new String(buff, last,
                                        counter - last - 1), attr);
                                pos += (counter - last - 1);
                            }
                            // else nothing to do, can skip \r, next write will
                            // write \n
                            lastWasCR = false;
                            last = counter;
                            isCRLF = true;
                        }
                        break;
                    default:
                        if (lastWasCR) {
                            isCR = true;
                            if (counter == 0) {
                                doc.insertString(pos, EOL, attr);
                                pos++;
                            } else {
                                buff[counter - 1] = EOL_char;
                            }
                            lastWasCR = false;
                        }
                        break;
                }
            }
            if (last < nch) {
                if (lastWasCR) {
                    if (last < (nch - 1)) {
                        doc.insertString(pos, new String(buff, last,
                                nch - last - 1), attr);
                        pos += (nch - last - 1);
                    }
                } else {
                    doc.insertString(pos, new String(buff, last,
                            nch - last), attr);
                    pos += (nch - last);
                }
            }
        }
        if (lastWasCR) {
            doc.insertString(pos, EOL, attr);
            isCR = true;
        }
        if (wasEmpty) {
            if (isCRLF) {
                doc.putProperty(EndOfLineStringProperty, EOL_CRLF);
            } else if (isCR) {
                doc.putProperty(EndOfLineStringProperty, EOL_CR);
            } else {
                doc.putProperty(EndOfLineStringProperty, EOL);
            }
        }
    }
}

