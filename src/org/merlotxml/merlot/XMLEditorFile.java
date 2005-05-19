/*
 *  ======================================================================
 *  The Xerlin XML Editor is Copyright (c) 2002 SpeedLegal Holdings, Inc.
 *  and other contributors.  It includes software developed for the
 *  Merlot XML Editor which is Copyright (c) 1999-2000 ChannelPoint, Inc.
 *  All rights reserved.
 *  ======================================================================
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistribution of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *  2. Redistribution in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this
 *  software must display the following acknowledgment:
 *  "This product includes software developed by the SpeedLegal Group for
 *  use in the Xerlin XML Editor www.xerlin.org and software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"
 *  4. Except for the acknowledgments required by these conditions, any
 *  names trademarked by SpeedLegal Holdings, Inc. must not be used to
 *  endorse or promote products derived from this software without prior
 *  written permission. For written permission, please contact
 *  info@speedlegal.com. Any names trademarked by ChannelPoint, Inc. must
 *  not be used to endorse or promote products derived from this software
 *  without prior written permission. For written permission, please
 *  contact legal@channelpoint.com.
 *  5. Except for the acknowledgment required by these conditions, Products
 *  derived from this software may not be called "Xerlin" nor may "Xerlin"
 *  appear in their names without prior written permission of SpeedLegal
 *  Holdings, Inc. Products derived from this software may not be called
 *  "Merlot" nor may "Merlot" appear in their names without prior written
 *  permission of ChannelPoint, Inc.
 *  6. Redistribution of any form whatsoever must retain the following
 *  acknowledgment:
 *  "This product includes software developed by the SpeedLegal Group for
 *  use in the Xerlin XML Editor www.xerlin.org and software developed by
 *  ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"
 *  7. Developers who choose to contribute code or documentation to Xerlin
 *  (which is encouraged but not required) acknowledge and agree that: (a) any
 *  such contributions accepted and included in Xerlin will be subject to this
 *  license; (b) SpeedLegal Holdings, Inc. or any successor that hosts the
 *  Xerlin project will always have the right to make those contributions
 *  available under this license or an equivalent open source license; and
 *  (c) all contributions are made with the full authority of their owner/s.
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 *  SPEEDLEGAL HOLDINGS, INC. OR CHANNELPOINT, INC. OR ANY CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 *  THE POSSIBILITY OF SUCH DAMAGE.
 *  ======================================================================
 *  For more information on SPEEDLEGAL visit www.speedlegal.com
 *  For information on the XERLIN project visit www.xerlin.org
 */
package org.merlotxml.merlot;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;

import org.merlotxml.util.DavEntityResolver;
import org.merlotxml.util.xml.ValidDOMLiaison;

import com.speedlegal.webdav.DAVProperty;
import com.speedlegal.webdav.EditorFile;

/**
 *  An XML file that is a WebDav resource
 *
 *@author     Justin Lipton
 *@created    16 March 2003
 */
public class XMLEditorFile extends XMLFile {
    protected EditorFile _efile = null;
    /**
     *  Status holder for marking the file as needing a save
     */
    private static HashMap _fileLocks = new HashMap();

    private boolean _lockedByOther = false;

    /**
     *  WebDav property map
     */
    protected Vector _webDavProperties = new Vector();

    static int TIMEOUT =
        ((Integer) Integer
            .decode(
                XMLEditorSettings.getSharedInstance().getProperty(
                    "webdav.timeout")))
            .intValue();

    /**
     *  Reads in the given filename to create the Document tree
     *
     *@param  f                    Description of the Parameter
     *@exception  MerlotException  Description of the Exception
     */
    public XMLEditorFile(File f) throws MerlotException {
        if (f instanceof EditorFile) {
            _efile = (EditorFile) f;
            ValidDOMLiaison vdl = XMLEditor.getSharedInstance().getDOMLiaison();
            // Add dav entity resolver
            vdl.addEntityResolver(new DavEntityResolver());
        }
        _file = f;
        _propchange = new PropertyChangeSupport(this);

        // now parse the file and get a Document
        parseDocument();
    }

