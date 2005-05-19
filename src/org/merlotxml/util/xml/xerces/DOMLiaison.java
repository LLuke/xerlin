/*
====================================================================
Copyright (c) 1999-2000 ChannelPoint, Inc.  All rights reserved.
====================================================================

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions 
are met:

1. Redistribution of source code must retain the above copyright 
notice, this list of conditions and the following disclaimer. 

2. Redistribution in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the 
documentation and/or other materials provided with the distribution.

3. All advertising materials mentioning features or use of this 
software must display the following acknowledgment:  "This product 
includes software developed by ChannelPoint, Inc. for use in the 
Merlot XML Editor (http://www.merlotxml.org/)."
 
4. Any names trademarked by ChannelPoint, Inc. must not be used to 
endorse or promote products derived from this software without prior
written permission. For written permission, please contact
legal@channelpoint.com.

5.  Products derived from this software may not be called "Merlot"
nor may "Merlot" appear in their names without prior written
permission of ChannelPoint, Inc.

6. Redistribution of any form whatsoever must retain the following
acknowledgment:  "This product includes software developed by 
ChannelPoint, Inc. for use in the Merlot XML Editor 
(http://www.merlotxml.org/)."

THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO 
EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ====================================================================

For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.  
For information on the Merlot project, please see 
http://www.merlotxml.org.
*/
package org.merlotxml.util.xml.xerces;

import java.io.BufferedInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.XMLGrammarCachingConfiguration;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.merlot.plugin.PluginClassLoader;
import org.merlotxml.merlot.plugin.PluginConfig;
import org.merlotxml.merlot.plugin.PluginManager;
import org.merlotxml.util.xml.DOMLiaisonImplException;
import org.merlotxml.util.xml.DTDCache;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.DTDDocument;
import org.merlotxml.util.xml.GrammarDocument;
import org.merlotxml.util.xml.ValidDocument;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Xerces DOM Liaison Implementation
 * 
 * @author Tim McCune
 */

public class DOMLiaison implements org.merlotxml.util.xml.ValidDOMLiaison {

    protected Vector _entityResolverList;
    private String _encoding = "UTF-8";

    /**
     * Create a Document
     * @return An empty Document
     */
    public Document createDocument() {
        //	System.out.println( "Creating Xerces document." );
        return new DocumentImpl();
    }

    public void print(
        ValidDocument doc,
        Writer output,
        String resultns,
        boolean format)
        throws DOMLiaisonImplException {
        Document d = doc.getDocument();
        DocumentType doctype = d.getDoctype();
        String dtdname;
        if (doctype != null) {
            dtdname = doctype.getName();
        }

        String externid = null;
        //	System.out.println("dtdname = "+dtdname);

        DTDDocument dtd = doc.getMainDTDDocument();
        //	if (doctype instanceof DTD) {

        if (dtd != null) {
            externid = dtd.getExternalID();
        } else {
            System.out.println("Doctype was not a DTD");
            if (doctype != null) {
                System.out.println("doctype.class = " + doctype.getClass());
            } else {
                System.out.println("doctype was null");
            }
        }
        printImpl(d, output, resultns, format, externid);
    }

    /**
     * Print a Document
     * 
     * @param doc The Document to print
     * @param output Writer to send the output to
     * @param resultns Result name space for the output.  Used for things
     *		like HTML hacks.
     * @param format If true, output will be nicely tab-formatted.
     *		If false, there shouldn't be any line breaks or tabs between
     *		elements in the output.  Sometimes setting this to false
     *		is necessary to get your HTML to work right.
     * @exception DOMLiaisonImplException
     *		Wrapper exception that is thrown if the implementing class
     *		throws any kind of exception.
     */

    public void print(
        Document doc,
        Writer output,
        String resultns,
        boolean format)
        throws DOMLiaisonImplException {
        printImpl(doc, output, resultns, format, null);

    }

