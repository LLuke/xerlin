package org.merlotxml.util.xml.xerces;
import java.util.Vector;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLAttributeDecl;
import org.apache.xerces.impl.dtd.XMLElementDecl;
import org.apache.xerces.impl.dtd.models.ContentModelValidator;
import org.apache.xerces.xni.QName;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.GrammarComplexType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DTDGrammarComplexTypeImpl extends GrammarComplexType {
    //private XMLElementDecl _elementDecl = null;
    private int _elementDeclIndex = -1;
    private DTDGrammar _grammar = null;
    private DTDGrammarDocumentImpl _grammarDocument = null;
    private DTDGrammarSimpleTypeImpl _simpleContentSimpleType = null;
    private XMLElementDecl _elementDecl = null;
    /** The parent element within which this is defined. */
    protected GrammarComplexType _parentComplexType = null;

    public DTDGrammarComplexTypeImpl(DTDGrammarDocumentImpl grammarDocument,
                                     int elementDeclIndex,
                                     XMLElementDecl elementDecl) {
        _grammarDocument = grammarDocument;
        _grammar = _grammarDocument.getGrammar();
        _elementDeclIndex = elementDeclIndex;
        _elementDecl = elementDecl;
        debug(3, "Creating DTDGrammarComplexTypeImpl " + getName());
        int index = _grammar.getFirstAttributeDeclIndex(_elementDeclIndex);
        while (index != -1) {
            XMLAttributeDecl attributeDecl = new XMLAttributeDecl();
            boolean found = _grammar.getAttributeDecl(index, attributeDecl);
            if (found) {
                DTDGrammarSimpleTypeImpl simpleType =
                    new DTDGrammarSimpleTypeImpl(this, index, attributeDecl);
                _attributes.add(simpleType);
                _namedAttributes.put(simpleType.getName(), simpleType);
            }
            index = _grammar.getNextAttributeDeclIndex(index);
            debug(3, "Next attribute decl index: " + index);
        }
    }

    public GrammarComplexType getChild(String name) {
        return _grammarDocument.getTopLevelGrammarComplexType(name);
    }

    public GrammarComplexType[] getChildren() {
        // This is wrong and will cause getInsertableElements to be slightly
        // slower, but couldn't find a right way to do it. --Evert
        GrammarComplexType[] ret = _grammarDocument.getTopLevelGrammarComplexTypes();
        return ret;
    }

    public GrammarComplexType getParentComplexType(Element el) {
        return _parentComplexType;
    }
    
    public String getDocumentation() {
        return "Documentation is not yet implemented.";
    }

    public boolean getIsEachAttributeValid(Element el) {
        return true;
    }

    public boolean getIsNillable() {
        // Will always be false for DTDs.
        return false;
    }

    public boolean getIsSimpleContentAllowed() {
        if (_elementDecl.type == XMLElementDecl.TYPE_ANY ||
            _elementDecl.type == XMLElementDecl.TYPE_MIXED ||
            _elementDecl.type == XMLElementDecl.TYPE_SIMPLE)
            return true;
        return false;
    }

    public boolean getIsSimpleContentValid(Element el) {
        return true;
    }

    public int getMaxOccurs() {
        throw new UnsupportedOperationException("Not yet implemented.");
        // ToDo: Not correct yet.
    }

    public int getMinOccurs() {
        throw new UnsupportedOperationException("Not yet implemented.");
        // ToDo: Not correct yet.
    }

    public String getName() {
        return _elementDecl.name.localpart;
    }

    /*
        This is needed to set the _parentComplexType
        otherwise DTDs cannot determine that a child is in
        an incorrect location
    */
    public boolean getIsLocationValid(Element el) {
        Node parentNode = el.getParentNode();
        if (!(parentNode instanceof Element)) {
            MerlotDebug.msg(" Parent node is not an element.");
            return true;
        }
        // At top of tree, will always be valid.
        if (_parentComplexType == null || 
                !_parentComplexType.getName().equals(parentNode.getNodeName())) {
            _parentComplexType = 
                _grammarDocument.getTopLevelGrammarComplexType(
                                                    parentNode.getNodeName());
        }
        return super.getIsLocationValid(el);
    }


    public boolean isValid(Element el) {
        boolean ret = false;
        debug(2, "Validating " + el.getNodeName());
        Node parentNode = el.getParentNode();
        if (!(parentNode instanceof Element)) {
            debug(3, " Parent node is not an element.");
            int result = validate(el);
            return (result==-1);
        }
        String parentName = parentNode.getNodeName();
        if (_parentComplexType==null)
            _parentComplexType = _grammarDocument.getTopLevelGrammarComplexType(parentName);
        int valid = _parentComplexType.validate((Element)parentNode);
        debug(2, "   Error Position: " + valid);
        if (valid == -1)
            ret = true;
        else if (valid != getPositionAmongPeers(parentNode, el)) {
            // This means one of the children is wrong, but not this one.
            ret = true;
        }

        // If there are no children check the element itself for validity
        if (ret && el.getChildNodes().getLength()==0) {
            int result = validate(el);
            ret = (result==-1);
        }
            
        return ret;
    }

    /* 
        Used to check the vailidity of an element
        This can be removed when getMinOccurs is implemented
    */
    public boolean isElementValid(Element el) {
        if (!(el instanceof Element))
            return true;
        int result = validate(el);
        return (result==-1);
    }


    public int validate(Element el) {
        int ret = -1;
        String elementName = el.getNodeName();
        String[] children = getChildNodeNamesWithoutText(el);
        debug(2, "   Validating structure for " + elementName
             + ": " + toString(children));
        return validate(children);
    }
    
    public void resetValidation(Element el) {
    }

    public boolean isEmptyType() {
        return (_elementDecl.type == XMLElementDecl.TYPE_EMPTY);
    }

    public boolean isMixedType() {
        return (_elementDecl.type == XMLElementDecl.TYPE_MIXED);
    }

    protected int validate(String[] children) {
        int ret = -1;
        debug(4, "   Element Declaration: " + getName());
        // REVISIT:
        // This is a temp workaround, due to a bug when there are no children.
        if (children.length == 0) {
            debug(3, "   Valid because there are no children.");
            //return -1;
        }

        ContentModelValidator model = _elementDecl.contentModelValidator;
        if (model == null) {
            debug(1, "   Model is null for element " + getName());
            return ret;
        }
        debug(4, "   Model: " + model.getClass());

        Vector v = new Vector();
        for (int i = 0; i < children.length; i++) {
            String child = children[i];
            QName qName = convertName(child);
            // REVISIT: Throw an exception if the child node name could not
            // be found
            if (qName != null) {
                v.add(qName);
                debug(4, "    Adding QName: " + qName);
            }
        }

        QName[] childrenSpec = new QName[0];
        childrenSpec = (QName[])v.toArray(childrenSpec);
        // Not sure if it is still necessary to do this
        if (childrenSpec == null) {
            childrenSpec = new QName[0];
        }
        debug(4, "    Length: " + childrenSpec.length);
        debug(4, "    Validating childrenspec: ");
        for (int i = 0; i < childrenSpec.length; i++)
            debug(4, "    QName: " + childrenSpec[i]);
        int result = -1;
        try {
            result = model.validate(childrenSpec, 0, childrenSpec.length);
            // This code overcomes a bug in the SimpleContentModel of Xerces
            // it's been reported with a patch as a fix so should be fixed in 
            // coming releases
            // The bug affects elements of type C(D, E) the validator in this
            // case allows ANY content
            // This workaround tests to see if an arbitrary sequence of characters
            // is treated as allowable - if so we overcome the bug by telling
            // the validator that 2 elements are being passed in
            if (model instanceof org.apache.xerces.impl.dtd.models.SimpleContentModel
                && result==1 && childrenSpec.length==1) {
                // Check if it's allowing any old rubish through
                QName [] testQ = new QName[1];
                testQ[0] = convertName("QwErTyZxCvB");
                int check = model.validate(testQ, 0, 1);
                if (check==1) {
                    debug(4, "SimpleContentModel workaround for "+childrenSpec[0]);
                    // Needs to be of length 2 to work - bug in SimpleContentModel
                    result = model.validate(childrenSpec, 0, 2);
                }
            }
        } catch (Exception ex) {
            debug(3, "   Exception validating content: " + ex);
            ret = 0;
        }
        if (result > -1) {
            ret = result;
            debug(3, "   Problem child at position " + result);
        }
        debug(4, "   Valid? " + ret);
        return ret;
    }

    private QName convertName(String name) {
        QName ret = null;
        if (name.equalsIgnoreCase("#text"))
            ret = new QName();
        else
            ret = new QName(null, name, name, null);
        return ret;
    }
}

