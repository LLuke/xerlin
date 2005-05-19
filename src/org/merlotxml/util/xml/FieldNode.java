/*

   ======================================================================
   The Xerlin XML Editor is Copyright (c) 2002 SpeedLegal Holdings, Inc.
   and other contributors.  It includes software developed for the
   Merlot XML Editor which is Copyright (c) 1999-2000 ChannelPoint, Inc.
   All rights reserved.
   ======================================================================

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

   1. Redistribution of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistribution in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. All advertising materials mentioning features or use of this
   software must display the following acknowledgment:
   "This product includes software developed by the SpeedLegal Group for
   use in the Xerlin XML Editor www.xerlin.org and software developed by
   ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"

   4. Except for the acknowledgments required by these conditions, any
   names trademarked by SpeedLegal Holdings, Inc. must not be used to
   endorse or promote products derived from this software without prior
   written permission. For written permission, please contact
   info@speedlegal.com. Any names trademarked by ChannelPoint, Inc. must
   not be used to endorse or promote products derived from this software
   without prior written permission. For written permission, please
   contact legal@channelpoint.com.

    5. Except for the acknowledgment required by these conditions, Products
    derived from this software may not be called "Xerlin" nor may "Xerlin"
    appear in their names without prior written permission of SpeedLegal
    Holdings, Inc. Products derived from this software may not be called
    "Merlot" nor may "Merlot" appear in their names without prior written
    permission of ChannelPoint, Inc.

    6. Redistribution of any form whatsoever must retain the following
    acknowledgment:
    "This product includes software developed by the SpeedLegal Group for
    use in the Xerlin XML Editor www.xerlin.org and software developed by
    ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"

    7. Developers who choose to contribute code or documentation to Xerlin
    (which is encouraged but not required) acknowledge and agree that: (a) any
    such contributions accepted and included in Xerlin will be subject to this
    license; (b) SpeedLegal Holdings, Inc. or any successor that hosts the
    Xerlin project will always have the right to make those contributions
    available under this license or an equivalent open source license; and
    (c) all contributions are made with the full authority of their owner/s.

    THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
    AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
    SPEEDLEGAL HOLDINGS, INC. OR CHANNELPOINT, INC. OR ANY CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
    THE POSSIBILITY OF SUCH DAMAGE.
    ======================================================================

    For more information on SPEEDLEGAL visit www.speedlegal.com

    For information on the XERLIN project visit www.xerlin.org

*/
package org.merlotxml.util.xml;

import java.util.Hashtable;

import org.merlotxml.merlot.MerlotDebug;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A place holder for the org.w3c.dom.Node that would represent a field -
 * either an attribute (org.w3c.dom.Attr) or the simple content #text
 * (org.w3c.dom.Text).   The place holder is necessary because the Node may be
 * null if it doesn't yet have a value.
 * 
 * @author Evert Hoff
 */
public class FieldNode {
    protected static int _debugLevel = 0;
    private static Hashtable _elementFieldNodeHashtables = new Hashtable();
    protected Node _node = null;
    protected Element _element = null;
    protected String _fieldName = null;

    protected FieldNode() {}

    protected FieldNode(Element element, Node node, String fieldName) {
        //debug(1, "FieldNode(" + element + ", " + node + ", " + 
        //                fieldName + ")");
        _element = element;
        _node = node;
        _fieldName = fieldName;
    }
    
    protected static void debug(int level, String message) {
        if (level <= _debugLevel)
            MerlotDebug.msg(message);
    }

    /**
     * Gets a FieldNode that corresponds to a non-null Node.
     * 
     * @param node The Attr or Text Node.
     * 
     * @return The corresponding FieldNode or null.
     */
    public static FieldNode getFieldNode(Node node) {
        return getFieldNode(null, node, null);
    }

    /**
     * Gets a FieldNode that corresponds to these parameters.
     * 
     * @param element The owner element of the field.
     * @param node The Node for this field or null.
     * @param fieldName The node name of the field if the Node is not given.
     * 
     * @return FieldNode
     */
    public static FieldNode getFieldNode(Element element, Node node, 
                                         String fieldName) {
        debug(1, "Getting field node " + element + ", " + node + ", " + fieldName);
        if (node != null) {
            //if (element == null) {
                element = (Element) node.getParentNode();
                if (node instanceof Attr) {
                    element = ((Attr) node).getOwnerElement();
                }
            //}
        }
        if (element == null) {
            debug(1, " Could not get FieldNode. Element is null.");
            return null;
        }
        if (node != null) {
            fieldName = node.getNodeName();
        }
        if (fieldName == null) {
            debug(1, " Could not get FieldNode. Field name is null.");
            return null;
        }
        debug(1, " Searching for [" + element + ", " + fieldName + "]");
        Hashtable elementFieldNodes = (Hashtable) _elementFieldNodeHashtables.get(
                                              element);
        if (elementFieldNodes == null) {
            elementFieldNodes = new Hashtable();
            _elementFieldNodeHashtables.put(element, elementFieldNodes);
        }
        FieldNode fieldNode = (FieldNode) elementFieldNodes.get(fieldName);
        if (fieldNode == null) {
            fieldNode = new FieldNode(element, node, fieldName);
            elementFieldNodes.put(fieldName, fieldNode);
        }
        debug(1, " Returning '" + fieldNode.hashCode() + "'");
        return fieldNode;
    }

    /**
     * Same as the org.w3c.dom.Node method.
     * 
     * @return Node
     */
    public Node getParentNode() {
        if ((_node != null) && (_element == null)) {
            debug(1, "Parent of FieldNode is null.");
        }
        return _element;
    }

    /**
     * Same as the org.w3c.dom.Node method.
     * 
     * @return String
     */
    public String getNodeName() {
        if (_fieldName != null)
            return _fieldName;
        return getNode().getNodeName();
    }

    /**
     * Same as the org.w3c.dom.Node method.
     * 
     * @return String
     */
    public String getNodeValue() {
        getNode();
        if (_node == null) {
            return "";
        }
        return _node.getNodeValue();
    }

    /**
     * Gets the Node. If it was null, but has in the meantime been created,
     * this will return the current Node.
     * 
     * @return Node
     */
    public Node getNode() {
        _node = _element.getAttributeNode(_fieldName);
        if (_node == null
            && (_fieldName.equalsIgnoreCase("#text"))) {
            _node = getTextNode(_element);
        }
        return _node;
    }

    private Node getTextNode(Element el) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("#text"))
                return child;
        }
        return null;
    }

    public boolean equals(Object o) {
        if (!(o instanceof FieldNode)) {
            return false;
        }
        FieldNode other = (FieldNode) o;
        if (this == other)
            return true;
        if (this.getNode() == other.getNode())
            return true;
        if (other.getParentNode() != getParentNode()) {
            return false;
        }
        if (!other.getNodeName().equals(getNodeName())) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "<" + getNodeName() + ">" + getNodeValue() + "</>";
    }
}