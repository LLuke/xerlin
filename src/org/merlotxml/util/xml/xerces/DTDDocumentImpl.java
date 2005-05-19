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

package org.merlotxml.util.xml.xerces;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.xerces.dom.ASDOMImplementationImpl;
import org.apache.xerces.dom.ASModelImpl;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.dom3.as.ASModel;
import org.apache.xerces.dom3.as.DOMASBuilder;
import org.apache.xerces.dom3.as.DOMImplementationAS;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.merlotxml.util.xml.DTDCache;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.DTDDocument;
import org.merlotxml.util.xml.DTDElement;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.w3c.dom.ls.DOMInputSource;
import org.xml.sax.InputSource;

/**
 * 
 *
 * @author Evert Hoff
 */

public class DTDDocumentImpl implements DTDDocument {
    private Hashtable _elements = null;
    private Hashtable _complexTypes = new Hashtable();
    private boolean _initialized = false;

    private String _pluginId;
    private String _publicId;
    private String _systemId;

    private GrammarDocument _grammarDocument = null;

    /**
     * @deprecated Use GrammarDocument.
     */
    public DTDDocumentImpl(String pluginId, String publicId, String systemId) {
        _pluginId = pluginId;
        _publicId = publicId;
        _systemId = systemId;
        System.err.println(
            "Created Xerces DTDDocumentImpl: " + getExternalID());
    }

    /**
     * @deprecated Use GrammarDocument.
     */
    public DTDDocumentImpl(String publicId, String systemId) {
        _publicId = publicId;
        _systemId = systemId;
        System.err.println(
            "Created Xerces DTDDocumentImpl: " + getExternalID());
    }

    /**
     * @deprecated Use GrammarDocument.
     */
    public String getName() {
        // REVISIT
        String tempName = "No_name_yet_" + System.currentTimeMillis();
        return tempName;
    }

    /**
     * @deprecated Use GrammarDocument.
     */
    public Enumeration getElements() {
        if (!_initialized && _elements == null && _grammarDocument != null) {
            _initialized = true;
            _elements = new Hashtable();
            GrammarComplexType[] elements =
                _grammarDocument.getTopLevelGrammarComplexTypes();
            for (int i = 0; i < elements.length; i++) {
                GrammarComplexType element = elements[i];
                String elementName = element.getName();
                DTDElement el =
                    new DTDElementImpl(this, (GrammarComplexType) element);
                _elements.put(elementName, el);
                _complexTypes.put(elementName, element);
            }
        }
        if (_elements != null)
            return _elements.elements();
        else if (_grammarDocument == null) {
            // This is used when creating a new file from a schema...
            //Lets try to get it for a Schema
            //FIXME We can't tell if its a Schems or a DTD at this point
            //Really should have separate classes and an interface
            // Get the DTDEntry from the cache
            DTDCache cache = DTDCache.getSharedInstance();
            DTDCacheEntry ce =
                cache.findDTDbySystemId(_publicId, _systemId, _pluginId);
            String s = new String(ce.getCachedDTDStream());
            StringReader reader = new StringReader(s);
            DOMImplementationAS domImpl =
                (DOMImplementationAS) ASDOMImplementationImpl
                    .getDOMImplementation();
            // create a new parser, and set the error handler
            DOMASBuilder parser = domImpl.createDOMASBuilder();
            ASModel as;
            Vector v = null;
            try {
                //as = parser.parseASInputSource(new SchemaInputSource(reader));
                as = parser.parseASInputSource(new DOMInputImpl(null, null, null, reader, null));
                XSModel xsmodel = getSchemaGrammars(as);
                XSNamedMap namedmap =
                    xsmodel.getComponents(XSConstants.ELEMENT_DECLARATION);
                v = new Vector(namedmap.getLength());
                for (int i = 0; i < namedmap.getLength(); i++) {
                    XSElementDeclaration elementDecl =
                        (XSElementDeclaration) namedmap.item(i);
                    String name = elementDecl.getName();
                    v.add(name);
                    System.err.println("Element found " + name);
                }
                return v.elements();
            } catch (Exception ex) {
                System.err.println("ERROR: Exception...");
                ex.printStackTrace();
                return null;
            }

        }
        return null;
    }

    private XSModel getSchemaGrammars(ASModel as) {
        ASModelImpl model = (ASModelImpl) as;
        Vector models = model.getInternalASModels();
        SchemaGrammar[] grammars = new SchemaGrammar[models.size()];
        for (int i = 0; i < models.size(); i++)
            grammars[i] = ((ASModelImpl) models.elementAt(i)).getGrammar();
        return new XSModelImpl(grammars);
    }

    /**
     * @deprecated Use GrammarDocument.
     */
    public DTDElement fetchElement(String name) {
        if (_elements != null) {
            DTDElement el = (DTDElement) _elements.get(name);
            return el;
        }
        return null;
    }

