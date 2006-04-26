package org.merlotxml.util.xml;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.xerces.DTDGrammarComplexTypeImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides access to the grammar for an element. This class is extended by
 * xerces.DTDGrammarComplexTypeImpl and xerces.SchemaGrammarComplexTypeImpl.
 *
 * @author   Evert Hoff
 */
public abstract class GrammarComplexType {

    /**
     * The possible attributes that are defined for this element. Contains
     * GrammarSimpleType objects.
     */
    protected Vector _attributes = new Vector();
    /** GrammarSimpleType objects hashed by name */
    protected Hashtable _namedAttributes = new Hashtable();
    /** The definition for the node value, or #PCDATA, of this element. */
    protected GrammarSimpleType _simpleContent = null;
    /** Utility to minimise the output during debugging. */
    protected int debugLevel = 0;

    public static long totalDurationIsEachAttributeValid = 0;
    public static long totalDurationIsSimpleContentValid = 0;
    public static long totalDurationIsLocationValid = 0;

    /**
     * Gets a named attribute.
     *
     * @param name  The name of the attribute.
     * @return      The attribute definition.
     */
    public GrammarSimpleType getAttribute(String name) {
        return (GrammarSimpleType)_namedAttributes.get(name);
    }

    /**
     * Gets all the defined attributes.
     *
     * @return   All attributes.
     */
    public GrammarSimpleType[] getAttributes() {
        GrammarSimpleType[] ret = new GrammarSimpleType[0];
        ret = (GrammarSimpleType[])_attributes.toArray(ret);
        return ret;
    }

    /**
     * Gets a locally defined element by name.
     *
     * @param name  The name of the child element.
     * @return      The child.
     */
    public abstract GrammarComplexType getChild(String name);

    /**
     * Gets all the children defined within the scope of this parent.
     *
     * @return   The children.
     */
    public abstract GrammarComplexType[] getChildren();

    public abstract GrammarComplexType getParentComplexType(Element el);

    /**
     * Gets the value of the <xs:annotation><xs:documentation>value
     * </xs:documentation></xs:annotation> defined in the Schema. This is a
     * useful place to store help documentation about each element.
     *
     * @return   The documentation value
     */
    public abstract String getDocumentation();

    /**
     * Gets the position where a child named childElementName may be inserted.
     *
     * @param parent            The parent to hold this child.
     * @param childElementName  The node name of the child.
     * @return                  The first zero-based position where this child
     *                          may be inserted.
     */
    public int getInsertPosition(Node parent, String childElementName) {
        debug(1, "getInsertPosition(" + parent.getNodeName() + ", " + childElementName + ")");
        String parentName = parent.getNodeName();
        String[] currentChildElements = getChildNodeNames(parent);
        int insertPosition = -1;
        // All the available insert positions
        outer :
        for (int i = 0; i <= currentChildElements.length; i++) {
            String currentChild = null;
            if (i < currentChildElements.length) {
                currentChild = currentChildElements[i];
            }
            debug(1, " Considering " + currentChild);
            GrammarComplexType[] insertables = getInsertableElements(parent, i);
            for (int j = 0; j < insertables.length; j++) {
                GrammarComplexType el = insertables[j];
                if (el!=null) {
                	debug(1, "  Insertable " + el.getName());
                    String name = el.getName();
                    if (name.equals(childElementName)) {
                    	debug(1, "  Insertable at position " + i);
                        insertPosition = i;
                    }
                }   
            }
        }
        if (insertPosition == -1)
            insertPosition = currentChildElements.length;
        debug(1, " Returning " + insertPosition);
        return insertPosition;
    }

