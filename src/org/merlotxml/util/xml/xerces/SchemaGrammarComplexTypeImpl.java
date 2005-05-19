package org.merlotxml.util.xml.xerces;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.merlotxml.util.xml.FieldNode;
import org.merlotxml.util.xml.GrammarComplexType;
import org.merlotxml.util.xml.GrammarSimpleType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of GrammarComplexType for Schemas.
 *
 * @author   Evert Hoff
 */
public class SchemaGrammarComplexTypeImpl extends GrammarComplexType {
    private XSComplexTypeDefinition _complexTypeDefinition = null;
    private Vector _complexTypes = new Vector();
    private static Hashtable _deferredSGTs = new Hashtable();
    private XSElementDeclaration _elementDeclaration;
    private SchemaGrammarDocumentImpl _grammarDocument;
    private Hashtable _namedComplexTypes = new Hashtable();
    private XSSimpleTypeDefinition _simpleContentDefinition = null;
    private XSParticle _topParticle = null;
    public static final String INSERT_PLACE_HOLDER = "*INSERT*POSITION*";
    private Hashtable _validationResults = new Hashtable();
    private int _minOccurs = 0;
    private int _maxOccurs = 0;

    public SchemaGrammarComplexTypeImpl(SchemaGrammarDocumentImpl grammarDocument,
                                        XSElementDeclaration elementDeclaration) {
        this(grammarDocument, null, elementDeclaration, null);
    }

    public SchemaGrammarComplexTypeImpl(SchemaGrammarDocumentImpl grammarDocument,
                                        SchemaGrammarComplexTypeImpl parentComplexType,
                                        XSElementDeclaration elementDeclaration,
                                        XSParticle particle) {
        _grammarDocument = grammarDocument;
        _elementDeclaration = elementDeclaration;
        debug(3, "SchemaGrammarComplexTypeImpl(" + _elementDeclaration.getName() + ")");
        debug(3, " Constraint type: " + _elementDeclaration.getConstraintType());
        debug(3, " Constraint value: " + _elementDeclaration.getConstraintValue());
        debug(3, " Nillable: " + _elementDeclaration.getNillable());
        _simpleContent = new SchemaGrammarSimpleTypeImpl(this, _elementDeclaration);
        XSTypeDefinition typeDefinition = elementDeclaration.getTypeDefinition();
        debug(3, " Type Definition: " + typeDefinition.getClass());
        XSTypeDefinition baseTypeDefinition = typeDefinition.getBaseType();
        debug(3, " Base Type Definition: " + baseTypeDefinition.getClass());
        _topParticle = particle;
        debug(3, " Particle: " + particle);
        if (particle != null) {
            _minOccurs = particle.getMinOccurs();
            _maxOccurs = particle.getMaxOccurs();
            debug(3, " Min Occurs: " + _minOccurs + " Max Occurs: " + _maxOccurs);
        }
        XSSimpleTypeDefinition simpleTypeDefinition = null;
        if (typeDefinition instanceof XSComplexTypeDefinition) {
            XSComplexTypeDefinition _complexTypeDefinition =
                (XSComplexTypeDefinition)typeDefinition;
            debug(3, " Setting _complexTypeDefinition to " + _complexTypeDefinition);
            _simpleContentDefinition = _complexTypeDefinition.getSimpleType();
            _topParticle = _complexTypeDefinition.getParticle();
            debug(3, " _topParticle: " + _topParticle);
            XSObjectList attributeUses = _complexTypeDefinition.getAttributeUses();
            for (int i = 0; i < attributeUses.getLength(); i++) {
                XSAttributeUse attributeUse = (XSAttributeUse)attributeUses.item(i);
                XSAttributeDeclaration attrDeclaration =
                    attributeUse.getAttrDeclaration();
                String name = attrDeclaration.getName();
                debug(3, " Attribute use: " + name);
                GrammarSimpleType simpleType =
                    new SchemaGrammarSimpleTypeImpl(this, attributeUse);
                _namedAttributes.put(name, simpleType);
                _attributes.add(simpleType);
            }
        }
        if (typeDefinition instanceof XSSimpleTypeDefinition) {
            _simpleContentDefinition = (XSSimpleTypeDefinition)typeDefinition;
            _complexTypeDefinition = elementDeclaration.getEnclosingCTDefinition();
            if (_topParticle == null && _complexTypeDefinition != null)
                _topParticle = _complexTypeDefinition.getParticle();
        }
        addParticle(_topParticle);
    }
    
