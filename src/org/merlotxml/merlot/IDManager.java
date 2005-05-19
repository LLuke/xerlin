/*
====================================================================
Copyright (c) 2001 ChannelPoint, Inc..  All rights reserved.
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

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
/*
import javax.swing.*;
import javax.swing.text.*;
import com.sun.javax.swing.*;

import matthew.awt.StrutLayout;
import com.ibm.xml.parser.*;
*/
import org.merlotxml.util.xml.*;

/**
 * Manages ID and IDRef attributes for one XML file.
 * Generic implementation.
 * 
 * @author Jean-Michel Sizun
 *
 */
public class IDManager implements DTDConstants, MerlotNodeListener
{
    protected MerlotDOMNode _rootNode = null;
    
    /**
     * contains the last index value used for each named node.
     */
    protected Hashtable _attrLastId = null;
	
	/**
	 *  Used to check that the same millisecond is not assigned twice.
	 */
	protected long _lastTimeAssigned = 0;

    /**
     *  Used to disable the IDManager such that a new ID is not created - ie for
     *  a DND move 
     */
    private boolean _disabled = false;
    
    public IDManager(MerlotDOMNode rootNode) {
	_rootNode = rootNode.getMerlotDOMDocument();
	_attrLastId = new Hashtable();
    }
    
    /**
     * Returns all the ID attributes in the document.
     * They are returned in a hashtable with Attr instancies as keys 
     * and the parent node as values. 
     *
     * @param targetNode the node containing the IDREF attribute
     * @param targetAttrName the name of the IDREF attribute
     */
    public Map getIDAttrs (MerlotDOMNode targetNode, String targetAttrName)
    {
	Map savedIDAttrs = getIDAttrs (_rootNode, targetNode, targetAttrName);
	return savedIDAttrs;
    }
    
    /**
     * 
     * Returns all the ID attributes in the document
     * which belongs to a node and its children. 
     * Unsaved Ids are not taken into account.
     * They are returned in a hashtable with Attr instancies as keys 
     * and the parent node as values.
     *
     * @param node the parent node
     * @param targetNode the node containing the IDREF attribute
     * @param targetAttrName the name of the IDREF attribute
     */
    protected Map getIDAttrs (MerlotDOMNode node, MerlotDOMNode targetNode, String targetAttrName)
    {
	if(node==null)
	    return null;
	
	Map ret = new TreeMap(new Comparator() {
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}
		public int compare(Object o1, Object o2) {
			if ((o1==null)||(o2==null))
				return 0;
			if (o1 instanceof Attr) {
				Attr a1 = (Attr) o1;
				if (o2 instanceof Attr) {
					Attr a2 = (Attr) o2;
					String a1Value = a1.getValue();
					if (a1Value==null)
					{
						return 0;
					}
					String a2Value = a2.getValue();
					if (a2Value==null)
					{
						return 0;
					}
					return a1Value.compareTo(a2Value);
				}

			}
            return 0;
		}
	});
	Enumeration e = node.getDTDAttributes();
	if (e != null) {
	    while (e.hasMoreElements()) {	
		DTDAttribute dtdAttr = (DTDAttribute)e.nextElement();
		NamedNodeMap attrs = node.getAttributes();
		if ((dtdAttr == null) || (dtdAttr.getType() != ID) || attrs == null )
		    continue;
		Attr attr = (Attr)attrs.getNamedItem(dtdAttr.getName());
		if (attr != null)
		    ret.put(attr, node);
	    }
	}
		
