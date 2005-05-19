package org.merlotxml.util.xml.xerces;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xni.grammars.Grammar;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarDocument;

/**
 * An implementation of GrammarDocument for Schemas.
 *
 * @author   Evert Hoff
 */
public class SchemaGrammarDocumentImpl implements GrammarDocument {
    private Vector _complexTypes = new Vector();
    private Vector _declaredComplexTypes = new Vector();
    private Hashtable _elementDeclarationComplexTypes = new Hashtable();
    private SchemaGrammar _grammar = null;
    private Grammar[] _grammars = null;
    private XSModel _model = null;
    private Hashtable _namedComplexTypes = new Hashtable();
    private SchemaIdentityConstraintValidator _schemaIdentityConstraintValidator;

    /**
     * Constructor for the SchemaGrammarDocumentImpl object. Called from
     * DOMLiaison.parseValidXMLStream()
     *
     * @param grammars  Description of Parameter
     */
    public SchemaGrammarDocumentImpl(Grammar[] grammars) {
        long start = System.currentTimeMillis();
        _grammars = grammars;
        // ToDo: We are ignoring the rest of the grammars for the moment.
        // Still need to find out under which circumstances we'll get more than one. --Evert
        _grammar = (SchemaGrammar)_grammars[0];
        _model = _grammar.toXSModel();
        XSNamedMap topLevelElementDecls = _grammar.getComponents(XSConstants.ELEMENT_DECLARATION);
        for (int i = 0; i < topLevelElementDecls.getLength(); i++) {
            XSElementDeclaration elementDecl =
                (XSElementDeclaration)topLevelElementDecls.item(i);
            GrammarComplexType complexType =
                new SchemaGrammarComplexTypeImpl(this, elementDecl);
            String name = elementDecl.getName();
            _complexTypes.add(complexType);
            _namedComplexTypes.put(name, complexType);
            addComplexType(elementDecl, complexType);
        }
        long end = System.currentTimeMillis();
        System.out.println("Duration - initializing grammar document: " +
                           (end - start) + " ms.");
    }

    /**
     * Gets the grammarComplexType attribute of the SchemaGrammarDocumentImpl
     * object
     *
     * @param parent  Description of Parameter
     * @param name    Description of Parameter
     * @return        The grammarComplexType value
     */
    public GrammarComplexType getGrammarComplexType(GrammarComplexType parent, String name) {
        String parentName = null;
        if (parent != null)
            parentName = parent.getName();
        //MerlotDebug.msg("SchemaGrammarDocumentImpl.getGrammarComplexType(" + parentName + ", " + name + ")");
        if (parent == null)
            return getTopLevelGrammarComplexType(name);
        return parent.getChild(name);
    }

    /**
     * Gets the topLevelGrammarComplexType attribute of the
     * SchemaGrammarDocumentImpl object
     *
     * @param name  Description of Parameter
     * @return      The topLevelGrammarComplexType value
     */
    public GrammarComplexType getTopLevelGrammarComplexType(String name) {
        return (GrammarComplexType)_namedComplexTypes.get(name);
    }

    /**
     * Gets the topLevelGrammarComplexTypes attribute of the
     * SchemaGrammarDocumentImpl object
     *
     * @return   The topLevelGrammarComplexTypes value
     */
    public GrammarComplexType[] getTopLevelGrammarComplexTypes() {
        GrammarComplexType[] ret = new GrammarComplexType[0];
        ret = (GrammarComplexType[])_complexTypes.toArray(ret);
        return ret;
    }

    /**
     * Gets called from the constructors of children, so that the document knows
     * about all of them.
     *
     * @param decl         The feature to be added to the ComplexType attribute
     * @param complexType  The feature to be added to the ComplexType attribute
     */
    protected void addComplexType(XSElementDeclaration decl, GrammarComplexType complexType) {
        _elementDeclarationComplexTypes.put(decl, complexType);
    }

    /**
     *
     * Allow access to a complex type - this may be required when an element is used
     * in different parts of the schema
     *
     */
    protected GrammarComplexType getComplexType(XSElementDeclaration decl) {
        return (GrammarComplexType)_elementDeclarationComplexTypes.get(decl);
    }


    /*
        The _declaredComplexTypes contains any XSElementDeclarations that have
        been declared. This is needed to prevent loops in the parsing of the
        schema
    */
    protected void addDeclaredType(XSElementDeclaration decl) {
        _declaredComplexTypes.add(decl);
    }
 
    protected boolean isDeclaredType(XSElementDeclaration decl) {
        return (_declaredComplexTypes.contains(decl));
    }

    /**
     * Method setSchemaIdentityConstraintValidator.
     * @param schemaIdentityConstraintValidator
     */
    public void setSchemaIdentityConstraintValidator(SchemaIdentityConstraintValidator schemaIdentityConstraintValidator) {
        _schemaIdentityConstraintValidator = schemaIdentityConstraintValidator;
    }

    /**
     * Returns the _schemaIdentityConstraintValidator.
     * @return SchemaIdentityConstraintValidator
     */
    public SchemaIdentityConstraintValidator getSchemaIdentityConstraintValidator() {
        return _schemaIdentityConstraintValidator;
    }

}

