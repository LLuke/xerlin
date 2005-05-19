/*
 *  ======================================================================
 *  The Xerlin XML Editor is Copyright (c) 2002 SpeedLegal Holdings, Inc.
 *  and other contributors.  It includes software developed for the
 *  Merlot XML Editor which is Copyright (c) 1999-2000 ChannelPoint, Inc.
 *  All rights reserved.
 *  ======================================================================
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistribution of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  2. Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this
 *  software must display the following acknowledgment:
 *  "This product includes software developed by the SpeedLegal Group for
 *  use in the Xerlin XML Editor www.xerlin.org and software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"
 *  4. Except for the acknowledgments required by these conditions, any
 *  names trademarked by SpeedLegal Holdings, Inc. must not be used to
 *  endorse or promote products derived from this software without prior
 *  written permission. For written permission, please contact
 *  info@speedlegal.com. Any names trademarked by ChannelPoint, Inc. must
 *  not be used to endorse or promote products derived from this software
 *  without prior written permission. For written permission, please
 *  contact legal@channelpoint.com.
 *  5. Except for the acknowledgment required by these conditions, Products
 *  derived from this software may not be called "Xerlin" nor may "Xerlin"
 *  appear in their names without prior written permission of SpeedLegal
 *  Holdings, Inc. Products derived from this software may not be called
 *  "Merlot" nor may "Merlot" appear in their names without prior written
 *  permission of ChannelPoint, Inc.
 *  6. Redistribution of any form whatsoever must retain the following
 *  acknowledgment:
 *  "This product includes software developed by the SpeedLegal Group for
 *  use in the Xerlin XML Editor www.xerlin.org and software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"
 *  7. Developers who choose to contribute code or documentation to Xerlin
 *  (which is encouraged but not required) acknowledge and agree that: (a) any
 *  such contributions accepted and included in Xerlin will be subject to this
 *  license; (b) SpeedLegal Holdings, Inc. or any successor that hosts the
 *  Xerlin project will always have the right to make those contributions
 *  available under this license or an equivalent open source license; and
 *  (c) all contributions are made with the full authority of their owner/s.
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 *  SPEEDLEGAL HOLDINGS, INC. OR CHANNELPOINT, INC. OR ANY CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 *  ======================================================================
 *  For more information on SPEEDLEGAL visit www.speedlegal.com
 *  For information on the XERLIN project visit www.xerlin.org
 */
package org.merlotxml.util.xml.xerces;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.merlotxml.merlot.MerlotDOMNode;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.GrammarDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This makes the schema available as a org.w3c.dom.Document so that 
 * documentation contained in the schema file can be accessed. This is necessary
 * because Xerces has not yet implemented the method for accessing the
 * documentation.
 * 
 * @author   Evert Hoff
 */
public class Schema {
    private GrammarDocument _doc;
    private Hashtable _complexTypes = new Hashtable();
    private Document _document = null;
    private static Schema _instance = null;
    private boolean _debug = true;

    public Schema(GrammarDocument doc) {
        this._doc = doc;
    }

    private void debug(String msg) {
        if (_debug)
            MerlotDebug.msg(msg);
    }

    public static Schema getInstance() {
        if (_instance == null)
            _instance = new Schema(null);
        return _instance;
    }

    public GrammarDocument getGrammarDocument() {
        return _doc;
    }

    public void setDocument(Document document) {
        _document = document;
        initDocument();
    }

    Element _rootElement = null;
    Hashtable _topLevelElementsByName = new Hashtable();

    void initDocument() {
        debug("Schema.initDocument()");
        if (_document == null) {
            debug(" Document is null.");
            return;
        }
        _rootElement = _document.getDocumentElement();
        debug(" Root element: " + _rootElement);
        Element[] topLevelElements = getSchemaChildren(_rootElement);
        for (int i = 0; i < topLevelElements.length; i++) {
            Element el = topLevelElements[i];
            debug(" Top level element: " + nodeToString(el));
            String name = el.getAttribute("name");
            _topLevelElementsByName.put(name, el);
        }
    }