	MerlotDOMNode[] children = node.getChildNodes();
	if (children != null) {
	    int childCount = children.length;
	    for (int i=0; i<childCount; i++) {
		ret.putAll(getIDAttrs(children[i], targetNode, targetAttrName));
	    }
	}
	return ret;
    }

    /**
     * Returns all the ID attributes in the document.
     *
     * @param targetNode the node containing the IDREF attribute
     * @param targetAttrName the name of the IDREF attribute
     */
    protected Vector getIdValues (MerlotDOMNode targetNode, String targetAttrName)
    {
    	Map idAttrs = getIDAttrs (targetNode, targetAttrName);
    	Vector ret = new Vector();
    	Iterator e = idAttrs.keySet().iterator();
    	if (e != null) {
    	    while (e.hasNext())
    	    {
    	    	Attr attr = (Attr) e.next();
    		ret.add(attr.getValue());
    	    }
    	}
	return ret;
    }

    /**
     * Returns a default string ID attribute value for a node.
     * This methods checks if there is no interference
     * with another ID  in the document.
     *
     * @param node node containing the ID attribute
     * @param attrName name of the ID attribute
     */
    public String getDefaultIdValue (MerlotDOMNode node, String attrName)
    {
		String ret = null;
		int idType = getIdTypeSetting();
		switch ( idType )
		{
			case NONE:
			{
				return null;
			}
			case TIMESTAMP:
			{
				long currentTime = System.currentTimeMillis();
				while ( currentTime == _lastTimeAssigned )
					currentTime = System.currentTimeMillis();
				_lastTimeAssigned = currentTime;
				ret = ( new Long( currentTime ) ).toString();
				break;
			}
			
			case NODE_TIMESTAMP:
			{
				String nodeName = node.getNodeName();
				long currentTime = System.currentTimeMillis();
				while ( currentTime == _lastTimeAssigned )
					currentTime = System.currentTimeMillis();
				_lastTimeAssigned = currentTime;
				ret = nodeName + "_" + currentTime;
				break;
			}
			
			default:
			{
				int index = 0;
				String nodeName = node.getNodeName();
				if (_attrLastId.containsKey(nodeName))
					index = ((Integer)_attrLastId.get(nodeName)).intValue()+1;
				else
					_attrLastId.put(nodeName, new Integer (0));
				String value = getDefaultIdValueForIndex(node, attrName, index);
				Vector idValues = getIdValues(node, attrName);
				while (idValues.contains(value)) {
					index++;
					value = getDefaultIdValueForIndex(node,attrName,index);
				}
				_attrLastId.put(nodeName, new Integer(index));
				ret = value;	
			}
		}
		return ret;
	}

	public static final int NONE = 1;
	public static final int NODE_SMALLEST = 2;
	public static final int NODE_HIGHEST = 3;
	public static final int TIMESTAMP = 4;
	public static final int NODE_TIMESTAMP = 5;
	public static final int USERID_TIMESTAMP = 6;
	
	private int _idType = 0;
	
	public int getIdTypeSetting()
	{
		if ( _idType == 0 )
		{
			int ret = 2;
			XMLEditorSettings settings = XMLEditorSettings.getSharedInstance();
			String idType = settings.getProperty( "merlot.idmanager.idtype" );
			if ( idType == null )
				idType = "node_smallest";
			idType = idType.toLowerCase().trim();
			if ( idType.equals( "none" ) )
				ret = NONE;
			if ( idType.equals( "node_smallest" ) )
				ret = NODE_SMALLEST;
			if ( idType.equals( "node_highest" ) )
				ret = NODE_HIGHEST;
			if ( idType.equals( "timestamp" ) )
				ret = TIMESTAMP;
			if ( idType.equals( "node_timestamp" ) )
				ret = NODE_TIMESTAMP;
			if ( idType.equals( "userid_timestamp" ) )
				ret = USERID_TIMESTAMP;
			_idType = ret;
		}
		return _idType;
	}

    /**
     * Returns a default string ID attribute value for a node 
     * according to an integer index.
     *
     * @param node node containing the ID attribute
     * @param attrName name of the ID attribute
     * @param index arbitrary integer
     */
    protected String getDefaultIdValueForIndex (MerlotDOMNode node, String attrName, int index)
    {
	String nodeName = node.getNodeName();
	return (nodeName + "_" + index);
    }

	/**
	 * notifies the listener that one or more nodes were inserted under the 
	 * given parent.
	 * IDManager is added as a listener in DOMTreeTableAdapter.
	 */
	public void nodeInserted(MerlotDOMNode parent, int[] indices,
							 MerlotDOMNode[] children)
	{
        if (_disabled)
            return;
		//System.out.println( "IDManager knows that a node was inserted." );
		String test = "This parameter is never used.";
		Vector idValues = getIdValues( parent, test );
		for ( int i = 0; i < children.length; i++ )
		{
			MerlotDOMNode child = (MerlotDOMNode)children[i];
			if ( child instanceof MerlotDOMElement )
			{
				MerlotDOMElement childElement = (MerlotDOMElement)child;
				HashMap childChanges = preventDuplicateId( childElement, idValues );
				updateIdRefs( childElement, childChanges );
				logChange( childElement );
			}
		}
	}

	/**
	 * notifies the listener that one or more nodes under the given parent
	 * were deleted. Their previous indices are given.
	 */
	public void nodeRemoved(MerlotDOMNode parent, int[] indices,
							MerlotDOMNode[] children)
	{
		// Do nothing
	}
	
	public void nodeDeleted(MerlotDOMNode node)
	{
		// Do nothing.
	}
	
	public void nodeChanged(MerlotDOMNode parent, int[] indices,
							 MerlotDOMNode[] children)
	{
		MerlotDOMNode changedNode = children[0];
		logChange( changedNode );
	}
	
	public HashMap preventDuplicateId( MerlotDOMElement suspect, Vector idValues )
	{
		HashMap changes = new HashMap();
		Vector idNodes = suspect.getAttributesOfType( DTDConstants.ID );
		if ( ! idNodes.isEmpty() )
		{
			Attr idAttr = (Attr)idNodes.iterator().next();
			String currentId = idAttr.getValue();
			if ( isDuplicate( idAttr, idValues ) )
			{
				String idAttrName = idAttr.getNodeName();
				String newId = getDefaultIdValue( suspect, idAttrName );
				suspect.setAttribute( idAttrName, newId );
				changes.put( currentId, newId );
			}
		}
		Iterator children = suspect.getChildElements().iterator();
		while ( children.hasNext() )
		{
			MerlotDOMElement child = (MerlotDOMElement)children.next();
			HashMap childChanges = preventDuplicateId( child, idValues );
			changes.putAll( childChanges );
		}
		return changes;
	}
	
	public boolean isDuplicate( Attr idAttr, Vector idValues )
	{
		boolean ret = true;
		if ( idAttr != null )
		{
			String id = idAttr.getValue();
			if ( id == null || 
				 id.equals( "" ) ||
				 ( idValues.indexOf( id ) < idValues.lastIndexOf( id ) ) )
			{
				ret = true;
			}
			else
				ret = false;
		}
		if ( ret == true )
			MerlotDebug.msg( "Found duplicate id: " + idAttr.getValue() );
		return ret;
	}
	
	public void updateIdRefs( MerlotDOMElement root, HashMap changes )
	{
		HashMap idRefs = collectAttributesOfType( root, DTDConstants.IDREF );
		Set oldIds = changes.keySet();
		Iterator it = idRefs.keySet().iterator();
		while ( it.hasNext() )
		{
			Attr idRef = (Attr)it.next();
			String refersTo = idRef.getValue();
			if ( oldIds.contains( refersTo ) )
			{
				String attrName = (String)idRef.getNodeName();
				String newValue = (String)changes.get( refersTo );
				MerlotDOMElement node = (MerlotDOMElement)idRefs.get( idRef );
				node.setAttribute( attrName, newValue );
			}
		}
	}
	
	public HashMap collectAttributesOfType( MerlotDOMElement start, int dtdConstant )
	{
		HashMap ret = new HashMap();
		Vector attributes = start.getAttributesOfType( dtdConstant );
		Iterator attrIt = attributes.iterator();
		while ( attrIt.hasNext() )
		{
			Attr attr = (Attr)attrIt.next();
			ret.put( attr, start );
		}
		Vector children = start.getChildElements();
		Iterator it = children.iterator();
		while ( it.hasNext() )
		{
			MerlotDOMElement child = (MerlotDOMElement)it.next();
			ret.putAll( collectAttributesOfType( child, dtdConstant ) );
		}
		return ret;
	}
	
	private boolean _isLogOnSet = false;
	private boolean _logOn = false;
	
	public boolean getLogLastModifiedSetting()
	{
		if ( ! _isLogOnSet )
		{
			XMLEditorSettings settings = XMLEditorSettings.getSharedInstance();
			String logOn = settings.getProperty( "merlot.idmanager.log-last-modified" );
			logOn = logOn.toLowerCase().trim();
			if ( logOn.equals( "true" ) )
				_logOn = true;
		}
		return _logOn;
	}
	
	private String _logAttribute = null;
	
	public String getLogAttribute()
	{
		if ( _logAttribute == null )
		{
			_logAttribute = "last-modified";
			XMLEditorSettings settings = XMLEditorSettings.getSharedInstance();
			String logAttribute = settings.getProperty( "merlot.idmanager.last-modified-attribute" );
			if ( logAttribute != null )
			{
				logAttribute = _logAttribute.toLowerCase().trim();
				_logAttribute = logAttribute;
			}
		}
		return _logAttribute;
	}

    public void setDisabled(boolean disable) {
        _disabled = disable;
    }
	
	public void logChange( MerlotDOMNode node )
	{
		boolean logOn = getLogLastModifiedSetting();
		if ( logOn )
		{
			// Setting the attribute on the real node and not the MerlotDOMNode,
			// to prevent another change event to be fired.
			Element realNode = (Element)node.getRealNode();
			String logAttribute = getLogAttribute();
			String time = ( new Long( System.currentTimeMillis() ) ).toString();
			String nodeId = node.getProperty( new Integer( DTDConstants.ID ) );
			MerlotDebug.msg( "Logging change to node " 
			+ nodeId + ": " + logAttribute + ": " + time );
			realNode.setAttribute( logAttribute, time );
		}
	}
}
