/*

   ======================================================================
   The Xerlin XML Editor is Copyright (c) 2002 SpeedLegal Holdings, Inc.
   and other contributors.  It includes software developed for the
   Merlot XML Editor which is Copyright (c) 1999-2000 ChannelPoint, Inc.
   All rights reserved.
   ======================================================================

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

   1. Redistribution of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistribution in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. All advertising materials mentioning features or use of this
   software must display the following acknowledgment:
   "This product includes software developed by the SpeedLegal Group for
   use in the Xerlin XML Editor www.xerlin.org and software developed by
   ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"

   4. Except for the acknowledgments required by these conditions, any
   names trademarked by SpeedLegal Holdings, Inc. must not be used to
   endorse or promote products derived from this software without prior
   written permission. For written permission, please contact
   info@speedlegal.com. Any names trademarked by ChannelPoint, Inc. must
   not be used to endorse or promote products derived from this software
   without prior written permission. For written permission, please
   contact legal@channelpoint.com.

    5. Except for the acknowledgment required by these conditions, Products
    derived from this software may not be called "Xerlin" nor may "Xerlin"
    appear in their names without prior written permission of SpeedLegal
    Holdings, Inc. Products derived from this software may not be called
    "Merlot" nor may "Merlot" appear in their names without prior written
    permission of ChannelPoint, Inc.

    6. Redistribution of any form whatsoever must retain the following
    acknowledgment:
    "This product includes software developed by the SpeedLegal Group for
    use in the Xerlin XML Editor www.xerlin.org and software developed by
    ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"

    7. Developers who choose to contribute code or documentation to Xerlin
    (which is encouraged but not required) acknowledge and agree that: (a) any
    such contributions accepted and included in Xerlin will be subject to this
    license; (b) SpeedLegal Holdings, Inc. or any successor that hosts the
    Xerlin project will always have the right to make those contributions
    available under this license or an equivalent open source license; and
    (c) all contributions are made with the full authority of their owner/s.

    THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
    AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
    SPEEDLEGAL HOLDINGS, INC. OR CHANNELPOINT, INC. OR ANY CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
    THE POSSIBILITY OF SUCH DAMAGE.
    ======================================================================

    For more information on SPEEDLEGAL visit www.speedlegal.com

    For information on the XERLIN project visit www.xerlin.org

*/
package org.merlotxml.merlot.editors;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.merlotxml.util.xml.xerces.SchemaGrammarDocumentImpl;
import org.merlotxml.util.xml.xerces.SchemaIdentityConstraintValidator;


/**
 * @author Evert Hoff
 *
 * Creates a JComboBox with the valid values for a field of type IDREF or
 * KeyRef.
 */
public class KeyRefEditor extends SimpleTypeEditor {
    JComboBox _component;
    Vector _idObjects = new Vector();

    public KeyRefEditor(ComplexTypeEditPanel complexTypeEditPanel, 
                        GrammarSimpleType grammarSimpleType) {
        super(complexTypeEditPanel, grammarSimpleType);
    }

