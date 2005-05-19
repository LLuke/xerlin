package org.merlotxml.util.xml.xerces;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;

/**
 * An implementation of GrammarSimpleType for Schemas.
 *
 * @author   everth
 */
public class SchemaGrammarSimpleTypeImpl implements GrammarSimpleType {
    /** Utility to minimise the output during debugging. */
    protected int _debugLevel = 0;
    private XSAttributeDeclaration _attrDeclaration;
    private XSAttributeUse _attributeUse = null;
    private SchemaGrammarComplexTypeImpl _complexType = null;
    private short _defaultType;
    private String _defaultValue;
    private XSElementDeclaration _elementDeclaration = null;
    private boolean _isRequired = false;
    private String _name = null;
    private XSSimpleTypeDefinition _primitiveTypeDefinition = null;
    private XSSimpleType _simpleType = null;
    private XSSimpleTypeDefinition _simpleTypeDefinition = null;
    private static ValidationContext _validationContext = new MyValidationContext();
    private int _type = -2; // Meaning not yet set.

    // These are not public in XSSimpleTypeDecl, so we need to copy them here.
    static final int PRIMITIVE_NOTATION = XSSimpleType.PRIMITIVE_NOTATION; // 19
    static final int DV_NOTATION      = PRIMITIVE_NOTATION; // 19
    static final int DV_ANYSIMPLETYPE = 0; // 0
    static final int DV_ID            = DV_NOTATION + 1; // 20
    static final int DV_IDREF         = DV_NOTATION + 2; // 21
    static final int DV_ENTITY        = DV_NOTATION + 3; // 22
    static final int DV_LIST          = DV_NOTATION + 4; // 23
    static final int DV_UNION         = DV_NOTATION + 5; // 24

    /**
     * Constructor for an attribute.
     */
    public SchemaGrammarSimpleTypeImpl(SchemaGrammarComplexTypeImpl complexType,
                                       XSAttributeUse attributeUse) {
        _complexType = complexType;
        _attributeUse = attributeUse;
        _attrDeclaration = _attributeUse.getAttrDeclaration();
        _simpleTypeDefinition = _attrDeclaration.getTypeDefinition();
        _primitiveTypeDefinition = _simpleTypeDefinition.getPrimitiveType();
        _defaultType = _attributeUse.getConstraintType();
        _isRequired = _attributeUse.getRequired();
        _defaultValue = _attrDeclaration.getConstraintValue();
        if (_defaultValue == null)
            _defaultValue = _attributeUse.getConstraintValue();
        _name = _attrDeclaration.getName();
        debug(3, "  Created SchemaGrammarSimpleTypeImpl for attribute "
             + complexType.getName() + ":" + getName());
        debug(3, "   Default value: " + _defaultValue);
        if (_simpleTypeDefinition != null && _simpleTypeDefinition instanceof XSSimpleType)
            _simpleType = (XSSimpleType)_simpleTypeDefinition;
        String simpleTypeDefinitionClass = null;
        if (_simpleTypeDefinition != null)
            simpleTypeDefinitionClass = _simpleTypeDefinition.getClass().getName();
        String primitiveTypeDefinitionClass = null;
        if (_primitiveTypeDefinition != null)
            primitiveTypeDefinitionClass = _primitiveTypeDefinition.getClass().getName();
        if (_simpleType == null && _primitiveTypeDefinition instanceof XSSimpleType)
            _simpleType = (XSSimpleType)_primitiveTypeDefinition;
        //debug(1, "  Simple type definition: " + simpleTypeDefinitionClass);
        //debug(1, "  Primitive type definition: " + primitiveTypeDefinitionClass);
    }

