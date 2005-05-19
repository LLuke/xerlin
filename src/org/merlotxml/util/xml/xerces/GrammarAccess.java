package org.merlotxml.util.xml.xerces;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.validators.common.*;
import org.apache.xerces.validators.dtd.DTDGrammar;
import org.apache.xerces.validators.schema.SubstitutionGroupComparator;
import org.apache.xerces.utils.*;
import org.apache.xerces.framework.*;
import java.io.*;
import org.w3c.dom.*;
import java.util.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.apache.xerces.utils.QName;

import java.net.URL;
import org.merlotxml.merlot.plugin.*;

/**
 * This class extends DOMParser in order to get access to protected members. It
 * is an ugly way to do it, but I couldn't find a way to do it through the
 * public interfaces. 
 * The consequence of this is that we need a document to parse - a DTD or Schema
 * file on it's own cannot be used. In order to get around this in the case
 * where grammar is required for a new document, a temporary document is created
 * which is then parsed.
 * 
 * @author Evert Hoff
 * @version 
 */

public class GrammarAccess extends DOMParser
{
	// Temporary measure to get around a bug in Xerces.. See convertName and
	// getGrammar
	private boolean isDTD = true;
    protected String _pluginUri;
    protected String _systemUri;
	
	/**
	 * @param uri A URI to either a XML document, a XML Schema file, or a DTD
	 * file. Currently only file URIs work.
	 */
	public GrammarAccess( String uri )
	{
		super();
		// Ideally, we should get a URI of the document, but if we get a URI of
		// only a DTD or a Schema, then a temporary document is created  with a
		// link to the DTD or Schema.
		int dot = uri.lastIndexOf( "." );
		String ext = uri.substring( dot + 1 ).toLowerCase();
		System.out.println( "URI extension: " + ext );
		if ( ext.equals( "dtd" ) )
			initWithDTD( uri );
		if ( ext.equals( "xsd" ) )
			initWithSchema( uri );
		if ( ext.equals( "xml" ) )
			initWithDocument( uri );
	}

    public GrammarAccess( String pluginUri, String systemUri )
    {
        super();
        // Ideally, we should get a URI of the document, but if we get a URI of
        // only a DTD or a Schema, then a temporary document is created  with a
        // link to the DTD or Schema.
        _pluginUri = pluginUri;
        _systemUri = systemUri;
        String uri = systemUri;
        int dot = uri.lastIndexOf( "." );
        String ext = uri.substring( dot + 1 ).toLowerCase();
        System.out.println( "URI extension: " + ext );
        if ( ext.equals( "dtd" ) )
            initWithDTD( uri );
        if ( ext.equals( "xsd" ) )
            initWithSchema( uri );
        if ( ext.equals( "xml" ) )
            initWithDocument( uri );
    }

	
	public void initWithDocument( String uri )
	{
		try { setFeature("http://apache.org/xml/features/domx/grammar-access", true); }
        catch (Exception e) { System.out.println("warning: unable to set feature."); }
		try
		{
	    	setValidation( true );

            if (uri.equals("GrammarAccess_temp.xml")) {
				if (_pluginUri != null) 
					setEntityResolver(new MyEntityResolver(_pluginUri));
			    else
                    setEntityResolver(new MyEntityResolver(_systemUri));    
			}
			
			parse( uri );
		}
		catch ( Exception ex )
		{
			System.out.println( "Could not parse document " + uri );
			System.out.println( "Exception: " + ex );
			ex.printStackTrace();
		}
	}

    public void initWithDocument(StringBuffer buf)
    {
System.out.println("Using document...");
        try { setFeature("http://apache.org/xml/features/domx/grammar-access", true); }
        catch (Exception e) { System.out.println("warning: unable to set feature."); }
        try
        {
            setValidation( true );

            if (_pluginUri != null)
                setEntityResolver(new MyEntityResolver(_pluginUri));
            else
                setEntityResolver(new MyEntityResolver(_systemUri));
          
            ByteArrayInputStream stream = 
                            new ByteArrayInputStream(buf.toString().getBytes());
            InputSource source = new InputSource(stream);
             
     
            parse(source);
        }
        catch ( Exception ex )
        {
            System.out.println( "Could not parse document ");
            System.out.println( "Exception: " + ex );
            ex.printStackTrace();
        }
    }
	
