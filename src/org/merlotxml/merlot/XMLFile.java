/*
 *  ====================================================================
 *  Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
 *  ====================================================================
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistribution of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  2. Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this
 *  software must display the following acknowledgment:  "This product
 *  includes software developed by ChannelPoint, Inc. for use in the
 *  Merlot XML Editor (http://www.merlotxml.org/)."
 *  4. Any names trademarked by ChannelPoint, Inc. must not be used to
 *  endorse or promote products derived from this software without prior
 *  written permission. For written permission, please contact
 *  legal@channelpoint.com.
 *  5.  Products derived from this software may not be called "Merlot"
 *  nor may "Merlot" appear in their names without prior written
 *  permission of ChannelPoint, Inc.
 *  6. Redistribution of any form whatsoever must retain the following
 *  acknowledgment:  "This product includes software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor
 *  (http://www.merlotxml.org/)."
 *  THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO
 *  EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  ====================================================================
 *  For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.
 *  For information on the Merlot project, please see
 *  http://www.merlotxml.org.
 */
// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

import org.merlotxml.util.FileUtil;
import org.merlotxml.util.xml.DOMLiaisonImplException;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.DTDDocument;
import org.merlotxml.util.xml.ValidDOMLiaison;
import org.merlotxml.util.xml.ValidDocument;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *  An XML file. This provides an internface into a particular XML file,
 *  including its dtd and its file location. It provides methods for loading and
 *  parsing a file, saving a file, and accessing the content model in the dtd.
 *
 *@author     Kelly A. Campbell
 *@created    16 March 2003
 */
public class XMLFile implements MerlotConstants {
    /**
     *  The parsed DOM document with validation
     */
    protected ValidDocument _doc = null;

    /**
     *  The document type (dtd)
     */
    protected DocumentType _docType = null;

    /**
     *  The file on the filesystem
     */
    protected File _file = null;

    /**
     *  Status holder for marking the file as needing a save
     */
    protected boolean _dirty = false;
    /**
     *  Status marker for brand new files so we can call saveas instead of save
     */
    protected boolean _new = false;

    /**
     *  property change delegate
     */
    protected PropertyChangeSupport _propchange;

    /**
     *  A cache of MerlotDOMNodes that have already been instanciated
     */
    private static Hashtable _instanciatedNodes = new Hashtable();

    private XMLEditorDoc _xmlEditorDoc = null;

    /**
     *  Reads in the given filename to create the Document tree
     *
     *@param  f                    Description of the Parameter
     *@exception  MerlotException  Description of the Exception
     */

    public XMLFile(File f) throws MerlotException {

        _file = f;

        _propchange = new PropertyChangeSupport(this);

        // now parse the file and get a Document
        parseDocument();

    }

    /**
     *  creates a new file with a blank Document tree
     *
     *@exception  MerlotException  Description of the Exception
     */
    public XMLFile() throws MerlotException {

        _propchange = new PropertyChangeSupport(this);

        _doc =
            XMLEditor.getSharedInstance().getDOMLiaison().createValidDocument();
    }

    public void setXMLEditorDoc(XMLEditorDoc doc) {
        _xmlEditorDoc = doc;
    }
    
    public XMLEditorDoc getXMLEditorDoc() {
        return _xmlEditorDoc;
    }

    /**
     *  Returns the DOM document for this file
     *
     *@return    The document value
     */
    public Document getDocument() {
        return _doc.getDocument();
    }

    /**
     *  Returns the DOMLiaison ValidDocument wrapper for this file
     *
     *@return    The validDocument value
     */
    public ValidDocument getValidDocument() {
        return _doc;
    }

    /**
     *  Returns the main DTDDocument for this file
     *
     *@param  name  Description of the Parameter
     *@return       The dTD value
     */
    public DTDDocument getDTD(String name) {
        return _doc.getDTDDocument(name);
    }

    /**
     *  returns the DTDCacheEntry for this document. Useful to get access to the
     *  DTD plugin associated with this file
     *
     *@return    The dTDCacheEntry value
     */
    public DTDCacheEntry getDTDCacheEntry() {
        return _doc.getDTDCacheEntry();
    }

    /**
     *  Sets the new property
     *
     *@param  tf  The new new value
     */
    public void setNew(boolean tf) {
        _new = tf;
    }