    /**
     * Gets all the child complex types that may be inserted as children of the
     * node at the given position.
     *
     * @param node   The parent node.
     * @param index  The zero-based position where a child node is to be
     *      inserted.
     * @return       The possible complex types from which one may be chosen.
     */
    public GrammarComplexType[] getInsertableElements(Node node, int index) {
        debug(1, "  getInsertableElements(" + node.getNodeName() + ", " + index + ")");
        GrammarComplexType[] ret = new GrammarComplexType[0];
        if (!(node instanceof Element))
            return ret;
        Element el = (Element)node;
        String elementName = el.getNodeName();
        String[] currentChildElements = getChildNodeNamesWithoutText(el);
        // Recalculate the index as if the #text nodes never existed.
        index = getIndexWithoutTextNodes(el, index);
        debug(4, "  new index: " + index);
        String[] results = whatCanGoHere(currentChildElements, index);
        List list = new Vector();
        for (int i = 0; i < results.length; i++) {
            String result = results[i];
            GrammarComplexType type = getChild(result);
            debug(1, "   Result: " + result + " Type: " + type);
            list.add(type);
        }
        ret = (GrammarComplexType[])list.toArray(ret);
        String[] retNames = getNames(ret);
		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 1) {
        	MerlotDebug.msg(" returning getInsertableElements(node, "
        			+ index + "): " + toString(retNames));
        }
        return ret;
    }

    /**
     * Gets all the possible insertable elements that are valid somewhere
     * between the current children of the node.
     *
     * @param node  The parent node.
     * @return      The possible elements, from which one may be chosen.
     */
    public GrammarComplexType[] getInsertableElements(Node node) {
        GrammarComplexType[] ret = new GrammarComplexType[0];
        Element el = null;
        if (node instanceof Element)
            el = (Element)node;
        String elementName = el.getNodeName();
        String[] currentChildElements = getChildNodeNames(el);
		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 1) {
        	MerlotDebug.msg("getInsertableElements(" + node.getNodeName()
        			+ "): Children: " + toString(currentChildElements));
        }
        // Use a list temporarily to maintain the sequence
        List list = new Vector();
        int length = currentChildElements.length;
        //Optimised for MixedContent types - same inserts at every index
        //
        // I don't think this is true for Schemas. I hope this is the right
        // way to let it optimise only for DTDs. --Evert
        if (isMixedType() && this instanceof DTDGrammarComplexTypeImpl)
            length = 0;
        for (int i = 0; i <= length; i++) {
            GrammarComplexType[] insertables = getInsertableElements(el, i);
            for (int j = 0; j < insertables.length; j++) {
                GrammarComplexType insertable = insertables[j];
                list.add(insertable);
            }
        }
        // Remove duplicates while maintaining sequence
        Comparator c = new ListComparator(list);
        TreeSet noDuplicates = new TreeSet(c);
        noDuplicates.addAll(list);
        ret = (GrammarComplexType[])noDuplicates.toArray(ret);
        String[] retNames = getNames(ret);
		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 1) {
        	MerlotDebug.msg("returning getInsertableElements(node): "
        			+ toString(retNames));
        }
        return ret;
    }
    
    /**
     * Checks the validity of the values of each of the attributes.
     *
     * @param el  The element for which all attributes must be checked.
     * @return    False if any attribute is invalid.
     */
    public boolean getIsEachAttributeValid(Element el) {
        return true;
    }

    /**
     * Determines if the element may contain simple content (#PCDATA).
     *
     * @return   The isSimpleContentAllowed value
     */
    public abstract boolean getIsSimpleContentAllowed();

    /**
     * Validates the value of the simple content of this element. In other
     * words, the value of the #text node below this element is validated.
     *
     * @param el  The element to be validated.
     * @return    The isSimpleContentValid value
     */
    public boolean getIsSimpleContentValid(Element el) {
        return true;
    }

    /**
     * Checks whether this element is in an allowed place in the structure of
     * the document.
     *
     * @param el  The element to be validated.
     * @return    The valid value
     */
    public boolean getIsLocationValid(Element el) {
        long start = System.currentTimeMillis();
        boolean ret = false;
        debug(1, "Validating structure of " + el.getNodeName());
        Node parentNode = el.getParentNode();
        if (!(parentNode instanceof Element)) {
            MerlotDebug.msg(" GrammarComplexType.getIsLocationValid: Parent node is not an element.");
            return true;
        }
        GrammarComplexType parentComplexType = getParentComplexType(el);
        // At top of tree, will always be valid.
        if (parentComplexType == null) {
            MerlotDebug.msg(" parentComplexType is null.");
            return true;
        }
        int valid = parentComplexType.validate((Element)parentNode);
        debug(1, " Error Position: " + valid);
        if (valid == -1)
            ret = true;
        else if (valid != getPositionAmongPeers(parentNode, el)) {
            // This means one of the children is wrong, but not this one.
            ret = true;
        }
        long end = System.currentTimeMillis();
        totalDurationIsLocationValid += (end-start);
        return ret;
    }
  
    public boolean getIsComplete(Element el) {
        String missing = getFirstMissingChildName(el);
        if (missing == null)
            return true;
        return false;
    }
 
    public String getFirstMissingChildName(Element el) {
        //Handle special DTD case where getMinOccurs() is not
        //yet implemented
        if (this instanceof DTDGrammarComplexTypeImpl) {
            if (!((DTDGrammarComplexTypeImpl)this).isElementValid(el))
                return el.getNodeName();
            return null;
        }
        debug(2, "getFirstMissingChildName(" + el + ")");
        GrammarComplexType[] possibleChildren = getChildren();
        outer: for (int i = 0; i < possibleChildren.length; i++) {
            GrammarComplexType possibleChild = possibleChildren[i];
            String name = possibleChild.getName();
            int minOccurs = possibleChild.getMinOccurs();
            debug(2, " Possible Child '" + name + "' Min Occurs '"
            + minOccurs + "'");
            if (minOccurs >= 1) {
                NodeList children = el.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    String childNodeName = child.getNodeName();
                    if (childNodeName.equalsIgnoreCase(name))
                        continue outer;
                }
                debug(2, " Missing: " + name);
                return name;
            }
        }
        return null;   
    }
    
    public abstract void resetValidation(Element el);

    /**
     * Means whether an attribute xsi:nil='true' may be inserted into the
     * XML file.
     * <p>
     * For DTDs always returns false.
     * <p>
     * For Schemas, returns whether nillable has been set to true for this
     * element.
     */
    public abstract boolean getIsNillable();

    /**
     * Gets the maximum number of instances of this type of element that are
     * allowed.
     *
     * @return   The maxOccurs value, or -1 for infinite.
     */
    public abstract int getMaxOccurs();

    /**
     * Gets the valid minimum number of instances of this type of element.
     *
     * @return   The minOccurs value
     */
    public abstract int getMinOccurs();

    /**
     * Gets the name of this complex type. Might be null if it is an anonymous
     * complex type.
     *
     * @return   The name value
     */
    public abstract String getName();

    /**
     * Gets the GrammarSimpleType that defines the simple content (#PCDATA) of
     * this element.
     *
     * @return   The simpleContent value
     */
    public GrammarSimpleType getSimpleContent() {
        return _simpleContent;
    }

    /**
     * Validates the element in terms of its position in the structure of the
     * document.
     *
     * @param el  The element to be validated for structure.
     * @return    The position of the first child that is not valid.
     */
    public abstract int validate(Element el);

    /**
     * Utility for debugging.
     *
     * @param level  0=no debugging, 1 little bit, 2 more, etc.
     * @param msg    The message to print.
     */
    protected void debug(int level, String msg) {
        if (debugLevel >= level)
            MerlotDebug.msg(msg);
    }

    /**
     * Returns the node names of all the current children of the element.
     *
     * @param el  Description of Parameter
     * @return    The childNodeNames value
     */
    protected String[] getChildNodeNames(Node el) {
        Vector v = new Vector();
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = (Node)children.item(i);
            String childName = child.getNodeName();
            if (childName != null) {
                v.add(childName);
            }
        }
        String[] childNames = new String[0];
        childNames = (String[])v.toArray(childNames);
        return childNames;
    }

    /**
     * Returns the node names of all children of this node, except for the
     * children that are #text, #comment, or pi_node nodes.
     *
     * @param el  The parent element.
     * @return    The childNodeNamesWithoutText value
     */
    protected String[] getChildNodeNamesWithoutText(Node el) {
        Vector v = new Vector();
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = (Node)children.item(i);
            if (child instanceof Element) {
                String childName = child.getNodeName();
                v.add(childName);
            }
        }
        String[] childNames = new String[0];
        childNames = (String[])v.toArray(childNames);
        return childNames;
    }

    /**
     * Recalculates what a child position would have been if there weren't any
     * text nodes.
     *
     * @param el     The parent element.
     * @param index  The index of the child element.
     * @return       The indexWithoutTextNodes value
     */
    protected int getIndexWithoutTextNodes(Node el, int index) {
        int countWithoutText = 0;
        if (index < 0)
            index = 0;
        NodeList children = el.getChildNodes();
        for (int i = 0; i <= index; i++) {
            if (i >= index) {
                return countWithoutText;
            }
            if (i < children.getLength()) {
                Node child = (Node)children.item(i);
                if (child instanceof Element) {
                    countWithoutText++;
                }
            }
        }
        return 0;
    }

    /**
     * Convenience method to get an array of names from an array of complex
     * types.
     *
     * @param complexTypes  Description of Parameter
     * @return              The names value
     */
    protected String[] getNames(GrammarComplexType[] complexTypes) {
        Vector childNamesVector = new Vector();
        for (int i = 0; i < complexTypes.length; i++) {
            GrammarComplexType child = complexTypes[i];
            if (child != null) {
                String name = child.getName();
                childNamesVector.add(name);
            }
        }
        String[] childNames = new String[0];
        childNames = (String[])childNamesVector.toArray(childNames);
        return childNames;
    }

    /**
     * Gets the position of the child among its peers under the parent.
     *
     * @param parent  The parent node.
     * @param child   The child node.
     * @return        The position of this child.
     */
    protected int getPositionAmongPeers(Node parent, Node child) {
        int ret = 0;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = (Node)children.item(i);
            if (node.getNodeName().equalsIgnoreCase("#text"))
                continue;
            if (node == child)
                break;
            ret++;
        }
        return ret;
    }

    /**
     * Convenience method for debugging.
     *
     * @param strings  Description of Parameter
     * @return         Description of the Returned Value
     */
    protected String toString(String[] strings) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            ret.append(string);
            if (i < (strings.length - 1))
                ret.append(" ");
        }
        return ret.toString();
    }

    /**
     * Convenience method for debugging.
     *
     * @param strings  Description of Parameter
     * @return         Description of the Returned Value
     */
    protected String toString(Vector strings) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < strings.size(); i++) {
            String string = (String)strings.get(i);
            ret.append(string);
            if (i < (strings.size() - 1))
                ret.append(" ");
        }
        return ret.toString();
    }

    public String toString() {
        return getName();
    }

    /**
     * Validates the candidate node names for their position in the structure.
     *
     * @param candidateNodeNames  Description of Parameter
     * @return                    The position of the first child that is not
     *      valid.
     */
    protected abstract int validate(String[] candidateNodeNames);

    /**
     * The possible elements that could be inserted at this position, given the
     * current children.
     *
     * @param currentChildElements  The current children node names.
     * @param insertPosition        The zero-based position where a new child is
     *      to be inserted.
     * @return                      The possible element node names, of which
     *      one may be chosen.
     */
    protected String[] whatCanGoHere(String[] currentChildElements,
                                     int insertPosition) {
        long start = System.currentTimeMillis();
        if (isEmptyType())
            return new String[0];
		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 2) {
        	MerlotDebug.msg("  whatCanGoHere: " + insertPosition + " "
        			+ toString(currentChildElements));
        }
        if (insertPosition > currentChildElements.length) {
            debug(1, "  Insert position is beyond last child.");
            insertPosition = currentChildElements.length;
        }
        String[] childNames = getNames(getChildren());
		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 2) {
        	MerlotDebug.msg("  ChildNames: " + toString(childNames));
        }
        String[] candidates = new String[currentChildElements.length + 1];
        for (int i = 0; i < insertPosition; i++) {
            candidates[i] = currentChildElements[i];
        }
        for (int i = (insertPosition + 1); i < (currentChildElements.length + 1); i++) {
            candidates[i] = currentChildElements[i - 1];
        }

		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 2) {
        	MerlotDebug.msg("  Candidates: " + toString(candidates));
        }
        Vector results = new Vector();
        for (int i = 0; i < childNames.length; i++) {
            String candidate = childNames[i];
            debug(3, "  Candidate: " + candidate);
            candidates[insertPosition] = candidate;
            int valid = validate(candidates);
            debug(2, "  Candidate: " + candidate + " Valid: " + valid);
            // This gets all allowable elements - JML
            if (valid == -1 || valid==candidates.length)
                results.add(candidate);
        }
        String[] ret = new String[0];
        ret = (String[])results.toArray(ret);

		// this toString() is expensive... to prevent it from
		// unnecessarily being called, don't use debug() helper function 
        if(debugLevel >= 2) {
        	MerlotDebug.msg("  whatCanGoHere: " + toString(ret));
        }
        long end = System.currentTimeMillis();
        System.out.println("Duration - what can go here: " + (end-start) + " ms.");
        return ret;
    }

    /* Returns true if this element is of type EMPTY - and can contain no other elements */
    public abstract boolean isEmptyType();

    /* Returns true if this element is of type MIXED - and can be optimised */
    public abstract boolean isMixedType();

    /**
     * Sorts objects according to their positions in another list.
     *
     * @author   everth
     */
    protected class ListComparator implements Comparator {
        public List list;

        /**
         * Constructor for the ListComparator object
         *
         * @param list  Description of Parameter
         */
        public ListComparator(List list) {
            this.list = list;
        }

        public int compare(Object o1, Object o2)
             throws ClassCastException {
            int rank1 = list.indexOf(o1);
            int rank2 = list.indexOf(o2);
            if (rank1 == -1 || rank2 == -1)
                return 0;
            int ret = rank1 - rank2;
            return ret;
        }

        public boolean equals(Object obj) {
            boolean ret = false;
            if (obj instanceof ListComparator) {
                ListComparator lc = (ListComparator)obj;
                if (lc.list == this.list)
                    ret = true;
            }
            return ret;
        }
    }

}