    private void printImpl(
        Document doc,
        Writer output,
        String resultns,
        boolean format,
        String externid)
        throws DOMLiaisonImplException {

        //String enc = "UTF-8";
        String enc = _encoding;
        try {

            /*
            PrintWriter pw = new PrintWriter(output);
            pw.print("<?xml version=\"1.0\"");
            	
            if (output instanceof OutputStreamWriter) {
            	enc = ((OutputStreamWriter)output).getEncoding();
            	String s = EncodingMap.getXMLFromJava(enc);
            	if (s != null) {
            				pw.print(" encoding=\""+s+"\"");
            	}
            }
            pw.println("?>");
            */
            // print out the doctype
            /*
            DocumentType dtd = doc.getDoctype();
            if (dtd != null) {
            	//	System.out.println( "Printing doctype" );
            	pw.print("<!DOCTYPE "+dtd.getName());
            	
            	if (externid != null) {
            		pw.print(" "+externid);
            	}
            	pw.println(">");
            }
            */

            SerializerFactory sfact =
                SerializerFactory.getSerializerFactory(Method.XML);

            OutputFormat outformat = new OutputFormat(doc, enc, format);
            outformat.setLineWidth(0);

            // REVISIT: Let Xerces set the document type so that it will work
            // for Schema as well.
            //outformat.setOmitDocumentType( true );
            outformat.setPreserveSpace(false);

            Serializer serializer = sfact.makeSerializer(output, outformat);
            if (serializer instanceof DOMSerializer) {
                ((DOMSerializer) serializer).serialize(doc);
            }
        } catch (IOException ex) {
            throw new DOMLiaisonImplException(ex);
        }

        /*
        try {
        	PrintWriter pw = new PrintWriter(output);
        	// spit out the headers that xml4j leaves off
        	pw.print("<?xml version=\"1.0\"");
        	if (output instanceof OutputStreamWriter) {
        		String s = ((OutputStreamWriter)output).getEncoding();
        		s = EncodingMap.getXMLFromJava(s);
        		if (s != null) {
        			pw.print(" encoding=\""+s+"\"");
        		}
        	}
        	pw.println("?>");
        	// print out the doctype
        	DocumentType dtd = doc.getDoctype();
        	if (dtd != null) {
        		pw.print("<!DOCTYPE "+dtd.getName());
        		
        		if (externid != null) {
        			pw.print(" "+externid);
        		}
        		pw.println(">");
        	}
        	
        	liaison.toMarkup(doc, pw, XML4JLiaison4dom.OUTPUT_METH_XML, format);
        }
        catch (Exception e) {
        	throw new DOMLiaisonImplException(e);
        }
        */
    }

    /**
     * Parse a stream of XML into a Document
     * 
     * @param xmlReader XML stream reader
     * @return The Document that was parsed
     * @exception DOMLiaisonImplException
     *		Wrapper exception that is thrown if the implementing class
     *		throws any kind of exception.
     * @deprecated Use parseXMLStream(Reader)
     */
    public Document parseXMLStream(InputStream is)
        throws DOMLiaisonImplException {
        return parseXMLStream(new InputSource(is));
    }

    public Document parseXMLStream(Reader in) throws DOMLiaisonImplException {
        return parseXMLStream(new InputSource(in));
    }

    private Document parseXMLStream(InputSource is) throws DOMLiaisonImplException {
        return parseXMLStream(is, true);
    }

    private Document parseXMLStream(InputSource is, boolean validate)
        throws DOMLiaisonImplException {
        //	System.out.println( "Xerces: parseXMLStream" );
        DOMParser parser;

        parser = new DOMParser();

        try {
            parser.setFeature("http://xml.org/sax/features/validation", validate);
        } catch (SAXException e) {
            System.out.println("error in setting up parser feature");
        }

        try {
            parser.setFeature(
                "http://apache.org/xml/features/domx/grammar-access",
                validate);
        } catch (Exception e) {
            System.out.println(
                "warning: unable to set grammar-access feature.");
        }
        //parser.setEntityResolver(new MyResolver());

        try {
            parser.parse(is);
        } catch (SAXException e) {
            throw new DOMLiaisonImplException(e);
        } catch (IOException e) {
            throw new DOMLiaisonImplException(e);
        }
        /*
        String[] features = parser.getFeaturesRecognized();
        for ( int i = 0; i < features.length; i++ )
        {
        	String feature = features[i];
        	boolean featureOn = false;
        	try
        	{
        		featureOn = parser.getFeature( feature );
        	}
        	catch ( Exception ex )
        	{
        	}
        	System.out.println( "Parser feature: " + feature + ": "
        	+ featureOn );
        }
        */
        return parser.getDocument();
    }

    public void setProperties(Properties props) {}

