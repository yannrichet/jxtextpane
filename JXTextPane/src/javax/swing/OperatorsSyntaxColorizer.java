/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.swing;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author richet
 */
public class OperatorsSyntaxColorizer extends DefaultSyntaxColorizer {

    private MutableAttributeSet operator;
    private MutableAttributeSet numbers;

    public OperatorsSyntaxColorizer(JXTextPane component, HashMap<String, Color> keywords) {
        super(component, keywords);
        operator = new SimpleAttributeSet();
        StyleConstants.setForeground(operator, Color.blue);
        numbers = new SimpleAttributeSet();
        StyleConstants.setForeground(numbers, Color.orange);

        //don't use . as delimiter because it splits numbers...
        getOperands().replace(".", "");
    }

    public boolean isOperator(char character) {
        if (getOperands().indexOf(character) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;

        while (endOfToken <= endOffset) {
            if (isTokenSeparator(content.charAt(endOfToken))) {
                break;
            }

            endOfToken++;
        }

        String token = content.substring(startOffset, endOfToken);

        if (isNumber(token)) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, numbers, false);
        } else if (isKeyword(token)) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, colors.get(keywords.get(token)), false);
        }

        return endOfToken;
    }

    @Override
    protected void checkForTokens(String content, int startOffset, int endOffset) {
        while (startOffset <= endOffset) {
            //  skip the delimiters to find the start of a new token

            while (isTokenSeparator(content.charAt(startOffset))) {
                if (startOffset < endOffset) {
                    if (isOperator(content.charAt(startOffset))) {
                        doc.setCharacterAttributes(startOffset, 1, operator, false);
                    }
                    startOffset++;
                } else {
                    return;
                }
            }

            //  Extract and process the entire token
            if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1))) {
                startOffset = getQuoteToken(content, startOffset, endOffset);
            } else {
                startOffset = getOtherToken(content, startOffset, endOffset);
            }
        }
    }

    private boolean isNumber(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c != '.' && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
