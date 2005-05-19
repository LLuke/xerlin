package org.merlotxml.util.xml.xerces;

import org.apache.xerces.parsers.DOMParser;

/**
 * This class extends DOMParser in order to get access to protected members. It
 * is an ugly way to do it, but I couldn't find a way to do it through the
 * public interfaces. 
 * The consequence of this is that we need a document to parse - a DTD or Schema
 * file on it's own cannot be used. In order to get around this in the case
 * where grammar is required for a new document, a temporary document is created
 * which is then parsed.
 * 
 * @author Evert Hoff
 * @deprecated Use GrammarDocument.
 */
public class GrammarAccess extends DOMParser
{
}