    public void addEntityResolver(EntityResolver er) {
        if (_entityResolverList == null)
            _entityResolverList = new Vector();
        _entityResolverList.add(er);
    }

    /*
    public class MyResolver implements EntityResolver {
    	public InputSource resolveEntity (String publicId, String systemId)
    	{
        
    		// return a special input source
    		StringReader reader = new StringReader("");
        
    		return new InputSource(reader);
    	}
    }
    */

    public class MyEntityResolver implements EntityResolver {
        ValidDocument _vdoc = null;
        
        String publicId = null;
        String systemId = null;

        public MyEntityResolver(ValidDocument doc) {
            _vdoc = doc;
        }
        
        /**
         * This entity resolver finds a dtd file on the filesystem if it can.
         * It does this by first checking the specified file (given as the
         * systemId paramter which comes from the SYSTEM specifier in the
         * XML &lt;!DOCTYPE&gt; definition. If the systemId isn't a full path or
         * url to a valid file, then the resolver tries to find the file using the
         * path.dtd resource from ResourceCatalog.
         *
         * @param publicId the public identifier for the entity
         * @param systemId the system identifier (usually a filename or url)
         * of the external entitiy.
         * @exception SAXException this is thrown by the DTD parser during DTD parsing.
         * @exception IOException FileNotFound is the typical IOException thrown in the
         * case that the external entity file can't be found. Other IOExceptions may be
         * thrown depending on the external entity file operations.
         */
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
            MerlotDebug.msg(
                "Resolving entity: publicId: "
                    + publicId
                    + " systemId: "
                    + systemId);
            this.publicId = publicId;
            this.systemId = systemId;
            DTDCacheEntry dtdentry = null;

            // Loads Entities defined relative to each other in jar files
            if (systemId.startsWith("plugin:")) {
                String path = systemId.substring(10);
                PluginClassLoader pcl;
                PluginManager pm = PluginManager.getInstance();
                List plugins = pm.getPlugins();
                URL url = null;
                File tryPlugin;
                for (int i = 0; i < plugins.size(); i++) {
                    tryPlugin = ((PluginConfig) plugins.get(i)).getSource();
                    pcl = new PluginClassLoader(tryPlugin);
                    url = pcl.findResource(path);
                    if (url != null) {
                        break;
                    }
                }

                if (url != null) {
                    InputSource src = new InputSource(url.openStream());
                    return src;
                }
            }

            dtdentry =
                DTDCache.getSharedInstance().findDTD(
                    publicId,
                    systemId,
                    _vdoc.getFileLocation());

            // Look into the plugins
            if (dtdentry != null) {
                _vdoc.addDTD(dtdentry, systemId);
                InputStream is = null;

                // Get the dtd from the JAR file
                PluginClassLoader pcl;
                PluginManager pm = PluginManager.getInstance();
                List plugins = pm.getPlugins();
                URL url = null;
                File tryPlugin;
                for (int i = 0; i < plugins.size(); i++) {
                    tryPlugin = ((PluginConfig) plugins.get(i)).getSource();
                    pcl = new PluginClassLoader(tryPlugin);
                    url = pcl.findResource(dtdentry.getFilePath());
                    if (url != null) {
                        break;
                    }
                }

                if (url != null) {
                    try {
                        is = url.openStream();
                        InputSource src = new InputSource(is);
                        //Indentify that this entity came from a plugin
                        src.setSystemId("plugin:///" + dtdentry.getFilePath());
                        return src;
                    } catch (IOException ioe) {}
                }

                // Couldn't get it from the JAR
                // Try filebased
                try {
                    String path = dtdentry.getFilePath();
                    if (path != null && path.startsWith("file:")) {
                        path = path.substring(5);
                        is = new FileInputStream(path);
                    } else if (
                        systemId != null && systemId.startsWith("http:")) {
                        url = new URL(systemId);
                        is = url.openStream();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("plugin uri is probably invalid");
                    return null;
                }

                if (is != null) {
                    InputSource src = new InputSource(is);
                    // Important! DTDs defined relative to each other will not work
                    // unless this is set
                    src.setSystemId(systemId);
                    return src;
                }

                char[] chars = dtdentry.getCachedDTDStream();
                CharArrayReader r = new CharArrayReader(chars);
                return new InputSource(r);

            }
            if (_entityResolverList != null) {
                // try other entity resolvers
                if (dtdentry == null) {
                    for (int i = _entityResolverList.size() - 1; i >= 0; i--) {
                        Object o = _entityResolverList.elementAt(i);
                        if (o instanceof EntityResolver) {
                            EntityResolver er = (EntityResolver) o;
                            try {
                                dtdentry =
                                    DTDCache.getSharedInstance().resolveDTD(
                                        publicId,
                                        systemId,
                                        er,
                                        _vdoc.getFileLocation());
                            } catch (IOException ioe) {
                                continue;
                            }
                        }
                        if (dtdentry != null) {
                            CharArrayReader car =
                                new CharArrayReader(
                                    dtdentry.getCachedDTDStream());
                            _vdoc.addDTD(dtdentry, systemId);

                            return new InputSource(car);
                        }
                    }
                }
            }

            //DTD cannot be cached...
            System.out.println("Resolve entity is returning null.");
            return null;

        }
        
