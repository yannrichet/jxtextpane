package javax.swing;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractWriter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.xml.XMLConstants;

/**Class to add more complete HTML rendering than just bold, italic and underline.
 * Following StyleConstants are supported: Foreground, Backgournd, FontSize, FontFamiliy
 * @author richet
 */
public class SyntaxHTMLWriter extends AbstractWriter {

    public static String toXML(String src) {
        if (src == null) {
            return src;
        }
        src = src.replace("&", "&amp;");
        src = src.replace("\"", "&quot;");
        src = src.replace("'", "&apos;");
        src = src.replace(">", "&gt;");
        src = src.replace("<", "&lt;");
        src = src.replace(" ", "&nbsp;");
        return src.replace("\n", "<br/>");
    }
    /**
     * These static finals are used to
     * tweak and query the fontMask about which
     * of these tags need to be generated or
     * terminated.
     */
    private static final int BOLD = 0x01;
    private static final int ITALIC = 0x02;
    private static final int UNDERLINE = 0x04;
    private int fontMask = 0;
    int startOffset = 0;
    int endOffset = 0;
    /**
     * Stores the attributes of the previous run.
     * Used to compare with the current run's
     * attributeset.  If identical, then a
     * &lt;span&gt; tag is not emitted.
     */
    private AttributeSet fontAttributes;
    /**
     * Maps from style name as held by the Document, to the archived
     * style name (style name written out). These may differ.
     */
    private Hashtable styleNameMapping;

    /**
     * Creates a new MinimalHTMLWriter.
     *
     * @param w  Writer
     * @param doc StyledDocument
     *
     */
    public SyntaxHTMLWriter(Writer w, StyledDocument doc) {
        super(w, doc);
    }

    /**
     * Creates a new MinimalHTMLWriter.
     *
     * @param w  Writer
     * @param doc StyledDocument
     * @param pos The location in the document to fetch the
     *   content.
     * @param len The amount to write out.
     *
     */
    public SyntaxHTMLWriter(Writer w, StyledDocument doc, int pos, int len) {
        super(w, doc, pos, len);
    }

    /**
     * Generates HTML output
     * from a StyledDocument.
     *
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     *
     */
    public void write() throws IOException, BadLocationException {
        styleNameMapping = new Hashtable();
        //writeStartTag("<html>");
        writeBody();
        //writeEndTag("</html>");
    }

    /**
     * Writes out text.
     *
     * @exception IOException on any I/O error
     */
    @Override
    protected void text(Element elem) throws IOException, BadLocationException {
        String contentStr = toXML(getText(elem));
        if ((contentStr.length() > 0)
                && (contentStr.charAt(contentStr.length() - 1) == NEWLINE)) {
            contentStr = contentStr.substring(0, contentStr.length() - 1);
        }
        if (contentStr.length() > 0) {
            write(contentStr);
        }
    }

    /**
     * Writes out a start tag appropriately
     * indented.  Also increments the indent level.
     *
     * @exception IOException on any I/O error
     */
    protected void writeStartTag(String tag) throws IOException {
        indent();
        write(tag);
        write(NEWLINE);
        incrIndent();
    }

    /**
     * Writes out an end tag appropriately
     * indented.  Also decrements the indent level.
     *
     * @exception IOException on any I/O error
     */
    protected void writeEndTag(String endTag) throws IOException {
        decrIndent();
        indent();
        write(endTag);
        write(NEWLINE);
    }

    /**
     * Iterates over the elements in the document
     * and processes elements based on whether they are
     * branch elements or leaf elements.  This method specially handles
     * leaf elements that are text.
     *
     * @exception IOException on any I/O error
     */
    protected void writeBody() throws IOException, BadLocationException {
        ElementIterator it = getElementIterator();

        /*
        This will be a section element for a styled document.
        We represent this element in HTML as the body tags.
        Therefore we ignore it.
         */
        it.current();

        Element next = null;

        //writeStartTag("<body>");

        boolean inContent = false;

        while ((next = it.next()) != null) {
            if (!inRange(next)) {
                continue;
            }
            if (next instanceof AbstractDocument.BranchElement) {
                if (inContent) {
                    writeEndParagraph();
                    inContent = false;
                    fontMask = 0;
                }
                writeStartParagraph(next);
            } else if (isText(next)) {
                writeContent(next, !inContent);
                inContent = true;
            } else {
                writeLeaf(next);
                inContent = true;
            }
        }
        if (inContent) {
            writeEndParagraph();
        }
        //writeEndTag("</body>");
    }

