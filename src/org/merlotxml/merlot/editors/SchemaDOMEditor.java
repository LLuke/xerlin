package org.merlotxml.merlot.editors;
import java.beans.PropertyVetoException;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.merlotxml.merlot.CommentEditPanel;
import org.merlotxml.merlot.MerlotDOMComment;
import org.merlotxml.merlot.MerlotDOMEditor;
import org.merlotxml.merlot.MerlotDOMNode;
import org.merlotxml.merlot.MerlotDOMProcessingInstruction;
import org.merlotxml.merlot.MerlotDOMText;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.merlot.ProcInstructionEditPanel;
import org.merlotxml.merlot.TextEditPanel;
import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.GrammarComplexType;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

/**
 * A new editor and an alternative to GenericDOMEditPanel for use with schemas.
 * The reason for the change is that GenericDOMEditPanel's code was starting to
 * get difficult to read.
 *
 * @author   Evert Hoff
 */
public class SchemaDOMEditor implements MerlotDOMEditor {

    /**
     * returns a panel for editing this type of component.
     *
     * @param node  Description of Parameter
     * @return      The editPanel value
     */
    public JPanel getEditPanel(MerlotDOMNode node) {
        JPanel p = null;

        Node nd = node.getRealNode();

        int t = nd.getNodeType();
        switch (t) {
         case Node.TEXT_NODE:
             p = new TextEditPanel((MerlotDOMText)node);
             break;
         case Node.COMMENT_NODE:
             p = new CommentEditPanel((MerlotDOMComment)node);
             break;
         case Node.PROCESSING_INSTRUCTION_NODE:
             p = new ProcInstructionEditPanel((MerlotDOMProcessingInstruction)node);
             break;
         case Node.DOCUMENT_NODE:
         case Node.DOCUMENT_TYPE_NODE:
         case Node.DOCUMENT_FRAGMENT_NODE:
         default:
             p = new ComplexTypeEditPanel(node);
             break;
        }
        return p;
    }

    /**
     * Returns a set of menu items for any special actions for this particular
     * editor that it wants in the popup menu. Standard stuff like cut, copy,
     * paste is taken care of by other objects. If nothing needs added, can
     * return null.
     *
     * @param node  the node for which to get the menu items
     * @return      The menuItems value
     */
    public JMenuItem[] getMenuItems(MerlotDOMNode node) {
        return null;
    }

    /**
     * Tells the edit panel it's gaining focus, so it can put the cursor in the
     * first field. XXX this should probably be handled by event listening
     * instead
     *
     * @param p  Description of Parameter
     */
    public void grabFocus(JPanel p) {
        if (p instanceof ComplexTypeEditPanel) {
            ((ComplexTypeEditPanel)p).grabFocus();
        }
    }

    /**
     * called by the editor when the user has chosen to save their changes in a
     * panel.
     *
     * @param p                          the panel that was retreived with
     *      getEditPanel(node);
     * @exception PropertyVetoException  Description of Exception
     */
    public void savePanel(JPanel p)
         throws PropertyVetoException {
        MerlotDebug.msg("SchemaDOMEditor.savePanel()");
        if (p instanceof ComplexTypeEditPanel) {
            ((ComplexTypeEditPanel)p).save();
        } else {
            MerlotDebug.msg("Cannot save! Panel is not the right type.");
        }
    }

    /**
     * allows the plugin to hide certain items on the add-> menu. For example,
     * the plugin for the accessibility permissions might not want the user to
     * be able to directly add an "access" element, so it can request that that
     * be suppressed.
     *
     * @param el     Description of Parameter
     * @return       Description of the Returned Value
     * @deprecated
     */
    public boolean suppressAddType(DTDElement el) {
        return false;
    }

    public boolean suppressAddType(GrammarComplexType el) {
        return false;
    }

    /**
     * Returns true if the component editor wants a particular node hidden from
     * the user. If the editor wants to filter what the user sees in their
     * display, it should look at the given node, otherwise it should return
     * false. This is usefull particularly if the editor handles its children.
     * It can hide the children nodes from the user's view.
     *
     * @param node  Description of Parameter
     * @return      Description of the Returned Value
     */
    public boolean suppressNode(MerlotDOMNode node) {
        // check this node out to see if it's a #text or #comment
        // node, if so, hide it

        Node nd = node.getRealNode();
        //MerlotDebug.msg("filter nd = "+nd+" nodename = "+node.getNodeName());

        int t = nd.getNodeType();
        switch (t) {
         case Node.TEXT_NODE:
             GrammarComplexType complexType = null;
             if (node instanceof MerlotDOMText) {
                MerlotDOMNode parentNode = node.getParentNode();
                complexType = parentNode.getGrammarComplexType();
             } 
             if (complexType != null && complexType.isMixedType()) {
                 // Hide #text nodes:
                 if (nd instanceof CharacterData) {
                    if (node instanceof MerlotDOMText 
                        && ((MerlotDOMText)node).isVisible()) {
                        return false;
                    }
                    String s = ((CharacterData)nd).getData();
                    //MerlotDebug.msg("#text = "+s);
                    if (s != null) {
                        s = s.trim();
                        if (s.equals("")) {
                            return true;
                        }
                        return false;
                    }
                 }
             }
             return true;
             
         case Node.COMMENT_NODE:
             return false;
         case Node.PROCESSING_INSTRUCTION_NODE:
             return false;
         case Node.DOCUMENT_NODE:
             return true;
         case Node.DOCUMENT_TYPE_NODE:
             return true;
         case Node.DOCUMENT_FRAGMENT_NODE:
             return true;
        }

        return false;
    }

}

