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
import javax.swing.JTextField;

import org.merlotxml.util.xml.DTDConstants;
import org.w3c.dom.ProcessingInstruction;


public class ProcInstructionEditPanel extends GenericDOMEditPanel
{
	//protected JTextField _target = null;

	public ProcInstructionEditPanel(MerlotDOMProcessingInstruction node) 
	{
		super(node);
	}
	
	protected void setupPanel() 
	{
		MerlotDebug.msg("SetupPanel");
       
		MerlotDOMProcessingInstruction piNode = (MerlotDOMProcessingInstruction)_node;
           
		String sTarget = piNode.getTarget().trim(); // trim target
		_target = new JTextField(sTarget);
		JLabel lTarget = new JLabel(MerlotResource.getString(UI,"xml.pi.target.label") + ":",JLabel.RIGHT);
		addAttributeComponent(lTarget,_target,ALIGN_TOP);

		String sData = piNode.getText();
		_text = new JTextArea(sData);
		_text.setLineWrap(true);
		_text.setWrapStyleWord(true);
		_text.setPreferredSize(new Dimension(350,200));
		JScrollPane spData = new JScrollPane(_text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spData.setPreferredSize(new Dimension(350,200));
		JLabel lData = new JLabel(MerlotResource.getString(UI,"xml.pi.data.label") + ":",JLabel.RIGHT);
		addAttributeComponent(lData,spData,ALIGN_MIDDLE);
		
		System.out.println(_target);
	}
	

	protected void save(HashMap attributes)
		throws PropertyVetoException
	{
		System.out.println(_target);

		MerlotDOMProcessingInstruction piNode = (MerlotDOMProcessingInstruction)_node;
		if (_text != null) {
			if (_text.getText().trim().equals("")) {
				piNode.setText(null);
			} else {
				MerlotDebug.msg("Saving Processing Instruction Data");
				MerlotDebug.msg("Target = " + _target.getText());
				MerlotDebug.msg("Data = " + _text.getText());

				// piNode.setTarget(_target.getText()); // Placeholder call
				MerlotDebug.msg("Setting PI DataText"); 
				// piNode.setText(_text.getText()); // Placeholder call

				// Workaround limitation in DOM
				org.w3c.dom.Document doc = _node.getDocument();
				ProcessingInstruction newpi = null;
				try {
					// Current limitation in DOM Model, PI target is 
					// not "required" per say, but document exceptions if there is
					// no name - Still researching what to do in the long term
					// on this
					String targetText = null;
					// System.out.println("TARGET TEXT = " + _target.getText());
					// System.out.println("TARGET TEXT LENGTH = " + _target.getText().length());
					if ((_target.getText() == null) || (_target.getText().length() == 0)){
						targetText = "PI";
					} else {
						targetText = _target.getText();
					}
					// System.out.println("TARGET TEXT = " + targetText);
					newpi = doc.createProcessingInstruction(targetText, _text.getText());
				}
				//catch (com.ibm.xml.dom.DOMExceptionImpl e) {
				catch (Exception e) {
					MerlotError.exception(e,MerlotResource.getString(ERR,"xml.processing_instruction.invalidtarget"));
				}
				if (newpi != null) {
					// replace the child node in the document
					MerlotDOMProcessingInstruction newpinode = 
						new MerlotDOMProcessingInstruction(newpi,_node.getXMLFile());
					MerlotDOMNode parent = _node.getParentNode();
					int location = parent.getChildIndex(_node);
					parent.removeChild(_node);
					parent.insertChildAt(newpinode,location);
				}
			}
            /*
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
            */
		}
	}

}