    /**
     * Emits an end tag for a &lt;p&gt;
     * tag.  Before writing out the tag, this method ensures
     * that all other tags that have been opened are
     * appropriately closed off.
     *
     * @exception IOException on any I/O error
     */
    protected void writeEndParagraph() throws IOException {
        writeEndMask(fontMask);
        if (inFontTag()) {
            endSpanTag();
        } else {
            write(NEWLINE);
        }
        //writeEndTag("</p>");
    }

    /**
     * Emits the start tag for a paragraph. If
     * the paragraph has a named style associated with it,
     * then this method also generates a class attribute for the
     * &lt;p&gt; tag and sets its value to be the name of the
     * style.
     *
     * @exception IOException on any I/O error
     */
    protected void writeStartParagraph(Element elem) throws IOException {
        /*AttributeSet attr = elem.getAttributes();
        Object resolveAttr = attr.getAttribute(StyleConstants.ResolveAttribute);
        /*if (resolveAttr instanceof StyleContext.NamedStyle) {
            writeStartTag("<p class=" + mapStyleName(((StyleContext.NamedStyle) resolveAttr).getName()) + ">");
        } else {
            writeStartTag("<p>");
        }*/
    }

    /**
     * Responsible for writing out other non-text leaf
     * elements.
     *
     * @exception IOException on any I/O error
     */
    protected void writeLeaf(Element elem) throws IOException {
        indent();
        if (elem.getName() == StyleConstants.IconElementName) {
            writeImage(elem);
        } else if (elem.getName() == StyleConstants.ComponentElementName) {
            writeComponent(elem);
        }
    }

    /**
     * Responsible for handling Icon Elements;
     * deliberately unimplemented.  How to implement this method is
     * an issue of policy.  For example, if you're generating
     * an &lt;img&gt; tag, how should you
     * represent the src attribute (the location of the image)?
     * In certain cases it could be a URL, in others it could
     * be read from a stream.
     *
     * @param elem element of type StyleConstants.IconElementName
     */
    protected void writeImage(Element elem) throws IOException {
    }

    /**
     * Responsible for handling Component Elements;
     * deliberately unimplemented.
     * How this method is implemented is a matter of policy.
     */
    protected void writeComponent(Element elem) throws IOException {
    }

    /**
     * Returns true if the element is a text element.
     *
     */
    protected boolean isText(Element elem) {
        return (elem.getName() == AbstractDocument.ContentElementName);
    }

    /**
     * Writes out the attribute set
     * in an HTML-compliant manner.
     *
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     */
    protected void writeContent(Element elem, boolean needsIndenting)
            throws IOException, BadLocationException {

        AttributeSet attr = elem.getAttributes();
        writeNonHTMLAttributes(attr);
        if (needsIndenting) {
            indent();
        }
        writeHTMLTags(attr);
        text(elem);
    }

