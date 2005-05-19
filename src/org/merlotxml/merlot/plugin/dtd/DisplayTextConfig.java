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
Merlot XML Editor (http://www.merlotxml.org/)."
 
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
(http://www.merlotxml.org/)."

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
http://www.merlotxml.org/.
*/

package org.merlotxml.merlot.plugin.dtd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.XPathUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * @author Evert Hoff (evert@openminds.co.za)
 */
public class DisplayTextConfig 
{
	
	//Constants
	protected static final String XPATH_DEFAULT_ORDER = "//default-property-order/*/@name";
	protected static final String XPATH_FIXED_PROPERTIES = "//fixed-property";
	
	// Attributes
	protected java.util.List defaultOrder;
	protected Map nodeProperties;

	// Operations
	
	public DisplayTextConfig() 
	{
	}
	
	public void parse(Node node) throws SAXException
	{
		Object property;
		
		defaultOrder = new Vector();
		java.util.List list = (List)XPathUtil.getValueList( node, XPATH_DEFAULT_ORDER );
		Iterator it = list.iterator();
		while ( it.hasNext() )
		{
			property = it.next();
			MerlotDebug.msg( "Default display property: " + property );
			property = castProperty( property );
			defaultOrder.add( property );
		}
		
		nodeProperties = new HashMap();
		NodeList fixedProperties = XPathUtil.selectNodes(node, XPATH_FIXED_PROPERTIES);
		for ( int i=0; i<fixedProperties.getLength(); i++ )
		{
			Node fixed = (Node)fixedProperties.item( i );
			if ( fixed != null )
			{
				String element = XPathUtil.getValue( fixed, "@element-name" );
				property = XPathUtil.getValue( fixed, "*/@name" );
				if ( element != null && property != null )
				{	
					property = castProperty( property );
					nodeProperties.put( element, property );
					MerlotDebug.msg( "Fixed element: " + element + "; property: "
					+ property );
				}
			}
		}
	}
	
	public Object castProperty( Object property )
	{
		if ( property.equals( "TOKEN_GROUP" ) )
			property = new Integer( DTDConstants.TOKEN_GROUP );
		if ( property.equals( "NMTOKEN" ) )
			property = new Integer( DTDConstants.NMTOKEN );
		if ( property.equals( "ID" ) )
			property = new Integer( DTDConstants.ID );
		if ( property.equals( "IDREF" ) )
			property = new Integer( DTDConstants.IDREF );
		if ( property.equals( "CDATA" ) )
			property = new Integer( DTDConstants.CDATA );
		if ( property.equals( "PCDATA" ) )
			property = new Integer( DTDConstants.PCDATA );
		return property;
	}
	
	public List getDefaultOrder()
	{
		return defaultOrder;
	}
	
	public Map getNodeProperties()
	{
		return nodeProperties;
	}

}