	public void initWithDTD( String uri )
	{
		int slash = uri.lastIndexOf( "/" );
		int backslash = uri.lastIndexOf( "\\" );
		int nameStart = Math.max( slash, backslash );
		nameStart ++; 	// The first character after the slash or zero if no
						// slash or backslash
		int dot = uri.lastIndexOf( "." );
		// Using the filename as the root element of the document. It is an
		// assumption that the root element will be the same as the filename. I
		// suspect that it might work no matter what we put in here as long as
		// the file has a root element.
		String name = uri.substring( nameStart, dot );
        StringBuffer buffer = new StringBuffer();
		buffer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		buffer.append( "<!DOCTYPE " + name + " SYSTEM \"" + uri + "\">" );
		buffer.append( "<" + name + "/>" );
		initWithDocument(buffer);
	}
	
	public void initWithSchema( String uri )
	{
		int slash = uri.lastIndexOf( "/" );
		int backslash = uri.lastIndexOf( "\\" );
		int nameStart = Math.max( slash, backslash );
		nameStart ++; 	// The first character after the slash or zero if no
						// slash or backslash
		int dot = uri.lastIndexOf( "." );
		String name = uri.substring( nameStart, dot );
        StringBuffer buffer = new StringBuffer();
		buffer.append( "<" + name + " xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\"" );
	   	buffer.append( " xsi:noNamespaceSchemaLocation='" + uri + "'>" );
	    buffer.append( "</" + name + ">" );
		initWithDocument(buffer);
	}

	/**
	 * @return An array with the node names of all allowed elements in the
	 * document
	 */
	public String[] getElements()
	{
		HashSet set = new HashSet();
		XMLElementDecl[] decls = getElementDecls();
        if (decls==null)
            return null;
		for ( int i = 0; i < decls.length; i++ )
		{
			XMLElementDecl decl = decls[i];
            if (decl!=null) {
    			QName qName = decl.name;
	    		String name = getStringPool().toString( qName.rawname );
		    	set.add( name );
            }
		}
		String[] ret = new String[1];
		ret = (String[])set.toArray( ret );
		return ret;
	}
	
	/**
	 * @param elementName The node name of an element
	 * @return An array with the names of all allowed attributes for this element
	 */
	public String[] getAttributeNames( String elementName )
	{
		Vector v = new Vector();
		XMLAttributeDecl[] attributes = getAttributeDecls( elementName );
		for ( int i = 0; i < attributes.length; i++ )
		{
			XMLAttributeDecl attribute = attributes[i];
			QName qName = attribute.name;
			String name = getStringPool().toString( qName.rawname );
			//System.out.println( "Attribute: " + name );
			if ( name != null )
				v.add( name );
		}
		String[] ret = new String[0];
		ret = (String[])v.toArray( ret );
		return ret;
	}
	
	/**
	 * @param elementName The node name of an element
	 */
	public XMLElementDecl getElementDecl( String elementName )
	{
		XMLElementDecl ret = null;
		XMLElementDecl[] decls = getElementDecls();
		for ( int i = 0; i < decls.length; i++ )
		{
			XMLElementDecl decl = decls[i];
			QName qName = decl.name;
			String name = getStringPool().toString( qName.rawname );
			//System.out.println( "Checking if " + elementName + " = " + name );
			if ( name.equals( elementName ) )
			{
				ret = decl;
				break;
			}
		}
		if ( ret == null )
			System.out.println( "Could not find element decl for " + elementName );
		return ret;
	}
	
	/**
	 * @param elementName The node name of an element
	 * @param elementName The name of an attribute of the above element
	 */
	public XMLAttributeDecl getAttributeDecl( String element, String attribute )
	{
		XMLAttributeDecl[] decls = getAttributeDecls( element );
		for ( int i = 0; i < decls.length; i++ )
		{
			XMLAttributeDecl decl = decls[i];
			QName qName = decl.name;
			String name = getStringPool().toString( qName.rawname );
			if ( name.equals( attribute ) )
				return decl;
		}
		return null;
	}
	
