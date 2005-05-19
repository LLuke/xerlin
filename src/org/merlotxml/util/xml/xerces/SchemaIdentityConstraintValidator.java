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
package org.merlotxml.util.xml.xerces;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xpath.XPath;
import org.apache.xerces.impl.xpath.XPath.Axis;
import org.apache.xerces.impl.xpath.XPath.LocationPath;
import org.apache.xerces.impl.xpath.XPath.NodeTest;
import org.apache.xerces.impl.xpath.XPath.Step;
import org.apache.xerces.impl.xs.identity.Field;
//import org.apache.xerces.impl.xs.identity.IDValue;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.Selector;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSIDCDefinition;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.merlotxml.util.xml.XPathUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * Validates Identity Constraints - Unique, Key and KeyRef - for XML Schema.
 * Xerces validates these during initial parsing, but not dynamically. In
 * other words, if a value changes that causes a duplicate Key Xerces won't
 * pick this up.
 * <p>
 * The validation that Xerces does during parsing can be
 * repeated by calling Document.normalizeDocument(), but this takes too much
 * time, because the entire document is revalidated. Errors could be caught
 * and linked to nodes  using an org.w3c.dom.DOMErrorHandler. This approach
 * works, but is too slow to do with each change in value.
 * <p>
 * This class implements the same functionality that Xerces uses during 
 * parsing, but in a dynamic way.  
 * <p>
 * See org.apache.xerces.impl.xs.XMLSchemaValidator.ValueStoreCache
 * <p>
 * This class is instanciated from 
 * {@link DOMLiaison#parseValidXMLStream(InputStream, String) 
 * parseValidXMLStream}
 * 
 * @author Evert Hoff
 */
public class SchemaIdentityConstraintValidator {
    /** Utility to minimise the output during debugging. */
    protected int debugLevel = 0;
    Document _document = null;
    SchemaGrammarDocumentImpl _grammarDocument = null;
    Hashtable _simpleTypeFields = new Hashtable();
    Hashtable _fieldComplexTypes = new Hashtable();
    Hashtable _dynamicValueStoreCaches = new Hashtable();
    Hashtable _identityConstraintComplexTypes = new Hashtable();
    HashSet _keyRefs = new HashSet();
    Hashtable _errorMessagePerNode = new Hashtable();

    /**
     * Create and initialize this validator with the current value of each
     * field in the document.
     */
    public SchemaIdentityConstraintValidator(Document document, 
                                             SchemaGrammarDocumentImpl grammarDocument) {
        _document = document;
        _grammarDocument = grammarDocument;
        _grammarDocument.setSchemaIdentityConstraintValidator(this);
        initialize();
    }

    /**
     * Utility for debugging.
     * 
     * @param level  0=no debugging, 1 little bit, 2 more, etc.
     * @param msg    The message to print.
     */
    protected void debug(int level, String msg) {
        if (debugLevel >= level) {
            MerlotDebug.msg(msg);
        }
    }

    /**
     * Create a DynamicValueStore for each IdentityConstraint and populate each
     * value store with the current values of fields.
     */
    private void initialize() {
        long start = System.currentTimeMillis();
        // Create all the value stores
        GrammarComplexType[] complexTypes = _grammarDocument.getTopLevelGrammarComplexTypes();
        for (int i = 0; i < complexTypes.length; i++) {
            GrammarComplexType complexType = complexTypes[i];
            initialize(complexType);
        }
        long end = System.currentTimeMillis();
        System.out.println("Duration - initializing identity constraints: " + 
                           (end - start) + " ms.");
        start = end;
        // Set the values as they are after parsing.
        Element element = _document.getDocumentElement();
        initialize(element);
        end = System.currentTimeMillis();
        System.out.println(
                "Duration - setting values for identity constraints: " + 
                (end - start) + " ms.");
    }

    /**
     * Find the IdentityConstraints defined on each complex type and  create a
     * value store for each one.
     */
    private void initialize(GrammarComplexType complexType) {
        if (!(complexType instanceof SchemaGrammarComplexTypeImpl)) {
            return;
        }
        SchemaGrammarComplexTypeImpl impl = (SchemaGrammarComplexTypeImpl) complexType;
        IdentityConstraint[] constraints = getIdentityConstraints(complexType);
        for (int i = 0; i < constraints.length; i++) {
            IdentityConstraint constraint = constraints[i];
            _identityConstraintComplexTypes.put(constraint, complexType);
            //createDynamicValueStore(constraint);

            // Find the declarations of all the elements governed by this
            // constraint. Thus, the GrammarComplexTypes of all the elements
            // selected by the XPath of the Selector of this constraint.
            Selector selector = constraint.getSelector();
            XPath xpath = selector.getXPath();
            GrammarComplexType[] selectedComplexTypes = 
                    selectComplexTypes(complexType, xpath);
            for (int j = 0; j < selectedComplexTypes.length; j++) {
                GrammarComplexType selectedComplexType = 
                        selectedComplexTypes[j];

                // Identify the declarations of all the fields (attributes or
                // simple content) that are governed by this constraint.
                for (int k = 0; k < constraint.getFieldCount(); k++) {
                    Field field = constraint.getFieldAt(k);
                    XPath fieldXPath = field.getXPath();
                    GrammarSimpleType selectedSimpleType = 
                            selectSimpleType(selectedComplexType, fieldXPath);

                    // Make it easy to later find this field again if the
                    // simple type is known.
                    associateFieldWithSimpleType(field, selectedSimpleType);

                    // Make it easy to later find the complex type again if
                    // the field is known.
                    associateSelectedComplexTypeWithField(selectedComplexType, 
                                                          field);
                }
            }
        }
        GrammarComplexType[] childComplexTypes = complexType.getChildren();
        for (int i = 0; i < childComplexTypes.length; i++) {
            GrammarComplexType childComplexType = childComplexTypes[i];
            initialize(childComplexType);
        }
    }

