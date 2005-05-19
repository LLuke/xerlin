/*
 *  ====================================================================
 *  Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
 *  ====================================================================
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistribution of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  2. Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this
 *  software must display the following acknowledgment:  "This product
 *  includes software developed by ChannelPoint, Inc. for use in the
 *  Merlot XML Editor (http://www.merlotxml.org/)."
 *  4. Any names trademarked by ChannelPoint, Inc. must not be used to
 *  endorse or promote products derived from this software without prior
 *  written permission. For written permission, please contact
 *  legal@channelpoint.com.
 *  5.  Products derived from this software may not be called "Merlot"
 *  nor may "Merlot" appear in their names without prior written
 *  permission of ChannelPoint, Inc.
 *  6. Redistribution of any form whatsoever must retain the following
 *  acknowledgment:  "This product includes software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor
 *  (http://www.merlotxml.org/)."
 *  THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO
 *  EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  ====================================================================
 *  For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.
 *  For information on the Merlot project, please see
 *  http://www.merlotxml.org/.
 */
package org.merlotxml.util.xml;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.merlotxml.merlot.MerlotDebug;
import org.merlotxml.merlot.XMLEditorFrame;
import org.merlotxml.merlot.XerlinDavFileDialogs;
import org.merlotxml.merlot.XerlinFileDialogs;
import org.merlotxml.util.FileUtil;
import org.merlotxml.util.xml.xerces.DTDDocumentImpl;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.speedlegal.webdav.DAVServer;
import com.speedlegal.webdav.EditorFile;

/**
 *  This singleton class is responsible for loading and caching all DTD's
 *  required by the system. This manager can load DTD's from the filesystem,
 *  URL's, and zip/jar files (not currently implemented). <P>
 *
 *  Apps should use this class to retrieve all their DTD's for valid documents
 *  (non-validating apps usually ignore the DTD anyway, so they don't really
 *  need to use this, but if they do get a DTD, it might be a good idea to call
 *  into this class. <P>
 *
 *  Here's an example of getting a dtd:<br>
 *  <tt>DTDCacheEntry dtdentry = DTDCache.getSharedInstance().findDTD(publicId,
 *  systemId);</tt> <br>
 *  where <tt>publicId</tt> is the DOCTYPE's given public identifier (can be
 *  null), and <tt>systemId</tt> is a system designator (file path or URL) <P>
 *
 *  This can also cache DTD's from other entity resolvers via the <A
 *  href="#resolveDTD">resolveDTD</a> method.
 *
 *@author     Kelly A. Campbell
 *@author     Frank Blecha
 *@created    25 April 2003
 */
public class DTDCache {
    /**
     *  key is the public id, val is a DTDCacheEntry, e.g. (publicId,
     *  DTDCacheEntry)
     */
    protected Map _publicIdCache;

    /**
     *  key is the system id, val is a DTDCacheEntry, e.g. (systemId,
     *  DTDCacheEntry)
     */
    protected Map _systemIdCache;

    /**
     *  key is the file path (including a ! and the path within a jar), val is a
     *  DTDCacheEntry
     */
    protected Map _filepathCache;

    /**
     *  List of unique dtd entries. key is the dtd entry, value is not used
     *  (null)
     */
    protected Map _dtdEntries;

    /**
     *  Properties for getting dtd path, etc. from
     */
    protected Properties _properties;

    /**
     *  singleton instance
     */
    protected static DTDCache _instance;
    /**
     *  synch object for creating the instance
     */
    protected final static Object _synchronizer = new Object();

    /**
     *  url prefix to know we're working with a local file
     */
    protected final static String FILE_PROTOCOL_NAME = "file:";


    /**
     *  Constructor for the DTDCache object
     */
    protected DTDCache() {
        _publicIdCache = new HashMap();
        _systemIdCache = new HashMap();
        _filepathCache = new HashMap();
        _dtdEntries = new HashMap();

    }


    /**
     *  gets the singleton instance.
     *
     *@return    The sharedInstance value
     */
    public static DTDCache getSharedInstance() {
        if (_instance == null) {
            synchronized (_synchronizer) {
                if (_instance == null) {
                    _instance = new DTDCache();
                }
            }
        }
        return _instance;
    }


