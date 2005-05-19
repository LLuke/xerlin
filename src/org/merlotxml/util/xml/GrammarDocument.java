package org.merlotxml.util.xml;

/**
 * Replaces DTDDocument and provides access to the grammars of DTDs and Schemas
 * through a single interface. Provides access to the definitions of elements
 * (complex types) and attributes (simple types).
 *
 * @author   Evert Hoff
 */
public interface GrammarDocument {
    /**
     * Gets the elements that are defined in the top level. For DTDs, this is
     * all elements. For Schemas, all the elements that are not defined within
     * the scope of other elements. Remember that with Schemas, complex types
     * might not always have names and more than one complex type with the same
     * name might exist, as long as they are defined within the scopes of
     * different parents. Thus, with Schemas a complex type can not be assumed
     * to be unique by name within the whole XSD. This is why we need to work
     * with complex types and not with elements.
     *
     * @return   The top level elements.
     */
    public GrammarComplexType[] getTopLevelGrammarComplexTypes();

    /**
     * Gets a specific top level complex type.
     *
     * @param name  The name of a named, top level element.
     * @return      A top level element with this name.
     */
    public GrammarComplexType getTopLevelGrammarComplexType(String name);

    /**
     * Gets a complex type that is defined within the scope of a parent based on
     * its name.
     *
     * @param parent  The parent element.
     * @param name    The child element name.
     * @return        The grammarComplexType value
     */
    public GrammarComplexType getGrammarComplexType(GrammarComplexType parent, String name);
}

