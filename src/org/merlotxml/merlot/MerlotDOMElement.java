/*
====================================================================
Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
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


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.merlotxml.util.xml.DTDAttribute;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.DTDDocument;
import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOM element container for Merlot. Contains a DOM node, handles getting an
 * icon for a particular node, getting the editor for a node, etc. Also
 * implements transferable so that the node can be drag and dropped, or 
 * cut and pasted.
 */

public class MerlotDOMElement extends MerlotDOMNode
{
	
    private static boolean _writeDefaults =
        XMLEditorSettings.getSharedInstance().
            getProperty("merlot.write.default-atts").equals("true");

    private static boolean _autoAdd =
        XMLEditorSettings.getSharedInstance().
            getProperty("merlot.auto.add.nodes").equals("true"); 


	public MerlotDOMElement(Element data, XMLFile doc) 
	{
		super(data,doc);
        //Handle the auto adding of compulsory child elements
        // Add compulsory children if the property merlot.auto.add.nodes is set
        if (_autoAdd) {
            constructNewNode(this);
       }
	}
	
	public boolean isElement() 
	{
		return true;
	}
	

	/**
	 * Returns the DTD name of the element (which we consider it's type)
	 * in the context of merlot
	 */
	public String getElementName() 
	{
		return _theNode.getNodeName();
				
	}
	
	public String getAttribute(String s) 
	{
		return ((Element)_theNode).getAttribute(s);
	}
	
	/**
	 * Sets the attributes according the given hashtable. 
	 * (probably should make getAttributes consistent with this at some point
	 */
	public void setAttributes(HashMap h) 
	{
		Iterator i = h.keySet().iterator();
		
		while (i.hasNext()) {
			String key = (String)i.next();
			String val;
			Object o = h.get(key);
			if (o instanceof String) {
				val = (String)o;
			}
			else if (o != null) {
				val = o.toString();
			}
			else {
				val = null;
			}
			setAttribute(key,val);
		}
	}
	
	/**
	 * sets the attributes one at a time
	 */
	public void setAttribute(String name, String value) 
	{
        MerlotDebug.msg("Setting attribute " + name + " to " + value);
        XMLEditorSettings xes = XMLEditorSettings.getSharedInstance();
		boolean hasChanged = false;
		Element el = (Element)_theNode;
        GrammarSimpleType simpleType = getGrammarAttribute(name);
        if (simpleType == null) {
            MerlotDebug.msg("Can't set attribute: GrammarSimpleType is null.");
            return;
        }
        Node valueNode = el.getAttributeNode(name);
        FieldNode fieldNode = FieldNode.getFieldNode(el, valueNode, name);
        if (value != null && value.equals("") && !simpleType.getIsRequired()) {
            MerlotDebug.msg(" Value is empty and not required. Changing to null.");
            value = null;
        }
		if (value != null) {
			String oldValue = el.getAttribute( name );
            //DTDAttribute dtdAttr = getDTDAttribute( name );
            int type = simpleType.getType();
			if ( !value.equals( oldValue ) 
                || (_writeDefaults 
                    && (type==DTDConstants.TOKEN_GROUP)
                        || type==DTDConstants.CDATA))
			{
                MerlotDebug.msg(" Setting attribute.");
                el.setAttribute(name,value);
                hasChanged = true;
                
                simpleType.setValue(fieldNode, value);

			} else if(simpleType.getDefaultType()==DTDConstants.FIXED) {
                MerlotDebug.msg(" Setting fixed value.");
                el.setAttribute(name,simpleType.getDefaultValue());
                hasChanged = true;
            }
		}
		else {
            MerlotDebug.msg(" Removing attribute node.");
			if ( el.getAttributeNode( name ) != null )
			{
                el.setAttribute(name, null);
                el.removeAttribute(name);
                hasChanged = true;
			}
                
            //Node valueNode = el.getAttributeNode(name);
            //FieldNode fieldNode = FieldNode.getFieldNode(el, null, name);
            simpleType.setValue(fieldNode, value);
		}
		
		if ( hasChanged )
			fireNodeChanged();
	}