    /**
     * Constructor for the simple content of an element.
     */
    public SchemaGrammarSimpleTypeImpl(SchemaGrammarComplexTypeImpl complexType,
                                       XSElementDeclaration elementDeclaration) {
        _complexType = complexType;
        _elementDeclaration = elementDeclaration;
        _defaultType = _elementDeclaration.getConstraintType();
        _defaultValue = _elementDeclaration.getConstraintValue();
        _isRequired = !_elementDeclaration.getNillable();
        // It won't have a name like an attribute, so we use the node name instead.
        _name = "#text";
        debug(3, "  Created SchemaGrammarSimpleTypeImpl for simple content "
             + complexType.getName() + ":" + getName());
        debug(3, "   Default value: " + _defaultValue);
        XSTypeDefinition typeDefinition = elementDeclaration.getTypeDefinition();
        XSSimpleTypeDefinition simpleTypeDefinition = null;
        if (typeDefinition instanceof XSComplexTypeDefinition) {
            XSComplexTypeDefinition complexTypeDefinition =
                (XSComplexTypeDefinition)typeDefinition;
            short contentType = complexTypeDefinition.getContentType();
            debug(3, "   contentType: " + contentType);
            _simpleTypeDefinition = complexTypeDefinition.getSimpleType();
        }
        else {
            debug(3, "   Simple Type Class: " + typeDefinition.getClass());
            _simpleTypeDefinition = (XSSimpleTypeDefinition) typeDefinition;
        }
        if (_simpleTypeDefinition != null) {
            _primitiveTypeDefinition = _simpleTypeDefinition.getPrimitiveType();
            if (_simpleTypeDefinition != null && _simpleTypeDefinition instanceof XSSimpleType)
                _simpleType = (XSSimpleType)_simpleTypeDefinition;
        }
        if (_simpleType == null && _primitiveTypeDefinition instanceof XSSimpleType)
            _simpleType = (XSSimpleType)_primitiveTypeDefinition;
        debug(3, "   Simple Type: " + _simpleType);
        String simpleTypeDefinitionClass = null;
        if (_simpleTypeDefinition != null)
            simpleTypeDefinitionClass = _simpleTypeDefinition.getClass().getName();
        debug(3, "   Simple type definition: " + simpleTypeDefinitionClass);
    }

    public int getDefaultType() {
        int type = DTDConstants.NONE;
        //short constraintType = _attributeUse.getConstraintType();
        switch (_defaultType) {
         case XSConstants.VC_NONE:
             type = DTDConstants.NONE;
             break;
         case XSConstants.VC_DEFAULT:
             type = DTDConstants.IMPLIED;
             break;
         case XSConstants.VC_FIXED:
             type = DTDConstants.FIXED;
             break;
        }
        // To do: There might possibly be a need to have a seperated method call
        // for getIsRequired, instead of passing this along as a default type.
        if (_isRequired)
            type = DTDConstants.REQUIRED;
        return type;
    }

    public String getDefaultValue() {
        String ret = _defaultValue;
        //String ret = _attrDeclaration.getConstraintValue();
        debug(2, "  Returning default value for " + getName() + ": " + ret);
        return ret;
    }

    public String[] getEnumeration() {
        debug(2, "Getting enumeration for " + getName());
        String[] ret = new String[0];
        StringList enumerations = _simpleTypeDefinition.getLexicalEnumeration();
        if (enumerations == null) {
            debug(2, " Lexical patterns is null.");
            if (_simpleType.getPrimitiveKind() == XSSimpleType.PRIMITIVE_BOOLEAN) {
                ret = new String[2];
                ret[0] = "true";
                ret[1] = "false";
                return ret;
            } else {
                return ret;
            }
        }
        ret = new String[enumerations.getLength()];
        for (int i = 0; i < enumerations.getLength(); i++) {
            String item = enumerations.item(i);
            debug(3, " Adding enumeration literal: " + item);
            ret[i] = item;
        }
        return ret;
    }
    
    public boolean getIsRequired() {
        // According to the Schema spec, a simple content with a KeyRef must 
        // always refer to a valid value. It can't be empty, even if the 
        // element is nillable. Silly, but true.
        if (_name.equalsIgnoreCase("#text") && getType() == DTDConstants.IDREF)
            return true;
        return _isRequired;
    }

