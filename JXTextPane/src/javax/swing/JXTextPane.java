package javax.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.action.ActionManager;

/**
 * Lacking class in swingX. Inspired from JTextPane implementation (in Swing) rlateed to JEditorPane.
 * Add Undo/Redo and search features.
 * @author richet (heavily inspired by swingX code)
 */
public class JXTextPane extends JXEditorPane {
    
    private final static String ACTION_UNDO = "undo";
    private final static String ACTION_REDO = "redo";
    private UndoableEditListener doHandler;
    private UndoManager doManager;
    
    public JXTextPane() {
        super();

        //from JXEditorPane
        addPropertyChangeListener(new PropertyHandler());
        getDocument().addUndoableEditListener(getUndoableEditListener());

        //from JXEditorPane
        try {
            getActionMap().put(ACTION_UNDO, new DoActions(ACTION_UNDO));
            getActionMap().put(ACTION_REDO, new DoActions(ACTION_REDO));
        } catch (Exception e) {
            System.err.println("Could not support actions unod/redo");
        }
        
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), ACTION_UNDO);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), ACTION_REDO);
        
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    }
    
    @Override
    protected EditorKit createDefaultEditorKit() {
        return new StyledEditorKit();
    }

    /** To turn around ugly overloading of paste() in JXEditorPane ... (intended to handle html style, which is not our subject)*/
    @Override
    public void paste() {
        if (isEditable() && isEnabled()) {
            //invokeAction("paste", TransferHandler.getPasteAction());
            ActionMap map = getActionMap();
            Action action = null;
            if (map != null) {
                action = map.get("paste");
            }
            int modifiers = 0;
            AWTEvent currentEvent = EventQueue.getCurrentEvent();
            if (currentEvent instanceof InputEvent) {
                modifiers = ((InputEvent) currentEvent).getModifiers();
            } else if (currentEvent instanceof ActionEvent) {
                modifiers = ((ActionEvent) currentEvent).getModifiers();
            }
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, (String) action.getValue(Action.NAME),
                    EventQueue.getMostRecentEventTime(), modifiers));
        }
    }
    
    @Override
    public void setText(String t) {
        //getDocument().removeUndoableEditListener(getUndoableEditListener());// pour debrayer lengthregistre undo/redo
        super.setText(t);
        doManager.discardAllEdits();
        //getDocument().addUndoableEditListener(getUndoableEditListener());
    }

    //from JXEditorPane
    private class PropertyHandler implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name.equals("document")) {
                Document doc = (Document) evt.getOldValue();
                if (doc != null) {
                    doc.removeUndoableEditListener(getUndoableEditListener());
                }
                
                doc = (Document) evt.getNewValue();
                if (doc != null) {
                    doc.addUndoableEditListener(getUndoableEditListener());
                }
            }
        }
    }

    /**
     * Creates a new <code>JTextPane</code>, with a specified document model.
     * A new instance of <code>javax.swing.text.StyledEditorKit</code>
     *  is created and set.
     *
     * @param doc the document model
     */
    public JXTextPane(StyledDocument doc) throws BadLocationException {
        this();
        setStyledDocument(doc);
    }
    
    public void setTabSize(int size) {
        FontMetrics fm = this.getFontMetrics(this.getFont());
        int charWidth = fm.charWidth(' ');
        int tabWidth = charWidth * size;

        // this means that only size*100 line length is supported...
        TabStop[] tabs = new TabStop[100];
        for (int j = 0; j < tabs.length; j++) {
            int tab = j + 1;
            tabs[j] = new TabStop(tab * tabWidth);
        }
        
        TabSet tabSet = new TabSet(tabs);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        int length = this.getDocument().getLength();
        
        try {
            this.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
        } catch (Exception e) {
        }
    }

    //from JXEditorPane
    public UndoableEditListener getUndoableEditListener() {
        if (doHandler == null) {
            doHandler = new UndoHandler();
            doManager = new UndoManager();
        }
        return doHandler;
    }

    //from JXEditorPane
    private class UndoHandler implements UndoableEditListener {
        
        public void undoableEditHappened(UndoableEditEvent evt) {
            doManager.addEdit(evt.getEdit());
            updateActionState();
        }
    }

    //from JXEditorPane
    /**
     * Updates the state of the actions in response to an undo/redo operation. <p>
     * 
     */
    private void updateActionState() {
        // Update the state of the undo and redo actions
        // JW: fiddling with actionManager's actions state? I'm pretty sure
        // we don't want that: the manager will get nuts with multiple
        // components with different state.
        // It's up to whatever manager to listen
        // to our changes and update itself accordingly. Which is not
        // well supported with the current design ... nobody 
        // really cares about enabled as it should. 
        //
        Runnable doEnabled = new Runnable() {
            
            public void run() {
                ActionManager manager = ActionManager.getInstance();
                manager.setEnabled(ACTION_UNDO, doManager.canUndo());
                manager.setEnabled(ACTION_REDO, doManager.canRedo());
            }
        };
        SwingUtilities.invokeLater(doEnabled);
    }

    //from JXEditorPane
    /**
     * A small class which dispatches actions.
     * TODO: Is there a way that we can make this static?
     * JW: these if-constructs are totally crazy ... we live in OO world!
     * 
     */
    private class DoActions extends AbstractAction {
        
        DoActions(String name) {
            super();
            setName(name);
        }
        
        @Override
        public void actionPerformed(ActionEvent evt) {
            String name = getName();
            if (ACTION_UNDO.equals(name)) {
                try {
                    doManager.undo();
                } catch (CannotUndoException ex) {
                    System.err.println("Could not undo");
                }
                updateActionState();
            } else if (ACTION_REDO.equals(name)) {
                try {
                    doManager.redo();
                } catch (CannotRedoException ex) {
                    System.err.println("Could not redo");
                }
                updateActionState();
            } else {
                System.out.println("ActionHandled: " + name);
            }
            
        }
        
        @Override
        public boolean isEnabled() {
            String name = getName();
            if (ACTION_UNDO.equals(name)) {
                return isEditable() && doManager.canUndo();
            }
            if (ACTION_REDO.equals(name)) {
                return isEditable() && doManager.canRedo();
            }
            return true;
        }
    }

    /**
     * Associates the editor with a text document.  This
     * must be a <code>StyledDocument</code>.
     *
     * @param doc  the document to display/edit
     * @exception IllegalArgumentException  if <code>doc</code> can't
     *   be narrowed to a <code>StyledDocument</code> which is the
     *   required type of model for this text component
     */
    @Override
    public void setDocument(Document doc) {
        if (doc instanceof StyledDocument) {
            super.setDocument(doc);
        } else {
            throw new IllegalArgumentException("Model must be StyledDocument");
        }
    }

    /**
     * Associates the editor with a text document.
     * The currently registered factory is used to build a view for
     * the document, which gets displayed by the editor.
     *
     * @param doc  the document to display/edit
     */
    public void setStyledDocument(StyledDocument doc) {
        super.setDocument(doc);
    }

    /**
     * Fetches the model associated with the editor.
     *
     * @return the model
     */
    public StyledDocument getStyledDocument() {
        return (StyledDocument) getDocument();
    }

    /**
     * Replaces the currently selected content with new content
     * represented by the given string.  If there is no selection
     * this amounts to an insert of the given text.  If there
     * is no replacement text this amounts to a removal of the
     * current selection.  The replacement text will have the
     * attributes currently defined for input at the point of
     * insertion.  If the document is not editable, beep and return.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param content  the content to replace the selection with
     */
    @Override
    public void replaceSelection(String content) {
        replaceSelection(content, true);
    }
    
    private void replaceSelection(String content, boolean checkEditable) {
        if (checkEditable && !isEditable()) {
            UIManager.getLookAndFeel().provideErrorFeedback(JXTextPane.this);
            return;
        }
        Document doc = getStyledDocument();
        if (doc != null) {
            try {
                Caret caret = getCaret();
                boolean composedTextSaved = saveComposedText2(this, caret.getDot());
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                AttributeSet attr = getInputAttributes().copyAttributes();
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument) doc).replace(p0, p1 - p0, content, attr);
                } else {
                    if (p0 != p1) {
                        doc.remove(p0, p1 - p0);
                    }
                    if (content != null && content.length() > 0) {
                        doc.insertString(p0, content, attr);
                    }
                }
                if (composedTextSaved) {
                    restoreComposedText2(this);
                }
                
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(JXTextPane.this);
            }
        }
    }
    private final String[] composedTextMethodNames = {"saveComposedText", "restoreComposedText"};
    private final Method[] composedTextMethods = new Method[2];
    
    private Object invokeComposedTextMethod(final JTextComponent c,
                                            final int index, final Class[] argTypes, final Object[] args) {
        return AccessController.doPrivileged(new PrivilegedAction() {
            
            @Override
            public Object run() {
                try {
                    Method m = composedTextMethods[index];
                    if (m == null) {
                        m = JTextComponent.class.getDeclaredMethod(
                                composedTextMethodNames[index], argTypes);
                        m.setAccessible(true);
                        composedTextMethods[index] = m;
                    }
                    return m.invoke(c, args);
                } catch (Exception e) {
                    throw new RuntimeException(e); // shouldn't happen
                }
            }
        });
    }
    
    boolean saveComposedText2(JTextComponent c, int pos) {
        return (Boolean) invokeComposedTextMethod(
                c, 0, new Class[]{Integer.TYPE}, new Object[]{pos});
    }
    
    void restoreComposedText2(JTextComponent c) {
        invokeComposedTextMethod(c, 1, new Class[0], new Object[0]);
    }

    /**
     * Inserts a component into the document as a replacement
     * for the currently selected content.  If there is no
     * selection the component is effectively inserted at the
     * current position of the caret.  This is represented in
     * the associated document as an attribute of one character
     * of content.
     * <p>
     * The component given is the actual component used by the
     * JTextPane.  Since components cannot be a child of more than
     * one container, this method should not be used in situations
     * where the model is shared by text components.
     * <p>
     * The component is placed relative to the text baseline
     * according to the value returned by
     * <code>Component.getAlignmentY</code>.  For Swing components
     * this value can be conveniently set using the method
     * <code>JComponent.setAlignmentY</code>.  For example, setting
     * a value of <code>0.75</code> will cause 75 percent of the
     * component to be above the baseline, and 25 percent of the
     * component to be below the baseline.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param c    the component to insert
     */
    public void insertComponent(Component c) {
        MutableAttributeSet inputAttributes = getInputAttributes();
        inputAttributes.removeAttributes(inputAttributes);
        StyleConstants.setComponent(inputAttributes, c);
        replaceSelection(" ", false);
        inputAttributes.removeAttributes(inputAttributes);
    }

    /**
     * Inserts an icon into the document as a replacement
     * for the currently selected content.  If there is no
     * selection the icon is effectively inserted at the
     * current position of the caret.  This is represented in
     * the associated document as an attribute of one character
     * of content.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param g    the icon to insert
     * @see Icon
     */
    public void insertIcon(Icon g) {
        MutableAttributeSet inputAttributes = getInputAttributes();
        inputAttributes.removeAttributes(inputAttributes);
        StyleConstants.setIcon(inputAttributes, g);
        replaceSelection(" ", false);
        inputAttributes.removeAttributes(inputAttributes);
    }

    /**
     * Adds a new style into the logical style hierarchy.  Style attributes
     * resolve from bottom up so an attribute specified in a child
     * will override an attribute specified in the parent.
     *
     * @param nm   the name of the style (must be unique within the
     *   collection of named styles).  The name may be <code>null</code>
     *   if the style is unnamed, but the caller is responsible
     *   for managing the reference returned as an unnamed style can't
     *   be fetched by name.  An unnamed style may be useful for things
     *   like character attribute overrides such as found in a style
     *   run.
     * @param parent the parent style.  This may be <code>null</code>
     *   if unspecified
     *   attributes need not be resolved in some other style.
     * @return the new <code>Style</code>
     */
    public Style addStyle(String nm, Style parent) {
        StyledDocument doc = getStyledDocument();
        return doc.addStyle(nm, parent);
    }

    /**
     * Removes a named non-<code>null</code> style previously added to
     * the document.
     *
     * @param nm  the name of the style to remove
     */
    public void removeStyle(String nm) {
        StyledDocument doc = getStyledDocument();
        doc.removeStyle(nm);
    }

    /**
     * Fetches a named non-<code>null</code> style previously added.
     *
     * @param nm  the name of the style
     * @return the <code>Style</code>
     */
    public Style getStyle(String nm) {
        StyledDocument doc = getStyledDocument();
        return doc.getStyle(nm);
    }

    /**
     * Sets the logical style to use for the paragraph at the
     * current caret position.  If attributes aren't explicitly set
     * for character and paragraph attributes they will resolve
     * through the logical style assigned to the paragraph, which
     * in term may resolve through some hierarchy completely
     * independent of the element hierarchy in the document.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param s  the logical style to assign to the paragraph,
     *		or <code>null</code> for no style
     */
    public void setLogicalStyle(Style s) {
        StyledDocument doc = getStyledDocument();
        doc.setLogicalStyle(getCaretPosition(), s);
    }

    /**
     * Fetches the logical style assigned to the paragraph represented
     * by the current position of the caret, or <code>null</code>.
     *
     * @return the <code>Style</code>
     */
    public Style getLogicalStyle() {
        StyledDocument doc = getStyledDocument();
        return doc.getLogicalStyle(getCaretPosition());
    }

    /**
     * Fetches the character attributes in effect at the
     * current location of the caret, or <code>null</code>.
     *
     * @return the attributes, or <code>null</code>
     */
    public AttributeSet getCharacterAttributes() {
        StyledDocument doc = getStyledDocument();
        Element run = doc.getCharacterElement(getCaretPosition());
        if (run != null) {
            return run.getAttributes();
        }
        return null;
    }

    /**
     * Applies the given attributes to character
     * content.  If there is a selection, the attributes
     * are applied to the selection range.  If there
     * is no selection, the attributes are applied to
     * the input attribute set which defines the attributes
     * for any new text that gets inserted.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param attr the attributes
     * @param replace if true, then replace the existing attributes first
     */
    public void setCharacterAttributes(AttributeSet attr, boolean replace) {
        int p0 = getSelectionStart();
        int p1 = getSelectionEnd();
        if (p0 != p1) {
            StyledDocument doc = getStyledDocument();
            doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
        } else {
            MutableAttributeSet inputAttributes = getInputAttributes();
            if (replace) {
                inputAttributes.removeAttributes(inputAttributes);
            }
            inputAttributes.addAttributes(attr);
        }
    }

    /**
     * Fetches the current paragraph attributes in effect
     * at the location of the caret, or <code>null</code> if none.
     *
     * @return the attributes
     */
    public AttributeSet getParagraphAttributes() {
        StyledDocument doc = getStyledDocument();
        Element paragraph = doc.getParagraphElement(getCaretPosition());
        if (paragraph != null) {
            return paragraph.getAttributes();
        }
        return null;
    }

    /**
     * Applies the given attributes to paragraphs.  If
     * there is a selection, the attributes are applied
     * to the paragraphs that intersect the selection.
     * If there is no selection, the attributes are applied
     * to the paragraph at the current caret position.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param attr the non-<code>null</code> attributes
     * @param replace if true, replace the existing attributes first
     */
    public void setParagraphAttributes(AttributeSet attr, boolean replace) {
        int p0 = getSelectionStart();
        int p1 = getSelectionEnd();
        StyledDocument doc = getStyledDocument();
        doc.setParagraphAttributes(p0, p1 - p0, attr, replace);
    }

    /**
     * Gets the input attributes for the pane.
     *
     * @return the attributes
     */
    public MutableAttributeSet getInputAttributes() {
        return getStyledEditorKit().getInputAttributes();
    }

    /**
     * Gets the editor kit.
     *
     * @return the editor kit
     */
    protected final StyledEditorKit getStyledEditorKit() {
        return (StyledEditorKit) getEditorKit();
    }
    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "TextPaneUI";

    /**
     * See <code>readObject</code> and <code>writeObject</code> in
     * <code>JComponent</code> for more
     * information about serialization in Swing.
     *
     * @param s the output stream
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }

    /**
     * Sets the currently installed kit for handling
     * content.  This is the bound property that
     * establishes the content type of the editor.
     *
     * @param kit the desired editor behavior
     * @exception IllegalArgumentException if kit is not a
     *		<code>StyledEditorKit</code>
     */
    /**
     * Overidden to perform document initialization based on type.
     */
    @Override
    public void setEditorKit(EditorKit kit) {
        if (kit instanceof StyledEditorKit) {
            super.setEditorKit(kit);
        } else {
            throw new IllegalArgumentException("Must be StyledEditorKit");
        }
    }
}