        public String getSavedPublicId() {
            return publicId;
        }
        
        public String getSavedSystemId() {
            return systemId;
        }
    }

    // ValidDOMLiaison Methods:
    public ValidDocument createValidDocument() {
        ValidDocument vdoc = new ValidDocument(createDocument());
        return vdoc;
    }

    /**
     * Parses an input stream containing XML using a validating parser.
     * Returns a ValidDocument which gives access to DTD information
     * and stuff.
     */
    public ValidDocument parseValidXMLStream(
        InputStream is,
        String fileLocation)
        throws DOMLiaisonImplException {
        MerlotDebug.msg("Xerces: parseValidXMLStream");
        ValidDocument doc = new ValidDocument();
        doc.setFileLocation(fileLocation);

        try {
            // hack to parse out the encoding first
            is = new BufferedInputStream(is);

            if (is.markSupported()) {
                is.mark(1024);
                byte[] b = new byte[512];
                int i = is.read(b);
                if (i > 0) {
                    String s = new String(b);
                    int e = s.indexOf("encoding=");
                    if (e >= 0) {
                        String encbuf = s.substring(e);
                        StringTokenizer tok =
                            new StringTokenizer(encbuf, "\"'", true);
                        boolean encfound = false;
                        while (tok.hasMoreTokens()) {
                            s = tok.nextToken();
                            //	System.out.println("token = '"+s+"'");

                            if (s.equals("'") || s.equals("\"")) {
                                encfound = true;
                            } else {
                                if (encfound) {
                                    _encoding = s;
                                    break;
                                }
                            }
                        }
                    }
                }
                //	System.out.println("Encoding = "+encoding);

                is.reset();
            } else {
                //	System.out.println("Mark not supported");
            }
        } catch (IOException ex) {}

        //encoding = EncodingMap.getJavaFromXML(encoding);

        // create a validating DOMParser
        InputSource inputSource = new InputSource(is);
        //input.setEncoding(encoding);
        // This is important!
        // Xerces does not like internal subsets if this is not set
        inputSource.setSystemId(fileLocation);

        ErrorHandler errorHandler = new DefaultErrorHandler();
        EntityResolver entityResolver = new MyEntityResolver(doc);

        parseValidXMLStream(doc, inputSource, errorHandler, entityResolver);

        return doc;
    }