    /**
     *  set the properties. should really only be called once by some app
     *  initializer
     *
     *@param  props  The new properties value
     */
    public void setProperties(Properties props) {
        _properties = props;
    }


    /**
     *@param  systemID  ba
     *@return           DTDCacheEntry null if we're unable to find the entry,
     *      otherwise the entry
     *@author           Frank Blecha
     */
    protected DTDCacheEntry findCacheEntryBySystemID(String systemID) {
        DTDCacheEntry entry = null;
        entry = (DTDCacheEntry)_systemIdCache.get(systemID);
        if (entry != null) {
            debug("found cached DTD: systemId=" + systemID);
            checkCacheEntryTimestamp(entry);
        }
        return entry;
    }


    /**
     *@param  publicID
     *@param  systemID
     *@return           DTDCacheEntry the cache entry or null if we couldn't
     *      find it or encoutered an error
     *@author           Frank Blecha
     */
    protected DTDCacheEntry setupCacheEntryFromFile(
            String publicID,
            String systemID) {
        DTDCacheEntry entry = null;
        try {
            File dtdFile = new File(systemID);
            entry = new DTDCacheEntry(publicID, systemID);
            entry.setTimestamp(dtdFile.lastModified());
            entry.setFilePath(dtdFile.getCanonicalPath());
        } catch (IOException iex) {}
        return entry;
    }


    /**
     *@param  publicID
     *@param  systemID
     *@return           DTDCacheEntry
     *@author           Frank Blecha
     */
    protected DTDCacheEntry setupCacheEntryFromClassLoader(
            String publicID,
            String systemID) {
        DTDCacheEntry entry = new DTDCacheEntry(publicID, systemID);
        // ok? set the timestamp to never reload
        entry.setTimestamp(0);
        String dtdFile = "blahblahblah";
        //replace this with something meaningful later - fb3
        entry.setFilePath(this.getClass().toString() + ":" + dtdFile);
        return entry;
    }


    /**
     *@param  publicID  Description of the Parameter
     *@param  systemID  Description of the Parameter
     *@return           DTDCacheEntry the cache entry or null if we cannot set
     *      it up correctly.
     *@author           Frank Blecha
     */
    protected DTDCacheEntry setupCacheEntryFromURL(
            String publicID,
            String systemID) {
        DTDCacheEntry ret = null;
        try {
            URL dtdURL = new URL(systemID);
            URLConnection connection = dtdURL.openConnection();
            ret = new DTDCacheEntry(publicID, systemID);
            ret.setFilePath(dtdURL.toString());
            ret.setTimestamp(connection.getExpiration());
            if (ret.getTimestamp() < System.currentTimeMillis()) {
                /*
                 *  camk comments:
                 *  ok, they don't want us to cache it...we will anyway.
                 *  Use the modified time as the timestamp
                 */
                ret.setTimestamp(connection.getLastModified());
            }
        } catch (IOException iex) {
            ret = null;
        }

        return ret;
    }


    /**
     *  see if the systemID exists as-is (is the absolute path embedded in the
     *  xml?)
     *
     *@param  systemID  Description of the Parameter
     *@return           InputStream - the InputStream from dtdFile or null if we
     *      can't find it
     *@author           Frank Blecha
     */
    protected InputStream findDTDFromFile(String systemID) {
        InputStream ret = null;
        try {
            File dtdFile = new File(systemID);

            /*
             *  we may modify the systemID if it's not prepended
             *  with FILE_PROTOCOL_NAME
             */
            String modifiedSystemID = systemID;

            if (dtdFile.exists()) {
                if (!systemID.startsWith(FILE_PROTOCOL_NAME)) {
                    modifiedSystemID = FILE_PROTOCOL_NAME + systemID;
                }
                ret = new FileInputStream(dtdFile);
            }
        } catch (IOException iex) {
            ret = null;
        }
        if (ret == null) {
            // Might be WebDAV
            XMLEditorFrame xef = XMLEditorFrame.getSharedInstance();
            if (xef != null) {
                XerlinFileDialogs xfd = xef.getFileDialogs();
                if (xfd instanceof XerlinDavFileDialogs) {
                    DAVServer server =
                            ((XerlinDavFileDialogs)xfd).getClient();
                    EditorFile dtd = server.getNode(systemID);
                    if (dtd.exists()) {
                        return dtd.getContents();
                    }
                }
            }
        }
        return ret;
    }


