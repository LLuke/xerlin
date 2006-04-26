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
//
// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 2 -*-
//
package org.merlotxml.merlot;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.merlotxml.merlot.plugin.dtd.DTDPluginConfig;
import org.merlotxml.merlot.plugin.dtd.DisplayTextConfig;
import org.merlotxml.merlot.plugin.dtd.PluginDTDCacheEntry;
import org.merlotxml.util.xml.DOMUtil;
import org.merlotxml.util.xml.DTDAttribute;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.DTDDocument;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarDocument;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.merlotxml.util.xml.ValidDocument;
import org.merlotxml.util.xml.XPathUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;


/**
 * DOM node container for Merlot. Contains a DOM node, handles getting an
 * icon for a particular node, getting the editor for a node, etc. Also
 * implements transferable so that the node can be drag and dropped, or 
 * cut and pasted.
 *
 * @author Kelly A. Campbell
 */

public class MerlotDOMNode
    implements Transferable, Cloneable, MerlotConstants
{

	private static final int INVALID_INDEX = -1;

	private static final boolean DEBUG = false;
	
    Icon _bean_gif = null;
    Icon _bean_gif_lg = null;
	

    // we delegate alot to theNode cause it's the actual DOM datastructure
    protected Node            _theNode;

    /**
     * The file this node came from
     */
    protected XMLFile     _file;
	
    protected MerlotDOMNode _parent = null;
    protected int _indexInParentList = MerlotDOMNode.INVALID_INDEX;

    /**
     * The node we were cloned from if any... used for dnd moves so we can
     * delete the original node that was being moved.
     */
    protected MerlotDOMNode[] _clonedFrom = null;

    /**
     * Listeners of for actions on this node
     */	
    protected Vector      _listeners ;
    /**
     * cached list of child nodes
     */    
    protected Vector _children = null;
	
    /**
     * the plugin this node is associated with
     */
    protected DTDPluginConfig _dtdPluginConfig;

    MerlotDOMEditor _editor = null;
    
    GrammarComplexType _grammarComplexType = null;

    private int validationQueueIndex = MerlotDOMNode.INVALID_INDEX;
 	private Object validationQueueIndexLock = new Object();

    public MerlotDOMNode (Node data, XMLFile file) 
    {
		if (data == null) {
			throw new NullPointerException("MerlotDOMNode constructor requires a non-null Node parameter");
		}
		
		_theNode = data;
		_file = file;
		_listeners = new Vector();
        if (file!=null)
		    XMLFile.putInstanciatedNode( data, this );
	
		
    }
	
    public Document getDocument()
    {
		return _file.getDocument();
    }
    /*
	public void setParentDTDPluginConfig(DTDPluginConfig parentConfig) {
		this._parentDTDPluginConfig = parentConfig;
	}
    */
    /**
     * this goes up the tree recursively until it finds the merlot dom document
     */
    public MerlotDOMDocument getMerlotDOMDocument() 
    {
		if (this instanceof MerlotDOMDocument) {
			return (MerlotDOMDocument)this;
		}
		else if (_parent != null) {
			return _parent.getMerlotDOMDocument();
		}
		else {
			return null;
		}
		
    }
	

    /**
     * Gets a small icon for this node based on its type
     *
     */

    public Icon getSmallIcon() 
    {
	DTDPluginConfig config = getDTDPluginConfig();
	
	if (config != null) {
	    return config.getIconFor(getNodeName(), DTDPluginConfig.ICON_SIZE_SMALL);
	}
	else {
	    return null;
	}		
    }
	
	
    /**
     * Gets a large icon
     *
     *
     */
    public Icon getIcon() 
    {
	DTDPluginConfig config = getDTDPluginConfig();
	
	if (config != null) {
	    
	    return config.getIconFor(getNodeName(), DTDPluginConfig.ICON_SIZE_LARGE);
	}
	else {
	    return null;
	}
    }
    
	
    /**
     * Element nodes should overload this to return true.
     */
    public boolean isElement() 
    {
		return false;
    }
	
    /**
     * returns the XML file object
     */
    public XMLFile getXMLFile() 
    {
		return _file;
    }
	

    /**
     * Returns the dtd plugin config for this node if it exists
     */
    public DTDPluginConfig getDTDPluginConfig() 
    {
	if (_dtdPluginConfig == null) {
	    DTDCacheEntry dtdentry = _file.getDTDCacheEntry();
	    if (dtdentry instanceof PluginDTDCacheEntry) {
		_dtdPluginConfig = ((PluginDTDCacheEntry)dtdentry).getPluginConfig();
	    }
	}
	
	return _dtdPluginConfig;
    }
    
	
    /**
     * This should return the editor object for this type of element if
     * one is found, otherwise a default one should be returned.
     */
    public MerlotDOMEditor getEditor() {
        if (_editor != null)
            return _editor;
        MerlotDOMEditor editor = null;
        MerlotEditorFactory editFactory = MerlotEditorFactory.getInstance();
        try {
	    	boolean useSchema = false;
	    	GrammarDocument grammarDocument = getGrammarDocument();
        	if (grammarDocument instanceof
            	org.merlotxml.util.xml.xerces.SchemaGrammarDocumentImpl)
            useSchema = true;
            editor = editFactory.getEditor(this.getNodeName(), this.getDTDPluginConfig(), useSchema);
        }
        catch (IllegalAccessException e) {
            MerlotDebug.msg("MerlotDOMNode.getEditor(): " + e);
            return null;
        }
        catch (InstantiationException e) {
            MerlotDebug.msg("MerlotDOMNode.getEditor(): " + e);
            return null;
        }
        String editorClass = editor.getClass().getName();
        //MerlotDebug.msg("Editor class: " + editorClass);
        _editor = editor;
        return editor;
    }

    public String getNodeName() 
    {
		return _theNode.getNodeName();
    }

    /**
     * Returns the index of the location of the child in the parent
     */
    public int getChildIndex(MerlotDOMNode child) 
		throws DOMException
    {
    	if(child._indexInParentList == MerlotDOMNode.INVALID_INDEX) {
    		throw new BastardChildException(DOMException.NOT_FOUND_ERR,
    				"Bastard child node");
    	}

    	return child._indexInParentList;
    }
    
	/** Gets the first child that is non-empty text, or returns null if none
	 *
	 */
	public MerlotDOMNode getFirstChild () 
	{
		MerlotDOMNode[] children = getChildNodes();
		MerlotDOMNode ret = null;
		int i=0;
		
		while (ret == null && i < children.length) {
			MerlotDOMNode nd = children[i];
			if (nd instanceof MerlotDOMText) {
				String txt = ((MerlotDOMText)nd).getText();
				if (!(txt.trim().equals(""))) {
					ret = nd;
				}
			}
			else {
				ret = nd;
			}
			i++;
		}
		return ret;
				
	}
	
	public Vector getChildElements()
	{
		Vector elements = new Vector();
		MerlotDOMNode[] nodes = getChildNodes();
		for (int i=0;i<nodes.length; i++) 
		{
	    	if (nodes[i] instanceof MerlotDOMElement) 
				elements.add( nodes[i] );
		}
		return elements;
	}

    /**gets a list of child nodes and creates an array of 
     * merlotDOMNodes. Returns null if no children are available
     */
    public MerlotDOMNode[] getChildNodes() 
    {
		if (_children == null) {
			_children = new Vector();
			if (_theNode != null) {
				NodeList list = _theNode.getChildNodes();
				int len = list.getLength();
				
				if (len > 0) {
					if (DEBUG) {
						MerlotDebug.msg("Node: "+getNodeName() + " array len = " + len);
					}
					
					
					for (int i=0;i<len;i++) {
						Node n = list.item(i);	
						if (n != null) {
							MerlotDOMNode nd = createDOMNode(n);
							if (nd != null) {
								nd.setParentNode(this);
							}
							
						}
					}
				}
			}
			
		}
		if (_children != null) {
			Object[] o = _children.toArray();
			MerlotDOMNode[] nodes = new MerlotDOMNode[o.length];
			for (int i=0;i<o.length;i++) {
				nodes[i] = (MerlotDOMNode)o[i];
			}
			return nodes;
		}
		return null;		

	
    }

    protected void resetCache() 
    {
		
    }
	
	
    public NamedNodeMap getAttributes() 
    {
		return _theNode.getAttributes();
    }
	
	/**
	 * Gets all the attributes (<code>org.w3c.dom.Node</code>) that have the specified type.
     * @see DTDConstants for the possible types
     */
	public String getFirstAttributeOfType( int attrType )
	{
		String text = null;
		Vector v = getAttributesOfType( attrType );
		Iterator attributes = v.iterator();
		while ( attributes.hasNext() ) {
			Node nmtoken = (Node)attributes.next();
			text = nmtoken.getNodeValue();
			if (text == null || text.trim().equals(""))
				continue;
			else
				break;
		}
		return text;
	}
	
    /** 
     * Gets only the attributes (<code>org.w3c.dom.Node</code>) that have the specified type.
     * @see DTDConstants for the possible types
     */
    public Vector getAttributesOfType( int type )
    {
		Vector results = new Vector();
		NamedNodeMap a = _theNode.getAttributes();
		if ( a == null )
			return results;
		Enumeration d = _file.getDTDAttributes( this.getNodeName() );
		while ( d != null && d.hasMoreElements() ) 
		{
			DTDAttribute dtdAttribute = (DTDAttribute)d.nextElement();
			if ( dtdAttribute.getType() == type )
			{
				String dtdName = dtdAttribute.getName();
				Node attribute = a.getNamedItem( dtdName );
				if (attribute != null)
					results.add( attribute );
			}
		}
		return results;
    }
	
    /**
     * Sets the attributes according the given hashtable. 
     * (probably should make getAttributes consistent with this at some point
     */
    public void setAttributes(HashMap h) 
    {
		// this really only works on Elements, so just ignore them here
		MerlotDebug.msg("MerlotDOMNode.setAttributes() ignored. Only available on Elements");
		
		
    }
	
    /**
     * returns an enumeration of DTDElements
     */
    public Enumeration getAppendableElements() {
	return getInsertableElements(getChildNodes().length);
    }
    /*
	Enumeration e = null;
		if (this instanceof MerlotDOMElement) {
		    try {
				e = _file.getAppendableElements((Element)_theNode);
		    }
		    catch (Exception ex) {
				ex.printStackTrace();
		    }
		    
		}

		return e;
	}
    */
	/**
	 * returns the nodes that are insertable at the given index.
     * @deprecated Use getGrammarComplexType().getInsertableElements(index)
	 */
	public Enumeration getInsertableElements(int index) 
	{
		Enumeration e = null;
		if (this instanceof MerlotDOMElement) {
		    try {
				e = _file.getInsertableElements((Element)_theNode, index);
		    }
		    catch (Exception ex) {
				ex.printStackTrace();
		    }
		    
		}

		return e;
	}
    
	/**
	 * returns the nodes that are insertable at any index.
     * @deprecated Use getGrammarComplexType().getInsertableElements()
	 */
	public Enumeration getInsertableElements() 
	{
		Enumeration e = null;
		if (this instanceof MerlotDOMElement) {
		    try {
				e = _file.getInsertableElements((Element)_theNode);
		    }
		    catch (Exception ex) {
				ex.printStackTrace();
		    }
		    
		}

		return e;
	}

	/**
	 * Returns true if the child is a valid type to be a child of this
	 * according to the DTD
	 */
	public boolean isAllowableChild(MerlotDOMNode child, boolean newChild)
	{
		return isAllowableChild (child,getChildNodes().length, newChild); 
	}

	public boolean isAllowableChild (MerlotDOMNode child, int index, boolean newChild)
	{
		if (child == null)
			return false;
		if (child.isSpecialNode())
			return true;
		String s = child.getNodeName ();
        return isAllowableChild(s, index);
    }

    public boolean isAllowableChild (String child, int index) {
        GrammarComplexType complexType = getGrammarComplexType();
        if (complexType == null)
            return false;
        if (child.equals("#text") && complexType != null)
            return complexType.getIsSimpleContentAllowed();
        /*
        Enumeration e;
        if (index>=0)
            e = getInsertableElements (index);
        else
            e = getInsertableElements();
        if (e == null)
            return false;
        while (e.hasMoreElements()){
            DTDElement el = (DTDElement)e.nextElement();
            if (el == null)
                return false;
            String elname = el.getName();
            if (elname == null)
                return false;
            if (elname.equals(child))
                return true;
        }
        */
        GrammarComplexType[] insertables;
        if (index >= 0)
            insertables = complexType.getInsertableElements(getRealNode(), index);
        else
            insertables = complexType.getInsertableElements(getRealNode());
        if (insertables == null)
            return false;
        for (int i = 0; i < insertables.length; i++) {
            GrammarComplexType insertable = insertables[i];
            if (insertable.getName().equals(child))
                return true;
        }
        return false;
	}

	
    /**
     * Returns the previous sibling or null if this is the first sibling
     */
    public MerlotDOMNode getPreviousSibling() 
    {
		if (_parent != null) {
			return _parent.getPreviousSibling(this);
		}
		return null;
    }
	
    public MerlotDOMNode getPreviousSibling(MerlotDOMNode child) 
		throws DOMException
    {
		MerlotDOMNode[] children = getChildNodes();
		MerlotDOMNode prev = null;
		if (children != null) {
			for (int i=0;i<children.length;i++) {
				MerlotDebug.msg("children[i] = "+children[i]+ "   child = "+child);
				
				if (children[i].equals(child)) {
					return prev;
				}
				prev = children[i];
			}
		}
		else {
			return null;
		}
		
		throw new BastardChildException(DOMException.NOT_FOUND_ERR, "Bastard child node");
    }
	
	
    public MerlotDOMNode getParentNode() 
    {
		//	MerlotDebug.msg("Parent node = "+ _parent);
		
		return _parent;
    }
	
    public void setParentNode(MerlotDOMNode nd) 
	{
		setParentNode(nd,true);
		
		
	}
	
    protected void setParentNode(MerlotDOMNode nd, boolean updateChildren) 
    {

		if (updateChildren) {
			if (nd == null) {
				if (_parent._children != null) {
					_parent.removeChildAndUpdateIndices(this);
				}
				
			}
			else {
				nd.addChild(this);
			}
			
		}
		
		_parent = nd;
		
		
    }

	private void addChild(MerlotDOMNode nd) {
		if (_children != null) {
			synchronized (_children) {
				_children.addElement(nd);

				nd._indexInParentList = _children.size()-1;
			}
		}
	}

	private void addChildAndUpdateIndices(MerlotDOMNode child, int index) {
		synchronized (_children) {
			_children.add(index,child);

			child._indexInParentList = index;

			final int size = _children.size();
			for(int idx = ++index; idx < size; ++idx) {
				MerlotDOMNode mdn = (MerlotDOMNode) _children.elementAt(idx);

				++mdn._indexInParentList;
			}
		}
	}

	private void removeChildAndUpdateIndices(MerlotDOMNode child) {
		synchronized (_children) {
			// Remove this node from the parent
			_children.removeElement(child);
			
			// Adjust the subsequent cached indices
			final int size = _children.size();

			for(int idx = child._indexInParentList; idx < size; ++idx) {
				MerlotDOMNode mdn =
					(MerlotDOMNode) _children.elementAt(idx);

				--mdn._indexInParentList;
			}
		}
	}
	

    protected void setClonedFrom(MerlotDOMNode[] nd) 
    {
		_clonedFrom = nd;
				
    }
    protected MerlotDOMNode[] getClonedFrom() 
    {
		return _clonedFrom;
    }
	

    /**
     * creates a new child and adds it to the end of the list
     */
    public MerlotDOMNode newChild(String nodename) 
		throws DOMException
    {
		MerlotDOMNode ret = createDOMNode(nodename);

		this.appendChild(ret);
		return ret;
		
	
    }

    public MerlotDOMNode newChild(Node nd) 
    {
		
		MerlotDOMNode ret = createDOMNode(nd);
		this.appendChild(ret);
		return ret;
			
    }

	/**
	 * creates a new sibling and inserts it after this node
	 */
	public MerlotDOMNode newSiblingAfter(Node nd) 
	{
		MerlotDOMNode ret = createDOMNode(nd);
		ret.insertAfter(this);
		return ret;
		
	}
	public MerlotDOMNode newSiblingAfter(String nodename) 
	{
		MerlotDOMNode ret = createDOMNode(nodename);
		ret.insertAfter(this);
		return ret;
	}
	
	/**
	 * creates a new sibling and inserts it after this node
	 */
	public MerlotDOMNode newSiblingBefore(Node nd) 
	{
		MerlotDOMNode ret = createDOMNode(nd);
		ret.insertBefore(this);
		return ret;
		
	}
	public MerlotDOMNode newSiblingBefore(String nodename) 
	{
		MerlotDOMNode ret = createDOMNode(nodename);
		ret.insertBefore(this);
		return ret;
	}
    /**
     * inserts this after the sibling
     */
    public void insertAfter (MerlotDOMNode sibling) 
    {
		Node nd = sibling._theNode.getNextSibling();
		Node p  = sibling.getParentNode()._theNode;
		if (nd != null) {
			try {
				p.insertBefore(_theNode, nd);
			}
			catch (DOMException ex) {
				MerlotDebug.exception(ex);
				//XXX throw this on up instead of catching it here

			}
		}
		else {
			// need to append it I guess
			p.appendChild(_theNode);
			
		}
		// hopefully that all worked.
		MerlotDOMNode parent = sibling.getParentNode();
		int ind = parent.getChildIndex(sibling);
		if (parent._children == null) {
			parent._children = new Vector();
		}
		
		parent.addChildAndUpdateIndices(this, ind+1);
		
		setParentNode(parent,false);
		
		//		parent.resetCache();		
		int[] myIndices = getIndices();
		MerlotDOMNode[] myNodes = getNodes();
		
		fireNodeInserted(getParentNode(),myIndices,myNodes);
		
		
    }

    /**
     * inserts this before the sibling
     */	

    public void insertBefore (MerlotDOMNode sibling)
    {

		Node p  = sibling.getParentNode()._theNode;
	
		try {
			if (_theNode != null) {
				p.insertBefore(_theNode,sibling._theNode);
			}
			else {
				// need to append it I guess
				p.appendChild(_theNode);
		
			}
			// hopefully that all worked.
		MerlotDOMNode parent = sibling.getParentNode();
		int ind = parent.getChildIndex(sibling);
		if (parent._children == null) {
			parent._children = new Vector();
		}

		parent.addChildAndUpdateIndices(this, ind);

		setParentNode(parent,false);

		//	parent.resetCache();		
		int[] myIndices = getIndices();
		MerlotDOMNode[] myNodes = getNodes();
		
		fireNodeInserted(getParentNode(),myIndices,myNodes);
		//			fireNodeInserted(getParentNode(),this);
		}
		catch (DOMException ex) {
			MerlotDebug.msg("error during insertBefore. nd = "+this + " sibling = "+sibling);
	    
			MerlotDebug.exception(ex);
			//XXX throw this on up instead of catching it here
		}
	
    }
    
    
    public void insertChildAt (MerlotDOMNode child, int loc) 
    {
		MerlotDebug.msg("insertChildAt loc = "+loc);
	
		MerlotDOMNode[] children = getChildNodes();
		if (children != null) {
			MerlotDebug.msg("children.length = "+children.length);
			//	MerlotDebug.msg("child at loc = "+children[loc]);
	    
			//	child.setParentNode(this);
	    
			if (children.length > loc) {
				child.insertBefore(children[loc]);
			}
			else {
				appendChild(child);
			}
		}
	
		else {
			appendChild(child);
		}
		
    }
   

    public void appendChild(MerlotDOMNode child) 
    {
        if (_theNode instanceof Element) {
		    int appendPosition = getChildNodes().length;
		    Element parent = (Element)_theNode;
		    DTDDocument dtd = _file.getValidDocument().getDTDForElement( parent );
		    String childName = child.getNodeName();
		    //int insertPosition = dtd.getInsertPosition( parent, childName );
            GrammarComplexType complexType = getGrammarComplexType();
            if (complexType != null) {
            	int insertPosition = getGrammarComplexType().getInsertPosition(parent, childName);
                	if (insertPosition == MerlotDOMNode.INVALID_INDEX) {
                    	MerlotDebug.msg("Not appending child - child not allowed here.");
                    	return;
                	}
		    	if ( appendPosition > insertPosition ) {   
			        MerlotDebug.msg( "Doing insert instead of append." );
			    	insertChildAt( child, insertPosition );
			    	return;
		    	}
        	}
        }
		try {
			_theNode.appendChild(child._theNode);
		}
		catch (DOMException ex) {
			MerlotDebug.exception(ex);
			
			//XXX throw this on up instead of catching it here
		}
		
		child.setParentNode(this);

		resetCache();		
		int[] myIndices = child.getIndices();
		MerlotDOMNode[] myNodes = child.getNodes();
		MerlotDebug.msg("appendChild: myIndices = "+array2String(myIndices) + " myNodes = "+array2String(myNodes));
   
		fireNodeInserted(this,myIndices,myNodes);
		//		fireNodeInserted(this,child);
	
		
    }
	

	
    /**
     * imports a child node from another document
     */
    public MerlotDOMNode importChild(MerlotDOMNode child) {
        return importChild(child, true);
    }
    public MerlotDOMNode importChild(MerlotDOMNode child, boolean asLastChild) 
    {
		Node nd = DOMUtil.importNode(getRealNode(),child.getRealNode(),
                                        asLastChild);
		MerlotDebug.msg(getNodeName()+"Imported node = "+nd);

		child = createDOMNode(nd);
	
		appendChild(child);
		/*
		child.setParentNode(this);
		fireNodeInserted(this,child);
		*/
		return child;
		
		
    }
	
    /**
     * imports a node and places it before this one
     */
    public MerlotDOMNode importChildBefore(MerlotDOMNode child) 
    {
		Node nd = DOMUtil.importNode(getRealNode(), child.getRealNode(), false);
		child = createDOMNode(nd);
		
		child.insertBefore(this);
		return child;
		
    }

    /**
     * imports a node and places it after this one
     */
    public MerlotDOMNode importChildAfter(MerlotDOMNode child) 
    {
		Node nd = DOMUtil.importNode(getRealNode(), child.getRealNode(), false);
		child = createDOMNode(nd);
		
		child.insertAfter(this);		
		return child;
		
    }
	

    /**
     * Removes a child node
     */
    public void removeChild(MerlotDOMNode nd) 
		throws DOMException
    {
		//		fireNodeRemoved(this,nd);
		MerlotDOMNode parent = nd.getParentNode();
		int[] myIndices = nd.getIndices();
		MerlotDOMNode[] myNodes = nd.getNodes();
		
		getRealNode().removeChild(nd.getRealNode());
		nd.setParentNode(null);

		resetCache();
		fireNodeRemoved(parent,myIndices,myNodes);
		nd.fireNodeDeleted();

        MerlotDOMDocument mdd = getMerlotDOMDocument();
        if (mdd!=null)
            mdd._nodeDescription.remove(this);
    }
	
	/**
	 * this gets the index of this child or if it's a fragment, then the indices of all
	 * the nodes in the top level of the fragment
	 */
	protected int[] getIndices()
	{
		int[] i = null;
	 
		MerlotDOMNode parent = getParentNode();
		if (parent == null) {
			MerlotDebug.msg("PARENT IS NULL!!!!");
		}
		else {
			i = new int[1];
			i[0] = parent.getChildIndex(this);
		}
		return i;
		
	}
	
	protected MerlotDOMNode[] getNodes()
	{
		MerlotDOMNode[] n = new MerlotDOMNode[1];
		n[0] = this;
		return n;
		
	}
	

	private MerlotDOMNode createDOMNode (String nodename) 
	{
	    MerlotDOMNode ret = null;
	    
	    MerlotDebug.msg("NodeName = " + nodename);
		Node nd = null;
		
		Document doc = getDocument();
		
		if ( nodename.equals( DTDConstants.PCDATA_KEY ) ) {
			nd = doc.createTextNode("");
		}
		else if (nodename.equals(DTDConstants.COMMENT_KEY)) {
		    nd = doc.createComment("");
		}
		else if (nodename.equals(DTDConstants.PROCESSING_INSTRUCTION_KEY)) {
                    // Backed out changes - return to original code
                    // changes caused bug #84
		    MerlotDebug.msg("Creating Processing Instruction");
		    try {
			nd = doc.createProcessingInstruction("pi_node","pi_target");
		    }
					// REVISIT
                    //catch (com.ibm.xml.dom.DOMExceptionImpl e) {
                    catch (Exception e) {
		        MerlotDebug.msg("Invalid PI");
                    }
		    if (nd != null) {
			String targetString =  ((ProcessingInstruction)nd).getTarget();
			String dataString =  ((ProcessingInstruction)nd).getData();
			if (targetString != null) {
			    MerlotDebug.msg("Target = " + targetString);
			}
			else {
			    MerlotDebug.msg("Target NULL");
			}
			if (dataString != null) {
			    MerlotDebug.msg( " Data = " + dataString); 
			}
			else {
			    MerlotDebug.msg("Data NULL");
			}
		    }
		    else {
			MerlotDebug.msg("Failed to create PI Node");
		    }
		}
		else {
			nd = doc.createElement(nodename);
		}
		
		if (nd != null) {
			ret = createDOMNode(nd);
			
		}
		else {
			ret = null;
		}
		// we only show blank TextNodes if they were created in Merlot 
		if (ret instanceof MerlotDOMText 
            && !this.getNodeName().equals(DTDConstants.PROCESSING_INSTRUCTION_KEY)) {
			((MerlotDOMText)ret).setVisible(true);
		}
				
		return ret;
		
	}
	

    private MerlotDOMNode createDOMNode(Node nd) 
    {
		MerlotDOMNode ret = null;
		int type = nd.getNodeType();
		switch (type) {
		case Node.ELEMENT_NODE:
			if (nd.getNodeName().equals("libitem")) {
				ret = new MerlotLibraryItem((Element)nd,_file);
            } else {
				ret = new MerlotDOMElement((Element)nd,_file);
                // Set default attributes here
                XMLEditorSettings xes = XMLEditorSettings.getSharedInstance();
                boolean writeDefaults = true;
                String writeDefaultsProperty = xes.getProperty("merlot.write.default-atts");
                if (writeDefaultsProperty != null)
                    writeDefaults = writeDefaultsProperty.equals("true");
                if (_file!=null && writeDefaults) {
                    DTDAttribute attr;
                    Enumeration e = ret.getDTDAttributes();
                    int t;
                    if (e != null) {
                        while (e.hasMoreElements()) {
                            attr = (DTDAttribute)e.nextElement();
                            t = attr.getType();

                            if (t==DTDConstants.TOKEN_GROUP 
                                || t==DTDConstants.CDATA) {
                                String name = attr.getName();
                                String defaultValue = attr.getDefaultValue();
                                if (((MerlotDOMElement)ret).
                                        getAttribute(name).equals(defaultValue)) {
                                    ((MerlotDOMElement)ret).setAttribute(
                                                            name, defaultValue);
                                }
                            }
                        }
                    }
                }
			}
			break;
		case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
		    String s = ((Text)nd).getData();
		    if (s!=null && s.equals(DTDConstants.PROCESSING_INSTRUCTION_KEY)) {
			ret = new MerlotDOMProcessingInstruction(nd,_file);
            } else {
			ret = new MerlotDOMText((Text)nd,_file);
		    }
		    if (s != null) {
			s = s.trim();
			if ( s.equals("")) {
			    ((MerlotDOMText)ret).setVisible(false);
                } else {
			    ((MerlotDOMText)ret).setVisible(true);
			}
			
		    }
		    break;
		    
		case Node.PROCESSING_INSTRUCTION_NODE:
		    ret = new MerlotDOMProcessingInstruction(nd,_file);
		    break;
			
		case Node.DOCUMENT_NODE:
			ret = new MerlotDOMDocument((Document)nd, _file);
			break;
			
		case Node.DOCUMENT_FRAGMENT_NODE:
			ret = new MerlotDOMFragment((DocumentFragment)nd,_file);
			break;
			
		case Node.DOCUMENT_TYPE_NODE:
			ret = new MerlotDOMDoctype((DocumentType)nd,_file);
			break;
			
		case Node.COMMENT_NODE:
			ret = new MerlotDOMComment((Comment)nd,_file);
			break;
				

		default:
			ret = null;
		}
		// show all comment and processing instruction nodes
		if (ret instanceof MerlotDOMComment) {
			((MerlotDOMComment)ret).setVisible(true);
		}
		if (ret instanceof MerlotDOMProcessingInstruction) {
			((MerlotDOMProcessingInstruction)ret).setVisible(true);
		}

		return ret;
		
    }
	
	

    public void delete() 
    {
		// delete this node and remove it from it's parent
		//	Node nd = getRealNode();
		//	Node parent = getParentNode().getRealNode();
		//	fireNodeRemoved(getParentNode(), this);	// this must be before the actual removal
		MerlotDOMNode parent = getParentNode();
		if (parent != null) {
			getParentNode().removeChild(this);
		}
		else {
			MerlotError.msg("Cannot delete. Parent is null");
		}
		
		//	_listeners = null;
		

    }
	
    /**
     * @deprecated Use getGrammarDocument.
     */
	public DTDDocument getDTDDocument()
	{
		ValidDocument doc = _file.getValidDocument();
		return doc.getDTDForElement( this.getNodeName() );
	}
    
    /**
     * @deprecated Use getGrammarAttributes
     */
    public Enumeration getDTDAttributes()
    {
		Enumeration e = _file.getDTDAttributes(this.getNodeName());
		return e;
    }
    
    /**
     * Provides access to element and attribute grammar for both
     * DTDs and Schemas.
     */
    public GrammarDocument getGrammarDocument() {
		ValidDocument doc = _file.getValidDocument();
        return doc.getGrammarDocument();
    }

    /**
     * Provides access to element grammar for both
     * DTDs and Schemas.
     */
    public GrammarComplexType getGrammarComplexType() {
        if (_grammarComplexType != null)
            return _grammarComplexType;
        GrammarComplexType ret = null;
        GrammarDocument grammarDocument = getGrammarDocument();
        if (grammarDocument==null) {
            //MerlotDebug.msg("GrammarDocument is null for " + getNodeName());
            return null;
        }
        GrammarComplexType parentComplexType = null;
        MerlotDOMNode parent = getParentNode();
        if (parent != null)
            parentComplexType = parent.getGrammarComplexType();
        ret = grammarDocument.getGrammarComplexType(parentComplexType, getNodeName());
        if (ret == null) {
            //MerlotDebug.msg("Complex type is null for " + getNodeName());
        }
        _grammarComplexType = ret;
        return ret;
    }
    
    /**
     * Provides access to the grammar of the attributes defined for this node
     * for both DTDs and Schemas.
     */
    public GrammarSimpleType[] getGrammarAttributes() {
        GrammarComplexType complexType = getGrammarComplexType();
        if (complexType == null) {
            MerlotDebug.msg("ComplexType is null for node " + getNodeName());
            return new GrammarSimpleType[0];
        }
        return complexType.getAttributes();
    }

    public GrammarSimpleType getGrammarAttribute(String name) {
        GrammarComplexType complexType = getGrammarComplexType();
        if (complexType == null) {
            MerlotDebug.msg("ComplexType is null for node " + getNodeName());
            return null;
        }
        return complexType.getAttribute(name);
    }
    
    public JPanel getEditPanel()
	throws InstantiationException, IllegalAccessException
    {
		return getEditor().getEditPanel(this);
    }


    public Node getRealNode() 
    {
		return _theNode;
    }
	

    // Transferable stuff

	
    final static int TREE = 0;
    final static int STRING = 1;
    final static int PLAIN_TEXT = 2;
	
    final public static DataFlavor 
		DOM_TREENODE_FLAVOR = 
		new DataFlavor(MerlotDOMNode.class,  "DOM Node");
	
    static DataFlavor flavors[] = {
		DOM_TREENODE_FLAVOR, 
		DataFlavor.stringFlavor, 
		DataFlavor.plainTextFlavor};


    public DataFlavor[] getTransferDataFlavors() {
		return flavors;
    }

    public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException,
			   IOException {
		Object returnObject = null;
		if (flavor.equals(flavors[TREE])) {
			// check to see if we are a doc frag, if so no need to do
			// and extra cloning
			if (this instanceof MerlotDOMFragment) {
				returnObject = this;
			}
			else {
				// create a DocumentFragment containing the node
				MerlotDOMNode cloned = (MerlotDOMNode)this.clone();
				// create a document fragment to hold this
				//	cloned.setClonedFrom(this);
				
				Document d = getDocument();
				if (d != null) {
					DocumentFragment frag = d.createDocumentFragment();
					frag.appendChild(cloned.getRealNode());
					MerlotDOMFragment gfrag = new MerlotDOMFragment(frag,_file);
					MerlotDOMNode[] clonedset = new MerlotDOMNode[1];
					clonedset[0] = this;
					gfrag.setClonedFrom(clonedset);
					
					//gfrag._listeners = cloned._listeners;
					
					returnObject = gfrag;
				}
			}
			
		} else if (flavor.equals(flavors[STRING])) {
			
			returnObject = _theNode.toString();
			
		} else if (flavor.equals(flavors[PLAIN_TEXT])) {
			
			String string = _theNode.toString();
			returnObject = new ByteArrayInputStream(string.getBytes());
			
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
		return returnObject;
    }


    public boolean isDataFlavorSupported(DataFlavor flavor) {
		boolean returnValue = false;
		for (int i=0, n=flavors.length; i<n; i++) {
			if (flavor.equals(flavors[i])) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
    }

    /**
     * provides a deep copy
     */	
    public Object clone() 
    {
		Object c = null;
		
		try {
			c = super.clone();
		}
		catch (CloneNotSupportedException ex) {
			MerlotDebug.exception(ex);
		}
		
		if (! (c instanceof MerlotDOMNode)) {
			MerlotDebug.msg("super.clone didn't return a MerlotDOMnode!!!");
		}
		
		// now deep copy all our fields
		((MerlotDOMNode)c)._theNode = this._theNode.cloneNode(true);
		// no parent for cloned nodes
		((MerlotDOMNode)c)._parent = null;
		((MerlotDOMNode)c)._listeners = this._listeners;
		return c;
				
    }
	
    public void addMerlotNodeListener(MerlotNodeListener l) 
    {

		MerlotDebug.msg("Node: "+this+" added Listener: "+l);
		// Can't see any reason for allowing duplicates -- Evert
		if ( ! _listeners.contains( l ) )
			_listeners.addElement(l);
    }
	public void removeMerlotNodeListener(MerlotNodeListener l)
	{
		MerlotDebug.msg("Node: "+this+" removed Listener: "+l);
		_listeners.removeElement(l);
	}


	/**
	* Removes all listeners from this node
	*/
	public void removeAllMerlotDOMNodeListeners()
	{
		_listeners.removeAllElements();
	}	
	
	

	protected Vector getMerlotNodeListeners() 
	{
		MerlotDOMNode parent = getParentNode();
		if (parent == null) {
			MerlotDebug.msg("This = "+this+"  Returning listeners: "+_listeners);
			
			return _listeners;
		}
		return parent.getMerlotNodeListeners();
	}
	
	public String array2String(int[] array) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i=0;i < array.length; i++) {
			sb.append(array[i]);
			if (i+1 < array.length) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
		
	}
	
	public String array2String(Object[] array) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i=0;i < array.length; i++) {
			sb.append(array[i]);
			if (i+1 < array.length) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
		
	}
	


    public void fireNodeInserted(MerlotDOMNode parent, int[] childindices, 
								 MerlotDOMNode[] newchildren) 
    {
		MerlotDebug.msg("Node: fireNodeInserted("+parent+", "+array2String(childindices)+", "+array2String(newchildren)+")");
		
		MerlotDebug.msg("getParent = "+getParentNode()+" parent.getParent() = "+parent.getParentNode());
		
		parent.resetCache();
		if ( parent != null && newchildren != null) {

			Vector listeners = getMerlotNodeListeners();
			Vector v = new Vector();
			v.addAll(listeners);
			v.addAll(parent._listeners);
			if (listeners != _listeners && _listeners != null) {
				v.addAll(_listeners);
			}
			// also notify listeners of the nodes being added
			for (int i=0; i<newchildren.length;i++) {
				if (newchildren[i] != null && newchildren[i]._listeners != null) {
					v.addAll(newchildren[i]._listeners);
				}
                // Revalidate the element in order to show the correct colour in the
                // tree
                if (newchildren[i] instanceof MerlotDOMElement)
                    ((MerlotDOMElement)newchildren[i]).resetValidation();
			}

			Enumeration e = v.elements();
            Vector doneListeners = new Vector(v.size());
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				MerlotDebug.msg("Node listener: "+o);
                if (doneListeners.contains(o))
                   continue; 
			    doneListeners.add(o);	
				
				if (o instanceof MerlotNodeListener) {
					if (DEBUG) {
						MerlotDebug.msg("notifying listener: "+o);
					}
					
					((MerlotNodeListener)o).nodeInserted(parent,childindices,newchildren);
				}
			}
			_file.setDirty(true);
			
		}
		
		
		
    }

	/**
	 * This handles the case of notifying anything editing the node that it is deleted.
	 */
	public void fireNodeDeleted() 
	{
        // Update the identity constraints

		//MerlotDebug.msg("nodeDeleted(): "+this);
        MerlotDOMNode parent = getParentNode();
        if (parent instanceof MerlotDOMElement)
            ((MerlotDOMElement)parent).resetValidationOfChildren();
        
		MerlotDOMNode[] children = getChildNodes();
		if (children != null && children.length > 0) {
			for (int i = 0; i<children.length;i++) {
				children[i].fireNodeDeleted();
			}
		}
		
		if (_listeners != null) {
			Enumeration e = _listeners.elements();
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				if (o instanceof MerlotNodeListener) {
					MerlotDebug.msg("listener: "+o);
					((MerlotNodeListener)o).nodeDeleted(this);
				}
			}
		}
	}
	
	/**
	 * This one handles the case of keeping the tree view up to date.
	 */
    public void fireNodeRemoved(MerlotDOMNode parent, int[] childindices, 
								 MerlotDOMNode[] oldchildren)  
    {
		MerlotDebug.msg("Node: fireNodeRemoved("+parent+", "+childindices+", "+oldchildren+")");
		
		// Revalidate the element in order to show the correct colour in the
		// tree
		if ( parent instanceof MerlotDOMElement )
			((MerlotDOMElement)parent).resetValidationOfChildren();
			
		if ( parent != null && oldchildren != null) {
			Vector listeners = getMerlotNodeListeners();
			Vector v = new Vector();
			v.addAll(listeners);
			// notify this node's listeners
			if (listeners != _listeners && _listeners != null) {
				v.addAll(_listeners);
			}
			// also notify listeners of the nodes being deleted
			for (int i=0; i<oldchildren.length;i++) {
				MerlotDebug.msg("oldchildren["+i+"] = "+oldchildren[i] + " oldchildren.listeners = "+oldchildren[i]._listeners);
                
                if (oldchildren[i] instanceof MerlotDOMElement)
                    ((MerlotDOMElement)oldchildren[i]).resetValidation();
				
				if (oldchildren[i] != null && oldchildren[i]._listeners != null) {
					MerlotDebug.msg("adding listeners for "+oldchildren[i]);
					v.addAll(oldchildren[i]._listeners);
				}
			}
			

			Enumeration e = v.elements();
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				MerlotDebug.msg("listener: "+o);
				
				if (o instanceof MerlotNodeListener) {
					((MerlotNodeListener)o).nodeRemoved(parent,childindices,oldchildren);
				}
			}
			
			_file.setDirty(true);
		}
		
		
    }


	public void fireNodeChanged() 
	{
		MerlotDebug.msg("nodeChanged(): "+this);
        
		MerlotDOMNode parent = getParentNode();
		
        if (this instanceof MerlotDOMElement)
            ((MerlotDOMElement)this).resetValidation();
        
		if ( parent != null ) {
			Vector listeners = getMerlotNodeListeners();
			Vector v = new Vector();
			v.addAll(listeners);
			// notify this node's listeners
			if (listeners != _listeners && _listeners != null) {
				v.addAll(_listeners);
			}

		
			Enumeration e = v.elements();
			MerlotDOMNode[] nodes = new MerlotDOMNode[1];
			nodes[0] = this;
			int[] indices = getIndices();
			
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				if (o instanceof MerlotNodeListener) {
					MerlotDebug.msg("listener: "+o);
					((MerlotNodeListener)o).nodeChanged(getParentNode(),indices,nodes);
				}
			}
			_file.setDirty(true);

            if (getMerlotDOMDocument() != null) {
            	getMerlotDOMDocument()._nodeDescription.remove(this);
			}
		}
		
	}
	

    public String toString() 
    {
		StringBuffer sb = new StringBuffer();
        sb.append(getClass()+":"+getNodeName());
        //sb.append(super.toString());
        
		if (this instanceof MerlotDOMElement) {
			sb.append(" {"+((MerlotDOMElement)this).getAttribute("name")+"}");
		}
		return sb.toString();
		
    }
    
    public String toPathString() {
        StringBuffer ret = new StringBuffer();
        MerlotDOMNode parent = this;
        while (parent != null) {
            ret.insert(0, ">" + parent.getNodeName());
            parent = parent.getParentNode();
        }
        return ret.toString();
    }
	
    public boolean equals (Object o) 
    {
		if (o instanceof MerlotDOMNode) {
			return ((MerlotDOMNode)o)._theNode == this._theNode;
		}
		else {
			return false;
		}
		
    }

	public String getDisplayText () 
	{
		String text = null;
		
		if (this instanceof MerlotDOMElement) {
			text = ((MerlotDOMElement)this).getAttribute("name");
		}
		if (text == null || text.trim().equals("")) {
			text = getNodeName();
		}
		return text;
		
	}
	
	/**
	 * Determines the most descriptive text for a node based on priorities set
	 * in plugin.xml.
	 */
	public String getDescriptiveText() 
	{
	    String description = 
            (String)getMerlotDOMDocument()._nodeDescription.get(this);

        if (description!=null)
            return description;
                
		DTDPluginConfig dtdConfig = getDTDPluginConfig();
		
		DisplayTextConfig displayTextConfig = null;
		if ( dtdConfig != null )
			displayTextConfig = dtdConfig.getDisplayTextConfig();
			
		java.util.List defaultOrder = new Vector();
		Map nodeProperties = new HashMap();
		if ( displayTextConfig != null )
		{
			defaultOrder = displayTextConfig.getDefaultOrder();
			nodeProperties = displayTextConfig.getNodeProperties();
		}
		
		description = getDescriptiveText( nodeProperties, defaultOrder );
        
       	getMerlotDOMDocument()._nodeDescription.put(this,description);       

        return description;
	}
	
	public String getDescriptiveText( Map nodeProperties, java.util.List defaultOrder )
	{
		String text = null;
		
		String nodeName = getNodeName();
				
		Object property = null;
			
		property = nodeProperties.get( nodeName );
			
		if ( property != null)
		{
			text = getProperty( property );
			return text;
		}
		else
		{
			Iterator it = defaultOrder.iterator();
			while ( text == null || text.trim().equals( "" ) )
			{
				if ( it.hasNext() )
					property = it.next();
				else
					break;
				text = getProperty( property );
			}
		}
		
		return text;
	}

    /**
     * Concept here is that a node can have it's description set externally
     * by a plugin and that text be displayed - it can double as a cache
     * for node descriptions
     */
    public void setDescriptiveText(String description)
    {
        getMerlotDOMDocument()._nodeDescription.put(this, description);
    }
	
	public String toXMLShort()
	{
		return XPathUtil.nodeToString( getRealNode() );
	}
	
	/**
	 * Property must either be a String containing the attribute name, or an
	 * Integer with a DTDConstant specifying an attribute type.
	 */
	public String getProperty( Object property )
	{
		String text = null;
		
		if ( property.equals( "CHILD_TEXT" ) )
		{
			Vector children = getChildElements();
			if ( children.size() > 0 )
			{
				MerlotDOMElement child = (MerlotDOMElement)children.get( 0 );
				if ( child != null )
					text = child.getDescriptiveText();
			}
			return text;
		}
		
		if ( property instanceof String )
		{
			if ( this instanceof MerlotDOMElement )
				text = ((MerlotDOMElement)this).getAttribute( (String)property );
		}
		
		if ( property instanceof Integer )
		{
			int attrType = ( (Integer)property ).intValue();
			text = ( (MerlotDOMNode)this ).getFirstAttributeOfType( attrType );
			if ( text != null && attrType == DTDConstants.IDREF )
			{
				MerlotDOMDocument doc = getMerlotDOMDocument();
				MerlotDOMNode ref = doc.findNodeById( text );
				if (ref != null)
					text = ref.getDescriptiveText();
			}
		}
		
		return text;
	}
	
    public int hashCode() 
    {
		return _theNode.hashCode();
    }
    
	public void printNode() 
	{
		printNode("");
	}
	
	public void printNode(String prepend) 
	{
		MerlotDebug.msg(prepend+toString());
		MerlotDOMNode[] children = getChildNodes();
		for (int i = 0; i < children.length; i++) {
			children[i].printNode(prepend+"  ");
		}
				
	}

    public String getChildText(boolean suppressWhiteSpace) 
    {
	MerlotDOMNode[] nodes = getChildNodes();
	for (int i=0;i<nodes.length; i++) {
	    if (nodes[i] instanceof MerlotDOMText) {
		String text = ((MerlotDOMText)nodes[i]).getText();
		if (text != null && suppressWhiteSpace) {
		    text = text.trim();
		}
		return text;
		
	    }
	    
	}
	return null;
    }

	public boolean isSpecialNode () {
		return false;
	}

	/** 
	 * Return THE IDManager specific to the document 
	 * containing this node.
	 */
	public IDManager getIdManager () {
		// gets the manager from the root node
		return getMerlotDOMDocument().getIdManager();
	}

	public boolean isRootElement () {
		return (_parent == null || _parent instanceof MerlotDOMDocument);
	}
	
	public boolean isValid () {
		return true;
	}

    public void removeAttributes() {
    }
	
	public boolean locationIsValid (boolean checkParents) {
	    // isValid() in MerlotDOMElement already checks for location. 
	    return true;
   	    /*
        boolean ret = false;
	    if (isRootElement())
	        ret = true;
        else
	    {
	        if (checkParents && !_parent.locationIsValid(true))
		        ret = false;
		    else
		        ret = _parent.isAllowableChild(this,_parent.getChildIndex(this),false);
     	}
        if ( ret == false )
    	    MerlotDebug.msg( "Node " + toXMLShort() + " has invalid location" );
        return ret;
        */
	}	
    
    public boolean mayBeRemoved() {
        // Refer to MerlotDOMElement.
        return true;
	}

     public boolean isInValidationQueue() {
     	synchronized (validationQueueIndexLock) {
     		return validationQueueIndex != MerlotDOMNode.INVALID_INDEX;
     	}
     }

     public int getValidationQueueIndex() {
     	synchronized (validationQueueIndexLock) {
     		return validationQueueIndex;
 		}
     }

     public void setValidationQueueIndex(int validationQueueIndex) {
 		synchronized (validationQueueIndexLock) {
 			this.validationQueueIndex  = validationQueueIndex;
 		}
 	}
}
