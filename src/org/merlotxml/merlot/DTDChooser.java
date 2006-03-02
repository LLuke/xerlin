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
http://www.merlotxml.org.
*/

// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.

package org.merlotxml.merlot;
import java.awt.Component;
import java.io.File;
import java.io.StringBufferInputStream;
import java.util.Collection;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import matthew.awt.StrutLayout;

import org.merlotxml.util.xml.DTDCache;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.ValidDOMLiaison;
import org.merlotxml.util.xml.ValidDocument;

/**
 * This class provides a way to choose a DTD either from a plugin or 
 * from the filesystem.
 */
public class DTDChooser implements MerlotConstants {
    protected DTDCache _cache;
    protected JPanel _chooserPanel = null;
    protected DTDListModel _listModel = null;
    protected JList _list;

    public DTDChooser(DTDCache dtdcache) {
        _cache = dtdcache;
    }

    /**
     * Lets the user choose a dtd, and returns the cache entry for it
     */
    public DTDCacheEntry chooseDTD() {

        return chooseDTD(
            MerlotResource.getString(UI, "dtd.chooser.defaultmsg"));
    }

    /**
     * Bring up the dtd chooser dialog with the given message for the user
     */
    public DTDCacheEntry chooseDTD(String message) {
        JLabel label = new JLabel(message);
        return chooseDTD(label);

    }

    /**
     * Gives the user a DTD Chooser dialog with the given message. 
     * @param msg the message to display above the list
     * @return a DTDCacheEntry for the chosen dtd, or null if none was chosen
     */