	/**
	 * Finds the node names of elements that may be inserted at the specified
	 * position, considering current children
	 * @param parentElement The node name of the element under which a new
	 * element is to be inserted
	 * @param currentChildElements The node names of current children. If there
	 * are more than one child with the same node name, then duplicates must be
	 * given.
	 * @param insertPosition Zero-based position where a new child is to be
	 * inserted.
	 * @return An array with the node names of alternative valid elements that may go
	 * into the specified position.
	 */
	public String[] whatCanGoHere( String parentElement, 
	 String[] currentChildElements, int insertPosition )
	{
		Vector v = new Vector();
		//System.out.println( "Adding currentChildElements..." );
		int max = Math.max( currentChildElements.length-1, insertPosition );
		for ( int i = 0; i <= max; i++ )
		{
			// An empty space must be provided at the insertion point
			if ( i == insertPosition || i >= currentChildElements.length )
				v.add( convertName( "" ) );
			if ( i < currentChildElements.length )
			{
				String current = currentChildElements[i];
				QName qName = convertName( current );
				// Check if there is such a name in the StringPool, else just ignore
				// the listed child. REVISIT: Throw an exception.
				if ( qName != null )
				{
					v.add( qName );
				}
				else
				{
					System.out.println( "Name not found: " + current );
				}
			}
		}
		// Make sure there is always at least one empty slot
		v.add( convertName( "" ) );
		QName[] curChildren = new QName[0];
		curChildren = (QName[])v.toArray( curChildren );
		//for ( int i = 0; i < curChildren.length; i++ )
		//	System.out.println( " curChild: " + curChildren[i] );
		InsertableElementsInfo info = new InsertableElementsInfo();
		info.canHoldPCData = false;
		// Don't count the empty one that was added
		info.childCount = curChildren.length-1;
		//System.out.println( "info.childCount: " + info.childCount );
		boolean fullyValid = true;
		if ( info.childCount <= 1 )
			fullyValid = false;
		info.curChildren = curChildren;
		info.isValidEOC = true;
		info.insertAt = insertPosition;
		//System.out.println( "info.insertAt: " + info.insertAt );
		info.possibleChildren = new QName[0];
		info.resultsCount = 0;
		info.results = new boolean[1];
		XMLContentModel model = getElementContentModel( parentElement );
        
        // There seems to be a bug here whereby when the childCount is > 1
        // the MixedContent model will flag all of the valid children as being
        // invalid
        // The disadavantage of doing this is that the current children are
        // never validated - but this happens else where anyway
        if (model instanceof MixedContentModel) {
                info.childCount=0;
        }

        
		// This is to get around a CMException that is thrown when the element
		// may not contain children. REVISIT
		if ( model == null )
		{
			//System.out.println( "Could not get insertable elements, because "
			//+ "XMLContentModel is null for " + parentElement );
			// This is not a problem. This happens when an element's contents is
			// declared as EMPTY.
			return new String[0];
		}
		try
		{
			int result;
            // DFA workaround to stop Merlot from insisting that the element
            // must be valid after the add - instead we return all allowable
            // states
            if (model instanceof DFAContentModel) { 
                result = model.whatCanGoHere( false, info );
                if (fullyValid) {
                    for (int index = 0; index < info.resultsCount; index++) {
                    // Don't need to consider this one since its not insertable
                    if (!info.results[index])
                        continue;

                    // Stick this element into the insert at spot
                    info.curChildren[info.insertAt] = info.possibleChildren[index];

                    // And validate it. If it fails, then this one loses
                    int valid = model.validateContent(info.curChildren, 0, info.childCount);
                    if (valid != -1 && valid!=info.childCount)
                        info.results[index] = false;
                    }
                }
            } else {
                result = model.whatCanGoHere( fullyValid, info );
            }
			if ( result != -1 )
				System.out.println( "Problem at position " + result );
		}
		catch ( Exception ex )
		{
			System.out.println( "Exception in whatCanGoHere: " + ex );
			ex.printStackTrace();
		}
		v = new Vector();
		for ( int i = 0; i < info.possibleChildren.length; i++ )
		{
			QName qName = info.possibleChildren[i];
			String name = null;
			if ( qName != null )
				name = getStringPool().toString( qName.rawname );
			//System.out.println( "Possible child: " + name + ": " 
			//+ info.results[i] );
			// REVISIT: Name should not be null.
			if ( name != null && info.results[i] == true )
				v.add( name );
		}
		boolean canHoldPCData = info.canHoldPCData;
		if ( canHoldPCData )
			v.add( "#text" );
		//System.out.println( "canHoldPCData: " + canHoldPCData );
		//System.out.println( "isValidEOC: " + info.isValidEOC );
		String[] ret = new String[0];
		ret = (String[])v.toArray( ret );
		//System.out.println( "canGoHere: " + ret );
		//for ( int i = 0; i < ret.length; i++ )
		//	System.out.println( "CanGoHere: " + ret[i] );
		return ret;
	}
	