    public void parseValidXMLStream(
        ValidDocument doc,
        InputSource inputSource,
        ErrorHandler errorHandler,
        EntityResolver entityResolver)
        throws DOMLiaisonImplException {

        MerlotDebug.msg("Xerces: parseValidXMLStream");
        DOMParser parser = null;

        try {
            XMLGrammarCachingConfiguration config =
                new XMLGrammarCachingConfiguration();
            // We need to use our own Cache via the entity resolver...
            config.clearGrammarPool();
            parser = new DOMParser(config);

            try {
                parser.setFeature(
                    "http://apache.org/xml/features/validation/dynamic",
                    true);
                parser.setFeature(
                    "http://xml.org/sax/features/validation",
                    true);
                parser.setFeature(
                    "http://apache.org/xml/features/validation/schema",
                    true);
            } catch (SAXException e) {
                System.out.println("error in setting up validation feature");
            }

            parser.setErrorHandler(errorHandler);
            parser.setEntityResolver(entityResolver);
            long start = System.currentTimeMillis();
            parser.parse(inputSource);
            long end = System.currentTimeMillis();
            System.out.println(
                "Duration - parsing document: " + (end - start) + " ms.");

            doc.setEncoding(inputSource.getEncoding());

            Document domDocument = parser.getDocument();
            doc.setDocument(domDocument);

            XMLGrammarPool pool = null;
            try {
                pool =
                    (XMLGrammarPool) parser.getProperty(
                        "http://apache.org/xml/properties/internal/grammar-pool");
            } catch (org.xml.sax.SAXNotRecognizedException ex) {
                MerlotDebug.msg("Exception: " + ex);
            }
            if (pool != null) {
                GrammarDocument grammarDocument = null;
                Grammar[] dtdGrammars =
                    pool.retrieveInitialGrammarSet(
                        XMLGrammarDescription.XML_DTD);
                Grammar[] schemaGrammars =
                    pool.retrieveInitialGrammarSet(
                        XMLGrammarDescription.XML_SCHEMA);
                if (dtdGrammars.length > 0) {
                    grammarDocument = new DTDGrammarDocumentImpl(dtdGrammars);
                } else if (schemaGrammars.length > 0) {
                    MerlotDebug.msg("SchemaGrammars: " + schemaGrammars.length);
                    grammarDocument =
                        new SchemaGrammarDocumentImpl(schemaGrammars);
                    new SchemaIdentityConstraintValidator(
                        domDocument,
                        (SchemaGrammarDocumentImpl) grammarDocument);
                        
                    // Get a Document for the schema grammar.
                    loadSchemaDocument(entityResolver);
                } else
                    MerlotDebug.msg("No Grammars found.");
                doc.setGrammarDocument(grammarDocument);
            } else
                MerlotDebug.msg("XMLGrammarPool is null.");
            return;
        } catch (Exception ex) {
            MerlotDebug.msg("Exception: class = " + ex.getClass().getName());

            // ex.printStackTrace();
            int linenumber;
            int colnumber;
            String appendMsg = null;
            if (ex instanceof SAXParseException) {
                linenumber = ((SAXParseException) ex).getLineNumber();
                colnumber = ((SAXParseException) ex).getColumnNumber();
                appendMsg = " on line " + linenumber + ", column " + colnumber;
                throw new DOMLiaisonImplException(ex, appendMsg);

            } else if (ex instanceof SAXException) {
                Exception inex = ((SAXException) ex).getException();
                if (inex != null) {
                    throw new DOMLiaisonImplException(inex);
                } else {
                    throw new DOMLiaisonImplException(ex);
                }
            } else {
                throw new DOMLiaisonImplException(ex);
            }
        }
    }
    
    void loadSchemaDocument(EntityResolver entityResolver) {
        try {
            if (entityResolver instanceof MyEntityResolver) {
                String publicId = ((MyEntityResolver)entityResolver).getSavedPublicId();
                String systemId = ((MyEntityResolver)entityResolver).getSavedSystemId();
                InputSource schemaInputSource = entityResolver.resolveEntity(publicId, systemId);
                if (schemaInputSource != null) {
                    Document schemaDocument = this.parseXMLStream(schemaInputSource, false);
                    MerlotDebug.msg("schemaDocument: " + schemaDocument);
                    Schema schema = Schema.getInstance();
                    schema.setDocument(schemaDocument);
                } else
                    MerlotDebug.msg("schemaInputSource is null.");
            } else {
                MerlotDebug.msg("Entity resolver class: " + entityResolver.getClass());
            } 
        } catch (Exception ex) {
            MerlotDebug.msg("Exception: " + ex);
        }
    }

    /**
     * Error handling class for the validating parser
     */
    // XXX clean up... make it report errors out to the user somehow
    public class DefaultErrorHandler implements ErrorHandler {
        public DefaultErrorHandler() {}

        public void warning(SAXParseException exception) throws SAXException {
            MerlotDebug.msg(
                "Line "
                    + exception.getLineNumber()
                    + ": Parser warning: "
                    + exception.getMessage());
        }

        public void error(SAXParseException exception) throws SAXException {
            MerlotDebug.msg(
                "Line "
                    + exception.getLineNumber()
                    + ": Parser error: "
                    + exception.getMessage());
        }

        public void fatalError(SAXParseException exception)
            throws SAXException {
            MerlotDebug.msg(
                "Line "
                    + exception.getLineNumber()
                    + ": Parser fatal error: "
                    + exception.getMessage());
            throw exception;
        }
    }
}