    /**
     *  returns the new property
     *
     *@return    The new value
     */
    public boolean isNew() {
        return _new;
    }

    /*
     *  * *
     *  sets this to the given document replacing the previous one
     *  /
     *  public void setDocument(Document doc)
     *  {
     *  /XXX	_doc = doc;
     *  / update this to handle setDocument for the new valid parser liaison
     *  }
     */
    /**
     *  Gets the doctype attribute of the XMLFile object
     *
     *@return    The doctype value
     */
    public DocumentType getDoctype() {
        return _docType;
    }

    /**
     *  Description of the Method
     *
     *@exception  MerlotException  Description of the Exception
     */
    protected void parseDocument() throws MerlotException {

        try {
            InputStream fis = getFileInputStream();
            // get a DOMLiaison from the settings and parse the given file
            ValidDOMLiaison domlia =
                XMLEditor.getSharedInstance().getDOMLiaison();

            if (domlia != null) {
                _doc =
                    domlia.parseValidXMLStream(fis, _file.getCanonicalPath());
            }
            if (_doc != null || _doc.getDocument() == null) {
                _docType = _doc.getDocument().getDoctype();
            } else {
                throw new MerlotException(
                    MerlotResource.getString(ERR, "xml.file.open.nodocument"));
            }

        } catch (FileNotFoundException fnf) {
            MerlotDebug.exception(fnf);
            throw new MerlotException("File not found: " + _file, fnf);
        } catch (IOException ioex) {
            MerlotDebug.exception(ioex);
            throw new MerlotException("IOException: " + _file, ioex);
        } catch (DOMLiaisonImplException dle) {
            Exception blah = dle.getRealException();
            if (blah != null) {
                MerlotDebug.msg("dle.msessage = " + dle.getMessage());

                MerlotDebug.exception(dle);
            } else {
                MerlotDebug.msg("wrapper exception with a null real exception");
            }

            throw new MerlotException("Parse error: " + dle.getMessage(), dle);
        }

    }

    protected InputStream getFileInputStream() throws FileNotFoundException {
        return FileUtil.getInputStream(_file, this.getClass());
    }

    /**
     *  Description of the Method
     *
     *@param  s                    Description of the Parameter
     *@param  pretty               Description of the Parameter
     *@exception  MerlotException  Description of the Exception
     */
    public void printRawXML(OutputStream s, boolean pretty)
        throws MerlotException {
        try {
            Writer w;
            String encoding = _doc.getEncoding();
            if (encoding != null) {
                w = new OutputStreamWriter(s, encoding);
            } else {
                w = new OutputStreamWriter(s);
            }

            XMLEditor.getSharedInstance().getDOMLiaison().print(
                _doc,
                w,
                null,
                pretty);
            /*
             *  Xerces pretty printing is really bad in some cases. going back to original save routine
             *  DOMLiaison xercesDomLiaison = new DOMLiaison();
             *  xercesDomLiaison.print(_doc,w,null,pretty);
             */
        } catch (Exception ex) {
            MerlotDebug.exception(ex);
            throw new MerlotException(
                MerlotResource.getString(ERR, "xml.file.write.err"),
                ex);
        }

    }

    /**
     *  Gets the name attribute of the XMLFile object
     *
     *@return    The name value
     */
    public String getName() {
        if (_file != null)
        	return _file.getName();
        return null;
    }

    /**
     *  Gets the path attribute of the XMLFile object
     *
     *@return    The path value
     */
    public String getPath() {
        return _file.getPath();
    }

    /**
     *  Gets the dTDAttributes attribute of the XMLFile object
     *
     *@param  elementName  Description of the Parameter
     *@return              The dTDAttributes value
     */
    public Enumeration getDTDAttributes(String elementName) {
        return _doc.getDTDAttributes(elementName);
    }

    /*
     *  public Enumeration getAppendableElements(Element el) {
     *  DTDDocument doc = _doc.getDTDForElement(el);
     *  if (doc != null) {
     *  Enumeration e = doc.getAppendableElements(el);
     *  return e;
     *  }
     *  return null;
     *  }
     */
    /**
     *  Gets the insertableElements attribute of the XMLFile object
     *
     *@param  el     Description of the Parameter
     *@param  index  Description of the Parameter
     *@return        The insertableElements value
     */
    public Enumeration getInsertableElements(Element el, int index) {
        DTDDocument doc = _doc.getDTDForElement(el);
        if (doc != null) {
            Enumeration e = doc.getInsertableElements(el, index);
            return e;
        }
        return null;
    }