    private IdentityConstraint[] getIdentityConstraints(GrammarComplexType complexType) {
        IdentityConstraint[] ret = new IdentityConstraint[0];
        SchemaGrammarComplexTypeImpl impl = (SchemaGrammarComplexTypeImpl) complexType;
        XSElementDeclaration declaration = impl.getXSElementDeclaration();
        XSNamedMap map = declaration.getIdentityConstraints();
        Vector constraints = new Vector();
        for (int i = 0; i < map.getLength(); i++) {
            XSObject object = map.item(i);
            if (object instanceof IdentityConstraint) {
                constraints.add(object);
            }
        }
        return (IdentityConstraint[]) constraints.toArray(ret);
    }

    /*
    private void createDynamicValueStore(IdentityConstraint constraint) {
        _dynamicValueStoreCache.put(constraint, 
                                    new DynamicValueStore(constraint));
    }
    */

    private GrammarComplexType[] selectComplexTypes(GrammarComplexType context, 
                                                    XPath xpath) {
        GrammarComplexType[] ret = new GrammarComplexType[0];
        Vector selection = selectObjects(context, xpath);
        return (GrammarComplexType[]) selection.toArray(ret);
    }

    /**
     * Performs an XPath selection. It uses the simplified version of XPath
     * that is defined for XML Schema identity constraints.
     * <p>
     * It works either on Elements and Nodes for selecting in a Document,
     * or on GrammarComplexTypes and GrammarSimpleTypes for selecting in
     * a GrammarDocument. The results returned corresponds to the type of
     * context object given.
     * 
     * @param context Either a GrammarComplexType or a Element.
     * @param xpath The xpath expression to be selected.
     */
    private Vector selectObjects(Object context, XPath xpath) {
        debug(3, "    selectObjects: " + getName(context) + ", " + xpath);
        Vector selection = new Vector();

        // There is an "OR" between each location path. Thus the results 
        // are added to previous results.
        LocationPath[] locationPaths = xpath.getLocationPaths();
        for (int i = 0; i < locationPaths.length; i++) {
            LocationPath locationPath = locationPaths[i];
            debug(3, "     locationPath: " + locationPath);
            Step[] steps = locationPath.steps;
            // Each step becomes the context for the next step.
            Vector contexts = new Vector();

            // The first step's context is given.
            contexts.add(context);
            Vector stepSelection = new Vector();
            for (int j = 0; j < steps.length; j++) {
                // Make the context the selection of the previous step.
                contexts.addAll(stepSelection);
                stepSelection.clear();
                Step step = steps[j];
                short axisType = step.axis.type;
                short nodeTestType = step.nodeTest.type;
                debug(3, 
                      "      Step: " + step + " axisType: " + axisType + 
                      " nodeTestType: " + nodeTestType);
                for (int k = 0; k < contexts.size(); k++) {
                    Vector candidates = new Vector();
                    Object stepContext = contexts.get(k);
                    debug(3, "       Step Context: " + getName(stepContext));
                    switch (axisType) {
                    case Axis.SELF:
                        candidates.add(stepContext);
                        break;
                    case Axis.CHILD:
                        candidates.addAll(getChildren(stepContext));
                        break;
                    case Axis.DESCENDANT:
                        candidates.addAll(getDescendants(stepContext));
                        break;
                    case Axis.ATTRIBUTE:
                        candidates.addAll(getAttributes(stepContext));
                        break;
                    }
                    for (int l = 0; l < candidates.size(); l++) {
                        Object candidate = candidates.get(l);
                        String candidateName = getName(candidate);
                        switch (nodeTestType) {
                        case NodeTest.WILDCARD:
                            stepSelection.add(candidate);
                            debug(3, "       Adding: " + getName(candidate));
                            break;
                        case NodeTest.NODE:
                            stepSelection.add(candidate);
                            debug(3, "       Adding: " + getName(candidate));
                            break;
                        case NodeTest.QNAME:
                            String qName = step.nodeTest.name.localpart;
                            int colonPosition = candidateName.indexOf(":");
                            if (colonPosition > -1) {
                                candidateName = candidateName.substring(colonPosition + 1);
                            }
                            debug(3, 
                                  "       Testing QName " + candidateName + 
                                  " versus " + qName);
                            if (candidateName.equals(qName)) {
                                stepSelection.add(candidate);
                                debug(3, "       Adding: " + 
                                      getName(candidate));
                            }
                            break;
                        case NodeTest.NAMESPACE:
                            String prefix = step.nodeTest.name.prefix;
                            if (candidateName.startsWith(prefix)) {
                                stepSelection.add(candidate);
                                debug(3, "       Adding: " + 
                                      getName(candidate));
                            }
                            break;
                        }
                    }
                }
                contexts.clear();
            }

            // Add what was selected in the final step for this locationPath.
            // LocationPaths are seperated by "|", so their results are 
            // unioned.
            selection.addAll(stepSelection);
        }
        debug(3, "     Selection: ");
        for (int i = 0; i < selection.size(); i++) {
            debug(3, "      " + getName(selection.get(i)));
        }
        return selection;
    }

