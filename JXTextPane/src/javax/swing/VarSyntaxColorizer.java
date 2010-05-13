package javax.swing;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author richet
 */
public class VarSyntaxColorizer extends DocumentFilter {

    private StyledDocument doc;
    private Element rootElement;
    private boolean multiLineComment;
    private MutableAttributeSet normal;
    private MutableAttributeSet comment;
    private MutableAttributeSet var;
    private HashMap<String, Color> keywords;
    private HashMap<Color, MutableAttributeSet> colors;
    UndoableEditListener undo;

    public VarSyntaxColorizer(JXTextPane component, HashMap<String, Color> keywords) {
        this.doc = component.getStyledDocument();
        this.undo = component.getUndoableEditListener();

        rootElement = doc.getDefaultRootElement();
        doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        normal = new SimpleAttributeSet();
        StyleConstants.setForeground(normal, Color.black);

        comment = new SimpleAttributeSet();
        StyleConstants.setForeground(comment, Color.gray);
        StyleConstants.setItalic(comment, true);

        var = new SimpleAttributeSet();
        StyleConstants.setBackground(var, Color.yellow);

        setKeywordColor(keywords);
    }

    public void setKeywordColor(HashMap<String, Color> keywords) {
        colors = new HashMap<Color, MutableAttributeSet>();
        this.keywords = keywords;
        if (keywords instanceof RegExpHashMap) {
            ((RegExpHashMap) keywords).keyAsRegexp = false;
        }

        for (String k : keywords.keySet()) {
            if (!colors.containsKey(keywords.get(k))) {
                MutableAttributeSet kw = new SimpleAttributeSet();
                StyleConstants.setForeground(kw, keywords.get(k));
                colors.put(keywords.get(k), kw);
            }
        }

        if (keywords instanceof RegExpHashMap) {
            ((RegExpHashMap) keywords).keyAsRegexp = true;
        }
    }

    /** Here is a way to handle regexp on keywords.*/
    public static class RegExpHashMap extends HashMap {

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
    /*
     *  Override to apply syntax highlighting after the document has been updated
     */

    @Override
    public void insertString(DocumentFilter.FilterBypass b, int offset, String str, AttributeSet a) throws BadLocationException {
        /*if (str.equals("{")) {
        str = addMatchingBrace(offset);
        }*/

        b.insertString(offset, str, a);
        if (undo != null) {
            doc.removeUndoableEditListener(undo);
            processChangedLines(offset, str.length());
            doc.addUndoableEditListener(undo);
        } else {
            processChangedLines(offset, str.length());
        }
    }

    /*
     *  Override to apply syntax highlighting after the document has been updated
     */
    @Override
    public void remove(DocumentFilter.FilterBypass b, int offset, int length) throws BadLocationException {
        b.remove(offset, length);
        processChangedLines(offset, 0);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass b, int offset, int length, String str, AttributeSet as) throws BadLocationException {
        b.replace(offset, length, str, as);
        if (undo != null) {
            doc.removeUndoableEditListener(undo);
            processChangedLines(offset, str.length());
            doc.addUndoableEditListener(undo);
        } else {
            processChangedLines(offset, str.length());
        }
    }

    /*
     *  Determine how many lines have been changed,
     *  then apply highlighting to each line
     */
    public void processChangedLines(int offset, int length)
            throws BadLocationException {
        String content = doc.getText(0, doc.getLength());

        //  The lines affected by the latest document update

        int startLine = rootElement.getElementIndex(offset);
        int endLine = rootElement.getElementIndex(offset + length);

        //  Make sure all comment lines prior to the start line are commented
        //  and determine if the start line is still in a multi line comment

        setMultiLineComment(commentLinesBefore(content, startLine));

        //  Do the actual highlighting

        for (int i = startLine; i <= endLine; i++) {
            applyHighlighting(content, i);
        }

        //  Resolve highlighting to the next end multi line delimiter

        if (isMultiLineComment()) {
            commentLinesAfter(content, endLine);
        } else {
            highlightLinesAfter(content, endLine);
        }
    }

