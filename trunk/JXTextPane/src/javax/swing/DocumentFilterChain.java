package javax.swing;

import java.util.LinkedList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Class intended to allow multiple DocumentFilter in one Document.
 * TODO not properly working
 * @author richet
 */
public class DocumentFilterChain extends DocumentFilter {

    LinkedList<DocumentFilter> chain = new LinkedList<DocumentFilter>();
    FilterBypassContainer[] doll;

    public DocumentFilterChain(DocumentFilter... c) {
        for (DocumentFilter documentFilter : c) {
            chain.add(documentFilter);
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
        doll[0] = new FilterBypassContainer(root, chain.get(0));
    }

    public void buildRussianDoll() {
        doll = new FilterBypassContainer[chain.size()];
        doll[0] = null;
        for (int j = 1; j < chain.size(); j++) {
            doll[j] = new FilterBypassContainer(doll[j - 1], chain.get(j));
        }
    }

    FilterBypass getFilterBypass() {
        return doll[chain.size() - 1];
    }

    class FilterBypassContainer extends FilterBypass {

        FilterBypass parent;
        DocumentFilter node;

        public FilterBypassContainer(FilterBypass parent, DocumentFilter node) {
            this.parent = parent;
            this.node = node;

        }

        @Override
        public Document getDocument() {
            return parent.getDocument();
        }

        @Override
        public void remove(int i, int i1) throws BadLocationException {
            node.remove(parent, i, i1);
        }

        @Override
        public void insertString(int i, String string, AttributeSet as) throws BadLocationException {
            node.insertString(parent, i, string, as);
        }

        @Override
        public void replace(int i, int i1, String string, AttributeSet as) throws BadLocationException {
            node.replace(parent, i, i1, string, as);
        }
    }
}
