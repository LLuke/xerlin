package org.merlotxml.util.xml;


/**
 * Represents either an attribute or the simple content of an element.
 *
 * @author   Evert Hoff
 */
public interface GrammarSimpleType {
    /**
     * Gets the name attribute of the GrammarSimpleType object
     *
     * @return   The name value
     */
    public String getName();
    
    /**
     * Gets the parent complex type.
     */
    public GrammarComplexType getGrammarComplexType();

    /**
     * Gets the type attribute of the GrammarSimpleType object
     *
     * @return   The type value
     */
    public int getType();

    /**
     * Gets the defaultType attribute of the GrammarSimpleType object
     *
     * @return   The defaultType value
     */
    public int getDefaultType();

    /**
     * Gets the allowed values for this simple type.
     *
     * @return   The enumeration value
     */
    public String[] getEnumeration();

    /**
     * Gets the defaultValue attribute of the GrammarSimpleType object
     *
     * @return   The defaultValue value
     */
    public String getDefaultValue();

    /**
     * Gets whether this simple type must have a value. 
     * <p>
     * XML Schema:
     * <ul>
     *  <li>For an attribute, it returns whether it has use="required".
     *  <li>For simple content, it returns whether the element has nillable="true".
     * </ul>
     */
    public boolean getIsRequired();

    /**
     * Gets the primitiveType attribute of the GrammarSimpleType object. This is
     * when a simple type has been derived from another.
     *
     * @return   The primitiveType value
     */
    public String getPrimitiveType();

    /**
     * Validates the proposed content.
     *
     * @param valueNode  The Attr or Text that contains the value.
     * @return         The isValid value
     */
    public boolean getIsValid(FieldNode valueNode);

    /**
     * Validates the proposed content, and returns an error message, if any.
     *
     * @param value  Description of Parameter
     * @return       The validationMessage value
     */
    public String getValidationMessage(FieldNode valueNode);
    
    /**
     * To be called so that the grammar can keep track of IDs or Keys and 
     * ensure that they remain unique.
     * <p>
     * This should later be replaced by something that listens to events for
     * changes in the document and updates itself.
     */
    public void setValue(FieldNode fieldNode, String value);
}