    /**
     * Generates
     * bold &lt;b&gt;, italic &lt;i&gt;, and &lt;u&gt; tags for the
     * text based on its attribute settings.
     *
     * @exception IOException on any I/O error
     */
    protected void writeHTMLTags(AttributeSet attr) throws IOException {

        int oldMask = fontMask;
        setFontMask(attr);

        int endMask = 0;
        int startMask = 0;
        if ((oldMask & BOLD) != 0) {
            if ((fontMask & BOLD) == 0) {
                endMask |= BOLD;
            }
        } else if ((fontMask & BOLD) != 0) {
            startMask |= BOLD;
        }

        if ((oldMask & ITALIC) != 0) {
            if ((fontMask & ITALIC) == 0) {
                endMask |= ITALIC;
            }
        } else if ((fontMask & ITALIC) != 0) {
            startMask |= ITALIC;
        }

        if ((oldMask & UNDERLINE) != 0) {
            if ((fontMask & UNDERLINE) == 0) {
                endMask |= UNDERLINE;
            }
        } else if ((fontMask & UNDERLINE) != 0) {
            startMask |= UNDERLINE;
        }
        writeEndMask(endMask);
        writeStartMask(startMask);
    }

    /**
     * Tweaks the appropriate bits of fontMask
     * to reflect whether the text is to be displayed in
     * bold, italic, and/or with an underline.
     *
     */
    private void setFontMask(AttributeSet attr) {
        if (StyleConstants.isBold(attr)) {
            fontMask |= BOLD;
        }

        if (StyleConstants.isItalic(attr)) {
            fontMask |= ITALIC;
        }

        if (StyleConstants.isUnderline(attr)) {
            fontMask |= UNDERLINE;
        }
    }

    /**
     * Writes out start tags &lt;u&gt;, &lt;i&gt;, and &lt;b&gt; based on
     * the mask settings.
     *
     * @exception IOException on any I/O error
     */
    private void writeStartMask(int mask) throws IOException {
        if (mask != 0) {
            if ((mask & UNDERLINE) != 0) {
                write("<u>");
            }
            if ((mask & ITALIC) != 0) {
                write("<i>");
            }
            if ((mask & BOLD) != 0) {
                write("<b>");
            }
        }
    }

    /**
     * Writes out end tags for &lt;u&gt;, &lt;i&gt;, and &lt;b&gt; based on
     * the mask settings.
     *
     * @exception IOException on any I/O error
     */
    private void writeEndMask(int mask) throws IOException {
        if (mask != 0) {
            if ((mask & BOLD) != 0) {
                write("</b>");
            }
            if ((mask & ITALIC) != 0) {
                write("</i>");
            }
            if ((mask & UNDERLINE) != 0) {
                write("</u>");
            }
        }
    }

    /**
     * Writes out the remaining
     * character-level attributes (attributes other than bold,
     * italic, and underline) in an HTML-compliant way.  Given that
     * attributes such as font family and font size have no direct
     * mapping to HTML tags, a &lt;span&gt; tag is generated and its
     * style attribute is set to contain the list of remaining
     * attributes just like inline styles.
     *
     * @exception IOException on any I/O error
     */
    protected void writeNonHTMLAttributes(AttributeSet attr) throws IOException {

        String style = "";
        String separator = "; ";

        if (inFontTag() && fontAttributes.isEqual(attr)) {
            return;
        }

        boolean first = true;
        Color color = (Color) attr.getAttribute(StyleConstants.Foreground);
        if (color != null) {
            style += "color: " + colorToHex(color);
            first = false;
        }

        //JUST ADDED THAT...
        Color colorb = (Color) attr.getAttribute(StyleConstants.Background);
        if (colorb != null) {
            if (!first) {
                style += separator;
            }
            style += "background-color: " + colorToHex(colorb);
            first = false;
        }


        Integer size = (Integer) attr.getAttribute(StyleConstants.FontSize);
        if (size != null) {
            if (!first) {
                style += separator;
            }
            style += "font-size: " + size.intValue() + "pt";
            first = false;
        }

        String family = (String) attr.getAttribute(StyleConstants.FontFamily);
        if (family != null) {
            if (!first) {
                style += separator;
            }
            style += "font-family: " + family;
            first = false;
        }

        if (style.length() > 0) {
            if (fontMask != 0) {
                writeEndMask(fontMask);
                fontMask = 0;
            }
            startSpanTag(style);
            fontAttributes = attr;
        } else if (fontAttributes != null) {
            writeEndMask(fontMask);
            fontMask = 0;
            endSpanTag();
        }
    }

