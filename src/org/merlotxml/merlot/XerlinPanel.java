/*

   ======================================================================
   The Xerlin XML Editor is Copyright (c) 2002 SpeedLegal Holdings, Inc.
   and other contributors.  It includes software developed for the
   Merlot XML Editor which is Copyright (c) 1999-2000 ChannelPoint, Inc.
   All rights reserved.
   ======================================================================

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

   1. Redistribution of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistribution in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. All advertising materials mentioning features or use of this
   software must display the following acknowledgment:
   "This product includes software developed by the SpeedLegal Group for
   use in the Xerlin XML Editor www.xerlin.org and software developed by
   ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"

   4. Except for the acknowledgments required by these conditions, any
   names trademarked by SpeedLegal Holdings, Inc. must not be used to
   endorse or promote products derived from this software without prior
   written permission. For written permission, please contact
   info@speedlegal.com. Any names trademarked by ChannelPoint, Inc. must
   not be used to endorse or promote products derived from this software
   without prior written permission. For written permission, please
   contact legal@channelpoint.com.

    5. Except for the acknowledgment required by these conditions, Products
    derived from this software may not be called "Xerlin" nor may "Xerlin"
    appear in their names without prior written permission of SpeedLegal
    Holdings, Inc. Products derived from this software may not be called
    "Merlot" nor may "Merlot" appear in their names without prior written
    permission of ChannelPoint, Inc.

    6. Redistribution of any form whatsoever must retain the following
    acknowledgment:
    "This product includes software developed by the SpeedLegal Group for
    use in the Xerlin XML Editor www.xerlin.org and software developed by
    ChannelPoint, Inc. for use in the Merlot XML Editor www.merlotxml.org"

    7. Developers who choose to contribute code or documentation to Xerlin
    (which is encouraged but not required) acknowledge and agree that: (a) any
    such contributions accepted and included in Xerlin will be subject to this
    license; (b) SpeedLegal Holdings, Inc. or any successor that hosts the
    Xerlin project will always have the right to make those contributions
    available under this license or an equivalent open source license; and
    (c) all contributions are made with the full authority of their owner/s.

    THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
    AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
    SPEEDLEGAL HOLDINGS, INC. OR CHANNELPOINT, INC. OR ANY CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
    THE POSSIBILITY OF SUCH DAMAGE.
    ======================================================================

    For more information on SPEEDLEGAL visit www.speedlegal.com

    For information on the XERLIN project visit www.xerlin.org

*/
package org.merlotxml.merlot;

import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

/**
 *  XerlinPanel is a JComponent containing an editor panel.
 *  This panel can be integrated into other applications.
 *
 *@author     justin
 *@created    August 14, 2002
 */
public class XerlinPanel extends JPanel implements ClipboardOwner {
    File _file;
    XMLFile _xmlFile;
    XMLEditorDoc _doc;
    XMLEditorDocUI _ui;

    /**
     *  The clipboard for this application. This one holds just about anything
     *  except tree nodes. It mainly holds text.
     *
     *@since
     */
    Clipboard _clipboard;

    /**
     *  Special clipboard for the tree nodes. We keep this separate because a
     *  paste in the tree is different from a paste in a text box.
     *
     *@since
     */
    Clipboard _treeClipboard;


    /**
     *  Constructor for the XerlinPanel object
     *
     *@param  f                    The file to be edited
     *@exception  MerlotException  Any config/setup issues
     *@since
     */
    public XerlinPanel(File f) throws MerlotException {
        load(f);
    }


    /**
     *  Constructor for the XerlinPanel object
     *
     *@param  rootname              The name of the root node
     *@param  publicid              The PUBLIC id or null
     *@param  systemid              The SYSTEM id or null
     *@exception  MerlotException  Any config/setup issues
     *@since
     */
    public XerlinPanel(String rootname, String publicid, String systemid) 
                        throws MerlotException{
        newFile(rootname, publicid, systemid);
    }


    /**
     *  Returns the DOMDocument - the modified DOM from the panel
     *
     *@return    the DOM Document for this panel
     *@since
     */
    public Document getDOMDocument() {
        _ui.saveOpenEditors();
        return _doc.getDocument();
    }


    /**
     *  Returns the XMLEditorDoc 
     *
     *@return    the XMLEditorDoc for this panel
     *@since
     */
    public XMLEditorDoc getXMLEditorDoc() {
        return _doc;
    }


    /**
     *  Returns the XMLFile 
     *
     *@return    the XMLFile for this panel
     *@since
     */
    public XMLFile getXMLFile() {
        return _xmlFile;
    }


    /**
     *  Returns true if this document is marked as dirty (needs saving)
     *@return    the dirtiness state of the DOM object
     *
     */
    public boolean isDirty() {
        return _xmlFile.isDirty();
    }


    /**
     *  Saves in the same file we opened
     */
    public void save() throws MerlotException {
        if(_ui.saveOpenEditors())
            _xmlFile.save();
    }


    /**
     * Saves to a new file
     */
    public void saveAs(File f) throws MerlotException {
        if(_ui.saveOpenEditors())
            _xmlFile.saveAs(f);
    } 


    /**
     *  Gets the XMLEditorDocUI attribute of the XerlinPanel object
     *
     *@return    The XMLEditorDocUI value
     *@since
     */
    public XMLEditorDocUI getXMLEditorDocUI() {
        return _ui;
    }

    
    /**
     *  Loads an XML file into the XerlinPanel
     *
     *@param  f                    The file to be loaded
     *@exception  MerlotException  Any config/setup issues
     *@since
     */
    public void load(File f) throws MerlotException {
        _file = f;
        init();
        this.removeAll();
        build();
        this.revalidate();                
    }


    /**
     *  Starts a brand new XML document in the XerlinPanel
     *
     **@param  rootname             The name of the root node
     *@param   publicid             The PUBLIC id or null
     *@param   systemid             The SYSTEM id or null
     *@exception  MerlotException   Any config/setup issues
     *@since
     *@since
     */
    public void newFile(String rootnode, String publicid, String systemid) 
                        throws MerlotException {
        try { 
            File f = XMLEditorFrame.createNewFile(rootnode, publicid, systemid);
            load(f);
        } catch (IOException ex) {
            throw new MerlotException("IOException: "+ _file, ex);
        }
    }


    /**
     *  Returns the text clipboard
     *
     *@return    The Clipboard value
     *@since
     */
    public Clipboard getClipboard() {
        return _clipboard;
    }


    /**
     *  Returns the DOMTree clipboard
     *
     *@return    the TreeClipboard 
     *@since
     */
    public Clipboard getTreeClipboard() {
        return _treeClipboard;
    }


    /**
     *  Notification that we lost ownership of a clipboard item
     *
     *@param  c  Clipboard
     *@param  t  Transferable
     *@since
     */
    public void lostOwnership(Clipboard c, Transferable t) {
    }


    /**
     *  Builds the XerlinPanel
     *
     *@since
     */
    protected void build() {
        this.setLayout(new BorderLayout());
        this.add(_ui, BorderLayout.CENTER);
    }


    /**
     *  Initializes the XerlinPanel
     *
     *@exception  MerlotException thrown when there were setup errors 
     *@since
     */
    private void init() throws MerlotException {
        new XMLEditor(new String[0], this);
        _xmlFile = new XMLFile(_file);
        _doc = new XMLEditorDoc(_xmlFile);
        _ui = _doc.getXMLEditorDocUI();
        _clipboard = new Clipboard("MerlotClipboard");
        _treeClipboard = new Clipboard("MerlotNodeClipboard");
    }
}

