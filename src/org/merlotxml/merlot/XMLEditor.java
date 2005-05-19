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

// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.
package org.merlotxml.merlot;

import java.awt.event.ActionEvent;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import matthew.awt.StrutLayout;

import org.merlotxml.merlot.plugin.PluginManager;
import org.merlotxml.util.xml.DTDCache;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.ValidDOMLiaison;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is the main class for Merlot. It provides the entry point to the
 * application with the main static method. This starts by loading up the
 * XMLEditorSettings which handles parsing the command line options, and reading
 * in the application properties files. Next, this loads plugins via the
 * PluginManager, and finally it initializes and opens up the main
 * XMLEditorFrame.
 * <P>
 * XMLEditor is a Singleton class. Use getSharedInstance to get the singleton
 * instance of this class.
 * 
 * @see XMLEditorSettings
 * @see org.merlotxml.merlot.plugin.PluginManager
 * @see XMLEditorFrame
 * 
 * @author Kelly A. Campbell
 * 
 */
public class XMLEditor implements MerlotConstants {
    /**
     * The main frame for the application
     */
    protected XMLEditorFrame _frame;

    /**
     * Global application settings
     */
    protected XMLEditorSettings _settings;

    /**
     * Singleton instance of the XMLEditor application
     */
    protected static XMLEditor _sharedInstance;

    /**
     * Validating DOMLiaison implementation instance
     */
    protected ValidDOMLiaison _domLiaison = null;

    private XerlinPanel _xpanel;

    /**
     * The main method. This is the entry point for the application when called
     * on the command line with java org.merlotxml.merlot.XMLEditor
     * 
     * @param args
     *            the command line arguments passed to the main method
     */
    public static void main(String[] args) {
        try {
            // workaround a bug in WebStart with file permissions on windows
            try {
                System.setSecurityManager(null);

            } catch (Throwable t) {
                t.printStackTrace();
            }

            XMLEditor editor = new XMLEditor(args);
            editor.run();
        } catch (MerlotException e) {
            e.printStackTrace();
            MerlotDebug.exception(e);
        }

    }

    /**
     * Get the singleton instance of this class
     */
    public static XMLEditor getSharedInstance() {
        return _sharedInstance;
    }

    public XerlinPanel getXerlinPanel() {
        return _xpanel;
    }

    /**
     * Construct an XMLEditor based on the given command line args
     * 
     * @param args
     *            command line args
     * @exception MerlotException
     *                if the plugin manager throws an exception
     */
    public XMLEditor(String[] args) throws MerlotException {
        this(args, null);
    }