    public DTDCacheEntry chooseDTD(Component msg) {

        DTDCacheEntry ret = null;

        if (_chooserPanel == null) {
            setupChooserPanel();
        } else {
            _listModel.reload();

        }

        JPanel topPanel = new JPanel();
        StrutLayout lay = new StrutLayout();
        StrutLayout.setDefaultStrutLength(10);
        topPanel.setLayout(lay);
        topPanel.add(msg);
        StrutLayout.StrutConstraint strut =
            new StrutLayout.StrutConstraint(
                msg,
                StrutLayout.BOTTOM_LEFT,
                StrutLayout.TOP_LEFT,
                StrutLayout.SOUTH,
                10);
        topPanel.add(_chooserPanel, strut);

        lay.setSprings(_chooserPanel, StrutLayout.SPRING_BOTH);

        Object[] options =
            {
                MerlotResource.getString(UI, "dtd.chooser.browse"),
                MerlotResource.getString(UI, "OK"),
                MerlotResource.getString(UI, "CANCEL")};

        int ok =
            MerlotOptionPane.showInternalOptionDialog(
                XMLEditorFrame.getSharedInstance().getDesktopPane(),
                topPanel,
                MerlotResource.getString(UI, "dtd.chooser.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]);
        switch (ok) {
            case 0 : // browse
                // browse for a dtd file
                File f = null;
                if ((f =
                    XMLEditorFrame.getSharedInstance().getFileToOpen(
                        new DTDFileFilter(),
                        "*.dtd",
                        false))
                    != null) {
                    String dtdfile = f.getPath();
                    // get a public id for the dtd
                    String publicId = requestPublicId();

                    ret =
                        DTDCache.getSharedInstance().findDTDbySystemId(
                            publicId,
                            dtdfile,
                            null);
                    // Here's where we need revert to some trickery to get
                    // the GrammarDocument object
                    try {
                        // This will fail for a schema - handled elsewhere...
                        if (ret != null) {
                            StringBuffer dummyXML = new StringBuffer();
                            dummyXML.append("<?xml version=\"1.0\"?>\n");
                            // How else can we tell?
                            // Assume that a dtd will never end with an xsd extension
                            if (!dtdfile.endsWith(".xsd")) {
                                dummyXML.append(
                                    "<!DOCTYPE dummy SYSTEM \""
                                        + dtdfile
                                        + "\">\n");
                                dummyXML.append("<dummy/>");
                            }
                            StringBufferInputStream is =
                                new StringBufferInputStream(
                                    dummyXML.toString());
                            // get a DOMLiaison from the settings and parse the given file
                            ValidDOMLiaison domlia =
                                XMLEditor.getSharedInstance().getDOMLiaison();
                            if (domlia != null) {
                                ValidDocument doc =
                                    domlia.parseValidXMLStream(is, dtdfile);
                                if (doc != null) {
                                    ret.getParsedDTD().setGrammarDocument(
                                        doc.getGrammarDocument());
                                    MerlotDebug.msg(
                                        "GDocument "
                                            + doc.getGrammarDocument());
                                } else
                                    MerlotDebug.msg("ValidDocument is null");
                            }
                        }
                    } catch (Exception ex) {
                        MerlotDebug.msg(
                            "Got an exception when trying to "
                                + "obtain the GrammarDocument as a DTD");
                        MerlotDebug.msg("Hopefully it's a schema");
                        if (!dtdfile.endsWith(".xs") 
                                && !dtdfile.endsWith(".xsd")) {
                            String emsg = MerlotResource.getString(ERR, "xml.invalid.dtd.schema");
                            MerlotError.exception(ex, emsg);
                            return null;
                        }
                    }
                    break;
                }
            case 1 : // ok
                // figure out which one they selected
                int selection = _list.getSelectedIndex();
                if (selection >= 0) {
                    ret = _listModel.getCacheEntry(selection);
                    DTDCache.getSharedInstance().checkCacheEntryTimestamp(ret);

                }
                break;

            case 2 : // cancel

                ret = null;

            default :
                ret = null;
        }
        MerlotDebug.msg("DTDChooser: ret = " + ret);

        return ret;

    }

    public String requestPublicId() {
        StrutLayout lay = new StrutLayout();
        JPanel p = new JPanel(lay);
        JLabel l =
            new JLabel(MerlotResource.getString(UI, "dtd.chooser.publicid"));
        JTextField f = new JTextField(40);
        StrutLayout.StrutConstraint strut =
            new StrutLayout.StrutConstraint(
                l,
                StrutLayout.MID_RIGHT,
                StrutLayout.MID_LEFT,
                StrutLayout.EAST,
                10);
        p.add(l);
        p.add(f, strut);

        Object[] options =
            {
                MerlotResource.getString(UI, "OK"),
                MerlotResource.getString(UI, "NONE")};

        int ok =
            MerlotOptionPane.showInternalOptionDialog(
                XMLEditorFrame.getSharedInstance().getDesktopPane(),
                p,
                MerlotResource.getString(UI, "dtd.chooser.publicid.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        switch (ok) {
            case 0 : // OK
                String s = f.getText();
                if (!s.trim().equals("")) {
                    return s;
                }
                break;
            case 1 :
                break;
        }
        return null;

    }

    protected void setupChooserPanel() {

        StrutLayout lay = new StrutLayout();
        _chooserPanel = new JPanel(lay);

        StrutLayout.setDefaultStrutLength(5);
        //		JLabel l = new JLabel(MerlotResource.getString(UI,"dtd.chooser.title"));
        _list = setupDTDList();
        JScrollPane listScroller = new JScrollPane(_list);
        //	_chooserPanel.add(l);

        /*		StrutLayout.StrutConstraint strut = new StrutLayout.StrutConstraint(l,
        																	StrutLayout.BOTTOM_LEFT,
        																	StrutLayout.TOP_LEFT, 
        																StrutLayout.SOUTH,10);
        */
        //	_chooserPanel.add(listScroller,strut);
        _chooserPanel.add(listScroller);

        lay.setSprings(listScroller, StrutLayout.SPRING_BOTH);

    }

    protected JList setupDTDList() {
        _listModel = new DTDListModel(_cache);
        JList list = new JList(_listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return list;
    }

    protected class DTDListModel extends AbstractListModel {
        protected DTDCache _cache;
        protected DTDCacheEntry[] _list = new DTDCacheEntry[0];

        public DTDListModel(DTDCache cache) {
            super();
            _cache = cache;
            reload();

        }

        public void reload() {
            // load up the list values from the DTDCache
            Collection c = _cache.getCachedDTDEntries();
            _list = (DTDCacheEntry[]) c.toArray(_list);
            // debug
            for (int i = 0; i < _list.length; i++) {
                MerlotDebug.msg("DTDListModel loading: " + _list[i]);
            }
            fireContentsChanged(this, 0, _list.length - 1);

        }
        public int getSize() {
            return _list.length;
        }

        public Object getElementAt(int i) {
            DTDCacheEntry entry = _list[i];
            String p = entry.getPublicId();
            String s = entry.getSystemId();
            if (p != null) {
                return p;
            } else {
                return s;
            }

        }
        public DTDCacheEntry getCacheEntry(int i) {
            return _list[i];
        }

    }

}