    /**
     * For some reason this becomes null after the constructor finished.
     * Use this to get the latest.
     */
    private XSComplexTypeDefinition getComplexTypeDefinition() {
        debug(3, "Getting complex type definition for " + getName());
        if (_elementDeclaration == null) {
            debug(3, " _elementDeclaration is null.");
            return null;
        }
        XSTypeDefinition typeDefinition = _elementDeclaration.getTypeDefinition();
        if (typeDefinition instanceof XSComplexTypeDefinition) {
            debug(3, " typeDefinition is complex.");
            _complexTypeDefinition = (XSComplexTypeDefinition)typeDefinition;
        } else if (typeDefinition instanceof XSSimpleTypeDefinition) {
            debug(3, " typeDefinition is simple.");
            _complexTypeDefinition = _elementDeclaration.getEnclosingCTDefinition();
            debug(3, " _complexTypeDefinition: " + _complexTypeDefinition);
        }
        if (_complexTypeDefinition == null) {
            _complexTypeDefinition = _elementDeclaration.getEnclosingCTDefinition();
            if (_complexTypeDefinition != null)
                _topParticle = _complexTypeDefinition.getParticle();
        }
        return _complexTypeDefinition;
    }

    public GrammarComplexType getChild(String name) {
        return (GrammarComplexType)_namedComplexTypes.get(name);
    }

    public GrammarComplexType[] getChildren() {
        GrammarComplexType[] ret = new GrammarComplexType[0];
        return (GrammarComplexType[])_complexTypes.toArray(ret);
    }

    public GrammarComplexType getParentComplexType(Element el) {
        debug(1, "getParentComplexType(" + el + ")");
        if (el == null)
            return null;
        Element[] elementPath = getElementPath(el);
        Element topLevelElement = elementPath[0];
        GrammarComplexType complexType = _grammarDocument.getTopLevelGrammarComplexType(topLevelElement.getNodeName());
        for (int i = 1; i < elementPath.length-1; i++) {
            debug(1, " complexType: " + complexType);
            if (complexType == null)
                return null;
            Element pathElement = elementPath[i];
            debug(1, "pathElement: " + pathElement.getNodeName());
            complexType = complexType.getChild(pathElement.getNodeName());
        }
        return complexType;
    }
    
    protected Element[] getElementPath(Element el) {
        Element[] elementPath = null;
        Node parentNode = el.getParentNode();
        if (parentNode instanceof Element) {
            Element[] parentElementPath = getElementPath((Element)parentNode);
            elementPath = new Element[parentElementPath.length+1];
            for (int i = 0; i < parentElementPath.length; i++) {
                elementPath[i] = parentElementPath[i];
            }
            elementPath[elementPath.length-1] = el;
        } else {
            elementPath = new Element[1];
            elementPath[0] = el;
        }
        return elementPath;
    }

    
    public String getDocumentation() {
        return "Documentation not implemented yet.";
    }

    public boolean getIsEachAttributeValid(Element el) {
        long start = System.currentTimeMillis();
        boolean ret = true;
        for (int i = 0; i < _attributes.size(); i++) {
            GrammarSimpleType attribute = (GrammarSimpleType)_attributes.get(i);
            String name = attribute.getName();
            Node attributeNode = el.getAttributeNode(name);
            FieldNode fieldNode = FieldNode.getFieldNode(el, attributeNode, name);
            ret = ret && attribute.getIsValid(fieldNode);
        }
        debug(1, "getIsEachAttributeValid(" + el.getNodeName() + "): " + ret);
        long end = System.currentTimeMillis();
        totalDurationIsEachAttributeValid += (end - start);
        return ret;
    }
    