    /*
     *  Highlight lines when a multi line comment is still 'open'
     *  (ie. matching end delimiter has not yet been encountered)
     */
    private boolean commentLinesBefore(String content, int line) {
        int offset = rootElement.getElement(line).getStartOffset();

        //  Start of comment not found, nothing to do

        int startDelimiter = lastIndexOf(content, getStartDelimiter(), offset - 2);

        if (startDelimiter < 0) {
            return false;
        }

        //  Matching start/end of comment found, nothing to do

        int endDelimiter = indexOf(content, getEndDelimiter(), startDelimiter);

        if (endDelimiter < offset & endDelimiter != -1) {
            return false;
        }

        //  End of comment not found, highlight the lines

        doc.setCharacterAttributes(startDelimiter, offset - startDelimiter + 1, comment, false);
        return true;
    }

    /*
     *  Highlight comment lines to matching end delimiter
     */
    private void commentLinesAfter(String content, int line) {
        int offset = rootElement.getElement(line).getEndOffset();

        //  End of comment not found, nothing to do

        int endDelimiter = indexOf(content, getEndDelimiter(), offset);

        if (endDelimiter < 0) {
            return;
        }

        //  Matching start/end of comment found, comment the lines

        int startDelimiter = lastIndexOf(content, getStartDelimiter(), endDelimiter);

        if (startDelimiter < 0 || startDelimiter <= offset) {
            doc.setCharacterAttributes(offset, endDelimiter - offset + 1, comment, false);
        }
    }

    /*
     *  Highlight lines to start or end delimiter
     */
    private void highlightLinesAfter(String content, int line)
            throws BadLocationException {
        int offset = rootElement.getElement(line).getEndOffset();

        //  Start/End delimiter not found, nothing to do

        int startDelimiter = indexOf(content, getStartDelimiter(), offset);
        int endDelimiter = indexOf(content, getEndDelimiter(), offset);

        if (startDelimiter < 0) {
            startDelimiter = content.length();
        }

        if (endDelimiter < 0) {
            endDelimiter = content.length();
        }

        int delimiter = Math.min(startDelimiter, endDelimiter);

        if (delimiter < offset) {
            return;
        }

        //	Start/End delimiter found, reapply highlighting

        int endLine = rootElement.getElementIndex(delimiter);

        for (int i = line + 1; i < endLine; i++) {
            Element branch = rootElement.getElement(i);
            Element leaf = doc.getCharacterElement(branch.getStartOffset());
            AttributeSet as = leaf.getAttributes();

            if (as.isEqual(comment)) {
                applyHighlighting(content, i);
            }
        }
    }

    /*
     *  Parse the line to determine the appropriate highlighting
     */
    private void applyHighlighting(String content, int line)
            throws BadLocationException {
        int startOffset = rootElement.getElement(line).getStartOffset();
        int endOffset = rootElement.getElement(line).getEndOffset() - 1;

        int lineLength = endOffset - startOffset;
        int contentLength = content.length();

        if (endOffset >= contentLength) {
            endOffset = contentLength - 1;
        }

        //  check for var line

        if (content.startsWith("//R<",startOffset)) {
            doc.setCharacterAttributes(startOffset, lineLength, var, true);
            return;
        }

        //  check for multi line comments
        //  (always set the comment attribute for the entire line)

        if (endingMultiLineComment(content, startOffset, endOffset)
                || isMultiLineComment()
                || startingMultiLineComment(content, startOffset, endOffset)) {
            doc.setCharacterAttributes(startOffset, endOffset - startOffset + 1, comment, false);
            return;
        }

        //  set normal attributes for the line

        doc.setCharacterAttributes(startOffset, lineLength, normal, true);

        //  check for single line comment

        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
        if ((index > -1) && (index < endOffset)) {
            doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
            endOffset = index - 1;
        }

        //  check for tokens

        checkForTokens(content, startOffset, endOffset);
    }

    /*
     *  Does this line contain the start delimiter
     */
    private boolean startingMultiLineComment(String content, int startOffset, int endOffset)
            throws BadLocationException {
        int index = indexOf(content, getStartDelimiter(), startOffset);

        if ((index < 0) || (index > endOffset)) {
            return false;
        } else {
            setMultiLineComment(true);
            return true;
        }
    }

    /*
     *  Does this line contain the end delimiter
     */
    private boolean endingMultiLineComment(String content, int startOffset, int endOffset)
            throws BadLocationException {
        int index = indexOf(content, getEndDelimiter(), startOffset);

        if ((index < 0) || (index > endOffset)) {
            return false;
        } else {
            setMultiLineComment(false);
            return true;
        }
    }

    /*
     *  We have found a start delimiter
     *  and are still searching for the end delimiter
     */
    private boolean isMultiLineComment() {
        return multiLineComment;
    }

