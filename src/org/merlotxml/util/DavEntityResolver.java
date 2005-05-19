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
package org.merlotxml.util;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.merlotxml.merlot.XMLEditorFrame;
import org.merlotxml.merlot.XerlinFileDialogs;
import org.merlotxml.merlot.XerlinDavFileDialogs;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.speedlegal.webdav.DAVServer;
import com.speedlegal.webdav.EditorFile;

public class DavEntityResolver implements EntityResolver {
    private static HashMap _buffermap;

    public DavEntityResolver() {
        _buffermap = new HashMap();
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        InputStream is;

        try {
            String path = systemId;
            if (path.startsWith("file:"))
                path = path.substring(5);
            is = getWebdavResource(path, true);
            if (is != null)
                return new InputSource(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private InputStream getWebdavResource(String path, boolean dual) {
        XerlinFileDialogs dialog =
            XMLEditorFrame.getSharedInstance().getFileDialogs();
        DAVServer client;
        if (dialog instanceof XerlinDavFileDialogs) {
            client = ((XerlinDavFileDialogs) dialog).getClient();
        } else
            return null;

        if (client == null)
            return null;

        DualInputStream stream;

        stream = (DualInputStream) _buffermap.get(path);

        if (stream != null) {
            _buffermap.remove(path);
            return stream.getSecondaryStream();
        }

        EditorFile sprn = client.getNode(path);

        if (sprn != null) {
            try {
                InputStream realStream = sprn.getContents();
                if (dual) {
                    stream = new DualInputStream(realStream, sprn);
                    _buffermap.put(path, stream);

                    return stream.getPrimaryStream();
                } else
                    return realStream;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private class DualInputStream extends FilterInputStream {
        boolean actuallyClose;
        EditorFile webfile;

        public DualInputStream(InputStream stream, EditorFile webfile) {
            super(new BufferedInputStream(stream));
            this.webfile = webfile;
            in.mark(1024 * 256);
        }

        public InputStream getPrimaryStream() {
            actuallyClose = false;
            return this;
        }

        public InputStream getSecondaryStream() {
            actuallyClose = true;
            try {
                in.reset();
            } catch (IOException e) {
                /* Mark was invalidated */
                try {
                    return webfile.getContents();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return null;
                }
            }
            return this;
        }

        public void close() throws IOException {
            if (actuallyClose) {
                in.close();
            }
        }

    }
}