    protected void parseDocument() throws MerlotException {
        if (!(_file instanceof EditorFile)) {
            super.parseDocument();
            return;
        }

        try {
            super.parseDocument();
        } catch (MerlotException me) {
            removeLock();
            throw me;
        } catch (Exception ex) {
            removeLock();
            throw new MerlotException("Error: " + ex.getMessage(), ex);
        }
    }

    protected InputStream getFileInputStream() throws FileNotFoundException {
        if (_file instanceof EditorFile)
            return _efile.getContents();
        return super.getFileInputStream();
    }

    /**
     *  Perfoms a repository lock on the file
     *
     *@param  f  The new lock value
     */
    public static boolean setLock(EditorFile f) {
        if (TIMEOUT == -1) {
            TIMEOUT = Integer.MAX_VALUE;
        }

        MerlotDebug.msg("locking " + f + " for " + TIMEOUT + " seconds");
        if (f.lock(TIMEOUT)) {
            _fileLocks.put(f, "locked");
            return true;
        }
        return false;
    }

    /**
     *  Sets the lock attribute of the XMLFile object
     */
    protected boolean setLock() {
        // This means that WE have set the lock therefore don't need hasLock
        return setLock(_efile);
    }

    /**
      *  Set the given properties on the file, using PROPPATCH
      *
      *@param  f           The new properties value
      *@param  properties  The new properties value
      */
    public static void setProperties(EditorFile f, Vector properties) {
        f.setProperties(properties);
    }

    public void setProperties() {
        _efile.setProperties(_webDavProperties);
    }

    /**
     *  Set the given WebDav property that will be updated/written when the file
     *  is saved with namespace support
     *
     *@param  namespace  The new webDavProperty value
     *@param  property   The new webDavProperty value
     *@param  value      The new webDavProperty value
     */
    public void setWebDavProperty(
        String namespace,
        String property,
        String value) {
        MerlotDebug.msg(
            "Adding prop " + namespace + " " + property + " " + value);
        _webDavProperties.add(new DAVProperty(namespace, property, value));
    }
    /**
      *  Description of the Method
      */
    public void removeProperties() {
        _webDavProperties.removeAllElements();
    }

    /**
     *  Perfoms a repository lock on the file
     *
     *@param  f  Description of the Parameter
     */
    public static void removeLock(EditorFile f) {
        MerlotDebug.msg("unlocking " + f.getPath());
        f.unlock();
        _fileLocks.remove(f);
    }

    /**
     *  Description of the Method
     */
    protected void removeLock() {
        MerlotDebug.msg("unlocking " + _efile.getPath());
        _efile.unlock();
        _fileLocks.remove(_efile);
    }