    /**
     *@param  systemID      Description of the Parameter
     *@param  fileLocation  Description of the Parameter
     *@return               Description of the Return Value
     *@author               Frank Blecha
     */
    protected InputStream findDTDFromFile(
            String systemID,
            String fileLocation) {
        InputStream ret = null;
        if (fileLocation == null) {
            return null;
        }

        try {
            File f = new File(fileLocation);
            String parent = f.getParent();
            if (parent != null) {
                File dtdFile = new File(parent, systemID);
                if (dtdFile.exists() && dtdFile.canRead()) {
                    ret = new FileInputStream(dtdFile);
                }
            }
        } catch (IOException iex) {
            ret = null;
        }
        return ret;
    }


    /**
     *@param  publicID
     *@param  systemID
     *@return           InputStream the InputStream for the DTD, or null we
     *      can't find it.
     *@author           Frank Blecha
     */
    protected InputStream findDTDFromURL(String publicID, String systemID) {
        InputStream ret = null;
        try {
            URL dtdURL = new URL(systemID);
            URLConnection connection = dtdURL.openConnection();
            ret = connection.getInputStream();
        } catch (IOException iex) {
            //set ret to null, which is enough of an error indicator
            ret = null;
        }
        return ret;
    }


    /**
     *@param  publicID
     *@param  systemID
     *@return           InputStream stream to read the dtd, or null if an error
     *      was encountered
     *@author           Frank Blecha
     */
    protected InputStream findDTDFromClassLoader(
            String publicID,
            String systemID) {
        InputStream ret = null;
        if (null != getDTDPath()) {

            StringTokenizer st =
                    new StringTokenizer(
                    getDTDPath(),
                    System.getProperty("path.separator"));

            while (st.hasMoreTokens()) {
                // try the file
                File dtdFile =
                        new File(st.nextToken() + "/" + formatFileName(systemID));
                try {
                    ret = FileUtil.getInputStream(dtdFile, this.getClass());
                    break;
                } catch (FileNotFoundException e) {}
            }
            //end while
        }
        return ret;
    }


    /**
     *@param  publicID
     *@param  systemID
     *@return           InputStream the stream to read the dtd from, or null if
     *      we got an error
     *@author           Frank Blecha
     */
    protected InputStream findDTDFromDTDPath(
            String publicID,
            String systemID) {
        InputStream ret = null;
        try {
            if (null != getDTDPath()) {
                StringTokenizer st =
                        new StringTokenizer(
                        getDTDPath(),
                        System.getProperty("path.separator"));
                while (st.hasMoreTokens()) {
                    File dtdFile =
                            new File(
                            st.nextToken() + "/" + formatFileName(systemID));
                    if (dtdFile.exists()) {
                        ret = new FileInputStream(dtdFile);
                        break;
                    }
                }
                //end while
            }
        } catch (IOException iex) {
            ret = null;
        }
        return ret;
    }