	/**
	 * Checks whether each of the current children is allowed in their current
	 * position.
	 * @param elementName The parent element node name.
	 * @param children The node names of current elements. Duplicates must be
	 * provided, where necessary
	 */
	public boolean validateContent( String elementName, String[] children )
	{
		//System.out.println( "Validating content for " + elementName );
		//System.out.println( "Children: " );
		//for ( int ii = 0; ii < children.length; ii++ )
		//	System.out.println( " " + children[ii] );
		// REVISIT:
		// This is a temp workaround, due to a bug when there are no children.
		if ( children.length == 0 )
		{
			//System.out.println( " Valid because there are no children." );
			return true;
		}
		XMLContentModel model = getElementContentModel( elementName );
		// This is a workaround in cases where the parent may not contain any
		// children. REVISIT
		if ( model == null )
		{
			//System.out.println( " Valid because cannot have children." );
			return true;
		}
		Vector v = new Vector();
		for ( int i = 0; i < children.length; i++ )
		{
			String child = children[i];
			// Must provide -1 for a PCDATA node
			/*
			if ( isPCDATA( child ) )
			{
				QName qName = new QName( -1, -1, -1, 0 );
				v.add( qName );
				//System.out.println( "Adding QName: " + qName );
			}
			else
			{
			*/
				QName qName = convertName( child );
				// REVISIT: Throw an exception if the child node name could not
				// be found
				if ( qName != null )
				{
					v.add( qName );
					//System.out.println( "Adding QName: " + qName );
				}
			//}
		}
		QName[] childrenSpec = new QName[0];
		childrenSpec = (QName[])v.toArray( childrenSpec );
		// Not sure if it is still necessary to do this
		if ( childrenSpec == null )
		{
			childrenSpec = new QName[0];
			QName qName = new QName( -1, -1, -1, 0 );
		}
		//System.out.println( "Length: " + childrenSpec.length );
		//System.out.println( "Validating childrenspec: " );
		//for ( int i = 0; i < childrenSpec.length; i++ )
			//System.out.println( " QName: " + childrenSpec[i] );
		int result = -1;
		try
		{
			result = model.validateContent( childrenSpec, 0, childrenSpec.length );
		}
		catch ( Exception ex )
		{
			System.out.println( "Exception validating content: " + ex );
			//ex.printStackTrace();
			return false;
		}
		boolean ret = true;
		if ( result > -1 )
		{
			ret = false;
			System.out.println( "Validate content: element=" + elementName
			+ " children=" + childrenSpec + ". Problem child at position "
			+ result );
		}
		//System.out.println( " Valid? " + ret );
		return ret;
	}

	/**
	 * Gets the enumeration for an attribute of type NMTOKENS, etc.
	 * @param element The node name of the element
	 * @param attribute The name of the attribute that has an enumeration
	 * @return An array of strings with the literals of the enumeration
	 */
	public String[] getEnumeration( String element, String attribute )
	{
		XMLAttributeDecl decl = getAttributeDecl( element, attribute );
		int enumeration = decl.enumeration;
		if ( enumeration < 0 )
			return null;
		StringPool stringPool = getStringPool();
		Vector v = new Vector();
		int[] stringList = stringPool.stringListAsIntArray( enumeration );
		for ( int i = 0; i < stringList.length; i++ )
		{
			int stringInt = stringList[i];
			String literal = stringPool.toString( stringInt );
			v.add( literal );
		}
		String[] ret = new String[1];
		ret = (String[])v.toArray( ret );
		return ret;
	}

