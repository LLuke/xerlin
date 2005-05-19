/*
====================================================================
Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
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
http://www.merlotxml.org/.
*/

package org.merlotxml.util.xml;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This contains information we need to keep with a dtd that has been
 * cached.
 *
 * @author Kelly A. Campbell
 */

public class DTDCacheEntry implements Comparable {
    /**
     * The publicId
     */
    protected String _publicId = null;
    /**
     * optional system id
     */
    protected String _systemId = null;
    /**
     * Root element for this particular entry
     */
    protected String _rootElement = null;

    /**
     * Path to the file containing the DTD... can be a system path, a url, 
     * or a path into a jar, including a ! if the file
     */
    protected String _filePath = null;

    /**
     * last modification time of the file the dtd was loaded from. if this is 0, then we cache
     * the file indefinitely, and never check back with the source
     */
    protected long _timestamp = 0;

    /**
     * cached char array of the dtd stream
     */
    protected char[] _cachedDTDStream;

    /**
     * a parsed version of the dtd
     */
    protected DTDDocument _parsedDTD = null;
    // this could be a weak ref if we want to reduce
    // some memory overhead

    public DTDCacheEntry(String publicId, String systemId) {
        _publicId = publicId;
        _systemId = systemId;

    }
    public void setPublicId(String s) {
        _publicId = s;
    }
    public void setSystemId(String s) {
        _systemId = s;
    }
    public String getPublicId() {
        return _publicId;
    }
    public String getSystemId() {
        return _systemId;
    }

    public void setFilePath(String s) {
        _filePath = s;
    }
    public String getFilePath() {
        return _filePath;
    }

    public void setTimestamp(long t) {
        _timestamp = t;
    }
    public long getTimestamp() {
        return _timestamp;
    }
    public void setCachedDTDStream(char[] s) {
        _cachedDTDStream = s;
    }
    public char[] getCachedDTDStream() {
        return _cachedDTDStream;
    }
    public void setParsedDTD(DTDDocument parsedDTD) {
        _parsedDTD = parsedDTD;
    }
    public DTDDocument getParsedDTD() {
        return _parsedDTD;
    }
    public String toString() {
        return "DTDCacheEntry(public="
            + _publicId
            + " system="
            + _systemId
            + " file="
            + _filePath
            + " cachedDTDStream is "
            + (_cachedDTDStream != null ? "non-" : "")
            + "null)";

    }

    public List getPossibleRootNames() {
        Enumeration e = _parsedDTD.getElements();
        ArrayList list = new ArrayList();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof String)
                list.add(o);
            else if (o instanceof DTDElement){
                DTDElement el = (DTDElement)o;
                String s = el.getName();
                list.add(s);
            }
        }
        return list;

    }

    public int compareTo(Object o) throws ClassCastException {
        int rtn = 0;
        if (o instanceof DTDCacheEntry) {
            DTDCacheEntry entry = (DTDCacheEntry) o;
            if (_publicId != null
                && entry != null
                && entry._publicId != null) {
                rtn = _publicId.compareTo(entry._publicId);
            }
            if (rtn == 0
                && _systemId != null
                && entry != null
                && entry._systemId != null) {
                rtn = _systemId.compareTo(entry._systemId);
            }
            return rtn;
        } else {
            throw new ClassCastException();
        }

    }

}