    /**
     *  Finds a dtd given a system identifier. If it cannot be found, null is
     *  returned
     *
     *@param  publicId
     *@param  systemId      can be a URL, and absolute filepath, or a filepath
     *      relative to the current document
     *@param  fileLocation  the location of the file which includes the given
     *      DTD
     *@return               Description of the Return Value
     */
    public DTDCacheEntry findDTDbySystemId(
            String publicId,
            String systemId,
            String fileLocation) {
        try {
            InputStream is = null;
            String modifiedSystemID = systemId;
            DTDCacheEntry entry = null;
            File dtdFile = null;

            if ((entry = findCacheEntryBySystemID(systemId)) != null) {
                return entry;
            } else if ((is = findDTDFromFile(systemId)) != null) {
                entry = setupCacheEntryFromFile(publicId, systemId);
            } else if ((is = findDTDFromURL(publicId, systemId)) != null) {
                entry = setupCacheEntryFromURL(publicId, systemId);
            } else if (
                    (is = findDTDFromFile(systemId, fileLocation)) != null) {
                entry = setupCacheEntryFromFile(publicId, systemId);
            } else if ((is = findDTDFromDTDPath(publicId, systemId)) != null) {
                entry = setupCacheEntryFromFile(publicId, systemId);
                modifiedSystemID = formatFileName(systemId);
            } else if (
                    (is = findDTDFromClassLoader(publicId, systemId)) != null) {
                entry = setupCacheEntryFromClassLoader(publicId, systemId);
                modifiedSystemID = formatFileName(systemId);
            }

            if (entry == null || is == null) {
                //	debug("DTD SYSTEMID='"+systemId+"' NOT FOUND");
                //				new Exception().printStackTrace();

                return null;
            }

            /*
             *  camk comments:
             *  XXX this is lame and uses the xml4j parser directly instead
             *  of through the DOMLiason... needs extrapolated and
             *  insulated a bit
             *  more than likely, someone else (like an entity resolver)
             *  wants to read this dtd stream also, so we might as
             *  well cache the whole dtd into memory (lame yes, but nice
             *  if the dtd came from a remote URL, or a jar/zip file.
             */
            loadDTDIntoCache(is, entry);

            if (modifiedSystemID != null) {
                entry.setSystemId(modifiedSystemID);
                _systemIdCache.put(modifiedSystemID, entry);
            }

            String filepath = entry.getFilePath();
            if (filepath != null) {
                _filepathCache.put(filepath, entry);
            }
            //	debug("DTDCache: returning PUBLIC='"+entry.getPublicId()+"' SYSTEM='"+entry.getSystemId()+"' FILE='"+filepath+"'");

            return entry;
        } catch (IOException iex) {
            return null;
        }
    }


    /**
     *  Looks in our cache for a file with a given public ID
     *
     *@param  publicId  Description of the Parameter
     *@param  systemId  Description of the Parameter
     *@return           Description of the Return Value
     */
    public DTDCacheEntry findDTDbyPublicId(String publicId, String systemId) {
        DTDCacheEntry entry = (DTDCacheEntry)_publicIdCache.get(publicId);
        debug("_publicIdCache.get(" + publicId + ") = " + entry);

        if (entry != null) {
            checkCacheEntryTimestamp(entry);
            return entry;
        }
        debug("findDTDbyPublicId returning null");

        return null;
    }


    /**
     *  find a DTD based on the public id and system id
     *
     *@param  pubid         Description of the Parameter
     *@param  sysid         Description of the Parameter
     *@param  fileLocation  Description of the Parameter
     *@return               Description of the Return Value
     */
    public DTDCacheEntry findDTD(
            String pubid,
            String sysid,
            String fileLocation) {
        MerlotDebug.msg(
                "DTDCache.findDTD(public='" + pubid + "', sysid='" + sysid + "')");

        DTDCacheEntry ret = null;
        if (pubid != null) {
            ret = findDTDbyPublicId(pubid, sysid);
        }
        if (ret == null && sysid != null) {
            ret = findDTDbySystemId(pubid, sysid, fileLocation);
        }

        return ret;
    }


    /**
     *  resolve a dtd from another resolver. This way we can cache it locally.
     *
     *@param  publicId          Description of the Parameter
     *@param  systemId          Description of the Parameter
     *@param  resolver          Description of the Parameter
     *@param  fileLocation      Description of the Parameter
     *@return                   Description of the Return Value
     *@exception  SAXException  Description of the Exception
     *@exception  IOException   Description of the Exception
     */
    public DTDCacheEntry resolveDTD(
            String publicId,
            String systemId,
            EntityResolver resolver,
            String fileLocation)
             throws SAXException, IOException {
        debug("Resolve DTD: " + systemId);

        InputSource is = resolver.resolveEntity(publicId, systemId);
        if (is != null) {

            String newPublicId = is.getPublicId();
            String newSystemId = is.getSystemId();
            if (newPublicId != null) {
                publicId = newPublicId;
                //	debug("publicId = "+publicId+" newPublicId = "+newPublicId);

            }
            if (newSystemId != null) {
                //	debug("systemId = "+systemId+" newSystemId = "+newSystemId);
                systemId = newSystemId;
            }

            // check to see if the resovler put one in our cache somehow
            if (_publicIdCache.containsKey(publicId)) {
                DTDCacheEntry entry =
                        (DTDCacheEntry)_publicIdCache.get(publicId);
                checkCacheEntryTimestamp(entry);
                //	debug("RESOLVE RETURNING DTD FROM PUBLIC MAP: "+entry);

                return entry;
            }

            // create a new DtdEntry for it
            DTDCacheEntry entry = new DTDCacheEntry(publicId, systemId);

            InputStream stream = is.getByteStream();
            Reader charstream = is.getCharacterStream();
            if (charstream == null && stream != null) {
                loadDTDIntoCache(stream, entry);
            } else if (charstream != null) {
                loadDTDIntoCache(charstream, entry);
            } else {
                return null;
            }

            //	debug("RESOLVE RETURNING DTD ENTRY: "+entry);

            return entry;
        }
        return null;
    }