    /**
      *  Does a user have repository access to read this file?
      *
      *@param  f  Description of the Parameter
      *@return    Description of the Return Value
      */
    public static boolean hasReadAccess(EditorFile f) {
        return f.canRead();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    protected boolean hasReadAccess() {
        return hasReadAccess(_efile);
    }

    /**
     *  Does a user have repository access to write to this file? This means
     *  that: - the file is not locked by another user - the user has access to
     *  write to the file
     *
     *@param  f  Description of the Parameter
     *@return    Description of the Return Value
     */
    public static boolean hasWriteAccess(EditorFile f) {
        boolean canWrite = f.canWrite();
        if (!canWrite) {
            return false;
        }
        if (!isLocked(f)) {
            return true;
        }
        if (isLockedByOther(f)) {
            return false;
        }
        //if (hasLock(f))
        //    return true;
        return true;
    }
    /**
      *  Description of the Method
      *
      *@param  f  Description of the Parameter
      *@return    Description of the Return Value
      */
    public static boolean hasLock(EditorFile f) {
        if (_fileLocks.get(f) == null) {
            return f.hasLock();
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean hasLock() {
        return hasLock(_efile);
    }

    /**
     *  Gets the locked attribute of the XMLFile object
     *
     *@return    The locked value
     */
    public boolean isLocked() {
        return isLocked(_efile);
    }
    /**
     *  Gets the locked attribute of the XMLFile class
     *
     *@param  f  Description of the Parameter
     *@return    The locked value
     */
    public static boolean isLocked(EditorFile f) {
        if (_fileLocks.get(f) == null) {
            return f.isLocked();
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean hasWriteAccess() {
        return hasWriteAccess(_efile);
    }

    /**
     *  Gets the lockedByOther attribute of the XMLFile object
     *
     *@return    The lockedByOther value
     */
    protected boolean isLockedByOther() {
        if (_lockedByOther) {
            return true;
        }
        if (_fileLocks.get(_efile) == null) {
            return true;
        }
        return false;
    }

    /**
     *  Gets the lockedByOther attribute of the XMLFile class
     *
     *@param  f  Description of the Parameter
     *@return    The lockedByOther value
     */
    protected static boolean isLockedByOther(EditorFile f) {
        if (_fileLocks.get(f) == null) {
            return !f.hasLock();
        }
        return false;
    }

    /**
      *  Sets the lockedByOther attribute of the XMLFile object
      *
      *@param  state  The new lockedByOther value
      */
    protected void setLockedByOther(boolean state) {
        _lockedByOther = state;
    }

    /**
    *  Description of the Method
    *
    *@param  f                    Description of the Parameter
    *@return                      Description of the Return Value
    *@exception  MerlotException  Description of the Exception
    */
    public void saveAs(File file) throws MerlotException {
        EditorFile f;
        if (file instanceof EditorFile)
            f = (EditorFile) file;
        else {
            super.saveAs(file);
            return;
        }
        try {
            // keep a backup of the original file incase the saveAs fails
            File tmpFile = File.createTempFile(f.getName(), ".tmpsave");

            // This saves the output to the tmp file
            OutputStream s = new FileOutputStream(tmpFile);
            printRawXML(s, true);
            s.close();

            // if it didn't work an exception will be thrown and we won't get
            // here now replace the old file with the tmp one
            try {
                XMLEditorFrame.setWaitCursor();
                // Should not need to do this at all...
                if (TIMEOUT == -1) {
                    TIMEOUT = Integer.MAX_VALUE;
                }
                // Explicitly set the type
                f.setType("text/xml");
                OutputStream os = f.putContents();

                FileInputStream is = new FileInputStream(tmpFile);
                byte buf[] = new byte[4096];
                int bytes;
                while ((bytes = is.read(buf)) > 0) {
                    os.write(buf, 0, bytes);
                }
                is.close();
                os.close();

                // Set our desired properties on it.
                // for the moment, our hashmap of (string)->(string)
                // property->value mappings is empty. Populate it.
                if (_webDavProperties.size() > 0) {
                    setProperties(f, _webDavProperties);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new MerlotException(
                    MerlotResource.getString(ERR, "file.webdav.error")
                        + " "
                        + e.getMessage(),
                    e);
            } finally {
                XMLEditorFrame.setDefaultCursor();
            }

            // If the old file exists and is locked - release the lock on it
            // Never remove the lock on a file you don't want to lock
            //andLock
            MerlotDebug.msg("Testing if can remove Lock in saveAs");
            if (_efile != null
                && _efile.isLocked()
                && !isNew()
                && hasWriteAccess()
                && !_efile.equals(f)) {
                removeLock();
            }

            _file = f;
            _efile = f;

            setDirty(false);
            setNew(false);
        } catch (IOException ex) {
            throw new MerlotException(
                "IOException while saving file: " + ex.getMessage(),
                ex);
        } finally {
            XMLEditorFrame.setDefaultCursor();
        }
    }

    protected void close() {
        if (_file instanceof EditorFile)
            removeLock();
        else
            super.close();
    }

    protected void close(File f) {
        if (f instanceof EditorFile)
            removeLock((EditorFile) f);
        else
            super.close(f);
    }

}