    private Vector getChildren(Object context) {
        Vector ret = new Vector();
        if (context instanceof GrammarComplexType) {
            GrammarComplexType complexType = (GrammarComplexType) context;
            GrammarComplexType[] children = complexType.getChildren();
            for (int i = 0; i < children.length; i++) {
                GrammarComplexType child = children[i];
                ret.add(child);
            }
        }
        if (context instanceof Element) {
            Element element = (Element) context;
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    ret.add(child);
                }
            }
        }
        return ret;
    }

    private Vector getDescendants(Object context) {
        Vector ret = new Vector();
        if (context instanceof GrammarComplexType) {
            GrammarComplexType complexType = (GrammarComplexType) context;
            GrammarComplexType[] children = complexType.getChildren();
            for (int i = 0; i < children.length; i++) {
                GrammarComplexType child = children[i];
                ret.add(child);
                ret.addAll(getDescendants(child));
            }
        }
        if (context instanceof Element) {
            Element element = (Element) context;
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    ret.add(child);
                    ret.addAll(getDescendants(child));
                }
            }
        }
        return ret;
    }

    private Vector getAttributes(Object context) {
        Vector ret = new Vector();
        if (context instanceof GrammarComplexType) {
            GrammarComplexType complexType = (GrammarComplexType) context;
            GrammarSimpleType[] attributes = complexType.getAttributes();
            for (int i = 0; i < attributes.length; i++) {
                GrammarSimpleType attribute = attributes[i];
                ret.add(attribute);
            }
        }
        if (context instanceof Element) {
            Element element = (Element) context;
            GrammarComplexType complexType = getGrammarComplexType(element);
            GrammarSimpleType[] attributeSimpleTypes = 
                    complexType.getAttributes();
            for (int i = 0; i < attributeSimpleTypes.length; i++) {
                GrammarSimpleType attributeSimpleType = attributeSimpleTypes[i];
                String attributeName = attributeSimpleType.getName();
                Node attributeNode = element.getAttributeNode(attributeName);
                FieldNode fieldNode = FieldNode.getFieldNode(element, 
                                                             attributeNode, 
                                                             attributeName);
                ret.add(fieldNode);
            }
        }
        return ret;
    }

    private String getName(Object object) {
        String ret = null;
        if (object instanceof GrammarComplexType) {
            GrammarComplexType complexType = (GrammarComplexType) object;

            // Don't know if this will include the namespace.
            // We'll have to see if it doesn't work.
            ret = complexType.getName();
        }
        if (object instanceof GrammarSimpleType) {
            GrammarSimpleType simpleType = (GrammarSimpleType) object;
            ret = simpleType.getName();
        }
        if (object instanceof Node) {
            Node node = (Node) object;
            ret = node.getNodeName();
        }
        if (object instanceof FieldNode) {
            FieldNode node = (FieldNode) object;
            ret = node.getNodeName();
        }
        return ret;
    }

    private GrammarSimpleType selectSimpleType(GrammarComplexType context, 
                                               XPath xpath) {
        GrammarSimpleType ret = null;
        debug(1, "  selectSimpleType: " + context.getName() + ", " + xpath);
        Vector selection = selectObjects(context, xpath);
        if (selection.size() > 1) {
            debug(1, 
                  "XPath attempted to select more than one field: Not implemented.");
        }
        for (int i = 0; i < selection.size(); i++) {
            Object o = selection.get(i);
            debug(1, "   Selected " + getName(o));
            // Return the first one found.
            if (o instanceof GrammarSimpleType) {
                ret = (GrammarSimpleType) o;
                break;
            }
            if (o instanceof GrammarComplexType) {
                GrammarComplexType complexType = (GrammarComplexType) o;
                ret = complexType.getSimpleContent();
                break;
            }
        }
        debug(1, "   Returning " + getName(ret));
        return ret;
    }

    private void associateFieldWithSimpleType(Field field, 
                                              GrammarSimpleType selectedSimpleType) {
        debug(1, 
              "  associateFieldWithSimpleType: " + field + ", " + 
              selectedSimpleType);
        if ((field == null) || (selectedSimpleType == null)) {
            return;
        }
        Vector fields = (Vector) _simpleTypeFields.get(selectedSimpleType);
        if (fields == null) {
            fields = new Vector();
            _simpleTypeFields.put(selectedSimpleType, fields);
        }
        fields.add(field);
    }

    private void associateSelectedComplexTypeWithField(GrammarComplexType selectedComplexType, 
                                                       Field field) {
        debug(1, 
              "  associateSelectedComplexTypeWithField: " + 
              selectedComplexType + ", " + field);
        if ((selectedComplexType == null) || (field == null)) {
            return;
        }
        _fieldComplexTypes.put(field, selectedComplexType);
    }

    /**
     * Find all the fields (attributes and simple content) associated with this
     * element, and set the current value in the appropriate value store.
     */
    private void initialize(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String value = attribute.getNodeValue();
            setValue(FieldNode.getFieldNode(attribute), value);
        }
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String value = childNode.getNodeValue();
            if (childNode instanceof Attr || childNode instanceof Text) {
                setValue(FieldNode.getFieldNode(childNode), value);
            } else if (childNode instanceof Element) {
                initialize((Element) childNode);
            }
        }
    }

    /**
     * Updates the value of this FieldNode in the value store and checks for
     * duplicates. If a duplicate or other error is encountered, the error is
     * stored for this FieldNode so that the next call to getErrorMessage will
     * be able to retrieve the error.
     * 
     * @param node      The field for which the value is being set.
     * @param value     The new value.
     */
    public void setValue(FieldNode node, String value) {
        debug(1, "Setting value of field node " + node + " to '" + value + 
              "'");
        GrammarSimpleType simpleType = getGrammarSimpleType(node);
        if (simpleType == null) {
            debug(1, 
                  " Could not set value '" + value + "' for node " + node + 
                  ". GrammarSimpleType is null.");
            return;
        }
        Vector fields = (Vector) _simpleTypeFields.get(simpleType);
        if (fields == null) {
            debug(1, 
                  " No fields have been associated with simple type for node " + 
                  node);
            return;
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.get(i);
            IdentityConstraint constraint = field.getIdentityConstraint();
            DynamicValueStore valueStore = getDynamicValueStore(node, field);
            valueStore.setValue(field, node, value);
        }
        debug(1, "Finished setting value of field node " + node 
              + " (" + node.hashCode() + ") to '" + value + "'");
    }

    private GrammarSimpleType getGrammarSimpleType(FieldNode node) {
        debug(2, "Getting GrammarSimpleType for node " + node);
        GrammarSimpleType ret = null;
        Element el = (Element) node.getParentNode();
        GrammarComplexType complexType = getGrammarComplexType(el);
        if (complexType == null) {
            debug(2, 
                  "Could not get GrammarSimpleType for node " + node + 
                  ". GrammarComplexType is null.");
            return null;
        }
        if (node.getNodeName().equalsIgnoreCase("#text")) {
            ret = complexType.getSimpleContent();
        } else {
            ret = complexType.getAttribute(node.getNodeName());
        }
        return ret;
    }

    private GrammarComplexType getGrammarComplexType(Element el) {
        //debug(1, "getGrammarComplexType: " + XPathUtil.nodeToString(el));
        if (el == null) {
            debug(2, "Could not get GrammarComplexType. Element is null.");
            return null;
        }
        Node parent = el.getParentNode();
        if (parent instanceof Element) {
            GrammarComplexType parentComplexType = getGrammarComplexType(
                                                           (Element) parent);
            if (parentComplexType != null)
                return parentComplexType.getChild(el.getNodeName());
        } else if (parent == null || parent instanceof Document) {
            return _grammarDocument.getTopLevelGrammarComplexType(
                           el.getNodeName());
        } else {
            debug(2, 
                  "getGrammarComplexType(Element): Unhandled type of parent: " + 
                  XPathUtil.nodeToString(parent));
        }
        return null;
    }

    /*
    private DynamicValueStore getDynamicValueStore(IdentityConstraint identityConstraint) {
        return (DynamicValueStore) _dynamicValueStoreCache.get(
                       identityConstraint);
    }
    */
    
    private DynamicValueStore getDynamicValueStore(FieldNode node, Field field) {
        Element selectorElement = getSelectorElement(node, field);
        IdentityConstraint constraint = field.getIdentityConstraint();
        Element baseElement = getBaseElement(selectorElement, constraint);
        Hashtable dynamicValueStoreCache = (Hashtable)_dynamicValueStoreCaches.get(constraint);
        if (dynamicValueStoreCache == null) {
            dynamicValueStoreCache = new Hashtable();
            _dynamicValueStoreCaches.put(constraint, dynamicValueStoreCache);
        }
        // If the scope of the constraint is global, then the base element will
        // be the document element. Else, it will use the local cache.
        DynamicValueStore valueStore = (DynamicValueStore)dynamicValueStoreCache.get(baseElement);
        if (valueStore == null) {
            valueStore = new DynamicValueStore(constraint);
            dynamicValueStoreCache.put(baseElement, valueStore);
        }
        return valueStore;
    }

    /**
     * Retrieves the current error message associated with this FieldNode.
     * 
     * @param node
     * @return String
     */
    public String getErrorMessage(FieldNode node) {
        debug(1, "   Getting error message for field node " + node);
        GrammarSimpleType simpleType = getGrammarSimpleType(node);
        if (simpleType == null) {
            debug(1, 
                  "    Could not get error message for node " + node + 
                  ". GrammarSimpleType is null.");
            return null;
        }
        Vector fields = (Vector) _simpleTypeFields.get(simpleType);
        if (fields == null) {
            debug(1, 
                  "    No fields have been associated with simple type for node " + 
                  node);
            return null;
        }
        StringBuffer message = new StringBuffer();
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.get(i);
            IdentityConstraint constraint = field.getIdentityConstraint();
            if (constraint.getCategory() == XSIDCDefinition.IC_KEYREF) {
                message.append(getKeyRefErrorMessage(node, constraint));
            } else {
                DynamicValueStore valueStore = getDynamicValueStore(node, field);
                message.append(valueStore.getErrorMessage(node));
            }
        }
        return message.toString();
    }

    /**
     * Checks whether this field's value corresponds to a valid, non-empty,
     * key.
     */
    private String getKeyRefErrorMessage(FieldNode fieldNode, 
                                         IdentityConstraint constraint) {
        if (fieldNode == null) {
            debug(1, "Trying to get KeyRefErrorMessage for null fieldNode");
            return "";
        }
        int positionAmongPeers = 0;
        FieldNode[] peers = getIdentityConstraintFieldNodePeers(fieldNode);
        for (int i = 0; i < peers.length; i++) {
            FieldNode peer = peers[i];
            if (fieldNode == peer) {
                positionAmongPeers = i;
                break;
            }
        }
        FieldNode[][] options = getPossibleReferenceFieldNodes(fieldNode);
        int numFields = 0;
        if (options.length > 0) {
            numFields = options[0].length;
        }
        for (int i = 0; i < options.length; i++) {
            FieldNode correspondingFieldNode = options[i][positionAmongPeers];
            if (correspondingFieldNode == null) {
                debug(1, "Corresponding field node is null for option " + i);
                continue;
            }
            if (fieldNode.getNodeValue()
                         .equals(correspondingFieldNode.getNodeValue())) {
                boolean found = true;
                for (int j = 0; j < numFields; j++) {
                    FieldNode peer = peers[j];
                    FieldNode optionPeer = options[i][j];
                    found = found && 
                            peer.getNodeValue()
                                .equals(optionPeer.getNodeValue());
                }
                if (found) {
                    return "";
                }
            }
            // Else this option doesn't contain the value we're looking for.
        }
        // If it reaches this statement, then none of the options matched.
        String ret = "KeyRef error: No corresponding key found for tuple '";
        for (int i = 0; i < peers.length; i++) {
            FieldNode peer = peers[i];
            if (i == positionAmongPeers) {
                ret += "[";
            }
            ret += peer.getNodeValue();
            if (i == positionAmongPeers) {
                ret += "]";
            }
            if (i != (peers.length - 1)) {
                ret += "|";
            }
        }
        ret += "'";
        debug(1, ret + "; Field Node: " + fieldNode 
        + " (" + fieldNode.hashCode() + ")");
        return ret;
    }

    /**
     * Finds the FieldNodes for the other fields that form part of the same 
     * Key, KeyRef or Unique as this one. 
     * 
     * @param node          One of the fields that is part of the key.
     * @return FieldNode[]  All the fields that are part of this key.
     */
    public FieldNode[] getIdentityConstraintFieldNodePeers(FieldNode node) {
        FieldNode[] ret = new FieldNode[0];
        debug(1, "Getting peer nodes for field node " + node);
        IdentityConstraint constraint = getIdentityConstraint(node);
        if (constraint == null) {
            debug(1, " Identity constraint is null.");
            return ret;
        }

        // First go back up to the element that served as the context for
        // this field's XPath statement.
        Element selectorElement = getSelectorElement(node);
        if (selectorElement == null) {
            debug(1, " Selector element is null.");
            return ret;
        }

        // Then, go back down to all the fields defined for this constraint.
        ret = new FieldNode[constraint.getFieldCount()];
        for (int i = 0; i < constraint.getFieldCount(); i++) {
            Field peerField = constraint.getFieldAt(i);
            XPath xpath = peerField.getXPath();
            FieldNode peerNode = selectFieldNode(selectorElement, xpath);
            ret[i] = peerNode;
        }
        return ret;
    }

    private IdentityConstraint getIdentityConstraint(FieldNode fieldNode) {
        IdentityConstraint ret = null;
        debug(1, "Getting identity constraint for field node " + fieldNode);
        Field field = getField(fieldNode);
        if (field == null) {
            debug(1, " Field is null. Returning null.");
            return null;
        }
        ret = field.getIdentityConstraint();
        return ret;
    }

    private Field getField(FieldNode fieldNode) {
        Field ret = null;
        debug(1, "Getting field for FieldNode " + fieldNode);
        GrammarSimpleType simpleType = getGrammarSimpleType(fieldNode);
        if (simpleType == null) {
            debug(1, " GrammarSimpleType is null.");
            return ret;
        }
        Vector fields = (Vector) _simpleTypeFields.get(simpleType);
        if (fields == null) {
            debug(1, 
                  " No fields have been associated with simple type for node " + 
                  fieldNode);
            return ret;
        }

        // Assumption: Each node (attribute or simple type) may only be a
        // field for one IdentityConstraint. Else it would be unclear which
        // identity constraint should determine which reference nodes are
        // allowed. --Evert
        if (fields.size() > 1) {
            debug(1, 
                  " WARNING: Assumption not valid. Found an example " + 
                  "where a field node can be associated with more than one identity " + 
                  "constraint.");
            debug(1, "  Field node: " + fieldNode);
            //debug(1, 
            //      "  Selector element: " + 
            //      XPathUtil.nodeToString(getSelectorElement(fieldNode)));
            for (int i = 0; i < fields.size(); i++) {
                Field field = (Field) fields.get(i);
                IdentityConstraint constraint = field.getIdentityConstraint();
                debug(1, 
                      "  Field: " + field + " Identity Constraint: " + 
                      constraint);
            }
        }
        if (fields.size() == 0) {
            debug(1, " Zero fields found. This node is probably not a field.");
            return ret;
        }
        ret = (Field) fields.get(0);
        return ret;
    }

    /**
     * Follows the reverse of the Field's XPath to get back from the Field to
     * the Selector element that served as the context element for the
     * Field's XPath statement.
     * 
     * @param fieldNode     The field for which the selector is sought.
     */
    public Element getSelectorElement(FieldNode fieldNode) {
        // This makes the assumtion that each FieldNode can only be
        // constrained by one constraint.
        Field field = getField(fieldNode);
        return getSelectorElement(fieldNode, field);
    }

    /**
     * Follows the reverse of the Field's XPath to get back from the Field to
     * the Selector element that served as the context element for the
     * Field's XPath statement.
     * 
     * @param fieldNode     The field for which the selector is sought.
     * @param field         The declared Field for this FieldNode.
     */
    private Element getSelectorElement(FieldNode fieldNode, Field field) {
        debug(2, " getSelectorElement(" + fieldNode + ", " + field + ")");
        GrammarComplexType selectorComplexType = (GrammarComplexType) _fieldComplexTypes.get(
                                                         field);
        debug(2, "  selectorComplexType: " + selectorComplexType.getName());
        if (selectorComplexType == null) {
            debug(2, "  getSelectorElement: Selector complex type is null.");
            return null;
        }
        Element element = (Element) fieldNode.getParentNode();
        debug(2, "  parent node: " + element);
        GrammarComplexType complexType = getGrammarComplexType(element);
        debug(2, "  parent complex type: " + complexType.getName());
        while (complexType != selectorComplexType) {
            Node parent = element.getParentNode();
            if (parent == null) {
                break;
            }
            if (parent instanceof Element) {
                element = (Element) parent;
                complexType = getGrammarComplexType((Element) parent);
            } else {
                break;
            }
        }
        debug(2, "  returning selector element: " + element);
        return element;
    }

    /**
     * Performs an XPath selection with an Element as the context node.
     * 
     * Note: XML Schema uses a simplified form of XPath for Selectors and
     * Fields. That's why we are not using Xalan for this - this is faster.
     */
    private FieldNode selectFieldNode(Element context, XPath xpath) {
        Vector selection = selectObjects(context, xpath);
        if (selection.size() > 1) {
            debug(1, 
                  "WARNING: selectFieldNode(Element, XPath) has return more than one result.");
        }
        if (selection.size() == 0) {
            return null;
        }
        Object result = selection.get(0);
        if (result instanceof FieldNode) {
            return (FieldNode) result;
        }
        if (result instanceof Element) {
            Element element = (Element) result;
            return FieldNode.getFieldNode(element, getTextNode(element), 
                                          "#text");
        }
        debug(1, 
              "WARNING: selectFieldNode(Element, XPath) returned something other than a Node.");
        return null;
    }

    private Text getTextNode(Element element) {
        Text ret = null;
        GrammarComplexType complexType = getGrammarComplexType(element);
        if (complexType == null || complexType.isEmptyType() || complexType.isMixedType()) {
            return ret;
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Text) {
                return (Text) child;
            }
        }
        return ret;
    }

    /**
     * Gets the valid possible key nodes that this keyref node may refer to.
     * But, it also returns the possible nodes that the peers to this field
     * node may refer to.
     * 
     * @param keyRefNode A field in the key.
     * 
     * @return FieldNode[possibility][field] The values for all fields for
     * each option.
     */
    public FieldNode[][] getPossibleReferenceFieldNodes(FieldNode keyRefNode) {
        FieldNode[][] ret = new FieldNode[0][0];
        debug(1, "Getting reference nodes for field node " + keyRefNode);
        IdentityConstraint constraint = getIdentityConstraint(keyRefNode);
        if (constraint == null) {
            debug(1, " Identity constraint is null.");
            return ret;
        }

        // Go up to the selector - the context that was used with the 
        // Field's XPath to get to the Field.
        Element keyRefSelectorElement = getSelectorElement(keyRefNode);
        if (keyRefSelectorElement == null) {
            debug(1, " Selector element is null.");
            return ret;
        }

        // Go further up to the base element - the context that was used with
        // the selector's XPath to select selector elements.
        Element baseElement = getBaseElement(keyRefSelectorElement, constraint);
        XSIDCDefinition referenceConstraintDefinition = 
                constraint.getRefKey();

        // The constraint related to the Key that this KeyRef is referring to.
        // Note: Xerces seems to allow a Unique to also serve as the key for
        // a KeyRef. This is actually incorrect, because a Unique doesn't have
        // the requirement that each field must have a value.
        IdentityConstraint referenceConstraint = null;
        if (referenceConstraintDefinition instanceof IdentityConstraint) {
            referenceConstraint = (IdentityConstraint) referenceConstraintDefinition;
        } else {
            debug(1, 
                  "Error: XSIDConstraintDefinition is not an IdentityConstraint.");
        }
        if (constraint.getFieldCount() != referenceConstraint.getFieldCount()) {
            debug(1, 
                  "Error: KeyRef doesn't point to a Key with the same number of fields.");
            return ret;
        }
        int fieldCount = constraint.getFieldCount();
        // Now we are selecting on the Key side.
        XPath xpath = referenceConstraint.getSelector().getXPath();
        // Selecting selector elements on the Key side.
        Element[] keySelectorElements = selectElements(baseElement, xpath);
        ret = new FieldNode[keySelectorElements.length][fieldCount];
        for (int i = 0; i < keySelectorElements.length; i++) {
            Element keySelectorElement = keySelectorElements[i];
            for (int j = 0; j < fieldCount; j++) {
                Field field = referenceConstraint.getFieldAt(j);
                XPath fieldXPath = field.getXPath();

                // For each Field, selecting the FieldNode that corresponds 
                // to it.
                FieldNode fieldNode = selectFieldNode(keySelectorElement, 
                                                      fieldXPath);
                ret[i][j] = fieldNode;
            }
        }
        return ret;
    }

    private Element getBaseElement(Element selectorElement, 
                                   IdentityConstraint identityConstraint) {
        GrammarComplexType identityConstraintComplexType = 
                (GrammarComplexType) _identityConstraintComplexTypes.get(
                        identityConstraint);
        GrammarComplexType complexType = getGrammarComplexType(selectorElement);
        Element ret = selectorElement;
        while (complexType != identityConstraintComplexType) {
            Node parentNode = ret.getParentNode();
            if (parentNode == null) {
                break;
            }
            if (!(parentNode instanceof Element)) {
                break;
            } else {
                ret = (Element) parentNode;
                complexType = getGrammarComplexType(ret);
            }
        }
        return ret;
    }

    private Element[] selectElements(Element context, XPath xpath) {
        Element[] ret = new Element[0];
        Vector selection = selectObjects(context, xpath);
        return (Element[]) selection.toArray(ret);
    }

    /**
     * Used to determine is the field represented by this simpleType is of 
     * type KeyRef. Sometimes, the simple type will be of type IDREF, but
     * this is not guaranteed, because this is just kept in Schema for 
     * backward compatibility. So, a different way of determining if a 
     * simple type is a KeyRef is required.
     * 
     * @param simpleType
     * @return boolean
     */
    public boolean getIsKeyRefField(GrammarSimpleType simpleType) {
        debug(1, " getIsKeyRefField()");
        Vector fields = (Vector) _simpleTypeFields.get(simpleType);
        if (fields == null) {
            return false;
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.get(i);
            IdentityConstraint constraint = field.getIdentityConstraint();
            if (constraint.getCategory() == XSIDCDefinition.IC_KEYREF) {
                return true;
            }
        }
        return false;
    }

    /**
     * Similar to org.apache.xerces.impl.xs.XMLSchemaValidator.ValueStoreCache
     * except that the Xerces ValueStores are not dynamic - they were 
     * intended to validate only once during parsing.
     * <p>
     * These ValueStores are used for identity constraints - to keep track of
     * the values of Keys, KeyRefs, and Uniques. It allows errors about 
     * duplicate IDs to be obtained. It also gives the possible values that 
     * may be  inserted into a KeyRef field.
     */
    public class DynamicValueStore {
        private IdentityConstraint _identityConstraint = null;
        private Selector _selector;
        private Hashtable _tuplesPerSelectorElement = new Hashtable();
        private Hashtable _tupleNodeMap = new Hashtable();
        private Vector _duplicateTuples = new Vector();
        private TreeSet _uniqueTuples = new TreeSet(new TupleComparator());
        /** Not a value (Unicode: #FFFF). */
        public final IDValue NOT_AN_IDVALUE = new IDValue("\uFFFF", null);
        public final IDValue EMPTY_IDVALUE = new IDValue("?", null);

        public DynamicValueStore(IdentityConstraint constraint) {
            _identityConstraint = constraint;
            _selector = constraint.getSelector();
        }

        public void setValue(Field field, FieldNode node, String value) {
            value = node.getNodeValue();
            debug(1, 
                  "DynamicValueStore.setValue(): IdentityConstraint: " + 
                  _identityConstraint + ": Field: " + field + ": node: " + 
                  node + ": Value: " + value);
            debug(2, " DynamicValueStore: " + this);

            // Clear out the old error message for this node.
            _errorMessagePerNode.remove(node);
            FieldNode[] peers = getIdentityConstraintFieldNodePeers(node);
            for (int i = 0; i < peers.length; i++) {
                FieldNode peer = peers[i];
                if (peer == null)
                    continue;
                String error = (String) _errorMessagePerNode.get(peer);
                // ToDo: Make this independent of the text of the message.
                if (error != null && error.startsWith("One or more fields in this key has empty values")) {
                    _errorMessagePerNode.remove(peer);
                }
            }
            SchemaGrammarSimpleTypeImpl grammarSimpleType = 
                    (SchemaGrammarSimpleTypeImpl) getGrammarSimpleType(node);
            if (grammarSimpleType == null) {
                debug(1, 
                      "DynamicValueStore could not set value for " + "node " + 
                      node + ".GrammarSimpleType is null.");
                return;
            }
            XSSimpleType simpleType = grammarSimpleType.getXSSimpleType();
            Element selectorElement = getSelectorElement(node, field);
            debug(1, " selector element: " + selectorElement + ": " + selectorElement.hashCode());
            Tuple tuple = (Tuple) _tuplesPerSelectorElement.get(selectorElement);
            debug(2, " existing tuple: " + tuple);
            if (tuple == null) {
                tuple = new Tuple(_identityConstraint.getFieldCount());
                _tuplesPerSelectorElement.put(selectorElement, tuple);
            } else {
                //Only remove from unique map if it's NOT a duplicate
                if (!_duplicateTuples.remove(tuple)) {
                    _uniqueTuples.remove(tuple);
                    _tupleNodeMap.remove(tuple);
                } 
                //Tuple duplicate = null;
                //Iterator it = _uniqueTuples.iterator();
                //while (it.hasNext()) {
                //    Tuple t = (Tuple)it.next();
                //    if (t == tuple || t.isDuplicateOf(tuple)) {
                //        duplicate = t;
                //        break;
                //    }
                //}
                //if (duplicate != null)
                //    _uniqueTuples.remove(duplicate);
            }
            for (int i = 0; i < _identityConstraint.getFieldCount(); i++) {
                Field f = _identityConstraint.getFieldAt(i);
                if (f == field) {
                    tuple.nodes[i] = node;
                    tuple.idValues[i] = new IDValue(value, simpleType);
                    tuple.addPrimitiveTypeValue(simpleType.getPrimitiveKind(), value, i);
                }
                // A node might have been deleted.
                if (tuple.nodes[i] == null) {
                    tuple.idValues[i] = NOT_AN_IDVALUE;
                    tuple.addPrimitiveTypeValue(-1, "NOT_AN_IDVALUE", i);
                }
            }
            short category = _identityConstraint.getCategory();
            // Check if all values are present.
            if ((category == XSIDCDefinition.IC_KEY) || 
                    (category == XSIDCDefinition.IC_KEYREF)) {
                for (int i = 0; i < tuple.idValues.length; i++) {
                    IDValue idValue = tuple.idValues[i];
                    if (idValue.isDuplicateOf(NOT_AN_IDVALUE) || 
                            idValue.isDuplicateOf(EMPTY_IDVALUE)) {
                        String message = 
                            "One or more fields in this key has empty values ' " + 
                             tuple + "'";
                        debug(1, " Empty key found. " + message);
                        _errorMessagePerNode.put(node, message);
                        return;
                    }
                }
            }

            // Check for duplicates.
            debug(1, " Checking for duplicate...");
            // Handle case where tuple has been removed from DOM tree
            Node element = (Node)_tupleNodeMap.get(tuple);
            if (element!=null && !isInDocument(element)) {
                debug(1, element.getNodeName()+" must have been deleted");
                _uniqueTuples.remove(tuple);
                _tupleNodeMap.remove(tuple);
            }


            if (_uniqueTuples.contains(tuple)) {
                debug(1, " Duplicate found. Field node: " + node);
                _duplicateTuples.add(tuple);
                _errorMessagePerNode.put(node, "Duplicate key '" + tuple + 
                                         "'");
            } else {
                debug(1, " Not a duplicate.");
                _uniqueTuples.add(tuple);
                _tupleNodeMap.put(tuple, selectorElement);
            }
        }


        /**
         * Checks to see if a node is still part of the DOM
         */
        private boolean isInDocument(Node element) {
            Node node = element;
            while (node!=null && (node instanceof Element))
                node = node.getParentNode();
            return (node!=null);
        }

        public String getErrorMessage(FieldNode node) {
            String error = (String) _errorMessagePerNode.get(node);
            if (error == null) {
                error = "";
            }
            return error;
        }

        /**
         * Stores the values for a set of fields that make up one instance of
         * a key. All these values together make up the key and their
         * combination needs to be unique.
         */
        class Tuple {
            public FieldNode[] nodes;
            public IDValue[] idValues;
            // Representation of the SimpleType types and values
            private String[] typeValues;

            public Tuple(int numFields) {
                nodes = new FieldNode[numFields];
                idValues = new IDValue[numFields];
                typeValues = new String[numFields];
            }

            public void addPrimitiveTypeValue(int type, String value, int i) {
                typeValues[i] = type+" "+value;
            }

            public boolean isDuplicateOf(Tuple tuple) {
                debug(1, "   Tuple.isDuplicateOf");
                // If it's exactly the same object, then it's not a duplicate.
                if (this == tuple)
                    return true;
                if (idValues.length != tuple.idValues.length) {
                    debug(1, 
                          "    Trying to compare tuples with different numbers of fields.");
                    return false;
                }
                if (idValues.length == 0) {
                    debug(1, "    Tuple has 0 idValues.");
                }
                for (int i = 0; i < tuple.idValues.length; i++) {
                    boolean isDuplicate = idValues[i].isDuplicateOf(
                                                  tuple.idValues[i]);
                    if (idValues[i].isDuplicateOf(EMPTY_IDVALUE) || 
                            idValues[i].isDuplicateOf(NOT_AN_IDVALUE) || 
                            tuple.idValues[i].isDuplicateOf(EMPTY_IDVALUE) || 
                            tuple.idValues[i].isDuplicateOf(NOT_AN_IDVALUE)) {
                        isDuplicate = false;
                    }
                    debug(1, 
                          "    Comparing " + idValues[i] + " with " + 
                          tuple.idValues[i] + ": " + isDuplicate);
                    if (!isDuplicate) {
                        return false;
                    }
                }
                debug(1, "    Tuple.isDuplicateOf: Returning true");
                return true;
            }

            public boolean equals(Object o) {
                debug(1, "   Tuple.equals");
                if (!(o instanceof Tuple)) {
                    return false;
                }
                Tuple other = (Tuple) o;
                return isDuplicateOf(other);
            }

            /**
             * Hash a tuple such that it can be stored in a hashtable
             * Need equivalent typeValues to hash to the same values
             */
            public int hashCode() {
                // Start with some hashcode for when idValues.length is 0
                int hash = 23551951;
                for (int i=0; i<idValues.length; i++) {
                    hash = hash ^ typeValues[i].hashCode();
                }
                return hash;
            }

            public String toString() {
                StringBuffer ret = new StringBuffer();
                ret.append('{');
                for (int i = 0; i < nodes.length; i++) {
                    String value = "";
                    if (nodes[i] != null)
                        value = nodes[i].getNodeValue();
                    ret.append(value);
                    if (i != (nodes.length - 1)) {
                        ret.append(", ");
                    }
                }
                ret.append('}');
                return ret.toString();
            }
        }

        class TupleComparator implements Comparator {
            public int compare(Object o1, Object o2) {
                debug(1, "  TupleComparator.compare(" + o1 + ", " + o2 + ")");
                if (!(o1 instanceof Tuple) || !(o2 instanceof Tuple)) {
                    debug(1, 
                          "  Comparator is trying to compare something that is not a Tuple.");
                    return 0;
                }
                Tuple t1 = (Tuple) o1;
                Tuple t2 = (Tuple) o2;
                if (t1.isDuplicateOf(t2)) {
                    return 0;
                }
                if (t1.idValues.length != t2.idValues.length) {
                    debug(1, 
                          "  Comparator is trying to compare tuples with different numbers of fields.");
                    return 0;
                }
                for (int i = 0; i < t1.idValues.length; i++) {
                    if (t1.idValues[i].isDuplicateOf(t2.idValues[i])) {
                        continue;
                    }
                    return t1.idValues[i].toString()
                                         .compareTo(t2.idValues[i].toString());
                }
                return -1;
            }

            public boolean equals(Object o) {
                debug(1, "  TupleComparator.equals");
                return false;
            }
        }
    }
}
