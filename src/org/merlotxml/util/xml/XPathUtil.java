package org.merlotxml.util.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * General XPath utilities
 * 
 * @author Tim McCune (with thanks to Scott Boag for providing the initial example)
 */
public class XPathUtil {
	
	/**
	 * Select a list of nodes using an XPath expression
	 * @param contextNode The node to start searching from.
	 * @param str A valid XPath string.
	 * @return The list of nodes that match the XPath, or null if none
	 */
	public static NodeList selectNodes(Node contextNode, String str)
		throws SAXException
	{
		NodeList ret = null;
		try
		{
			ret = XPathAPI.selectNodeList( contextNode, str );
		}
		catch (TransformerException ex )
		{
            transformerExceptionMsg(ex, contextNode, str);
		}
		return ret;
	}
	
	/**
	 * Select a single node using an XPath expression
	 * @param contextNode The node to start searching from.
	 * @param str A valid XPath string.
	 * @return The first node found that matches the XPath, or null if none
	 */
	public static Node selectSingleNode(Node contextNode, String str)
		throws SAXException
	{
        try {
            return XPathAPI.selectSingleNode(contextNode, str);
        } catch (TransformerException ex) {
            transformerExceptionMsg(ex, contextNode, str);
        }
        return null;
	}
	
	public static String getValue(Node contextNode, String str) throws SAXException 
	{
		Node match = selectSingleNode(contextNode, str);
		if (match != null) {
			return match.getNodeValue().trim();
		}
		else {
			return null;
		}
		
	}
	
	public static List getValueList(Node contextNode, String xpath) throws SAXException 
	{
		int i;
		List rtn = new ArrayList();
		NodeList nl;
		
		nl = selectNodes(contextNode, xpath);
		for (i = 0; i < nl.getLength(); i++) {
			rtn.add(nl.item(i).getNodeValue().trim());
		}
		return rtn;	
	}
	
	/*
	 * Finds the first variable name contained in the xPathString. Returns null
	 * if not found. The $ symbol is not included.
	 * For example:
	 * For xPathString "//myelement[attribute::id=$myvariable]", it returns
	 * "myvariable".
	 */
	public static String parseVariable( Node contextNode, String xPathString )
	{
		String varName = null;
		return varName;
	}
	
	public static XObject eval( Node contextNode, String str )
	{
		XObject ret = null;
		try
		{
			ret = XPathAPI.eval( contextNode, str );
		}
		catch (TransformerException ex )
		{
            transformerExceptionMsg(ex, contextNode, str);
		}
		return ret;
	}
	
	public static String selectString(Node contextNode, String str)
		throws SAXException
	{
		XObject x = eval( contextNode, str );
		if ( x instanceof XString )
			return x.toString();
		return null;
	}
	
	public static Element getNodeById( Node context, String id )
	{
		Element ret = null;
		String xPath = "id('" + id + "')";
		try
		{
			ret = (Element)selectSingleNode( context, xPath );
		}
		catch ( SAXException ex )
		{}
		/*
		if ( ret == null )
			System.out.println( "Could not find node by id: '" + id + "'" );
		else
			System.out.println( "did find node by id" );
		*/
		return ret;
	}
	
	public static String nodeToString( Node node )
	{
		String ret = "<null/>";
		if ( node != null )
		{
			ret = "<" + node.getNodeName() + " ";
			NamedNodeMap attrs = node.getAttributes();
			if ( attrs != null )
			{
				for ( int i = 0; i<attrs.getLength(); i++ )
				{
					Node attr = (Node)attrs.item( i );
					ret = ret + attr.getNodeName() + "='";
					ret = ret + attr.getNodeValue() + "' ";
				}
			}
			ret = ret + "/>";
		}
		return ret;
	}

    private static void transformerExceptionMsg(TransformerException ex,
                                                        Node contextNode, String str) {
        System.out.println( "[XPathUtil] Transformer exception selecting "
            + "XObject for " + "XPath String " + str + " with context node "
            + nodeToString( contextNode ) + ": " + ex );
    }
}