    public GrammarResolver getGrammarResolver()
    {
		return fGrammarResolver;    
    }
	
	public StringPool getStringPool()
	{
		return fStringPool;
	}
	
	public Grammar getGrammar()
	{
	    GrammarResolver resolver = getGrammarResolver();
		if ( resolver == null )
		{
			System.out.println( "GrammarResolver is null." );
			return null;
		}
		// REVISIT: There might be more than one grammar in some cases with
		// other namespaces. The default namespace seems to be an empty String
	    Grammar grammar = resolver.getGrammar( "" );
		if ( grammar == null )
			System.out.println( "Grammar is null." );
		if ( ! ( grammar instanceof DTDGrammar ) )
			isDTD = false;
		return grammar;
	}
	
	public XMLElementDecl[] getElementDecls()
	{
		HashSet set = new HashSet();
		Grammar grammar = getGrammar();
        if (grammar==null)
            return null;
		boolean found = true;
		int count = 0;
		// The length of the element declaration is not public or protected, so
		// we just continue while we can find more
		while ( found )
		{
			XMLElementDecl decl = new XMLElementDecl();
			found = grammar.getElementDecl( count, decl );
			if ( found )
				set.add( decl );
			count++;
		}
		XMLElementDecl[] ret = new XMLElementDecl[1];
		ret = (XMLElementDecl[])set.toArray( ret );
		return ret;
	}
	
	public int getElementDeclIndex( String elementName )
	{
		StringPool stringPool = getStringPool();
		// Adding a symbol returns an existing symbol if the name
		// already exists
		int element = stringPool.addSymbol( elementName );
		//System.out.println( "Element " + elementName + " in stringPool "
		//+ element );
		Grammar grammar = getGrammar();
		// -1 is the toplevel scope. This might be wrong but seems to work
		int elementIndex = grammar.getElementDeclIndex( element, -1 );
		return elementIndex;
	}
	
	public XMLContentModel getElementContentModel( String elementName )
	{
		XMLContentModel ret = null;
		try
		{
			int elementDeclIndex = getElementDeclIndex( elementName );
			Grammar grammar = getGrammar();
			Locator locator = getLocator();
			if ( locator == null )
				System.out.println( "Locator is null." );
			GrammarAccessErrorReporter reporter 
			 = new GrammarAccessErrorReporter( locator );
	    	GrammarResolver resolver = getGrammarResolver();
			StringPool stringPool = getStringPool();
			SubstitutionGroupComparator comparator 
			 = new SubstitutionGroupComparator( resolver, stringPool, reporter);
			ret = grammar.getElementContentModel( elementDeclIndex, comparator );
		}
		catch ( Exception ex )
		{
            // Don't print the debug if the content type is EMPTY
            if (getContentSpec(elementName)!=null)
			    System.out.println( "Exception getting ElementContentModel " +
                    "for element: "+elementName);
		}
		return ret;
	}
	
	public XMLAttributeDecl[] getAttributeDecls( String elementName )
	{
		Vector v = new Vector();
		int elementDeclIndex = getElementDeclIndex( elementName );
		Grammar grammar = getGrammar();
		int firstAttDeclIndex = grammar.getFirstAttributeDeclIndex(elementDeclIndex);
		XMLAttributeDecl attributeDecl = new XMLAttributeDecl();
		boolean found = grammar.getAttributeDecl(firstAttDeclIndex, attributeDecl);
		v.add( attributeDecl );
		int last = firstAttDeclIndex;
		while ( last > -1 )
		{
			int next = grammar.getNextAttributeDeclIndex( last );
			attributeDecl = new XMLAttributeDecl();
			found = grammar.getAttributeDecl( next, attributeDecl );
			if ( found )
				v.add( attributeDecl );
			last = next;
		}
		XMLAttributeDecl[] ret = new XMLAttributeDecl[0];
		ret = (XMLAttributeDecl[])v.toArray( ret );
		return ret;
	}
	
	public QName convertName( String fullName )
	{
		if ( fullName == null )
			return null;
		StringPool stringPool = getStringPool();
		int prefix = stringPool.addSymbol( "" );
		int name = stringPool.addSymbol( fullName );
		int uri = -1;
		// REVISIT: This is to get around a bug in Xerces. The correct value
		// should be 0 and not -1.
		if ( ! isDTD )
			uri = 0;
		QName qName = new QName( prefix, name, name, uri );
		return qName;
	}
	
