package javax.swing;

import javax.swing.text.DocumentFilter;

/**
 *
 * @author richet
 */
public abstract class SyntaxColorizer extends DocumentFilter {

    public final static String ALL_OPERANDS = ",;:.!?{}()[]<>+-*/=\\%&|^~$@#";
    private String operands = ALL_OPERANDS;
    public final static String ALL_QUOTES = "'\"`";
    private String quotes = ALL_QUOTES;

    public abstract boolean isTokenSeparator(char character);

    public abstract boolean isKeyword(String token);

    /**
     * @return the operands
     */
    public String getOperands() {
        return operands;
    }

    /**
     * @param operands the operands to set
     */
    public void setOperands(String operands) {
        this.operands = operands;
    }

    /**
     * @return the quotes
     */
    public String getQuotes() {
        return quotes;
    }

    /**
     * @param quotes the quotes to set
     */
    public void setQuotes(String quotes) {
        this.quotes = quotes;
    }
}
