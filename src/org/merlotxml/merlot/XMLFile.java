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
http://www.merlotxml.org.
*/


// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.awt.*;

import java.beans.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import org.w3c.dom.*;

import org.merlotxml.util.xml.*;
import org.merlotxml.util.*;
import org.merlotxml.util.xml.xerces.DOMLiaison;


/**
 * 
 * An XML file. This provides an internface into a particular XML file,
 * including its dtd and its file location. It provides methods for 
 * loading and parsing a file, saving a file, and accessing the content
 * model in the dtd.
 *
 * 
 * @author Kelly A. Campbell
 *
 * @version $Id: XMLFile.java,v 1.17 2002/09/23 23:29:28 justin Exp $
 *
 */
public class XMLFile
	implements MerlotConstants

{
    /**
     * The parsed DOM document with validation
     */
    protected ValidDocument _doc = null;
	
    /**
     * The document type (dtd)
     */
    protected DocumentType _docType = null;
	
    /**
     * The file on the filesystem
     */
    protected File      _file = null;

    /**
     * Status holder for marking the file as needing a save
     */
    protected boolean   _dirty = false;
    /**
     * Status marker for brand new files so we can call saveas instead of save
     */
    protected boolean   _new  = false;
	
    /**
     * property change delegate
     */
    protected PropertyChangeSupport _propchange;
	
	/**
	 * A cache of MerlotDOMNodes that have already been instanciated
	 */
	private Hashtable _instanciatedNodes = new Hashtable();

    /**
     * Indent output
     */
    private static boolean INDENT = XMLEditorSettings.getSharedInstance().
                            getProperty("xerlin.indent.output").equals("true");

    
    /**
     * Reads in the given filename to create the Document tree
     */
    
    public XMLFile (File f) 
	throws MerlotException
    {
	
	_file = f;
	
	_propchange = new PropertyChangeSupport(this);
	
	// now parse the file and get a Document
	parseDocument();
	
    }
    
    /**
     * creates a new file with a blank Document tree
     */
    public XMLFile () 
	throws MerlotException
    {
	
	_propchange = new PropertyChangeSupport(this);
	
	_doc = XMLEditor.getSharedInstance().getDOMLiaison().createValidDocument();
    }
    
    /**
     * Returns the DOM document for this file
     */
    public Document getDocument() 
    {
	return _doc.getDocument();
    }
    
    /**
     * Returns the DOMLiaison ValidDocument wrapper for this file
     */
    public ValidDocument getValidDocument() 
    {
	return _doc;
    }
    
    /**
     * Returns the main DTDDocument for this file
     */
    public DTDDocument getDTD(String name) 
    {
	return _doc.getDTDDocument(name);
    }
    
    /**
     * returns the DTDCacheEntry for this document. Useful to get access to the
     * DTD plugin associated with this file
     */
    public DTDCacheEntry getDTDCacheEntry() 
    {
	return _doc.getDTDCacheEntry();
    }
    
    /**
     * Sets the new property
     */
    public void setNew(boolean tf) 
    {
	_new = tf;
    }
    
    /**
     * returns the new property
     */
    public boolean isNew() 
    {
	return _new;
    }
    
    /*
    /* *
     * sets this to the given document replacing the previous one
     * /
    public void setDocument(Document doc) 
    {
	//XXX	_doc = doc;
		// update this to handle setDocument for the new valid parser liaison
    }
    */

    public DocumentType getDoctype() 
    {
	return _docType;
    }
	
    
    protected void parseDocument() 
	throws MerlotException
    {

	try {
	    InputStream fis = FileUtil.getInputStream(_file, this.getClass());
	    // get a DOMLiaison from the settings and parse the given file
	    ValidDOMLiaison domlia = XMLEditor.getSharedInstance().getDOMLiaison();
	    if (domlia != null) {
		_doc = domlia.parseValidXMLStream(fis,_file.getCanonicalPath());
	    }
	    if (_doc != null || _doc.getDocument() == null) {
		_docType = _doc.getDocument().getDoctype();
	    }
	    else {
		throw new MerlotException(MerlotResource.getString(ERR, "xml.file.open.nodocument"));
	    }
				
	}
	catch (FileNotFoundException fnf) {
	    MerlotDebug.exception(fnf);
	    throw new MerlotException("File not found: "+ _file, fnf);
	}
	catch (IOException ioex) {
	    MerlotDebug.exception(ioex);
	    throw new MerlotException("IOException: "+ _file, ioex);
	}
	
	catch (DOMLiaisonImplException dle) {
	    Exception blah = dle.getRealException();
	    if (blah != null) {
		MerlotDebug.msg("dle.msessage = "+dle.getMessage());
			
		MerlotDebug.exception(dle);
	    }
	    else {
		MerlotDebug.msg("wrapper exception with a null real exception");
	    }
			
	    throw new MerlotException("Parse error: "+dle.getMessage(), dle);
	}

		
		
    }


    public void printRawXML (OutputStream s, boolean pretty) 
	throws MerlotException
    {
	try {
	    Writer w;
	    String encoding = _doc.getEncoding();
	    if (encoding != null) {
		w = new OutputStreamWriter(s,encoding);
	    }
	    else {
		w = new OutputStreamWriter(s);
	    }
	    
	    XMLEditor.getSharedInstance().getDOMLiaison().print(_doc,w,null,pretty);
	    /* Xerces pretty printing is really bad in some cases. going back to original save routine

	      DOMLiaison xercesDomLiaison = new DOMLiaison();
		xercesDomLiaison.print(_doc,w,null,pretty);
	    */
	}
	catch (Exception ex) {
	    MerlotDebug.exception(ex);
	    throw new MerlotException (MerlotResource.getString(ERR,"xml.file.write.err"),ex);
	}
		
    }
	

    public String getName() 
    {
	return _file.getName();
    }
	
    public String getPath() 
    {
	return _file.getPath();
    }
	
    public Enumeration getDTDAttributes(String elementName) 
    {
	return _doc.getDTDAttributes(elementName);
    }
	
    /*
    public Enumeration getAppendableElements(Element el) {
	DTDDocument doc = _doc.getDTDForElement(el);
	if (doc != null) {
	    Enumeration e = doc.getAppendableElements(el);
	    return e;
	}
	return null;
    }
    */
    public Enumeration getInsertableElements(Element el, int index)
    {
	DTDDocument doc = _doc.getDTDForElement(el);
	if (doc != null) {
	    Enumeration e = doc.getInsertableElements(el, index);
	    return e;
	}
	return null;
    }
	
    public Enumeration getInsertableElements( Element el )
    {
	DTDDocument doc = _doc.getDTDForElement(el);
	if (doc != null) {
	    Enumeration e = doc.getInsertableElements( el );
	    return e;
	}
	return null;
    }
	
	public boolean elementIsValid (Element el, boolean checkChildren)
	{
		DTDDocument doc = _doc.getDTDForElement(el);
		if (doc == null) 
			return false;
		return doc.elementIsValid(el,checkChildren);
	}
			   
	public void setDirty(boolean tf) 
    {
	boolean old = _dirty;
	_dirty = tf;
		
	firePropertyChange("dirty",old,tf);
		

    }
	
    public boolean isDirty() 
    {
	return _dirty;
    }
	
    public void addPropertyChangeListener(PropertyChangeListener l) 
    {
	_propchange.addPropertyChangeListener(l);
    }
	
    public void firePropertyChange(String s, boolean ov, boolean nv) 
    {
	MerlotDebug.msg("XMLFile firePropertyChange: "+s);
	    
	_propchange.firePropertyChange(s,ov,nv);
    }

    /**
     * Saves in the same file we opened
     */
    public void save() 
	throws MerlotException
    {
	saveAs(_file);
		
    }
	
    /**
     * Saves to a new file
     */
    public void saveAs(File f) 
	throws MerlotException
    {
	try {

	    // keep a backup of the original file incase the saveAs fails
	    File tmpFile = new File(f.getAbsolutePath() + ".tmpsave");
			
	    //			_file = f;
	    OutputStream s = new FileOutputStream(tmpFile);
	    printRawXML(s, INDENT);
	    s.close();
			
			
	    // if it didn't work an exception will be thrown and we won't get here
	    // now replace the old file with the tmp one
	    File backup = new File(_file.getAbsolutePath() + ".$$$");
	    // if the backup already exists... remove it
	    boolean tf;
	    if (backup.exists()) {
		tf = backup.delete();
		MerlotDebug.msg("Deleting "+backup+" returns "+tf);
	    }
	    if (!_new) MerlotUtils.copyFile(_file, backup);
	    MerlotUtils.copyFile(tmpFile,f);
	    tf = tmpFile.delete();
	    MerlotDebug.msg("Deleting "+tmpFile+" returns "+tf);
			
		
	    _file = f;
			
	    setDirty(false);
	    setNew(false);
			
	}
	catch (IOException ex){
	    throw new MerlotException("IOException while saving file: "+ex.getMessage(), ex);
	}
		
    }
	
	public void putInstanciatedNode( Node node, MerlotDOMNode mNode )
	{
		_instanciatedNodes.put( node, mNode );
	}
	
	public MerlotDOMNode getInstanciatedNode( Node node )
	{
		return (MerlotDOMNode)_instanciatedNodes.get( node );
	}

}