	public XMLContentSpec getContentSpec( String elementName )
	{
		XMLElementDecl decl = getElementDecl( elementName );
		if ( decl == null )
			return null;
		int index = decl.contentSpecIndex;
		XMLContentSpec spec = new XMLContentSpec();
		Grammar grammar = getGrammar();
		//System.out.println( "Looking for contentSpec with index " + index );
		boolean found = grammar.getContentSpec( index, spec );
		if ( ! found )
		{
			spec = null;
			// This means the element is EMPTY, ie. cannot have children. Not
			// a problem.
			//System.out.println( "Could not find contentSpec for " + elementName );
		}
		return spec;
	}
	
	public int getNodeType( String elementName )
	{
		XMLContentSpec spec = getContentSpec( elementName );
		// This is not the right place to get the type from, but we
		// can determine if it is an element or PCDATA, and that is all
		// we need for now.
		int type = -2;
		int specType = spec.type;
		if ( specType == 0 && spec.value == -1 )
			type = Node.TEXT_NODE;
		else
			type = Node.ELEMENT_NODE;
		return type;
	}
	
	public boolean isPCDATA( String elementName )
	{
		if ( elementName == null )
			return false;
		if ( elementName.equals( "#text" ) )
			return true;
		boolean ret = false;
		XMLContentSpec spec = getContentSpec( elementName );
		if ( spec == null )
		{
			//System.out.println( "Could not find XMLContentSpec for " + elementName );
			// This means that the element is EMPTY, ie. cannot have children
			return false;
		}
		int type = spec.type;
		if ( type == XMLContentSpec.CONTENTSPECNODE_LEAF )
		{
			if ( spec.value == -1 )
				ret = true;
		}
		return ret;
	}
}
	
class MyErrorHandler implements ErrorHandler
{
    public void warning (SAXParseException exception)
	throws SAXException
    {
	System.out.println( "Warning: " + exception );
    }
    public void error (SAXParseException exception)
	throws SAXException
    {
	System.out.println( "Error: " + exception );
    }
    
    public void fatalError (SAXParseException exception)
	throws SAXException
    {
	System.out.println( "Fatal: " + exception );
    }
    
}

class GrammarAccessErrorReporter implements XMLErrorReporter
{
    private Locator _locator;

    public GrammarAccessErrorReporter( Locator locator )
    {
        _locator = locator;
    }

    public Locator getLocator()
    {
        return _locator;
    }

    public void reportError(Locator locator,
                            String errorDomain,
                            int majorCode,
                            int minorCode,
                            Object args[],
                            int errorType) throws Exception
    {
        System.out.println( "[GrammarAccess] Error at line "
        + locator.getLineNumber() + " column " + locator.getColumnNumber()
        + " errorDomain=" + errorDomain + " majorCode=" + majorCode
        + " minorCode=" + minorCode + " args=" + args + " errorType="
        + errorType );
    }
}

/**
 * Added to load the DTD from a source other than SYSTEM 
 * 
 */ 
class MyEntityResolver implements EntityResolver
{
    String _uri = null;

    public MyEntityResolver(String pluginUri)
    {
        System.out.println("Setting _uri to "+pluginUri);
        _uri=pluginUri;
    }

    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException
    {
        //System.out.println( "Resolving entity: publicId: " + publicId
        //    + " systemId: " + systemId + " at "+_uri);
        InputStream is = null;

        // If we have got here via a plugin get the dtd from the plugin
        PluginClassLoader pcl;
        PluginManager pm = PluginManager.getInstance();
        URL url=null;

        // Try to load the dtd from the plugin
        if (pm.getCurrentFilePath()!=null) {
            File tryPlugin = new File(pm.getCurrentFilePath());
            pcl = new PluginClassLoader(tryPlugin);
            url = pcl.findResource(_uri);
            //System.out.println("In plugin looking for ... "+_uri+" in "+tryPlugin.getName());
            //System.out.println("Found "+url);

            if (url!=null) {
                is = url.openStream();
            }
        } 

        if (is!=null)
            return new InputSource(is);

        return null;
    }
}
