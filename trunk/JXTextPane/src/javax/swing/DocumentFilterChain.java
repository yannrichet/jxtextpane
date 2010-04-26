package javax.swing;

import java.util.LinkedList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author richet
 */
public class DocumentFilterChain extends DocumentFilter {

    LinkedList<DocumentFilter> chain = new LinkedList<DocumentFilter>();

    public DocumentFilterChain(DocumentFilter... c) {
        for (DocumentFilter documentFilter : c) {
            chain.add(documentFilter);
        }
    }

    public void add(DocumentFilter documentFilter) {
        chain.add(documentFilter);
    }

    public void remove(DocumentFilter documentFilter) {
        chain.remove(documentFilter);
    }

    @Override
    public void insertString(final FilterBypass fb0, int i, String string, AttributeSet as) throws BadLocationException {
        chain.get(0).insertString(buildRussianDoll(fb0), i, string, as);
    }

    @Override
    public void remove(FilterBypass fb, int i, int i1) throws BadLocationException {
        chain.get(0).remove(buildRussianDoll(fb), i, i1);
    }

    @Override
    public void replace(FilterBypass fb, int i, int i1, String string, AttributeSet as) throws BadLocationException {
        chain.get(0).replace(buildRussianDoll(fb), i, i1, string, as);
    }

    public FilterBypass buildRussianDoll(FilterBypass root) {
        FilterBypassContainer[] fbc = new FilterBypassContainer[chain.size()];
        fbc[0] = new FilterBypassContainer(root, chain.get(0));
        for (int j = 1; j < chain.size(); j++) {
            fbc[j] = new FilterBypassContainer(fbc[j - 1], chain.get(j));
        }
        return fbc[chain.size() - 1];
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