    public XMLEditor(String[] args, XerlinPanel xpanel) throws MerlotException {
        File f;
        String[] openFiles;

        _sharedInstance = this;
        boolean showFrame = (xpanel == null);
        _settings = new XMLEditorSettings(args, showFrame);

        // initialize plugins
        try {
            PluginManager.getInstance().loadPlugins();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MerlotException(e);
        }
        DTDCache cache = DTDCache.getSharedInstance();
        cache.setProperties(_settings.getProperties());

        // set an alternate locale if the settings define one
        Locale.setDefault(_settings.getLocale());
        new MerlotError();

        if (showFrame) {

            openFiles = _settings.getOpenFiles();
            final XMLEditorDoc docs[] = new XMLEditorDoc[openFiles.length];
            for (int i = 0; i < openFiles.length; i++) {
                f = new File(openFiles[i]);
                if (f.exists())
                    docs[i] = new XMLEditorDoc(f);
            }

            final Runnable openDocs = new Runnable() {
                public void run() {
                    for (int i = 0; i < docs.length; i++) {
                        if (docs[i] != null)
                            _frame.add(docs[i]);
                    }
                }
            };

            final Thread currentThread = Thread.currentThread();
            final Thread openDocsThread = new Thread() {
                public void run() {
                    try {
                        currentThread.join();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    openDocs.run();
                }
            };

            setXMLEditorFrame();            

            XMLEditorSettings.getSharedInstance().closeSplash();
            openDocsThread.start();
        } else
            _xpanel = xpanel;
    }

    /**
     * Initialise the XMLEditorFrame
     */
    public void setXMLEditorFrame() {
        /*
         * Handle MAC frame - use refelection to avoid using non-present
         * classes
         */
        if (XMLEditorSettings.getOSType() == XMLEditorSettings.MACOS) {
            try {
                Class macFrame = Class
                        .forName("org.merlotxml.merlot.XMLEditorMACFrame");
                Constructor constructor = macFrame
                        .getConstructor(new Class[] { XMLEditor.class,
                                String.class });
                _frame = (XMLEditorFrame) constructor
                        .newInstance(new Object[] { this,
                                _settings.getFrameTitle() });
            } catch (Exception ex) {
                _frame = new XMLEditorFrame(this, _settings.getFrameTitle());
            }
            //_frame = new XMLEditorMACFrame(this,
            // _settings.getFrameTitle());
        } else
            _frame = new XMLEditorFrame(this, _settings.getFrameTitle());
    }

    /**
     * Bring up the frame for the user to start using
     */
    public void run() {
        MerlotDebug.msg("XMLEditor.run");

        _frame.pack();
        MerlotDebug.msg("  packDONE");
        _frame.show();
        MerlotDebug.msg("  showDONE");

    }

    /**
     * Returns the XMLEditorSettings object initialized for this editor instance
     */
    public XMLEditorSettings getSettings() {
        return _settings;
    }

    /**
     * Gets the validating dom liaison implementation for the application
     * 
     * @return a DOMLiaison instance to use for parsing and writing XML
     * @exception MerlotException
     *                if the dom liaison class specified in the properties file
     *                is not an instance of ValidDOMLiaison, or another error
     *                occurs while instanciating the DOMLiaison class
     */
    public ValidDOMLiaison getDOMLiaison() throws MerlotException {
        String dlclassname = null;

        try {
            if (_domLiaison == null) {
                dlclassname = getSettings().getDOMLiaisonClassname();
                Class dlclass = Class.forName(dlclassname);
                if (!ValidDOMLiaison.class.isAssignableFrom(dlclass)) {
                    throw new MerlotException("Class " + dlclassname
                            + " does not implement ValidDOMLiason");
                }
                _domLiaison = (ValidDOMLiaison) dlclass.newInstance();
                _domLiaison.setProperties(_settings._props);
                //This chooser needs the XMLEditorFrame to work
                //For a XerlinPanel we don't support browsing to a DTD
                if (_frame != null)
                    _domLiaison.addEntityResolver(new UserEntityResolver());
            }
            return _domLiaison;
        } catch (InstantiationException ie) {
            MerlotDebug.exception(ie);
            throw new MerlotException("Instantiation error on class: "
                    + dlclassname);
        } catch (ClassNotFoundException cnf) {
            MerlotDebug.exception(cnf);
            throw new MerlotException("DOMLiaison class not found: "
                    + dlclassname);
        } catch (IllegalAccessException ia) {
            MerlotDebug.exception(ia);
            throw new MerlotException(
                    "Illegal access while trying to create the DOM Document");
        }

    }

    /**
     * custom dtd resolver which allows the user to find the dtd or specify a
     * URL for it
     */
    public class UserEntityResolver implements EntityResolver {
        /**
         * Sets up a dialog box to allow a user to go and find the dtd on their
         * filesystem, or enter a dtd URL.
         * 
         * @param publicId
         *            the public identifier from the XML file for the dtd it's
         *            looking for
         * @param systemId
         *            the system identifier from the XML file
         *  
         */
        //XXX needs to be integrated with the DTDChooser panel
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            // throw up a dialog and ask the user to find the dtd.
            MerlotDebug.msg("Custom DTD resolver called for doing a dialog");

            JPanel msgPanel = new JPanel();
            String msg = MerlotResource.getString(UI, "dtd.notfound.mesg");
            String pubfmt = MerlotResource.getString(UI, "dtd.notfound.public");
            String sysfmt = MerlotResource.getString(UI, "dtd.notfound.system");
            String pubmsg = null;
            String sysmsg = null;

            Object[] pubargs = { publicId };
            Object[] sysargs = { systemId };

            if (publicId != null && !publicId.trim().equals("")) {
                pubmsg = MessageFormat.format(pubfmt, pubargs);
            }
            if (systemId != null && !systemId.trim().equals("")) {
                sysmsg = MessageFormat.format(sysfmt, sysargs);
            }
            StrutLayout lay = new StrutLayout();
            StrutLayout.StrutConstraint strut;
            StrutLayout.setDefaultStrutLength(5);
            msgPanel.setLayout(lay);

            // something to help with the strut layout
            JComponent lastComponent;
            lastComponent = new JLabel(msg);

            msgPanel.add(lastComponent);
            JLabel l;
            if (pubmsg != null) {
                l = new JLabel(pubmsg);
                strut = new StrutLayout.StrutConstraint(lastComponent,
                        StrutLayout.BOTTOM_LEFT, StrutLayout.TOP_LEFT,
                        StrutLayout.SOUTH, 10);
                msgPanel.add(l, strut);
                lastComponent = l;
            }
            if (sysmsg != null) {
                l = new JLabel(sysmsg);
                strut = new StrutLayout.StrutConstraint(lastComponent,
                        StrutLayout.BOTTOM_LEFT, StrutLayout.TOP_LEFT,
                        StrutLayout.SOUTH, 10);
                msgPanel.add(l, strut);
                lastComponent = l;

            }

            /*
             * l = new
             * JLabel(MerlotResource.getString(UI,"filename.label")+":"); strut =
             * new
             * StrutLayout.StrutConstraint(lastComponent,StrutLayout.BOTTOM_LEFT,
             * StrutLayout.TOP_LEFT, StrutLayout.SOUTH,10);
             * chooserPanel.add(l,strut); lastComponent = l;
             * 
             * JTextField fileNameField = new JTextField(systemId);
             * fileNameField.setColumns(50);
             * 
             * strut = new
             * StrutLayout.StrutConstraint(lastComponent,StrutLayout.MID_RIGHT,
             * StrutLayout.MID_LEFT, StrutLayout.EAST);
             * chooserPanel.add(fileNameField,strut);
             * 
             * JButton b = MerlotUtils.createButtonFromAction(new
             * DTDChooserFindFileAction(fileNameField)); strut = new
             * StrutLayout.StrutConstraint(fileNameField,StrutLayout.MID_RIGHT,
             * StrutLayout.MID_LEFT, StrutLayout.EAST);
             * chooserPanel.add(b,strut);
             *  // ok, that's all the stuff we need here... let the user do
             * their stuff Object[] options = {
             * MerlotResource.getString(UI,"OK"),
             * MerlotResource.getString(UI,"CANCEL") };
             * 
             * int ok =
             * MerlotOptionPane.showInternalOptionDialog(XMLEditorFrame.getSharedInstance().getDesktopPane(),
             * chooserPanel, MerlotResource.getString(UI,"dtd.notfound.title"),
             * JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
             * options, options[0]); if (ok == 0) { String filename =
             * fileNameField.getText(); if (!filename.trim().equals("")) {
             * 
             * DTDCacheEntry entry =
             * DTDCache.getSharedInstance().findDTDbySystemId(publicId,
             * filename); if (entry != null) { return new InputSource(new
             * CharArrayReader(entry.getCachedDTDStream())); } } }
             * 
             * return null;
             */

            DTDChooser chooser = new DTDChooser(DTDCache.getSharedInstance());
            DTDCacheEntry entry = chooser.chooseDTD(msgPanel);
            if (entry != null) {
                InputSource inputSource = new InputSource(new CharArrayReader(
                        entry.getCachedDTDStream()));
                String pId = entry.getPublicId();
                String sId = entry.getSystemId();

                if (pId != null) {
                    inputSource.setPublicId(pId);
                }
                if (sId != null) {
                    inputSource.setSystemId(sId);
                }
                return inputSource;

            }
            return null;

        }

    }

    /**
     * brings up a file dialog for the user to find a dtd file if they click the
     * "choose" button in the dtd resolver dialog
     */
    // XXX needs integrated with DTDChooser
    protected class DTDChooserFindFileAction extends AbstractAction {
        /**
         * the text field to update with the file URL once the user selects one
         */
        protected JTextComponent _field;

        /**
         * initializes an action with the text field that will be updated with
         * the user's choice
         * 
         * @param field
         *            the text field to update
         */
        DTDChooserFindFileAction(JTextComponent field) {
            MerlotUtils.loadActionResources(this, UI, "dtd.choose.file");
            _field = field;
        }

        /**
         * The user clicked the "Choose" button. Presents them with a file open
         * dialog for dtd files
         */
        public void actionPerformed(ActionEvent e) {
            File f = _frame.getFileToOpen(new DTDFileFilter(), "*.dtd");
            if (f != null) {
                String path = f.getAbsolutePath();
                if (path.startsWith("/")) {
                    _field.setText("file:" + path);
                } else {
                    _field.setText("file:/" + path);
                }
            }

        }
    }

}