    private void setMultiLineComment(boolean value) {
        multiLineComment = value;
    }

    /*
     *	Parse the line for tokens to highlight
     */
    private void checkForTokens(String content, int startOffset, int endOffset) {

        while (startOffset <= endOffset) {
            //  skip the delimiters to find the start of a new token

            /*while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
            if (startOffset < endOffset) {
            startOffset++;
            } else {
            return;
            }
            }*/

            //  Extract and process the entire token

            /*if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1))) {
            startOffset = getQuoteToken(content, startOffset, endOffset);
            } else*/

            if (Character.isWhitespace(content.charAt(startOffset))) {
                startOffset++;
                continue;
            }

            if (isParamStarter(content.charAt(startOffset))) {
                startOffset = processParamToken(content, startOffset, endOffset);
            } else {
                startOffset = processOtherToken(content, startOffset, endOffset);
            }
        }
    }

    /*
     *
     */
    /*private int getQuoteToken(String content, int startOffset, int endOffset) {
    String quoteDelimiter = content.substring(startOffset, startOffset + 1);
    String escapeString = getQuoteEscapeString(quoteDelimiter);
    
    int index;
    int endOfQuote = startOffset;
    
    //  skip over the escape quotes in this quote
    
    index = content.indexOf(escapeString, endOfQuote + 1);
    
    while ((index > -1) && (index < endOffset)) {
    endOfQuote = index + 1;
    index = content.indexOf(escapeString, endOfQuote);
    }
    
    // now find the matching delimiter
    
    index = content.indexOf(quoteDelimiter, endOfQuote + 1);
    
    if ((index < 0) || (index > endOffset)) {
    endOfQuote = endOffset;
    } else {
    endOfQuote = index;
    }
    
    doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);
    
    return endOfQuote + 1;
    }*/
    private int processParamToken(String content, int startOffset, int endOffset) {
        //System.err.println("processParamToken " + content.substring(startOffset, endOffset));
        char paramStarter = content.charAt(startOffset);
        int endOfParam = startOffset + 1;
        if (paramStarter == '@') {
            char o = '{';
            char c = '}';

            if (content.charAt(startOffset + 1) != o) {
                return processOtherToken(content, startOffset, endOffset);
            }

            String after = content.substring(startOffset + 2);

            int open = 1;
            int n = 0;
            while (open > 0) {
                if (after.charAt(n) == o) {
                    open++;
                }
                if (after.charAt(n) == c) {
                    open--;
                }
                n++;
            }
            endOfParam = startOffset + n + 1;
        } else {//paramStarter == '$'
            char o = '(';
            char c = ')';
            if (content.charAt(startOffset + 1) == o) {
                while (!Character.isWhitespace(content.charAt(endOfParam)) && !(content.charAt(endOfParam) == c)) {
                    endOfParam++;
                }
            } else {
                while (!Character.isWhitespace(content.charAt(endOfParam))) {
                    endOfParam++;
                }
            }

        }

        doc.setCharacterAttributes(startOffset, endOfParam - startOffset + 1, var, false);

        return endOfParam + 1;
    }

    /*
     *
     */
    private int processOtherToken(String content, int startOffset, int endOffset) {
        //System.err.println("processOtherToken " + content.substring(startOffset, endOffset));
        int endOfToken = startOffset + 1;

        while (endOfToken <= endOffset) {

            if (Character.isWhitespace(content.charAt(endOfToken)) || isParamStarter(content.charAt(endOfToken))) {
                break;
            }
            endOfToken++;
        }

        String token = content.substring(startOffset, endOfToken);

        if (isKeyword(token)) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, colors.get(keywords.get(token)), false);
        }