    /**
     * Gets the component for editing this simple type.
     *
     * @return   The component value
     */
    public JComponent getComponent() {
        MerlotDebug.msg("Getting JComboBox...");
        if (_component != null) {
            return _component;
        }
        _component = new JComboBox();
        _component.setRenderer(new KeyRefComboBoxRenderer());
        _component.setEditable(false);
        int selectedIndex = 0;
        FieldNode fieldNode = getFieldNode();
        String currentValue = fieldNode.getNodeValue();
        if (currentValue == null) {
            currentValue = "";
        }
        MerlotDebug.msg(" Current value: " + currentValue);
        SchemaGrammarDocumentImpl grammarDocument = (SchemaGrammarDocumentImpl) _complexTypeEditPanel._node.getGrammarDocument();
        SchemaIdentityConstraintValidator validator = grammarDocument.getSchemaIdentityConstraintValidator();
        FieldNode[] peers = validator.getIdentityConstraintFieldNodePeers(
                                    fieldNode);
        FieldNode[][] references = validator.getPossibleReferenceFieldNodes(
                                           fieldNode);
        if (references.length == 0) {
            return _component;
        }
        int positionAmongPeers = 0;
        for (int i = 0; i < peers.length; i++) {
            FieldNode peer = peers[i];
            if (peer.equals(fieldNode)) {
                positionAmongPeers = i;
                break;
            }
        }
        boolean isRequired = _grammarSimpleType.getIsRequired();
        boolean hasEmptyOption = false;
        if (!isRequired) {
            MerlotDebug.msg(" Adding empty option.");
            hasEmptyOption = true;
            IDObject idObject = new IDObject("", "");
            selectedIndex = 0;
            _component.addItem(idObject);
            _idObjects.add(idObject);
        }
        for (int i = 0; i < references.length; i++) {
            FieldNode reference = references[i][positionAmongPeers];
            String idValue = "";
            if (reference != null) {
                idValue = reference.getNodeValue();
            }
            if (idValue.equals(currentValue) && !idValue.equals("")) {
                selectedIndex = i;
            }
            StringBuffer displayText = new StringBuffer();

            // If there are two fields that make up this key ref
            // 'first-name' and 'last-name', but the current one being edited
            // is 'last-name', then the display text for that entry will be
            // formatted with the current field in square brackets:
            //      Evert|[Hoff]
            //      Justin|[Lipton]
            // If first name was being edited, then it would have displayed:
            //      [Evert]|Hoff
            //      [Justin]|Lipton
            int numFields = references[i].length;
            for (int j = 0; j < numFields; j++) {
                if (j == positionAmongPeers && numFields > 1) {
                    displayText.append('[');
                }
                FieldNode referencePeer = references[i][j];
                String value = "";
                if (referencePeer != null) {
                    value = referencePeer.getNodeValue();
                }
                displayText.append(value);
                if (j == positionAmongPeers && numFields > 1) {
                    displayText.append(']');
                }
                if (j != (references[i].length - 1)) {
                    displayText.append('|');
                }
            }
            IDObject idObject = new IDObject(idValue, displayText.toString());
            MerlotDebug.msg(" Adding option: " + idObject);
            _component.addItem(idObject);
            _idObjects.add(idObject);
        }
        if (selectedIndex > 0 && hasEmptyOption)
            selectedIndex++;
        if (_idObjects.size() > 0) {
            MerlotDebug.msg(" Setting selected index to " + selectedIndex);
            _component.setSelectedIndex(selectedIndex);
        }
        return _component;
    }

    protected FieldNode getFieldNode() {
        return _complexTypeEditPanel.getFieldNode(_grammarSimpleType);
    }

    /**
     * Gets whether the component is currently disabled or not.
     *
     * @return   The editable value
     */
    public boolean getEditable() {
        return _component.isEnabled();
    }

    /**
     * Retrieves the value from this component.
     *
     * @return   The value
     */
    public String getValue() {
        getComponent();
        String ret = "";
        Object item = _component.getSelectedItem();
        if (item != null) {
            ret = item.toString().trim();
        }
        return ret;
    }

    /**
     * Sets the component to editable or read-only.
     *
     * @param editable  The new editable value
     */
    public void setEditable(boolean editable) {
        _component.setEditable(editable);
        _component.setEnabled(editable);
    }

    /**
     * Sets the value of the component.
     *
     * @param value  The new value value
     */
    public void setValue(String value) {
        MerlotDebug.msg("KeyRefEditor.setValue(" + value + ")");
        getComponent();
        if (value == null) {
            value = _grammarSimpleType.getDefaultValue();
            MerlotDebug.msg(" Using default value of " + value);
        }
        if (value == null) {
            value = "";
        }
        for (int i = 0; i < _idObjects.size(); i++) {
            IDObject idObject = (IDObject) _idObjects.get(i);
            if (idObject.getIdValue().equals(value)) {
                MerlotDebug.msg(" Setting selected index to " + i);
                _component.setSelectedIndex(i);
            }
        }
        if (_grammarSimpleType.getDefaultType() == DTDConstants.FIXED) {
            setEditable(false);
        }
    }

    /**
     * A special renderer for the combobox to enable a different value
     * to be displayed from the value that gets saved.
     */
    class KeyRefComboBoxRenderer extends JLabel implements ListCellRenderer {
        public KeyRefComboBoxRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, 
                                                      int index, 
                                                      boolean isSelected, 
                                                      boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            IDObject idObject = (IDObject) value;
            String display = (idObject != null) ? idObject.getDisplayText() : null;
            setText((display == null) ? "" : display);
            return this;
        }
    }

    class IDObject {
        protected String _idValue = "";
        protected String _displayText = "";

        public IDObject(String idValue, String displayText) {
            if (idValue != null) {
                _idValue = idValue.trim();
            }
            if (displayText != null) {
                _displayText = displayText.trim();
            }
            if (_displayText.equals(""))
                _displayText = " ";
        }

        public String getIdValue() {
            return _idValue;
        }

        public String toString() {
            return _idValue;
        }

        public String getDisplayText() {
            return _displayText;
        }
    }
}