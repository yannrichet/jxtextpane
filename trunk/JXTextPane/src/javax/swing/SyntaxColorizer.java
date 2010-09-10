package javax.swing;

import javax.swing.text.DocumentFilter;

/**
 *
 * @author richet
 */
public abstract class SyntaxColorizer extends DocumentFilter {

    public final static String ALL_OPERANDS = ";:.!?{}()[]<>+-*/=\\%&|^~$@\"'`#";
    private String operands = ALL_OPERANDS;

    abstract boolean isDelimiter(char character);

    abstract boolean isKeyword(String token);

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
}