    /**
     * Returns true if we are currently in a &lt;font&gt; tag.
     */
    protected boolean inFontTag() {
        return (fontAttributes != null);
    }

    /**
     * This is no longer used, instead &lt;span&gt; will be written out.
     * <p>
     * Writes out an end tag for the &lt;font&gt; tag.
     *
     * @exception IOException on any I/O error
     */
    protected void endFontTag() throws IOException {
        write(NEWLINE);
        writeEndTag("</font>");
        fontAttributes = null;
    }

    /**
     * This is no longer used, instead &lt;span&gt; will be written out.
     * <p>
     * Writes out a start tag for the &lt;font&gt; tag.
     * Because font tags cannot be nested,
     * this method closes out
     * any enclosing font tag before writing out a
     * new start tag.
     *
     * @exception IOException on any I/O error
     */
    protected void startFontTag(String style) throws IOException {
        boolean callIndent = false;
        if (inFontTag()) {
            endFontTag();
            callIndent = true;
        }
        writeStartTag("<font style=\"" + style + "\">");
        if (callIndent) {
            indent();
        }
    }

    /**
     * Writes out a start tag for the &lt;font&gt; tag.
     * Because font tags cannot be nested,
     * this method closes out
     * any enclosing font tag before writing out a
     * new start tag.
     *
     * @exception IOException on any I/O error
     */
    private void startSpanTag(String style) throws IOException {
        boolean callIndent = false;
        if (inFontTag()) {
            endSpanTag();
            callIndent = true;
        }
        writeStartTag("<span style=\"" + style + "\">");
        if (callIndent) {
            indent();
        }
    }

    /**
     * Writes out an end tag for the &lt;span&gt; tag.
     *
     * @exception IOException on any I/O error
     */
    private void endSpanTag() throws IOException {
        write(NEWLINE);
        writeEndTag("</span>");
        fontAttributes = null;
    }

    /**
     * Adds the style named <code>style</code> to the style mapping. This
     * returns the name that should be used when outputting. CSS does not
     * allow the full Unicode set to be used as a style name.
     */
    private String addStyleName(String style) {
        if (styleNameMapping == null) {
            return style;
        }
        StringBuffer sb = null;
        for (int counter = style.length() - 1; counter >= 0; counter--) {
            if (!isValidCharacter(style.charAt(counter))) {
                if (sb == null) {
                    sb = new StringBuffer(style);
                }
                sb.setCharAt(counter, 'a');
            }
        }
        String mappedName = (sb != null) ? sb.toString() : style;
        while (styleNameMapping.get(mappedName) != null) {
            mappedName = mappedName + 'x';
        }
        styleNameMapping.put(style, mappedName);
        return mappedName;
    }

    /**
     * Returns the mapped style name corresponding to <code>style</code>.
     */
    private String mapStyleName(String style) {
        if (styleNameMapping == null) {
            return style;
        }
        String retValue = (String) styleNameMapping.get(style);
        return (retValue == null) ? style : retValue;
    }

    private boolean isValidCharacter(char character) {
        return ((character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z'));
    }

    /**
     * Converts a type Color to a hex string
     * in the format "#RRGGBB"
     */
    static String colorToHex(Color color) {

        String colorstr = new String("#");

        // Red
        String str = Integer.toHexString(color.getRed());
        if (str.length() > 2) {
            str = str.substring(0, 2);
        } else if (str.length() < 2) {
            colorstr += "0" + str;
        } else {
            colorstr += str;
        }

        // Green
        str = Integer.toHexString(color.getGreen());
        if (str.length() > 2) {
            str = str.substring(0, 2);
        } else if (str.length() < 2) {
            colorstr += "0" + str;
        } else {
            colorstr += str;
        }

        // Blue
        str = Integer.toHexString(color.getBlue());
        if (str.length() > 2) {
            str = str.substring(0, 2);
        } else if (str.length() < 2) {
            colorstr += "0" + str;
        } else {
            colorstr += str;
        }

        return colorstr;
    }
}