    /**
     * @deprecated Use GrammarComplexType.
     */
    public Enumeration getInsertableElements(Element el, int index) {
        String elementName = el.getNodeName();
        GrammarComplexType complexType = getComplexType(el);
        GrammarComplexType[] insertables =
            complexType.getInsertableElements(el, index);
        List list = new Vector();
        for (int i = 0; i < insertables.length; i++) {
            complexType = insertables[i];
            if (complexType != null) {
                String name = complexType.getName();
                DTDElement element = (DTDElement) _elements.get(name);
                list.add(element);
            }
        }
        Enumeration ret = Collections.enumeration(list);
        return ret;
    }

    /**
     * @deprecated Use GrammarComplexType.
     */
    private GrammarComplexType getComplexType(Node node) {
        GrammarComplexType ret = null;
        if (node == null)
            return null;
        if (!(node instanceof Element))
            return null;
        Element el = (Element) node;
        Node parent = node.getParentNode();
        GrammarComplexType parentComplexType = getComplexType(parent);
        if (parentComplexType != null)
            ret = parentComplexType.getChild(node.getNodeName());
        else
            ret = (GrammarComplexType) _complexTypes.get(node.getNodeName());
        return ret;
    }

    /**
     * @deprecated Use GrammarComplexType.
     */
    public Enumeration getInsertableElements(Element el) {
        String elementName = el.getNodeName();
        GrammarComplexType complexType = getComplexType(el);
        GrammarComplexType[] insertables =
            complexType.getInsertableElements(el);
        List list = new Vector();
        for (int i = 0; i < insertables.length; i++) {
            complexType = insertables[i];
            if (complexType != null) {
                String name = complexType.getName();
                DTDElement element = (DTDElement) _elements.get(name);
                list.add(element);
            }
        }
        Enumeration ret = Collections.enumeration(list);
        return ret;
    }

    /**
     * @deprecated Use GrammarComplexType.
     */
    public int getInsertPosition(Element parent, String childElementName) {
        String elementName = parent.getNodeName();
        GrammarComplexType complexType = getComplexType(parent);
        int ret = complexType.getInsertPosition(parent, childElementName);
        return ret;
    }

    /**
     * @deprecated Use GrammarComplexType.
     */
    public boolean elementIsValid(Element el, boolean checkChildren) {
        String elementName = el.getNodeName();
        GrammarComplexType complexType = getComplexType(el);
        boolean ret =
            complexType.getIsLocationValid(el)
                && complexType.getIsSimpleContentValid(el)
                && complexType.getIsEachAttributeValid(el);
        if (ret == true && checkChildren) {
            NodeList list = el.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node child = list.item(i);
                if (child instanceof Element) {
                    ret = elementIsValid((Element) child, checkChildren);
                    // JML - this method is now recursive
                    if (!ret) {
                        break;
                    }
                    /*
                    elementName = el.getNodeName();
                    complexType = getComplexType(child);
                    Element childElement = (Element)child;
                    ret = complexType.getIsLocationValid(childElement) &&
                     complexType.getIsSimpleContentValid(childElement) &&
                     complexType.getIsEachAttributeValid(childElement);
                    if ( ret == false )
                    	break;
                    */
                }
            }
        }

        // Finally check for overall validity - JML
        if (ret) {
            int result = complexType.validate(el);
            return (result == -1);
        }
        return ret;
    }

    /**
     * Returns the external identifier or null if there is none.
     * <P>
     * The string should include PUBLIC and SYSTEM identifiers if they
     * are available.
     */
    public String getExternalID() {
        StringBuffer sb = new StringBuffer();
        boolean added_public = false;

        if (_publicId != null && !_publicId.equals("")) {
            sb.append("PUBLIC \"" + _publicId + "\"");
            added_public = true;
        }
        if (_systemId == null) {
            _systemId = "";
        }

        if (!added_public) {
            sb.append("SYSTEM");
        }
        sb.append(" \"" + _systemId + "\"");

        if (sb.length() > 0) {
            return sb.toString();
        }

        /*
        ExternalID id = _doc.getExternalID();
        if (id != null) {
        	return id.toString();
        }
        */
        return null;

    }

    /**
     * Temporary method.
     * @deprecated Use GrammarComplexType.
     */
    public void setGrammarDocument(GrammarDocument grammarDocument) {
        _grammarDocument = grammarDocument;
    }

    /*
    private class SchemaInputSource
        extends InputSource
        implements DOMInputSource {
        public SchemaInputSource(Reader r) {
            super(r);
        }

        public String getStringData() {
            return null;
        }

        public void setStringData(String s) {}

        public String getBaseURI() {
            return null;
        }

        public void setBaseURI(String s) {}
    }
    */
}