    public boolean getIsValid(FieldNode valueNode) {
        boolean ret = false;
        debug(1, getName() + " getIsValid(" + valueNode + ")");
        String value = valueNode.getNodeValue();
        if (value == null || value.equals("")) {
            debug(2, "   Value is empty.");
            value = getDefaultValue();
        }
        if (value == null) {
            debug(2, "   Default value is null.");
            if (!getIsRequired()) {
                debug(2, "   Value not required. Returning valid.");
                return true;
            }
            value = "";
        }
        String message = getValidationMessage(valueNode);
        debug(1, "  Content valid: " + message);
        if (message == null || message.trim().equals("")) {
            message = getIdentityConstraintValidationMessage(valueNode);
            debug(2, "  Identity constraint valid: " + message);
        }
        if (message == null || message.trim().equals(""))
            ret = true;
        debug(1, "  Returning " + ret);
        return ret;
    }

    public String getName() {
        return _name;
    }
    
    public GrammarComplexType getGrammarComplexType() {
        return _complexType;
    }

    public String getPrimitiveType() {
        String ret = _primitiveTypeDefinition.getNamespace() + ":" +
            _primitiveTypeDefinition.getName();
        debug(2, "  Primitive Type: " + ret);
        return ret;
    }

    public int getType() {
        if (_type != -2)
            return _type;
        int ret = DTDConstants.CDATA;
        debug(2, "Getting type for simple type: " + getName());
        XSSimpleType simpleType = null;
        XSSimpleType primitiveType = null;
        if (_simpleTypeDefinition instanceof XSSimpleType)
            simpleType = (XSSimpleType) _simpleTypeDefinition;
        if (_primitiveTypeDefinition instanceof XSSimpleType)
            primitiveType = (XSSimpleType) _primitiveTypeDefinition;
        debug(3, " simpleType: " + simpleType);
        debug(3, " primitiveType: " + primitiveType);
        if (simpleType == null)
            simpleType = primitiveType;
        if (simpleType == null) {
            debug(1, " Could not determine type.");
            return ret;
        }
        int type = simpleType.getPrimitiveKind();
        debug(3, " Xerces type: " + type);
        switch (type) {
            case XSSimpleType.PRIMITIVE_BOOLEAN:
                ret = DTDConstants.TOKEN_GROUP;
                break;
            case DV_ID :
                ret = DTDConstants.ID;
                break;
            case DV_IDREF :
                ret = DTDConstants.IDREF;
                break;
            case DV_LIST :
                ret = DTDConstants.TOKEN_GROUP;
                break;
            default :
                SchemaIdentityConstraintValidator validator =
                    _complexType
                        .getSchemaGrammarDocumentImpl()
                        .getSchemaIdentityConstraintValidator();
                if (validator == null) {
                    //debug(1, " Can't determine if type is KeyRef. Validator is null.");
                    break;
                }
                boolean isKeyRef = validator.getIsKeyRefField(this);
                if (isKeyRef)
                    ret = DTDConstants.IDREF;
        }
        // Variety == 2 for type IDREFS (plural), e.g. "subordinates" in 
        // personal-schema.xml. Need to implement a multi-selection list 
        // box for this later.
        /*
        short variety = simpleType.getVariety();
        debug(1, " Xerces variety: " + variety);
        switch (variety) {
            case XSSimpleTypeDecl.VARIETY_LIST:
            ret = DTDConstants.TOKEN_GROUP;
            break;
        }
        */
        boolean hasEnumeration = simpleType.isDefinedFacet(XSSimpleTypeDefinition.FACET_ENUMERATION);
        if (hasEnumeration) {
            ret = DTDConstants.TOKEN_GROUP;
        }
        debug(2, " Returning type " + ret);
        _type = ret;
        return ret;
    }