    public boolean getIsNillable() {
        return _elementDeclaration.getNillable();
    }

    public boolean getIsSimpleContentAllowed() {
        if (_simpleContentDefinition == null)
            return false;
        short variety = _simpleContentDefinition.getVariety();
        if (variety == XSSimpleTypeDefinition.VARIETY_ABSENT)
            return false;
        return true;
    }

    public boolean getIsSimpleContentValid(Element el) {
        long start = System.currentTimeMillis();
        boolean ret = true;
        if (!getIsSimpleContentAllowed())
            return true;
        debug(1, "Validating simple content of " + el.getNodeName());
        if (_simpleContent != null) {
            Node valueNode = getTextNode(el);
            FieldNode fieldNode = FieldNode.getFieldNode(el, valueNode, "#text");
            ret = _simpleContent.getIsValid(fieldNode);
        } else
            debug(1, "Returning valid=true, because simple content is null.");
        debug(1, "getIsSimpleContentValid(" + el.getNodeName() + "): " + ret);
        long end = System.currentTimeMillis();
        totalDurationIsSimpleContentValid += (end - start);
        return ret;
    }
    
    protected Node getTextNode(Element el) {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("#text"))
                return child;
        }
        return null;
    }

    public int getMaxOccurs() {
        return _maxOccurs;
    }

    public int getMinOccurs() {
        return _minOccurs;
    }

    public String getName() {
        return _elementDeclaration.getName();
    }

    public int validate(Element el) {
        debug(2, "   Validating structure for element " + el 
        + " (" + el.hashCode() + ") against complextype " + getName());
        int ret = -1;
        String key = "" + el.hashCode();
        Integer result = (Integer)_validationResults.get(key);
        if (result != null) {
            ret = result.intValue();
            debug(2, "   Using stored result: " + ret);
            return ret;
        }
        String[] currentChildElements = getChildNodeNamesWithoutText(el);
        debug(2, "   Validating structure for " + el.getNodeName()
             + ": " + toString(currentChildElements));
        ret = validate(currentChildElements);
        _validationResults.put(key, new Integer(ret));
        return ret;
    }
    
    public void resetValidation(Element el) {
        String key = "" + el.hashCode();
        _validationResults.remove(key);
        Node parent = el.getParentNode();
        GrammarComplexType parentComplexType = getParentComplexType(el);
        if (parent != null && parentComplexType != null 
            && parent instanceof Element)
            parentComplexType.resetValidation((Element)parent);
    }

    public boolean isEmptyType() {
        if (_complexTypeDefinition == null)
            return false;
        return _complexTypeDefinition.getContentType()
            == XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
    }
    
    public XSElementDeclaration getXSElementDeclaration() {
        return _elementDeclaration;
    }

    public boolean isMixedType() {
        boolean ret = false;
        if (_complexTypeDefinition == null) {
            getComplexTypeDefinition();
        }
        if (_complexTypeDefinition != null)
            ret = _complexTypeDefinition.getContentType()
                    == XSComplexTypeDefinition.CONTENTTYPE_MIXED;
        else
            debug(1, "_complexTypeDefinition is null");
        debug(1, "isMixedType for " + getName() + ": " + ret);
        return ret;
    }

    protected int validate(String[] candidateNodeNames) {
        // Value for valid.
        int ret = -1;
        debug(3, " --------------------------------------------------");
        debug(4, "   Element Declaration: " + getName());
        Vector candidateNodeNamesVector = new Vector(Arrays.asList(candidateNodeNames));
        boolean valid = validate(_topParticle, candidateNodeNamesVector);
        int total = candidateNodeNames.length;
        // All valid ones have been removed from this Vector, until
        // the first problem has been encountered.
        int invalid = candidateNodeNamesVector.size();
        if (!valid || invalid > 0) {
            // The number of valid entries, and the position of the first problem.
            // Example: 3 total, 1 invalid: 2 valid; ret=2: #0=valid, #1=valid, #2=invalid
            // Example: 3 total, 2 invalid: 1 valid; ret=1: #0=valid, #1=invalid.
            // Example: 3 total, 3 invalid: 0 valid; ret=0: #0=invalid.
            // Example: 3 total, 0 invalid: 3 valid; ret=-1
            ret = total - invalid;
        }
        debug(3, "  Remaining: " + toString(candidateNodeNamesVector));
        debug(3, "  Returning: " + ret);
        return ret;
    }

    protected boolean validate(XSParticle particle, Vector candidateNodeNames) {
        return validate(particle, candidateNodeNames, 3);
    }

    protected boolean validate(XSParticle particle, Vector candidateNodeNames,
                               int level) {
        boolean ret = false;
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < level; i++)
            spaces.append(' ');
        debug(4, spaces + "Validating " + particle + ": " + toString(candidateNodeNames));
        if (particle == null)
            return true;
        if (particle == null) {
            debug(4, spaces + "Particle is null.");
            return true;
        }
        debug(4, spaces + "Particle: " + particle);
        XSTerm term = particle.getTerm();
        if (term == null) {
            debug(4, spaces + "Term is null.");
            return true;
        }
        debug(4, spaces + "Term: " + term.getClass() + ": " + term);
        if (candidateNodeNames.size() == 0) {
            // To do: If there are any required children, then it is not valid.
            return true;
        }
        String candidate = (String)candidateNodeNames.get(0);
        if (term instanceof XSElementDeclaration) {
            debug(4, spaces + "Element object: " + term);
            XSElementDeclaration elementDecl = (XSElementDeclaration)term;
            String name = elementDecl.getName();
            if (name.equalsIgnoreCase(candidate)) {
                debug(4, spaces + "Found " + name);
                // Found a valid one.
                candidateNodeNames.remove(0);
                ret = true;
                if (candidateNodeNames.isEmpty())
                    return ret;
            }
            else {
                debug(4, spaces + "Candidate not found here.");
            }
        }
        if (term instanceof XSModelGroup) {
            XSModelGroup modelGroup = (XSModelGroup)term;
            XSObjectList particles = modelGroup.getParticles();
            boolean groupValid = true;
            for (int i = 0; i < particles.getLength(); i++) {
                XSObject object = particles.item(i);
                debug(4, spaces + "Particle object: " + object.getClass());
                XSParticle childParticle = (XSParticle)object;
                boolean particleValid = true;
                int min = childParticle.getMinOccurs();
                int max = childParticle.getMaxOccurs();
                debug(4, spaces + "Min: " + min + " max: " + max);
                int j = 0;
                while (true) {
                    debug(4, spaces + "j: " + j);
                    boolean valid = validate(childParticle, candidateNodeNames, level + 1);
                    //particleValid = particleValid && valid;
                    debug(4, spaces + "ParticleValid: " + particleValid + " Valid: " + valid);
                    if (!valid) {
                        if (min == 0) {
                            debug(4, spaces + "Does not need to have this element.");
                            particleValid = true;
                            break;
                        }
                        if (j > min) {
                            debug(4, spaces + "Beyond minOccurs.");
                            break;
                        }
                        else {
                            debug(4, spaces + "Not yet beyond minOccurs.");
                        }
                    }
                    if (valid) {
                        if (max > -1 && (j >= max)) {
                            debug(4, spaces + "Exceeds maxOccurs.");
                            //FIXME
                            //TODO This code does not handle the XSModelGroup
                            //case - complex!
                            if (childParticle.getTerm() 
                                instanceof XSElementDeclaration) {
                                particleValid = false;
                                XSElementDeclaration childDecl =
                                    (XSElementDeclaration)childParticle.getTerm();
                                String name = childDecl.getName();
                                candidateNodeNames.insertElementAt(name, 0);
                            	break;
                            }
                        }
                    }
                    if (candidateNodeNames.isEmpty())
                        return true;
                    if ((childParticle.getMaxOccursUnbounded()) || (j < max))
                        j++;
                    else
                        break;
                }
                short compositor = modelGroup.getCompositor();
                if (compositor == XSModelGroup.COMPOSITOR_SEQUENCE) {
                    groupValid = groupValid && particleValid;
                    debug(4, spaces + "COMPOSITOR_SEQUENCE: " + groupValid);
                }
                if (compositor == XSModelGroup.COMPOSITOR_CHOICE) {
                    groupValid = groupValid || particleValid;
                    debug(4, spaces + "COMPOSITOR_CHOICE: " + groupValid);
                }
                if (compositor == XSModelGroup.COMPOSITOR_ALL) {
                    // This is a guess.
                    groupValid = groupValid || particleValid;
                    debug(4, spaces + "COMPOSITOR_ALL: " + groupValid);
                }
                if (!groupValid)
                    break;
            }
            ret = groupValid;
        }
        debug(4, spaces + "Returning " + ret);
        return ret;
    }

    protected String[] whatCanGoHere(String[] currentChildElements,
                                     int insertPosition) {
        String[] ret = new String[0];
        if (insertPosition > currentChildElements.length + 1)
            insertPosition = currentChildElements.length;
        Vector result = new Vector();
        Vector candidateNodeNames = new Vector();
        candidateNodeNames.addAll(Arrays.asList(currentChildElements));
        candidateNodeNames.insertElementAt(INSERT_PLACE_HOLDER, insertPosition);
        whatCanGoHere(_topParticle, candidateNodeNames, result, 3);
        ret = new String[result.size()];
        ret = (String[])result.toArray(ret);
        return ret;
    }
                                     
    protected boolean whatCanGoHere(XSParticle particle, Vector candidateNodeNames,
                               Vector result, int level) {
        boolean ret = false;
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < level; i++)
            spaces.append(' ');
        debug(3, spaces + "whatCanGoHere " + particle + ": " + toString(candidateNodeNames));
        if (particle == null)
            return true;
        if (particle == null) {
            debug(4, spaces + "Particle is null.");
            return true;
        }
        debug(3, spaces + "Particle: " + particle);
        XSTerm term = particle.getTerm();
        if (term == null) {
            debug(4, spaces + "Term is null.");
            return true;
        }
        debug(4, spaces + "Term: " + term.getClass() + ": " + term);
        if (candidateNodeNames.size() == 0) {
            // To do: If there are any required children, then it is not valid.
            return true;
        }
        String candidate = (String)candidateNodeNames.get(0);
        if (term instanceof XSElementDeclaration) {
            debug(4, spaces + "Element object: " + term);
            XSElementDeclaration elementDecl = (XSElementDeclaration)term;
            String name = elementDecl.getName();
            int insertPosition = candidateNodeNames.indexOf(INSERT_PLACE_HOLDER);
            int max = particle.getMaxOccurs();
            if (insertPosition == -1) {
                // Already past it, we need to stop now.
                debug(3, spaces + "At after-candidate. Need to stop now.");
                candidateNodeNames.clear();
                return false;
            } else if (insertPosition > 0 ) {
                // Not yet there.
                // Consume until we get to the insert position
                debug(3, spaces + "Not yet at insert position.");
                if (name.equalsIgnoreCase(candidate)) {
                    // Found a valid one.
                    debug(3, spaces + "Found " + name);
                    candidateNodeNames.remove(0);
                    return true;
                } else {
                    debug(3, spaces + "Candidate not found here.");
                    return false;
                }
            } else {
                // There now: insertPosition == 0
                debug(3, spaces + "At insert position.");
                if (candidateNodeNames.lastIndexOf(name) > 0) {
                    if (max != 1) {
                        debug(3, spaces + "Adding final result " + name);
                        result.add(name);
                    }
                    candidateNodeNames.clear();
                    return false;
                } else {  
                    // Add this name as an option
                    debug(3, spaces + "Adding result " + name);
                    // Prevent options from being added mutliple times
                    if (!result.contains(name))
                        result.add(name);
                    else if (candidateNodeNames.size()==1) {
                        // We've started repeating!
                        // I don't know if this is correct but it certainly stops
                        // infinite looping - JML
                        candidateNodeNames.clear();
                    }
                    return false;
                }
            }
        }
        if (term instanceof XSModelGroup) {
            XSModelGroup modelGroup = (XSModelGroup)term;
            XSObjectList particles = modelGroup.getParticles();
            boolean groupValid = true;
            for (int i = 0; i < particles.getLength(); i++) {
                XSObject object = particles.item(i);
                debug(4, spaces + "Particle object: " + object.getClass());
                XSParticle childParticle = (XSParticle)object;
                boolean valid = true;
                while (valid) {
                    valid = whatCanGoHere(childParticle, candidateNodeNames, result, level + 1);
                    int max = childParticle.getMaxOccurs();
                    if (max == 1 || candidateNodeNames.isEmpty())
                        break;
                }
            }
            ret = groupValid;
        }
        debug(4, spaces + "Returning " + ret);
        return ret;
    }
    
    private void addParticle(XSParticle particle) {
        if (particle == null) {
            debug(7, "Particle is null.");
            return;
        }
        debug(7, "Particle: " + particle);
        XSTerm term = particle.getTerm();
        if (term == null) {
            debug(7, "Term is null.");
            return;
        }
        debug(7, "Term: " + term.getClass());
        if (term instanceof XSElementDeclaration) {
            debug(7, " Element object: " + term);
            XSElementDeclaration elementDecl = (XSElementDeclaration)term;
            // This prevents loops during the parse
            String name = elementDecl.getName();
            if (!_grammarDocument.isDeclaredType(elementDecl)) {
                _grammarDocument.addDeclaredType(elementDecl);
                SchemaGrammarComplexTypeImpl childComplexType =
                    new SchemaGrammarComplexTypeImpl(_grammarDocument, this,
                                                    elementDecl, particle);
                _complexTypes.add(childComplexType);
                debug(7, "Adding child complex type: " + name);
                _namedComplexTypes.put(name, childComplexType);
                _grammarDocument.addComplexType(elementDecl, childComplexType);

                // This handles the recursive case where a complexType must be 
                // present in the _namedComplexTypes map such that whatCanGoHere
                // will work correctly
                if (_deferredSGTs.get(name)!=null) {
                    debug(3, "Adding deferred for "+name);
                    SchemaGrammarComplexTypeImpl sgc = 
                        (SchemaGrammarComplexTypeImpl)_deferredSGTs.get(name);
                    if (sgc._namedComplexTypes.get(name)==null) {
                        sgc._namedComplexTypes.put(name, childComplexType);
                        _deferredSGTs.remove(name);
                    }
                }

            } else {
                // Although looping is prevented the complex type must be
                // made available to this class _complexTypes and 
                // _namedComplexTypes  otherwise the whatCanGoHere method fails
                // to return valid adds
                debug(3, " Complex type is already known to grammar: "+name);
                GrammarComplexType childComplexType = 
                    _grammarDocument.getComplexType(elementDecl);
                if (childComplexType!=null) {
                    _complexTypes.add(childComplexType);
                    _namedComplexTypes.put(name, childComplexType);
                } else {
                    // Handle recursive case - needs to go into the
                    // _namedCompexTypes map such that getChild will return a
                    // non-null complex type when grammar access is reqired
                    // we can't add it now otherwise we get parsing loops
                    // so we add it to the deferred to be sorted out later on...
                    if (!particle.equals(_topParticle))
                        _deferredSGTs.put(name, this);
                }
            } 
        } 

        if (term instanceof XSModelGroup) {
            XSModelGroup modelGroup = (XSModelGroup)term;
            XSObjectList particles = modelGroup.getParticles();
            for (int i = 0; i < particles.getLength(); i++) {
                XSObject object = particles.item(i);
                debug(3, " Particle object: " + object.getClass());
                addParticle((XSParticle)object);
            }
        }
    }
    
    protected SchemaGrammarDocumentImpl getSchemaGrammarDocumentImpl() {
        return _grammarDocument;
    }
    
}

