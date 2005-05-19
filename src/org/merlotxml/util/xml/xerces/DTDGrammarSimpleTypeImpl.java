package org.merlotxml.util.xml.xerces;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.impl.dtd.XMLAttributeDecl;
import org.apache.xerces.impl.dtd.XMLSimpleType;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DTDGrammarSimpleTypeImpl implements GrammarSimpleType {
    private XMLAttributeDecl _attributeDecl;
    private int _attributeDeclIndex;
    private DTDGrammarComplexTypeImpl _complexType = null;
    private XMLSimpleType _xmlSimpleType = null;

    public DTDGrammarSimpleTypeImpl(DTDGrammarComplexTypeImpl complexType,
                                    int attributeDeclIndex,
                                    XMLAttributeDecl attributeDecl) {
        _complexType = complexType;
        _attributeDeclIndex = attributeDeclIndex;
        _attributeDecl = attributeDecl;
        _xmlSimpleType = attributeDecl.simpleType;
        MerlotDebug.msg("Creating DTDGrammarSimpleTypeImpl " + getName());
    }

    public int getDefaultType() {
        int type = DTDConstants.IMPLIED;
        int xercesType = _xmlSimpleType.defaultType;
        switch (xercesType) {
         case XMLSimpleType.DEFAULT_TYPE_DEFAULT:
             type = DTDConstants.NONE;
             break;
         case XMLSimpleType.DEFAULT_TYPE_FIXED:
             type = DTDConstants.FIXED;
             break;
         case XMLSimpleType.DEFAULT_TYPE_IMPLIED:
             type = DTDConstants.IMPLIED;
             break;
         case XMLSimpleType.DEFAULT_TYPE_REQUIRED:
             type = DTDConstants.REQUIRED;
             break;
        }
        return type;
    }

    public String getDefaultValue() {
        // To do: Find out what _xmlSimpleType.nonNormalizedDefaultValue is for.
        return _xmlSimpleType.defaultValue;
    }

    public String[] getEnumeration() {
        return _xmlSimpleType.enumeration;
    }
    
    public boolean getIsRequired() {
        return getDefaultType() == DTDConstants.REQUIRED;
    }

    public boolean getIsValid(FieldNode valueNode) {
        // To do: Use xerces to validate. We can at least check if valid characters are
        // being used, as was previously done in GenericDOMEditPanel.
        return true;
    }

    public String getName() {
        return _attributeDecl.name.localpart;
    }
    
    public GrammarComplexType getGrammarComplexType() {
        return _complexType;
    }

    public String getPrimitiveType() {
        return "Not defined for DTDs.";
    }

    public boolean isList() {
        return _xmlSimpleType.list;
    }

    public int getType() {
        int type = DTDConstants.CDATA;
        int xercesType = _xmlSimpleType.type;
        switch (xercesType) {
         case XMLSimpleType.TYPE_CDATA:
             type = DTDConstants.CDATA;
             break;
         case XMLSimpleType.TYPE_ENTITY:
             // Don't know what to do here.
             //if (isList())
             //type = DTDConstants.ENTITIES;
             //else
             // type = DTDConstants.ENTITY;
             break;
         case XMLSimpleType.TYPE_ENUMERATION:
             type = DTDConstants.TOKEN_GROUP;
             break;
         case XMLSimpleType.TYPE_ID:
             type = DTDConstants.ID;
             break;
         case XMLSimpleType.TYPE_IDREF:
             //if (isList())
             //type = DTDConstants.IDREFS;
             //else
             type = DTDConstants.IDREF;
             break;
         case XMLSimpleType.TYPE_NMTOKEN:
             //if (isList())
             //type = DTDConstants.NMTOKENS;
             //else
             type = DTDConstants.NMTOKEN;
             break;
         case XMLSimpleType.TYPE_NOTATION:
             // Don't know what to do here.
             // type = DTDConstants.NOTATION;
             break;
         case XMLSimpleType.TYPE_NAMED:
             // Don't know what to do here.
             break;
        }
        return type;
    }

    public String getValidationMessage(FieldNode valueNode) {
       //Error message not yet implemented for DTDs.
       return "";
    }
    
    /**
     * Called from MerlotDOMElement.
     */
    public void setValue(FieldNode fieldNode, String value) {
        if (getType() == DTDConstants.ID) {
            Element element = (Element) fieldNode.getParentNode();
            if (element != null) {
                Document doc = element.getOwnerDocument();
                DocumentImpl docImpl = (DocumentImpl) doc;
                // This updates Xerces' internal map of IDs versus Elements and
                // allows the new id to be found with
                // org.w3c.dom.Document.getElementById( String id )
                // Xerces doesn't automatically update this map when an
                // attribute is set.
                docImpl.putIdentifier(fieldNode.getNodeName(), element);
            }
        }
    }
}

