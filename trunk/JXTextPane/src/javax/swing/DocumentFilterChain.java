package javax.swing;

import java.util.LinkedList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Class intended to allow multiple DocumentFilter in one Document.
 * Be carefull with order of filters. May be not reversible.
 * For instance BlockModeHandler should be declared as last argument of DocumentFilterChain
 * @author richet
 */
public class DocumentFilterChain extends DocumentFilter {

    LinkedList<DocumentFilter> chain = new LinkedList<DocumentFilter>();
    FilterBypassContainer[] doll;

    public DocumentFilterChain(DocumentFilter... c) {
        for (DocumentFilter documentFilter : c) {
            if (documentFilter != null) {
                chain.add(documentFilter);
            }
        }
        buildRussianDoll();
    }

    public void add(DocumentFilter documentFilter) {
        chain.add(documentFilter);
        buildRussianDoll();
    }

    public void remove(DocumentFilter documentFilter) {
        chain.remove(documentFilter);
        buildRussianDoll();
    }

    @Override
    public void insertString(FilterBypass fb, int i, String string, AttributeSet as) throws BadLocationException {
        updateRussianDoll(fb);
        chain.get(0).insertString(getFilterBypass(), i, string, as);
    }

    @Override
    public void remove(FilterBypass fb, int i, int i1) throws BadLocationException {
        updateRussianDoll(fb);
        chain.get(0).remove(getFilterBypass(), i, i1);
    }

    @Override
    public void replace(FilterBypass fb, int i, int i1, String string, AttributeSet as) throws BadLocationException {
        updateRussianDoll(fb);
        chain.get(0).replace(getFilterBypass(), i, i1, string, as);
    }

    public void updateRussianDoll(FilterBypass root) {
        doll[0].setBypass(root);
    }

    void buildRussianDoll() {
        doll = new FilterBypassContainer[chain.size()];
        doll[0] = new FilterBypassContainer(null, chain.get(0));
        for (int j = 1; j < chain.size(); j++) {
            doll[j] = new FilterBypassContainer(doll[j - 1], chain.get(j));
        }
    }

    FilterBypass getFilterBypass() {
        return doll[doll.length - 1];
    }

    class FilterBypassContainer extends FilterBypass {

        private FilterBypass parent_bypass;
        DocumentFilter current_filter;

        public FilterBypassContainer(FilterBypass bypass, DocumentFilter node) {
            this.parent_bypass = bypass;
            this.current_filter = node;

        }

        @Override
        public Document getDocument() {
            return parent_bypass.getDocument();
        }

        @Override
        public void remove(int i, int i1) throws BadLocationException {
            current_filter.remove(parent_bypass, i, i1);
        }

        @Override
        public void insertString(int i, String string, AttributeSet as) throws BadLocationException {
            current_filter.insertString(parent_bypass, i, string, as);
        }

        @Override
        public void replace(int i, int i1, String string, AttributeSet as) throws BadLocationException {
            current_filter.replace(parent_bypass, i, i1, string, as);
        }

        /**
         * @param parent_bypass the parent_bypass to set
         */
        public void setBypass(FilterBypass parent_bypass) {
            this.parent_bypass = parent_bypass;
        }
    }
}