	private boolean _isLocationValid = true;
    private boolean _isComplete = true;
    private boolean _isContentValid = true;
    private boolean _isEachChildValid = true;
	public boolean _hasBeenValidated = false;

	public boolean isValid ()
	{
		// Each element used to be validated each time the mouse moves over a
		// node in the tree, now each one is validated once and thereafter only
		// revalidated when a change occurs.
		if ( !_hasBeenValidated )
			validate();
		boolean ret = _isLocationValid && _isComplete && _isContentValid && _isEachChildValid;
        return ret;
	}
    
    public boolean getHasBeenValidated() {
        return _hasBeenValidated;
    }
    
    public boolean getIsLocationValid() {
        return _isLocationValid;
    }
    
    public boolean getIsComplete() {
        return _isComplete;
    }
    
    public boolean getIsContentValid() {
        return _isContentValid;
    }
    
    public boolean getIsEachChildValid() {
        return _isEachChildValid;
    }
	
	/**
	 *  Adds element to queue in ValidationThread. 
     *  validateNow() will be called to do the 
     *  actual validation.
     **/
	public void validate() {
        XMLFile file = _file;
        XMLEditorDoc doc = _file.getXMLEditorDoc();
        if (doc == null) {
            MerlotDebug.msg("Can't validate " + getNodeName() + ". XMLEditorDoc is null.");
            return;
        }
        ValidationThread validationThread = doc.getValidationThread();
        validationThread.addElementToValidationQueue(this);
    }
    
    /**
     *  Called from ValidationThread
     **/
    public void validateNow() {
        long start = System.currentTimeMillis();
        //MerlotDebug.msg("Validating element " + getNodeName());
        GrammarComplexType complexType = getGrammarComplexType();
        if (complexType==null) {
            MerlotDebug.msg("Not validating " + this + " because complexType is null.");
            return;
        }
        Element el = (Element)_theNode;
        _isLocationValid = complexType.getIsLocationValid(el);
        _isComplete = complexType.getIsComplete(el);
        _isContentValid = complexType.getIsSimpleContentValid(el) &&
         complexType.getIsEachAttributeValid(el);
        _isEachChildValid = true;
        MerlotDOMNode[] children = getChildNodes();
        for (int i = 0; i < children.length; i++) {
            MerlotDOMNode child = children[i];
            if (child instanceof MerlotDOMElement) {
                _isEachChildValid = _isEachChildValid &&
                 ((MerlotDOMElement)child).isValid();
            }
        }
       boolean isValid = _isLocationValid && _isComplete && _isContentValid && _isEachChildValid;
 		_hasBeenValidated = true;
        refreshNodeInTree();
        if (!isValid) {
            MerlotDebug.msg("Validated element " + getNodeName());
            MerlotDebug.msg(" _isLocationValid: " + _isLocationValid);
            MerlotDebug.msg(" _isComplete: " + _isComplete);
            MerlotDebug.msg(" _isContentValid: " + _isContentValid);
            MerlotDebug.msg(" _isEachChildValid: " + _isEachChildValid);
        }
        long end = System.currentTimeMillis();
        MerlotDOMNode parent = getParentNode();
        if (parent != null && parent instanceof MerlotDOMElement)
            ((MerlotDOMElement)parent).resetValidation();
	}
    
    /**
     *  Determines if the minimum allowed instances of this element
     *  will be violated if it is cut or deleted.
     **/
	public boolean mayBeRemoved()
	{
		boolean ret = true;
        GrammarComplexType complexType = getGrammarComplexType();
		int minOccurs = 0;
        if (complexType != null) {
            try {
                minOccurs = getGrammarComplexType().getMinOccurs();
            } catch (java.lang.UnsupportedOperationException ex) {
                // If not yet implemented for DTDs
                return true;
            }
        }
		System.out.println("minOccurs: " + minOccurs);
		if ( minOccurs > 0 )
		{
			int numberAfterRemove = getNumberOfPeersOfSameType() -1;
			System.out.println( "numberAfterRemove: " + numberAfterRemove );
			ret = numberAfterRemove >= minOccurs;
		}
		return ret;
	}