    String nodeToString(Node node) {
        String ret = "<null/>";
        if (node != null) {
            ret = "<" + node.getNodeName() + " ";
            NamedNodeMap attrs = node.getAttributes();
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = (Node) attrs.item(i);
                    ret = ret + attr.getNodeName() + "='";
                    ret = ret + attr.getNodeValue() + "' ";
                }
            }
            ret = ret + "/>";
        }
        return ret;
    }

    public String getDocumentation(MerlotDOMNode node) {
        debug(
            "Getting documentation for "
                + node.getNodeName()
                + " from "
                + this);
        String[] path = getPathToThisElement(node);
        Element el = selectElement(_rootElement, path);
        if (el == null) {
            debug("Element not found in schema document.");
            return null;
        }
        String doc = getDocumentation(el);
        if (doc == null) {
            String ref = el.getAttribute("ref");
            if (ref != null) {
                el = (Element) _topLevelElementsByName.get(ref);
                doc = getDocumentation(el);
            }
        }
        return doc;
    }

    public String[] getPathToThisElement(MerlotDOMNode node) {
        String[] ret = new String[0];
        String[] parentPath = new String[0];
        MerlotDOMNode parent = node.getParentNode();
        if (parent != null) {
            parentPath = getPathToThisElement(parent);
        } else
            return ret;
        String name = node.getNodeName();
        String parentName = parent.getNodeName();
        if (name.equalsIgnoreCase(parentName))
            return parentPath;
        ret = new String[parentPath.length + 1];
        for (int i = 0; i < parentPath.length; i++) {
            ret[i] = parentPath[i];
        }
        ret[ret.length - 1] = name;
        return ret;
    }

    Element selectElement(Element baseElement, String[] path) {
        Element ret = null;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            String p = path[i];
            buf.append(p);
            buf.append(">");
        }
        debug(
            "selectElement( "
                + nodeToString(baseElement)
                + ", "
                + buf.toString()
                + ")");
        if (path.length == 0) {
            debug(" Path length is 0");
            return null;
        }
        String nextName = path[0];
        Element[] children = getSchemaChildren(baseElement);
        for (int i = 0; i < children.length; i++) {
            Element child = children[i];
            Element childElement = (Element) child;
            String childName = childElement.getAttribute("name");
            if (childName == null || childName.equals(""))
                childName = childElement.getAttribute("ref");
            debug(
                " considering: "
                    + nodeToString(childElement)
                    + ":"
                    + childName);
            if (childName != null && childName.equalsIgnoreCase(nextName)) {
                if (path.length == 1)
                    ret = childElement;
                else {
                    String[] newPath = new String[path.length - 1];
                    for (int j = 1; j < path.length; j++) {
                        newPath[j - 1] = path[j];
                    }
                    ret = selectElement(childElement, newPath);
                }
                break;
            }
        }
        debug(" selected: " + nodeToString(ret));
        return ret;
    }

    Element[] getSchemaChildren(Element parent) {
        Element[] ret = new Element[0];
        Vector v = new Vector();
        if (parent == null)
            return ret;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof Element))
                continue;
            String nodeName = child.getNodeName();
            if (nodeName.equalsIgnoreCase("xs:element"))
                v.add(child);
            else
                v.addAll(Arrays.asList(getSchemaChildren((Element) child)));
        }
        ret = (Element[]) v.toArray(ret);
        return ret;
    }

    String getDocumentation(Element el) {
        debug("getDocumentation() for " + nodeToString(el));
        String ret = null;
        if (el == null)
            return null;
        NodeList annotations = el.getElementsByTagName("xs:annotation");
        if (annotations.getLength() > 0) {
            Element annotation = (Element) annotations.item(0);
            NodeList documentations =
                el.getElementsByTagName("xs:documentation");
            if (documentations.getLength() > 0) {
                Element documentation = (Element) documentations.item(0);
                //Node text = documentation.getFirstChild();
                //if (text != null)
                //    ret = text.getNodeValue();
                ret = getNodeContent(documentation);
            }
        }
        debug(" Returning: " + ret);
        return ret;
    }
    
    String getNodeContent(Node node) {
        String ret = "";
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("#text"))
                ret += getNodeContent(child);
            else
                ret += "<" + child.getNodeName() + ">" + getNodeContent(child) + "</" + child.getNodeName() + ">";
        }
        String nodeValue = node.getNodeValue();
        if (nodeValue != null) {
            ret += node.getNodeValue();
        }
        return ret;
    }
}
