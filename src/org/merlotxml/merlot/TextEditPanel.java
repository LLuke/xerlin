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

import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.merlotxml.util.xml.DTDConstants;


public class TextEditPanel extends GenericDOMEditPanel
{

	public TextEditPanel(MerlotDOMText node) 
	{
		super(node);
	}
	
	protected void init() {
	}
		
	protected void setupPanel() 
	{
		MerlotDebug.msg("SetupPanel");
	        
		MerlotDOMText _textNode = (MerlotDOMText)_node;
		if (_textNode != null) {
			String s = _textNode.getText(); 
			if (s == null)
				s="";
			_text = new JTextArea(s);
			_text.setLineWrap(true);
			_text.setWrapStyleWord(true);
			JScrollPane sp = new JScrollPane(_text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(new Dimension(350,200));
			JLabel l = new JLabel(DTDConstants.PCDATA_KEY + ":",JLabel.RIGHT);
			
			addAttributeComponent(l,sp,ALIGN_TOP);
		}

	}
	
	protected void save(HashMap attributes)
		throws PropertyVetoException
    {
		MerlotDOMText _textNode = (MerlotDOMText)_node;
		if (_text != null) {
			if (_text.getText().trim().equals("")) {
				_textNode.setText(null);
			} else {
				// System.out.println("Text Node set Text");
				_textNode.setText(_text.getText());
			}
		}
	}
	
}
