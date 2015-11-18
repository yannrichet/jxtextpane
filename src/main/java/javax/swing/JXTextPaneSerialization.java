package javax.swing;

import org.irsn.javax.swing.JXTextPane;

/**
 * Wraps code for {@link JXTextPane} that needs to be inside javax.swing package
 *
 * @author Arnaud TROUCHE - Artenum SARL
 *
 */
public class JXTextPaneSerialization extends JComponent {

    private static final long serialVersionUID = -6908131063368800239L;

    public static byte getWriteObjCounter(final JComponent jc) {
        return JComponent.getWriteObjCounter(jc);
    }

    public static void setWriteObjCounter(final JComponent jc, final byte count) {
        JComponent.setWriteObjCounter(jc, count);
    }
}