    /**
     *  Loads a dtd into a DTDCacheEntry.
     *
     *@param  is               Description of the Parameter
     *@param  entry            Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void loadDTDIntoCache(InputStream is, DTDCacheEntry entry)
             throws IOException {
        Reader r = new InputStreamReader(is);
        loadDTDIntoCache(r, entry);

    }


    /**
     *  Loads a dtd into a DTDCacheEntry. The public and system id's should be
     *  set on the dtd entry.
     *
     *@param  r                Description of the Parameter
     *@param  entry            Description of the Parameter
     *@exception  IOException  Description of the Exception
     */

    public void loadDTDIntoCache(Reader r, DTDCacheEntry entry)
             throws IOException {
        BufferedReader br = new BufferedReader(r);
        CharArrayWriter caw = new CharArrayWriter();
        BufferedWriter bw = new BufferedWriter(caw);

        int c;
        while ((c = br.read()) >= 0) {
            bw.write(c);
        }
        bw.flush();
        bw.close();
        br.close();

        entry.setCachedDTDStream(caw.toCharArray());
        CharArrayReader car = new CharArrayReader(entry.getCachedDTDStream());

        // now parse the dtd for ourselves
        String filename = entry.getFilePath();
        if (filename == null) {
            filename = entry.getSystemId();
        }
        //	System.out.println("Parser("+filename+")");
        String errorPrefix = filename;
        if (errorPrefix == null || errorPrefix.trim().equals("")) {
            errorPrefix = "error";
        }

        //	com.ibm.xml.parser.Parser p = new com.ibm.xml.parser.Parser(errorPrefix);
        debug("Parsing DTD: " + errorPrefix);

        //	DTD dtd = p.readDTDStream(car);
        debug("readDTDStream ok");

        String publicId = entry.getPublicId();
        String systemId = entry.getSystemId();
        String filePath = entry.getFilePath();
        // Now doing this in DOMLiaison.parseValidXMLStream(). --Evert
        DTDDocumentImpl dtdimpl =
                new DTDDocumentImpl(filePath, publicId, systemId);
        //	DTDDocumentImpl dtdimpl = new DTDDocumentImpl( publicId, systemId );
        entry.setParsedDTD(dtdimpl);

        debug("dtd entry = " + entry);

        car.close();
        if (systemId != null && !systemId.trim().equals("")) {
            _systemIdCache.put(systemId, entry);
            debug("added " + systemId + " to cache [2]");

        }
        if (publicId != null && !publicId.trim().equals("")) {
            _publicIdCache.put(publicId, entry);
            debug("added public id " + publicId + " to cache");

        }
        _dtdEntries.put(entry, null);

    }