	/**
	 * @return includes the current node
	 **/
	public int getNumberOfPeersOfSameType()
	{
		int ret = 0;
		String thisNodeName = getNodeName();
		Vector peers = getParentNode().getChildElements();
		for ( int i = 0; i < peers.size(); i++ )
		{
			MerlotDOMElement peer = (MerlotDOMElement)peers.get(i);
            if (peer.getNodeName().equalsIgnoreCase(thisNodeName) )
				ret++;
		}
		return ret;
	}

    /**
     * Called from MerlotDOMNode.fireNodeInserted to update.
     */
    public void resetValidation() {
        // Reset this node
        //MerlotDebug.msg("Resetting validation of " + this.toPathString());
        _hasBeenValidated = false;
        GrammarComplexType complexType = this.getGrammarComplexType();
        if (complexType != null)
            complexType.resetValidation((Element)this.getRealNode());
        validate();
        refreshNodeInTree();
        
        // Reset all parents of this node
        MerlotDOMNode parent = getParentNode();
        if (parent != null && parent instanceof MerlotDOMElement)
            ((MerlotDOMElement)parent).resetValidation();
    }
    
    public void resetValidationOfChildren() {
        MerlotDOMNode[] children = getChildNodes();
        for (int i = 0; i < children.length; i++) {
            MerlotDOMNode child = children[i];
            if (child instanceof MerlotDOMElement) {
                ((MerlotDOMElement)child).resetValidation();
            }
        }
    }
    
    public void refreshNodeInTree() {
        XMLFile file = _file;
        XMLEditorDoc doc = _file.getXMLEditorDoc();
        if (doc == null) {
            //MerlotDebug.msg("Cannot refresh node in tree: XMLEditorDoc is null.");
            return;
        }
        DOMTreeTableAdapter tree = doc.getTreeTableModel();
        try {
            tree.refreshNode(this);
        } catch (Exception ex) {
            //MerlotDebug.msg("Exception refreshing node in tree: " + ex);
        }
    }
	
    /**
     * @deprecated Use MerlotDOMNode.getGrammarComplexType.
     */
	public DTDElement getDTDElement()
	{
		DTDDocument dtd = getDTDDocument();
		return dtd.fetchElement( this.getNodeName() );
	}
	
    /**
     * @deprecated Use GrammarComplexType.getAttribute(name)
     */
	public DTDAttribute getDTDAttribute( String name )
	{
		return getDTDElement().getAttribute( name );
	}

    private boolean constructNewNode(MerlotDOMNode node) {
        if (!(node instanceof MerlotDOMElement))
            return true;
        MerlotDOMNode dummy;
        GrammarComplexType complexType = getGrammarComplexType();
        if (complexType==null) {
            MerlotDebug.msg("Not doing constructNewNode on " + this + " because complexType is null.");
            return true;
        }
        GrammarComplexType[] allowables = complexType.getInsertableElements(node.getRealNode());
        boolean valid = complexType.getIsComplete((Element)node.getRealNode());
            // Case of 1 allowable that is compulsory
            Vector singleNodes = new Vector();

            while (!valid && allowables.length == 1) {
            dummy = node.newChild(allowables[0].getName());
                singleNodes.add(dummy);
            allowables = complexType.getInsertableElements(node.getRealNode());
            valid = complexType.getIsComplete((Element)node.getRealNode());
            }

            for (int i = 0; i < singleNodes.size(); i++)
                constructNewNode(
                    (MerlotDOMNode) singleNodes.elementAt(i));

            if (valid) {
                return true;
            }
            return false;

    }
}

