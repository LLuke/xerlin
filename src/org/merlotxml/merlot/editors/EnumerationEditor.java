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

import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarSimpleType;


/**
 * @author everth
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EnumerationEditor extends SimpleTypeEditor {
    private Vector options = new Vector();

    public EnumerationEditor(ComplexTypeEditPanel complexTypeEditPanel, 
                             GrammarSimpleType grammarSimpleType) {
        super(complexTypeEditPanel, grammarSimpleType);
    }

    /**
     * Gets the component for editing this simple type.
     *
     * @return   The component value
     */
    public JComponent getComponent() {
        if (_component != null) {
            return _component;
        }
        MerlotDebug.msg("Getting component for " + _grammarSimpleType.getName());
        options = new Vector();
        String[] enumeration = _grammarSimpleType.getEnumeration();
        boolean checkbox = true;
        FieldNode fieldNode = _complexTypeEditPanel.getFieldNode(
                                      _grammarSimpleType);
        String currentValue = fieldNode.getNodeValue();
        if (currentValue == null) {
            currentValue = "";
        }

        //if (_grammarSimpleType.getDefaultValue() == null) {
        //    options.addElement("");
        //}
        for (int i = 0; i < enumeration.length; i++) {
            String literal = enumeration[i];
            checkbox = checkbox && 
                       (literal.equals("true") || literal.equals("false"));
            MerlotDebug.msg("Adding option: " + literal);
            options.add(literal);
        }
        if ((options.size() == 2) && checkbox) {
            _component = new JCheckBox();
            ((JCheckBox) _component).setSelected((currentValue != null) && 
                                          currentValue.equals("true"));
            return _component;
        }

        _component = new JComboBox(options);
        ((JComboBox) _component).setEditable(false);
        int i = options.indexOf(currentValue);
        if (i < 0) {
            //i = getIndexInVector(v, _grammarSimpleType.getDefaultValue());
            i = options.indexOf(_grammarSimpleType.getDefaultValue());
        }
        if (i >= 0) {
            ((JComboBox) _component).setSelectedIndex(i);
        }
        return _component;
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
        String ret = null;
        if (_component instanceof JComboBox) {
            Object item = ((JComboBox) _component).getSelectedItem();
            if (item != null) {
                ret = item.toString().trim();
            }
        } else if (_component instanceof JCheckBox) {
            ret = ((JCheckBox) _component).isSelected() ? "true" : "false";
        }
        if (ret == null) {
            ret = "";
        }
        return ret;
    }

    /**
     * Sets the component to editable or read-only.
     *
     * @param editable  The new editable value
     */
    public void setEditable(boolean editable) {
        if (_component instanceof JComboBox) {
            ((JComboBox) _component).setEditable(editable);
        }
        _component.setEnabled(editable);
    }

    /**
     * Sets the value of the component.
     *
     * @param value  The new value value
     */
    public void setValue(String value) {
        getComponent();
        if (_component instanceof JComboBox) {
            int i = options.indexOf(value);
            if (i < 0) {
                //i = getIndexInVector(v, _grammarSimpleType.getDefaultValue());
                i = options.indexOf(_grammarSimpleType.getDefaultValue());
            }
            if (i >= 0) {
                MerlotDebug.msg("Setting selected index to: " + i);
                ((JComboBox) _component).setSelectedIndex(i);
            }
        } else if (_component instanceof JCheckBox) {
            boolean selected = false;
            if ((value != null) && value.trim().equalsIgnoreCase("true")) {
                selected = true;
                ((JCheckBox) _component).setSelected(selected);
            }
        }
    }
}