    /**
     *  Checks the timestamp associated with a cache entry and reloads the dtd
     *  file if it has changed.
     *
     *@param  entry  Description of the Parameter
     */
    public void checkCacheEntryTimestamp(DTDCacheEntry entry) {
        boolean found = false;
        if (entry != null) {
            long timestamp = entry.getTimestamp();
            if (timestamp > 0) {
                File dtdFile = null;
                InputStream is = null;
                URL u = null;

                // first see if the systemId exists as-is (is full path hard coded in the xml?)
                String path = entry.getFilePath();
                if (path != null) {
                    dtdFile = new File(path);
                    if (dtdFile.exists()) {
                        // we found it
                        found = true;
                        long newtime = dtdFile.lastModified();
                        if (newtime > timestamp) {
                            entry.setTimestamp(newtime);
                            try {
                                is = new FileInputStream(dtdFile);
                            } catch (Exception ex) {}
                        }
                    }

                    // still don't have it?
                    if (!found && is == null) {
                        // try it as a url
                        try {
                            u = new URL(path);
                            URLConnection connection = u.openConnection();
                            long newtime = connection.getExpiration();
                            if (newtime > timestamp) {
                                is = connection.getInputStream();

                                entry.setTimestamp(connection.getExpiration());
                                // cache until the document expires
                                if (entry.getTimestamp()
                                         < System.currentTimeMillis()) {
                                    // ok, they don't want us to cache it... we will anyway.
                                    // use the modified time as the timestamp
                                    entry.setTimestamp(
                                            connection.getLastModified());
                                }
                            }
                        } catch (Exception ex) {}
                    }
                    try {
                        if (is != null) {
                            //	debug("RELOADING CHANGED DTD: path = "+path);
                            loadDTDIntoCache(is, entry);
                        }
                    } catch (Exception ex) {}
                }
            }
        }
    }


    /**
     *  Gets the cachedDTDEntries attribute of the DTDCache object
     *
     *@return    The cachedDTDEntries value
     */
    public Collection getCachedDTDEntries() {
        Set keys = _dtdEntries.keySet();
        TreeSet sortedkeys = new TreeSet(keys);
        return sortedkeys;
    }


    /**
     *  Description of the Method
     */
    public void printCache() {
        Set s = _publicIdCache.keySet();
        Iterator it = s.iterator();
        debug("PUBLIC Id's:\n");

        while (it.hasNext()) {
            debug(it.next() + "\n");
        }
        s = _systemIdCache.keySet();
        it = s.iterator();
        debug("\nSYSTEM Id's:\n");

        while (it.hasNext()) {
            debug(it.next() + "\n");
        }
        debug("\n");

    }


    /**
     *@param  s      Description of the Parameter
     *@return        Description of the Return Value
     *@deprecated    use fixSlashes instead
     *@author        Frank Blecha
     */
    protected String fixslashes(String s) {
        return fixSlashes(s);
    }


    /**
     *  make all slashes forward slashes cause windows sucks
     *
     *@param  s  Description of the Parameter
     *@return    Description of the Return Value
     */
    protected String fixSlashes(String s) {
        StringBuffer sb = new StringBuffer(s);
        /*
         *  strip off any leading slashes cause windows sucks and makes URL's like:
         *  file:/C:/blah
         *  when the cwd comes out like C:/blah
         */
        if (sb.charAt(0) == '/' && sb.charAt(2) == ':') {
            sb.deleteCharAt(0);
        }

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\\') {
                sb.setCharAt(i, '/');
            }
        }
        return sb.toString();
    }


    /**
     *  simple debugging print routine
     *
     *@param  s  Description of the Parameter
     */
    protected void debug(String s) {
        //if (System.getProperty("DEBUG") != null) {
        //    System.out.println("[debug] "+s);
        //}
        MerlotDebug.msg(s);
    }


    /**
     *@param  originalFileName  - systemID
     *@return                   String
     *@author                   Frank Blecha
     */
    protected String formatFileName(String originalFileName) {
        String modifiedFileName = originalFileName;
        if (originalFileName.startsWith(FILE_PROTOCOL_NAME)) {
            modifiedFileName =
                    fixSlashes(
                    originalFileName.substring(FILE_PROTOCOL_NAME.length()));
        }
        String currentDir = fixSlashes(System.getProperty("user.dir"));

        if (originalFileName.startsWith(currentDir)) {
            modifiedFileName = modifiedFileName.substring(currentDir.length());
        }
        return modifiedFileName;
    }


    /**
     *  Gets the dTDPath attribute of the DTDCache object
     *
     *@return    The dTDPath value
     */
    protected String getDTDPath() {
        String ret = null;
        if (_properties != null) {
            ret = _properties.getProperty("path.dtd");
        } else {
            debug("DTDCache: properties is null");
            ret = null;
        }
        return ret;
    }

}