    public String getValidationMessage(FieldNode valueNode) {
        debug(1, "  Validating simple type " + getName());
        String ret = "";
        if (valueNode == null)
            return ret;
        String value = valueNode.getNodeValue();
        if (value == null || value.equals("")) {
            debug(2, "   Value is empty.");
            value = getDefaultValue();
        }
        if (value == null) {
            debug(2, "   Default value is null.");
            if (!getIsRequired()) {
                debug(2, "   Value not required. Returning valid.");
                return ret;
            }
            value = "";
        }
        debug(1, "   Using value: " + value);
        if (_simpleType == null) {
            debug(1, "   _simpleType is null.");
            return ret;
        }
        ValidatedInfo info = new ValidatedInfo();
        try {
            _simpleType.validate(value, _validationContext, info);
        } catch (Exception ex) {
            debug(2, "   InvalidDatatypeValueException: " + ex);
            ret = ex.getMessage();
        }
        debug(2, "   Normalized value: " + info.normalizedValue);
        debug(2, "   Actual value: " + info.actualValue);
        if (ret == null || ret.equals("")) {
            ret = getIdentityConstraintValidationMessage(valueNode);
        }
        debug(1, "   Returning message: '" + ret + "'");
        return ret;
    }
    
    private String getIdentityConstraintValidationMessage(FieldNode node) {
        String ret = "";
        SchemaIdentityConstraintValidator identityConstraintValidator =
            _complexType
                .getSchemaGrammarDocumentImpl()
                .getSchemaIdentityConstraintValidator();
        if (identityConstraintValidator == null) {
            MerlotDebug.msg("Cannot validate identity constraints - validator is null.");
            return ret;
        }
        ret = identityConstraintValidator.getErrorMessage(node);
        if (ret == null)
            ret = "";
        return ret;
    }
    
    public XSSimpleType getXSSimpleType() {
        return _simpleType;
    }
    
    public void setValue(FieldNode fieldNode, String value) {
        SchemaIdentityConstraintValidator validator =
            _complexType
                .getSchemaGrammarDocumentImpl()
                .getSchemaIdentityConstraintValidator();
        validator.setValue(fieldNode, value);
    }
    
    protected void debug(int level, String message) {
        if (level <= _debugLevel)
            MerlotDebug.msg(message);
    }

    static class MyValidationContext implements ValidationContext {
        public void addId(String name) {
        }

        // idref
        public void addIdRef(String name) {
        }

        // get symbol from symbol table
        public String getSymbol(String symbol) {
            return null;
        }

        // qname
        public String getURI(String prefix) {
            return null;
        }

        // entity
        public boolean isEntityDeclared(String name) {
            return true;
        }

        public boolean isEntityUnparsed(String name) {
            return true;
        }

        // id
        public boolean isIdDeclared(String name) {
            return false;
        }

        // whether to do extra id/idref/entity checking
        public boolean needExtraChecking() {
            return true;
        }
        // whether to validate against facets
        public boolean needFacetChecking() {
            return true;
        }

        // whether we need to normalize the value that is passed!
        public boolean needToNormalize() {
            return true;
        }

        public boolean useNamespaces() {
            // TODO Auto-generated method stub
            return false;
        }
    }
    
    public int getMinLength() {
        String facet = _simpleTypeDefinition.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MINLENGTH);
        //MerlotDebug.msg("Trying to parse min length from facet '" + facet + "'");
        try {
            return (new Integer(facet)).intValue();
        } catch (java.lang.NumberFormatException ex) {}
        return -1;
}

    public int getMaxLength() {
        String facet = _simpleTypeDefinition.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXLENGTH);
        //MerlotDebug.msg("Trying to parse max length from facet '" + facet + "'");
        try {
            return (new Integer(facet)).intValue();
        } catch (java.lang.NumberFormatException ex) {}
        return -1;
    }
}