    /**
     *  Gets the insertableElements attribute of the XMLFile object
     *
     *@param  el  Description of the Parameter
     *@return     The insertableElements value
     */
    public Enumeration getInsertableElements(Element el) {
        DTDDocument doc = _doc.getDTDForElement(el);
        if (doc != null) {
            Enumeration e = doc.getInsertableElements(el);
            return e;
        }
        return null;
    }

    /**
     *  Description of the Method
     *
     *@param  el             Description of the Parameter
     *@param  checkChildren  Description of the Parameter
     *@return                Description of the Return Value
     */
    public boolean elementIsValid(Element el, boolean checkChildren) {
        DTDDocument doc = _doc.getDTDForElement(el);
        if (doc == null) {
            return false;
        }
        return doc.elementIsValid(el, checkChildren);
    }

    /**
     *  Sets the dirty attribute of the XMLFile object
     *
     *@param  tf  The new dirty value
     */
    public void setDirty(boolean tf) {
        boolean old = _dirty;
        _dirty = tf;

        firePropertyChange("dirty", old, tf);

    }

    /**
     *  Gets the dirty attribute of the XMLFile object
     *
     *@return    The dirty value
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
     *  Adds a feature to the PropertyChangeListener attribute of the XMLFile
     *  object
     *
     *@param  l  The feature to be added to the PropertyChangeListener attribute
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        _propchange.addPropertyChangeListener(l);
    }

    /**
     *  Description of the Method
     *
     *@param  s   Description of the Parameter
     *@param  ov  Description of the Parameter
     *@param  nv  Description of the Parameter
     */
    public void firePropertyChange(String s, boolean ov, boolean nv) {
        MerlotDebug.msg("XMLFile firePropertyChange: " + s);

        _propchange.firePropertyChange(s, ov, nv);
    }

    /**
     *  Saves in the same file we opened
     *
     *@exception  MerlotException  Description of the Exception
     */
    public void save() throws MerlotException {
        saveAs(_file);

    }

    /**
     *  Saves to a new file
     *
     *@param  f                    Description of the Parameter
     *@exception  MerlotException  Description of the Exception
     */
    public void saveAs(File f) throws MerlotException {
        boolean tf;
        try {

            // keep a backup of the original file incase the saveAs fails
            File tmpFile = new File(f.getAbsolutePath() + ".tmpsave");

            //			_file = f;
            OutputStream s = new FileOutputStream(tmpFile);
            printRawXML(s, true);
            s.close();

            if (new File(_file.getAbsolutePath()).exists()) {
                // if it didn't work an exception will be thrown and we won't get here
                // now replace the old file with the tmp one
                File backup = new File(_file.getAbsolutePath() + ".$$$");
                // if the backup already exists... remove it
                if (backup.exists()) {
                    tf = backup.delete();
                    MerlotDebug.msg("Deleting " + backup + " returns " + tf);
                }
                if (!_new) {
                    MerlotUtils.copyFile(_file, backup);
                }
            }

            MerlotUtils.copyFile(tmpFile, f);
            tf = tmpFile.delete();
            MerlotDebug.msg("Deleting " + tmpFile + " returns " + tf);

            close(_file);
            _file = f;

            setDirty(false);
            setNew(false);
        } catch (IOException ex) {
            throw new MerlotException(
                "IOException while saving file: " + ex.getMessage(),
                ex);
        }

    }

    protected void close() {}

    protected void close(File f) {}

    /**
     *  Description of the Method
     *
     *@param  node   Description of the Parameter
     *@param  mNode  Description of the Parameter
     */
    public static void putInstanciatedNode(Node node, MerlotDOMNode mNode) {
        _instanciatedNodes.put(node, mNode);
    }

    /**
     *  Gets the instanciatedNode attribute of the XMLFile class
     *
     *@param  node  Description of the Parameter
     *@return       The instanciatedNode value
     */
    public static MerlotDOMNode getInstanciatedNode(Node node) {
        if (node == null) {
            return null;
        }
        return (MerlotDOMNode) _instanciatedNodes.get(node);
    }
}