        return endOfToken;
    }

    /*
     *  Assume the needle will the found at the start/end of the line
     */
    private int indexOf(String content, String needle, int offset) {
        int index;

        while ((index = content.indexOf(needle, offset)) != -1) {
            String text = getLine(content, index).trim();

            if (text.startsWith(needle) || text.endsWith(needle)) {
                break;
            } else {
                offset = index + 1;
            }
        }

        return index;
    }

    /*
     *  Assume the needle will the found at the start/end of the line
     */
    private int lastIndexOf(String content, String needle, int offset) {
        int index;

        while ((index = content.lastIndexOf(needle, offset)) != -1) {
            String text = getLine(content, index).trim();

            if (text.startsWith(needle) || text.endsWith(needle)) {
                break;
            } else {
                offset = index - 1;
            }
        }

        return index;
    }

    private String getLine(String content, int offset) {
        int line = rootElement.getElementIndex(offset);
        Element lineElement = rootElement.getElement(line);
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();
        return content.substring(start, end - 1);
    }

    /*
     *  Override for other languages
     */
    /*protected boolean isDelimiter(String character) {
    String operands = ";:{}()[]+-/%<=>!&|^~*";
    
    if (Character.isWhitespace(character.charAt(0))
    || operands.indexOf(character) != -1) {
    return true;
    } else {
    return false;
    }
    }*/

    /*
     *  Override for other languages
     */
    protected boolean isQuoteDelimiter(String character) {
        String quoteDelimiters = "\"'";

        if (quoteDelimiters.indexOf(character) < 0) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean isParamStarter(char character) {
        if ((character == '@') || (character == '$')) {
            //System.err.print("  ??? " + character);
            return true;
        } else {
            //System.err.print(character);
            return false;
        }
    }


    /*
     *  Override for other languages
     */
    protected boolean isKeyword(String token) {
        //return keywords.contains(token);
        return keywords.containsKey(token);
    }

    /*
     *  Override for other languages
     */
    protected String getStartDelimiter() {
        return "/*";
    }

    /*
     *  Override for other languages
     */
    protected String getEndDelimiter() {
        return "*/";
    }

    /*
     *  Override for other languages
     */
    protected String getSingleLineDelimiter() {
        return "//";
    }

    /*
     *  Override for other languages
     */
    protected String getQuoteEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }

    /*
     *
     */
    /*protected String addMatchingBrace(int offset) throws BadLocationException {
    StringBuffer whiteSpace = new StringBuffer();
    int line = rootElement.getElementIndex(offset);
    int i = rootElement.getElement(line).getStartOffset();
    
    while (true) {
    String temp = doc.getText(i, 1);
    
    if (temp.equals(" ") || temp.equals("\t")) {
    whiteSpace.append(temp);
    i++;
    } else {
    break;
    }
    }
    
    return "{\n" + whiteSpace.toString() + "\t\n" + whiteSpace.toString() + "}";
    }*/
    public static void main(String[] args) throws Exception {
        final HashMap syntax = new RegExpHashMap();
        //syntax.put("abstract", Color.BLUE);
        syntax.put("abs(\\w+)", Color.BLUE);
        syntax.put("boolean", Color.BLUE);
        syntax.put("import", Color.BLUE);
        syntax.put("class", Color.BLUE);
        syntax.put("public", Color.BLUE);
        syntax.put("case", Color.BLUE);
        syntax.put("cast", Color.BLUE);
        syntax.put("catch", Color.BLUE);
        syntax.put("char", Color.BLUE);
        syntax.put("class", Color.BLUE);
        syntax.put("const", Color.BLUE);
        syntax.put("throws", Color.GREEN);
        syntax.put("transient", Color.GREEN);
        syntax.put("true", Color.GREEN);
        syntax.put("try", Color.GREEN);
        syntax.put("var", Color.GREEN);
        syntax.put("void", Color.GREEN);
        syntax.put("volatile", Color.GREEN);
        syntax.put("while", Color.GREEN);
        syntax.put("\\$(\\w+)", Color.RED);

        final CodeEditorPane edit = new CodeEditorPane();
        edit.setVerticalLineAtPos(72);

        DocumentFilter blockDocumentFilter = new BlockModeHandler(edit);
        VarSyntaxColorizer syntaxDocumentFilter = new VarSyntaxColorizer(edit, syntax);
        ((AbstractDocument) edit.getDocument()).setDocumentFilter(new DocumentFilterChain(syntaxDocumentFilter, blockDocumentFilter));

        String txt = read("src/javax/swing/JXTextPane.java");
        txt = txt+txt+txt+txt+txt;
        edit.setText("sdvsdfn$(a)sdffsd\n$(a)\n $a\nsdvsdfn@{$a}sdffsd\n @{$(a)}\n@{1.0}\n//R< toto\nesdfsdf\n" + txt);

        JFrame frame = new JFrame("Var Syntax Highlighting");
        frame.getContentPane().add(edit.getContainerWithLines());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setVisible(true);
    }

    public static String read(String file) throws Exception {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer sb = new StringBuffer();
        String s;
        while ((s = br.readLine()) != null) {
            sb.append(s + "\n");
        }
        fr.close();
        return sb.toString();
    }
}
