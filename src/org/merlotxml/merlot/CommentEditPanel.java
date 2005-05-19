/*
  ====================================================================
  Copyright (c) 1999-2001 ChannelPoint, Inc..  All rights reserved.
  ====================================================================

  Redistribution and use in source and binary forms, with or without 
  modification, are permitted provided that the following conditions 
  are met:

  1. Redistribution of source code must retain the above copyright 
  notice, this list of conditions and the following disclaimer. 

  2. Redistribution in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the 
  documentation and/or other materials provided with the distribution.

  3. All advertising materials mentioning features or use of this 
  software must display the following acknowledgment:  "This product 
  includes software developed by ChannelPoint, Inc. for use in the 
  Merlot XML Editor (http://www.channelpoint.com/merlot/)."
 
  4. Any names trademarked by ChannelPoint, Inc. must not be used to 
  endorse or promote products derived from this software without prior
  written permission. For written permission, please contact
  legal@channelpoint.com.

  5.  Products derived from this software may not be called "Merlot"
  nor may "Merlot" appear in their names without prior written
  permission of ChannelPoint, Inc.

  6. Redistribution of any form whatsoever must retain the following
  acknowledgment:  "This product includes software developed by 
  ChannelPoint, Inc. for use in the Merlot XML Editor 
  (http://www.channelpoint.com/merlot/)."

  THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR 
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO 
  EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ====================================================================

 For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.  
 For information on the Merlot project, please see 
 http://www.channelpoint.com/merlot.
*/

package org.merlotxml.merlot;

import java.io.*;
import java.awt.*;
import java.beans.*;
import java.text.MessageFormat;

import java.awt.event.*;
import java.util.*;
import org.w3c.dom.*;
import javax.swing.*;
import javax.swing.text.*;
import com.sun.javax.swing.*;

import matthew.awt.StrutLayout;
import org.merlotxml.util.xml.*;


/**
 * This is a generic node editing panel which provides a component for each attribute
 * listed with the element it's created to edit, along with a text box for PCDATA.
 *  <P>
 * This class can be extended to change what the user sees for each attribute field.
 * Typically the easiest methods to overload for this type of custom editors are getEditComponent()
 * and sometimes save().
 *
 * @author Kelly Campbell
 */

public class CommentEditPanel extends GenericDOMEditPanel
{
	public CommentEditPanel(MerlotDOMComment node) 
	{
		super(node);
	}
	
	protected void setupPanel() 
	{
		MerlotDebug.msg("SetupPanel");

		MerlotDOMComment commentNode = (MerlotDOMComment)_node;
            
		String s = commentNode.getText();
		_text = new JTextArea(s);
		_text.setLineWrap(true);
		_text.setWrapStyleWord(true);
		JScrollPane sp = new JScrollPane(_text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
			 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setPreferredSize(new Dimension(350,200));
		JLabel l = new JLabel(MerlotResource.getString(UI,"xml.comment.label") + ":",JLabel.RIGHT);
			
		addAttributeComponent(l,sp,ALIGN_TOP);
	}	



	protected void save(HashMap attributes) 
		throws PropertyVetoException
	{
		MerlotDOMComment _commentNode = (MerlotDOMComment)_node;
		if (_text != null) {
			if (_text.getText().trim().equals("")) {
					_commentNode.setText(null);
			} else {
				MerlotDebug.msg("Comment Node setText " + _text.getText());
				_commentNode.setText(_text.getText());
					
				if (_subtext == null) {
					// create a new TextNode
					MerlotDOMNode nd = _node.newChild(DTDConstants.PCDATA_KEY);
					if (nd instanceof MerlotDOMText) {
						_subtext = (MerlotDOMText)nd;
					}
				}
				if (_subtext != null) {
					_subtext.setText(_text.getText());
				}
			}
		}

/*		// put together a hashtable of attributes to pass back to the node
		Enumeration e = _attrComponents.keys();
		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();
			DTDAttribute dtdAttr = (DTDAttribute)_dtd_attributes.get(key);
			Node oldnode = _node_attributes.getNamedItem(key);
			String oldval;
			if (oldnode != null) {
				oldval = oldnode.getNodeValue();
			} else {
				oldval = "";
			}
			JComponent c = (JComponent)_attrComponents.get(key);
			String newval = null;
			if (c instanceof JTextField) {
				newval = ((JTextField)c).getText();
			} else if (c instanceof JComboBox) {
				Object item = ((JComboBox)c).getSelectedItem();
				if (item != null) {
					newval = item.toString().trim();
				}
			} else {
				// nothing to do now
				MerlotDebug.msg("Unknown editing component in GenericDOMEditPanel.save: "+c);
				if (attributes.containsKey(key)) {
					newval = (String)attributes.get(key);
				}
			}
			if (newval != null && newval.trim().equals("")) {
				newval = null;
			}
			if (newval == null && dtdAttr != null && dtdAttr.getDefaultType() == DTDAttribute.REQUIRED) {
				String err[] = new String[2];
				err[0] = _node.getNodeName();
				err[1] = key;
				throw new PropertyVetoException(MessageFormat.format(MerlotResource.getString(ERR,"required.field"),err),new PropertyChangeEvent(_node,key,oldval,newval));
			}

			fireVetoableChange(new PropertyChangeEvent(_node,key,oldval,newval));

			// check if the attribute is already set in the hashtable
			if (!attributes.containsKey(key)) {
				attributes.put(key,newval);
			}
								
		}
		_node.setAttributes(attributes);
*/	}
	
}
