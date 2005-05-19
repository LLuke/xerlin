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
 *  http://www.merlotxml.org/
 */
// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.
package org.merlotxml.merlot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.undo.UndoManager;

import matthew.awt.StrutLayout;

import org.merlotxml.merlot.XMLEditorActions.PluginFireAction;
import org.merlotxml.merlot.plugin.PluginConfig;
import org.merlotxml.merlot.plugin.PluginManager;
import org.merlotxml.merlot.plugin.action.ActionConfig;
import org.merlotxml.merlot.plugin.action.ActionPluginConfig;
import org.merlotxml.merlot.plugin.dtd.PluginDTDCacheEntry;
import org.merlotxml.util.xml.DTDCache;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.ValidDocument;

import com.speedlegal.webdav.EditorFile;

/**
 * The main frame for the application. This sets up the menus and toolbars. This
 * also manages the internal frames via the JDesktopPane. It is a singleton
 * instance class similar to XMLEditor. You get the singleton instance with the
 * getSharedInstance() method.
 * <P>
 * 
 * Methods of interest in this class are the addInternalFrame() methods for
 * displaying a JInternalFrame within this main frame. This class also includes
 * some of the main operation methods which are called by their cooresponding
 * Action in XMLEditorActions. These include quit(), newFile(), openFile(),
 * revert(), save(), saveAs(), cut(), copy(), paste() and undo(). Library
 * handling methods are also located here.
 * <P>
 * 
 * The XMLEditorFrame is an InternalFrameListener which helps it manage the
 * Windows menu, and handles saving open files before quitting, saving a file
 * when it's close box is clicked, managing the undo stack based on which
 * document is active, and activating/deactivating available menu items based on
 * which docuement is active.
 * 
 * @author Kelly A. Campbell
 * @created 16 March 2003
 * @see XMLEditor
 * @see XMLEditorActions
 */
public class XMLEditorFrame extends JFrame implements ClipboardOwner,
        MerlotConstants, InternalFrameListener {
    /**
     * the singleton instance of this class
     */
    public static XMLEditorFrame _frame;

    /**
     * the actions toplevel object
     */
    protected XMLEditorActions _actions;

    /**
     * The list of internal frames that are being managed by this toplevel
     * Frame. Objects are of type JInternalFrame.
     */
    protected Vector _internalFrames;

    /**
     * The list of library objects which are open. Objects are of type
     * MerlotLibrary
     */
    protected Vector _libraries;

    /**
     * Hashtable mapping Files to JInternalFrames. This allows us to bring a
     * frame to the front if someone tries to open a file that is already open,
     * instead of giving a new XMLDoc for it
     */
    protected Hashtable _fileToFrameMap;

    /**
     * The main menu bar for the application
     */
    protected JMenuBar _menuBar;

    protected JMenu _fileMenu;

    private JMenu _recentFileMenu;

    protected JMenu _editMenu;

    protected JMenu _nodeMenu;

    protected JMenu _windowMenu;

    protected JMenu _pluginMenu;

    protected JMenu _helpMenu;

    protected JToolBar _toolBar;

    private HashMap _extraActions;

    protected Vector _documents;

    protected JDesktopPane _desktop;

    /**
     * The editor that created this class instance
     */
    protected XMLEditor _editor;

    /**
     * The background of this frame
     */
    protected JPanel _background;

    /**
     * A DTD chooser instance which can be reused throughout the application
     */
    // XXX why do we need to reuse this? it's only neccessary while creating a
    // new
    //     document... perhaps we should create a new one and free it up each time?
    protected DTDChooser _dtdChooser;

    /**
     * Description of the Field
     */
    protected JDialog _preferenceDialog = null;

    /**
     * the clipboard for this application. This one holds just about anything
     * except tree nodes. It mainly holds text.
     */
    protected Clipboard _clipboard;

    /**
     * Special clipboard for the tree nodes. We keep this separate because a
     * paste in the tree is different from a paste in a text box.
     */
    protected Clipboard _treeClipboard;

    /**
     * Directory to go to when opening a file. This is set to the last directory
     * that the user opened a file from.
     */
    protected String _current_dir = null;

    /**
     * Directory where the last library file was opened from. Open Library will
     * go here first.
     */
    private String _lib_dir = null;

    /**
     * Amount to stagger new windows horizontally
     */
    protected final static int STAGGER_X = 25;

    /**
     * Amount to stagger new windows vertically
     */
    protected final static int STAGGER_Y = 25;

    /**
     * maximum amount of total horizontal staggering before cycling back to the
     * start
     */
    protected final static int MAX_STAGGER_X = 200;

    /**
     * maximum amount of total vertical staggering before cycling back to the
     * start
     */
    protected final static int MAX_STAGGER_Y = 150;

    /**
     * amount to offset staggers when recycling
     */
    protected final static int STAGGER_OFFSET = 10;

    /**
     * Default Cursor
     */
    public static Cursor DEFAULT = Cursor.getDefaultCursor();

    /**
     * Wait Cursor
     */
    public static Cursor WAIT = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

    /**
     * current values of staggering
     */
    protected int _stagger_x = 0;

    /**
     * current values of staggering
     */
    protected int _stagger_y = 0;

    /**
     * Where to start the stagger
     */
    protected int _stagger_x_start = 0;

    /**
     * Where to start the stagger
     */
    protected int _stagger_y_start = 0;

    /**
     * mapping of JInternalFrame to a XMLEditorDoc which handles cut/copy/paste
     * and other events that can show up at the top level, but need to be passed
     * on to the right window
     */
    private Hashtable _frameToDocumentMap;

    private XMLEditorDoc _cursor_doc;

    private XerlinFileDialogs _file_dialog;

    /**
     * Returns the singleton instance. Does not create a new instance if one
     * doesn't exist.
     * 
     * @return The sharedInstance value
     */
    public static XMLEditorFrame getSharedInstance() {
        return _frame;
    }

    /**
     * Constructor which takes the XMLEditor and the title for the Frame. This
     * sets up the clipboards, the frame and document maps, and the actions.
     * Then it shows the main splash screen, and sets up the frame, its menus,
     * and its background.
     * 
     * @param master
     *            the XMLEditor which is creating this
     * @param title
     *            the frame title
     */
    public XMLEditorFrame(XMLEditor master, String title) {
        super(title);
        //	this.show();

        MerlotDebug.msg("Setup the frame UI");

        // setup the frame UI
        _frame = this;

        _editor = master;
        _clipboard = new Clipboard("MerlotClipboard");
        _treeClipboard = new Clipboard("MerlotNodeClipboard");

        _frameToDocumentMap = new Hashtable();
        _fileToFrameMap = new Hashtable();
        _extraActions = new HashMap();

        _documents = new Vector();
        _libraries = new Vector();

        _internalFrames = new Vector();

        _actions = new XMLEditorActions(this);
        _dtdChooser = new DTDChooser(DTDCache.getSharedInstance());

        String msg = MerlotResource.getString(UI,
                "splash.initializingFrame.msg");
        XMLEditorSettings.getSharedInstance().showSplashStatus(msg);

        setupFrame();

        String deflib = XMLEditorSettings.getSharedInstance()
                .getDefaultLibrary();
        if (deflib != null) {
            openLibrary(new File(deflib), true);
        }

        _file_dialog = new XerlinFileDialogs(_frame);
    }

    /**
     * add a XMLEditorDoc to the frame. This gets the internalframe from the Doc
     * and adds it to the maps and the desktop.
     * 
     * @param d
     *            the XMLEditorDoc to add to the frame
     * @return Description of the Return Value
     */

    public JInternalFrame add(XMLEditorDoc d) {
        _documents.addElement(d);
        JInternalFrame f = d.getInternalFrame();
        _frameToDocumentMap.put(f, d);

        addInternalFrame(f);
        return f;
    }

    /**
     * Adds an internal frame in a staggered manner
     * 
     * @param f
     *            The feature to be added to the InternalFrame attribute
     */
    public void addInternalFrame(JInternalFrame f) {
        addInternalFrame(f, false);
    }

    /**
     * Adds an internal frame to the desktop. This decides on the placement of
     * the window bassed on the stagger parameter. If this is false, the
     * InternalFrame is placed wherever its location is set. This also places it
     * in the window menu list of windows.
     * 
     * @param f
     *            the frame to add
     * @param stagger
     *            whether or not to stagger the placement of this internal frame
     *            within the stagger pattern.
     */
    public void addInternalFrame(JInternalFrame f, boolean stagger) {
        _desktop.add(f, JLayeredPane.DEFAULT_LAYER);

        _actions._windowCascadeAction.setEnabled(true);
        _actions._windowTileHorizontalAction.setEnabled(true);
        _actions._windowTileVerticalAction.setEnabled(true);

        f.pack();

        Object doc = _frameToDocumentMap.get(f);
        try {
            if (doc!=null)
                f.setMaximum(true);

        } catch (PropertyVetoException pve) {
            //pve.printStackTrace();
        }

        if (stagger) {
            _stagger_x += STAGGER_X;
            _stagger_y += STAGGER_Y;

            if (_stagger_x > MAX_STAGGER_X) {
                _stagger_x = (_stagger_x_start += STAGGER_OFFSET);
            }

            if (_stagger_y > MAX_STAGGER_Y) {
                _stagger_y = (_stagger_y_start += STAGGER_OFFSET);
            }

            f.setLocation(new Point(_stagger_x, _stagger_y));
        }

        /*
         * // Ensure the internal frame is smaller than the desktop Dimension
         * desktopsize = _desktop.getSize(); int newwidth = (int)
         * (desktopsize.getWidth() * 0.75); int newheight = (int)
         * (desktopsize.getHeight() * 0.75); // sanity check if (newwidth < 0) {
         * newwidth = 100; }
         * 
         * if (newheight < 0) { newheight = 100; }
         * 
         * Dimension fsize = f.getSize(); Dimension newsize = (Dimension)
         * fsize.clone();
         * 
         * if (fsize.width > newwidth) { newsize.width = newwidth; }
         * 
         * if (fsize.height > newheight) { newsize.height = newheight; }
         * 
         * f.setSize(newsize);
         */

        JMenuItem windowMenuItem = new JCheckBoxMenuItem(f.getTitle());
        WindowHideShowListener listener = new WindowHideShowListener(f);
        windowMenuItem.addActionListener(listener);

        f.show();
        f.addInternalFrameListener(this);

        _windowMenu.add(windowMenuItem);

        if (doc instanceof XMLEditorDoc) {
            XMLEditorDoc xdoc = (XMLEditorDoc) doc;
            docActivated(xdoc);

            // This needs to be called after pack() i.e. after the
            // components are built - see the comments in UI
            // setSplitPanelSizes()
            final XMLEditorDocUI ui = xdoc.getXMLEditorDocUI();
            ui.setSplitPanelSizes(true);
        }
    }

    /**
     * Sets up the desktop and menus. Sets the look and feel, and sets up the
     * application icon
     */
    protected void setupFrame() {
        MerlotDebug.msg("SetupFrame");

        setupDesktop();
        setupMenus();

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        MerlotDebug.msg("  addWindowListener");

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                quit();
            }
        });

        // set the l&f
        MerlotDebug.msg("  set the l&f");

        String lf = XMLEditorSettings.getSharedInstance().getLookAndFeel();
        if (lf.equalsIgnoreCase("Metal")) {
            try {
                UIManager
                        .setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
                MerlotDebug.exception(ex);
            }
        } else {
            try {
                String classname = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(classname);
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
                MerlotDebug.exception(ex);
            }
        }

        MerlotDebug.msg("  set the icon");
        // setup the icon
        ImageIcon img = XMLEditorSettings.getSharedInstance().getAppIconSmall();
        if (img != null) {
            this.setIconImage(img.getImage());
        }
    }

    /**
     * Sets up all the toplevel menus for the application. Menu resources are
     * retrieved from ResourceBundles via MerlotResource.
     */
    protected void setupMenus() {

        // get the menu bars and stuff setup
        _menuBar = new JMenuBar();
        _fileMenu = setupFileMenu();
        _editMenu = setupEditMenu();
        _nodeMenu = setupNodeMenu();
        _windowMenu = setupWindowMenu();
        _pluginMenu = setupPluginMenu();

        _helpMenu = setupHelpMenu();

        _menuBar.add(_fileMenu);
        _menuBar.add(_editMenu);
        _menuBar.add(_nodeMenu);
        _menuBar.add(_windowMenu);
        if (_pluginMenu != null) {
            _menuBar.add(_pluginMenu);
        }

        // this doesn't seem to be supported in the JDK1.2.2 version of swing
        //	_menuBar.setHelpMenu(_helpMenu);
        _menuBar.add(_helpMenu);

        this.setJMenuBar(_menuBar);

    }

    /**
     * Gets the fileMenu attribute of the XMLEditorFrame object
     * 
     * @return The fileMenu value
     */
    public JMenu getFileMenu() {
        return _fileMenu;
    }

    /**
     * Gets the editMenu attribute of the XMLEditorFrame object
     * 
     * @return The editMenu value
     */
    public JMenu getEditMenu() {
        return _editMenu;
    }

    /**
     * Gets the nodeMenu attribute of the XMLEditorFrame object
     * 
     * @return The nodeMenu value
     */
    public JMenu getNodeMenu() {
        return _nodeMenu;
    }

    /**
     * Adds an action to a menu - a tag is used to ensure the same action name
     * is not added twice
     * 
     * @param name
     *            The feature to be added to the ActionToMenu attribute
     * @param a
     *            The feature to be added to the ActionToMenu attribute
     * @param menu
     *            The feature to be added to the ActionToMenu attribute
     */
    public void addActionToMenu(String name, Action a, JMenu menu) {
        if (_extraActions.get(name) == null) {
            _extraActions.put(name, a);
            MerlotUtils.addActionToMenu(a, menu);
        }
    }

    /**
     * Adds an action to a menu at a given position
     * 
     * @param name
     *            The feature to be added to the ActionToMenu attribute
     * @param a
     *            The feature to be added to the ActionToMenu attribute
     * @param menu
     *            The feature to be added to the ActionToMenu attribute
     * @param position
     *            The feature to be added to the ActionToMenu attribute
     */
    public void addActionToMenu(String name, Action a, JMenu menu, int position) {
        if (_extraActions.get(name) == null) {
            _extraActions.put(name, a);
            MerlotUtils.addActionToMenu(a, menu, position);
        }
    }

    /**
     * Adds an action to the toolbar
     * 
     * @param name
     *            The feature to be added to the ActionToToolBar attribute
     * @param a
     *            The feature to be added to the ActionToToolBar attribute
     */
    public void addActionToToolBar(String name, Action a) {
        if (_extraActions.get(name) == null) {
            _extraActions.put(name, a);
            MerlotUtils.addActionToToolBar(a, _toolBar);
        }
    }

    /**
     * Adds an action to the toolbar at a specified location
     * 
     * @param name
     *            The feature to be added to the ActionToToolBar attribute
     * @param a
     *            The feature to be added to the ActionToToolBar attribute
     * @param position
     *            The feature to be added to the ActionToToolBar attribute
     */
    public void addActionToToolBar(String name, Action a, int position) {
        if (_extraActions.get(name) == null) {
            // Special case of a Separator
            if (a != null) {
                _extraActions.put(name, a);
            } else {
                _extraActions.put(name, "Separator");
            }
            MerlotUtils.addActionToToolBar(a, _toolBar, position);
        }
    }

    /**
     * Get a custom action by name
     * 
     * @param name
     *            Description of the Parameter
     * @return The action value
     */
    public Object getAction(String name) {
        return _extraActions.get(name);
    }

    /**
     * Sets up the file menu from the actions in XMLEditorActions
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupFileMenu() {
        JMenu f = new JMenu(MerlotResource.getString(UI, "file"));

        MerlotUtils.addActionToMenu(_actions._openFileAction, f);
        MerlotUtils.addActionToMenu(_actions._openDavFileAction, f);
        MerlotUtils.addActionToMenu(_actions._newFileAction, f);
        f.addSeparator();

        MerlotUtils.addActionToMenu(_actions._openLibraryAction, f);
        MerlotUtils.addActionToMenu(_actions._newLibraryAction, f);
        f.addSeparator();

        MerlotUtils.addActionToMenu(_actions._saveFileAction, f);
        MerlotUtils.addActionToMenu(_actions._saveAsAction, f);
        MerlotUtils.addActionToMenu(_actions._saveAsDavAction, f);
        MerlotUtils.addActionToMenu(_actions._revertFileAction, f);
        MerlotUtils.addActionToMenu(_actions._closeFileAction, f);
        f.addSeparator();

        _recentFileMenu = new JMenu(MerlotResource
                .getString(UI, "recent.files"));
        addRecentlyUsedFiles();
        f.add(_recentFileMenu);
        f.addSeparator();

        MerlotUtils.addActionToMenu(_actions._quitAction, f);

        return f;
    }

    protected void addRecentlyUsedFiles() {
        ArrayList recentFiles = XMLEditorSettings.getSharedInstance()
                .getRecentFiles();
        _recentFileMenu.removeAll();
        int numitems = 0;

        for (int i = 0; i < recentFiles.size(); i++) {
            String recent = (String) recentFiles.get(i);
            if (recent.length()>0) {
                addRecentlyUsedFileToMenu(recent);
                numitems++;
            }
        }

        if (numitems == 0) {
            _recentFileMenu.setEnabled(false);
        }

        if (isShowing())
            _recentFileMenu.revalidate();
    }

    private void addRecentlyUsedFileToMenu(final String file) {
        if (file.length() == 0)
            return;

        _recentFileMenu.setEnabled(true);
        
        // Only put the filename in the menu
        String fname = file;
        int index = file.lastIndexOf("/");
        if (index > 0)
            fname = file.substring(index + 1);

        RecentFileMenuItem item = new RecentFileMenuItem(fname, file);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EditorFile efile;

                String server = XMLEditorSettings.getSharedInstance()
                        .getProperty("webdav.server");
                String path = XMLEditorSettings.getSharedInstance()
                        .getProperty("webdav.path");
                String prefix = server + path;
                // The entire file gets passed in...
                if (file.startsWith(server + path)) {
                    efile = XerlinDavFileDialogs.getClient().getNode(
                            file.substring(prefix.length()));
                } else {
                    efile = EditorFile.getFile(new File(file));
                }
                // Get ordering right
                if (efile.exists()) {
                    XMLEditorSettings.getSharedInstance().addRecentFile(efile);
                    addRecentlyUsedFiles();
                    //XMLEditorFrame.this.lockFileToOpen(efile);
                    openFile(efile);
                }
            }
        });
        item.setToolTipText(file);
        _recentFileMenu.add(item);
    }

    class RecentFileMenuItem extends JMenuItem {
        private String _longName;

        public RecentFileMenuItem(String shortName, String longName) {
            super(shortName);
            _longName = longName;
        }

        public boolean equals(Object o) {
            if (o instanceof RecentFileMenuItem) {
                return this._longName
                        .equals(((RecentFileMenuItem) o)._longName);
            }
            return super.equals(o);
        }
    }

    /**
     * Creates the edit menu from actions in XMLEditorActions
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupEditMenu() {
        JMenu e = new JMenu(MerlotResource.getString(UI, "edit"));
        MerlotUtils.addActionToMenu(_actions._undoAction, e);
        MerlotUtils.addActionToMenu(_actions._cutAction, e);
        MerlotUtils.addActionToMenu(_actions._copyAction, e);
        MerlotUtils.addActionToMenu(_actions._pasteAction, e);
        e.addSeparator();
        MerlotUtils.addActionToMenu(_actions._prefsAction, e);
        // setup the L&F menu for temp demo purposes
        JMenu lf = new JMenu(MerlotResource.getString(UI, "edit.lf"));
        MerlotUtils.addActionToMenu(_actions._systemLFAction, lf);
        MerlotUtils.addActionToMenu(_actions._metalLFAction, lf);
        e.add(lf);

        return e;
    }

    /**
     * Creates the Node menu which is maintained by individual documents
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupNodeMenu() {
        JMenu e = new JMenu(MerlotResource.getString(UI, "node"));
        e.setEnabled(false);
        return e;
    }

    /**
     * Creates the windows menu from actions in XMLEditorActions
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupWindowMenu() {
        JMenu w = new JMenu(MerlotResource.getString(UI, "window"));

        MerlotUtils.addActionToMenu(_actions._windowCascadeAction, w);
        MerlotUtils.addActionToMenu(_actions._windowTileHorizontalAction, w);
        MerlotUtils.addActionToMenu(_actions._windowTileVerticalAction, w);
        // For Window Menu
        w.addSeparator();
        return w;
    }

    /**
     * Creates the plugin menu from actions in XMLEditorActions
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupPluginMenu() {
        JMenu p = new JMenu(MerlotResource.getString(UI, "plugin"));
        JMenu nextItem;
        JMenuItem actionMenuItem = null;
        Iterator iter = PluginManager.getInstance().getPlugins().iterator();
        Iterator iterAction;
        PluginConfig nextConfig;
        ActionConfig nextAction;

        while (iter.hasNext()) {
            nextConfig = (PluginConfig) iter.next();
            if (nextConfig instanceof ActionPluginConfig) {
                nextItem = new JMenu(nextConfig.getName());

                iterAction = ((ActionPluginConfig) nextConfig)
                        .getActionConfigs().iterator();

                while (iterAction.hasNext()) {
                    nextAction = (ActionConfig) iterAction.next();
                    actionMenuItem = nextAction.getMenuItem();
                    actionMenuItem.addActionListener(XMLEditorActions
                            .getSharedInstance().new PluginFireAction(
                            nextAction));
                    nextItem.add(actionMenuItem);
                }
                p.add(nextItem);
            }
        }
        if (actionMenuItem == null) {
            p.setEnabled(false);
        }
        return p;
    }

    /**
     * Creates the About menu for plugins, getting the action from the
     * PluginConfigs in PluginManager
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupPluginAboutMenu() {
        JMenu pa = new JMenu(MerlotResource.getString(UI, "plugin.about"));

        Iterator iter = PluginManager.getInstance().getPlugins().iterator();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                MerlotUtils.addActionToMenu(((PluginConfig) iter.next())
                        .getAboutAction(), pa);
            }
        } else {
            pa.setEnabled(false);
        }
        return pa;
    }

    /**
     * Sets up the Help menu which contains the about item and plugin about menu
     * 
     * @return Description of the Return Value
     */
    protected JMenu setupHelpMenu() {
        JMenu h = new JMenu(MerlotResource.getString(UI, "help"));
        MerlotUtils.addActionToMenu(_actions._aboutAction, h);
        JMenu about = setupPluginAboutMenu();
        h.add(about);
        if (XMLEditorSettings.getSharedInstance().getProperty(
                "merlot.help.file") != null) {
            MerlotUtils.addActionToMenu(_actions._helpAction, h);
        }

        return h;
    }

    /**
     * This creates the toolbar for the application and adds the actions to it
     * 
     * @return Description of the Return Value
     */
    protected JToolBar setupToolBar() {
        MerlotToolBar tb = new MerlotToolBar();

        MerlotUtils.addActionToToolBar(_actions._newFileAction, tb);
        MerlotUtils.addActionToToolBar(_actions._openFileAction, tb);
        MerlotUtils.addActionToToolBar(_actions._openDavFileAction, tb);
        MerlotUtils.addActionToToolBar(_actions._saveFileAction, tb);

        tb.addSeparator();
        MerlotUtils.addActionToToolBar(_actions._cutAction, tb);
        MerlotUtils.addActionToToolBar(_actions._copyAction, tb);
        MerlotUtils.addActionToToolBar(_actions._pasteAction, tb);
        MerlotUtils.addActionToToolBar(_actions._undoAction, tb);
        tb.addSeparator();
        MerlotUtils.addActionToToolBar(_actions._backAction, tb);
        MerlotUtils.addActionToToolBar(_actions._cancelAction, tb);

        return tb;
    }

    /**
     * Sets up the desktop for the application. This includes the JDesktopPane
     * and the background
     */
    protected void setupDesktop() {
        // Get saved bounds..
        Rectangle bounds = _editor.getSettings().getEditorBounds();

        if (bounds == null) {
            int inset = _editor.getSettings().getFrameInset();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setBounds(inset, inset
                    + _editor.getSettings().getTopDisplacement(),
                    screenSize.width - (inset * 2), screenSize.height
                            - (inset * 2)
                            - _editor.getSettings().getBottomDisplacement());
        } else {
            this.setBounds(bounds);
        }
        _desktop = new JDesktopPane();

        Dimension desktopSize = _editor.getSettings().getDeskTopDimension();
        if (desktopSize == null) {
            _desktop.setPreferredSize(this.getSize());
        } else {
            _desktop.setPreferredSize(desktopSize);
        }

        // speed improvements available in Swing 1.1.1
        //	_desktop.putClientProperty ("JDesktopPane.dragMode", "outline");
        _desktop.putClientProperty("JDesktopPane.dragMode", "faster");

        _desktop.setBackground(_editor.getSettings().getBackgroundColor());
        ImageIcon backgroundPic = _editor.getSettings().getBackgroundImage();
        if (backgroundPic != null) {
            MerlotDebug.msg("backgroundPic = " + backgroundPic);

            StrutLayout lay = new StrutLayout();
            JLabel blankLabel = new JLabel("");

            _background = new JPanel(lay);
            _background.add(blankLabel);

            _background.setBackground(_editor.getSettings()
                    .getBackgroundColor());

            Dimension d = this.getSize();

            _background.setBounds(0, 0, d.width, d.height);

            JLabel label = new JLabel(backgroundPic);
            lay.setSprings(label, StrutLayout.SPRING_BOTH);
            lay.setAlignment(StrutLayout.BOTTOM_RIGHT);

            _background.add(label, new StrutLayout.StrutConstraint(blankLabel,
                    StrutLayout.TOP_LEFT, StrutLayout.BOTTOM_RIGHT,
                    StrutLayout.NORTH_WEST, 50));

            _desktop.add(_background, JLayeredPane.FRAME_CONTENT_LAYER);

        }
        JPanel mainContent = new JPanel(new BorderLayout());
        _toolBar = setupToolBar();
        mainContent.add(_toolBar, BorderLayout.NORTH);
        mainContent.add(_desktop, BorderLayout.CENTER);

        this.setContentPane(mainContent);

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                frameResized(e);

            }
        });

    }

    /**
     * Description of the Field
     */
    protected final static int OFF_X = 50;

    /**
     * Description of the Field
     */
    protected final static int OFF_Y = 50;

    /**
     * The frame got resized... check to make sure all the windows are still in
     * the frame, and reset the background
     * 
     * @param evt
     *            Description of the Parameter
     */
    // XXX doesn't currently do anything for moving the background icon
    protected void frameResized(ComponentEvent evt) {

        Dimension d = this.getSize();
        if (_background != null) {
            //	_background.setBounds(0,0,d.width,d.height);
        }
        Enumeration e = _internalFrames.elements();
        Point p = new Point(0, 0);
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof JInternalFrame) {
                p = ((JInternalFrame) o).getLocation(p);
                if (p.x > d.width - OFF_X && d.width > OFF_X) {
                    p.x = d.width - OFF_X;
                }
                if (p.y > d.height - OFF_Y && d.height > OFF_Y) {
                    p.y = d.height - OFF_Y;
                }
                ((JInternalFrame) o).setLocation(p);

            }
        }
        e = _libraries.elements();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (o instanceof MerlotLibrary) {
                p = ((MerlotLibrary) o).getLocation(p);
                if (p.x > d.width - OFF_X && d.width > OFF_X) {
                    p.x = d.width - OFF_X;
                }
                if (p.y > d.height - OFF_Y && d.height > OFF_Y) {
                    p.y = d.height - OFF_Y;
                }
                ((MerlotLibrary) o).setLocation(p);

            }
        }

    }

    /**
     * Checks if the application can be shutdown. If it can, it calls
     * System.exit()
     */
    public void quit() {
        if (checkQuit()) {

            MerlotDebug.msg("Quitting");
            
            // Save the properties
            XMLEditorSettings.getSharedInstance().saveUserProperties();
            
            // XXX
            System.exit(0);
        }
    }

    /**
     * prepares the application to quit, asking the user if they want to save
     * each of the open files. If the user selects "cancel" on any of the open
     * file save questions, this returns false.
     * 
     * @return true if the application can quit, false otherwise
     */
    protected boolean checkQuit() {
        boolean ret = true;

        // run through all the documents in order from top to bottom
        MerlotDebug.msg("checkQuit(): _documents = " + _documents);

        JInternalFrame[] frames = _desktop.getAllFrames();
        for (int i = 0; i < frames.length; i++) {
            try {
                frames[i].setIcon(false);
            } catch (PropertyVetoException ex) {
                MerlotDebug.exception(ex);
            }

        }

        while (frames.length > 0 && ret) {
            // find the first frame in the layer

            for (int i = 0; i < frames.length; i++) {
                if (_desktop.getPosition(frames[i]) == 0) {
                    try {

                        frames[i].setClosed(true);
                    } catch (PropertyVetoException ex) {
                        ret = false;
                    }
                }
            }
            frames = _desktop.getAllFrames();
        }

        return ret;
    }

    /**
     * Create a new file. Show the DTDChooser, create a file which uses the DTD,
     * and then create an XMLEditorDoc for that new file
     */
    public void newFile() {
        File f;
        String rootElementName;

        XMLEditorDoc xed;

        MerlotDebug.msg("New File");

        try {
            DTDCacheEntry dtdEntry = _dtdChooser.chooseDTD();

            if (dtdEntry == null) {
                return;
            }

            String publicid = dtdEntry.getPublicId();
            String systemid = dtdEntry.getSystemId();

            rootElementName = getNewRootElementName(dtdEntry);
            if (rootElementName == null) {
                return;
            }

            /*
             * if (dtdEntry instanceof PluginDTDCacheEntry) { pluginDTDEntry =
             * (PluginDTDCacheEntry) dtdEntry; if ( (dtd =
             * pluginDTDEntry.getDoctype()) == null) { dtd = "foo"; } } else {
             * if (systemid.indexOf("://") > -1) { /It's a URL dtd =
             * systemid.substring(systemid.lastIndexOf("/"),
             * systemid.lastIndexOf(".")); } else { /It's a file dtd =
             * systemid.substring(systemid.lastIndexOf(File.separator) + 1,
             * systemid.lastIndexOf(".")); } }
             */
            // now write out the beginnings of a document in a temp file
            // XXX make this work via StringReader instead of having to write a
            // temp file
            f = createNewFile(rootElementName, publicid, systemid);

            XMLFile xmlFile = new XMLEditorFile(f);
            xmlFile.setNew(true);
            xed = new XMLEditorDoc(xmlFile);

            this.add(xed);

        } catch (Exception ex) {
            MerlotError.exception(ex, MerlotResource.getString(ERR,
                    "file.new.w"));
        }
    }

    /**
     * Description of the Method
     * 
     * @param rootElementName
     *            Description of the Parameter
     * @param publicid
     *            Description of the Parameter
     * @param systemid
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    protected static File createNewFile(String rootElementName,
            String publicid, String systemid) throws IOException {
        //Check for namespaces - colons cause Windows problems
        String newFileName;
        if (rootElementName.indexOf(":") >= 0) {
            newFileName = rootElementName.substring(rootElementName
                    .indexOf(":") + 1);
        } else {
            newFileName = rootElementName;
        }

        File f = File.createTempFile(newFileName, ".xml");
        f.deleteOnExit();

        FileWriter fw = new FileWriter(f);

        StringBuffer sb = new StringBuffer();
        if (!systemid.endsWith(".dtd")) {
            //Namespaces - not handled here properly yet
            sb
                    .append("<"
                            + rootElementName
                            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                            + " xsi:noNamespaceSchemaLocation=\"" + systemid
                            + "\">");
        } else {
            sb.append("<!DOCTYPE " + rootElementName + " ");

            if (publicid != null && !(publicid.trim().equals(""))) {
                sb.append("PUBLIC \"" + publicid + "\" ");
            } else {
                sb.append("SYSTEM ");
            }
            sb.append("\"" + systemid + "\"");
            sb.append(">\n");
            sb.append("<" + rootElementName + ">");
        }
        String doctype = sb.toString();

        fw.write(MerlotResource.getString(XML, "xml.declaration") + "\n");
        fw.write(doctype + "\n");
        fw.write("</" + rootElementName + ">\n");
        fw.close();
        return f;
    }

    /**
     * Get the root element name from the user for a new file
     * 
     * @param dtdEntry
     *            Description of the Parameter
     * @return The newRootElementName value
     */
    public String getNewRootElementName(DTDCacheEntry dtdEntry) {
        String rootElementName = null;

        if (dtdEntry instanceof PluginDTDCacheEntry) {
            rootElementName = ((PluginDTDCacheEntry) dtdEntry).getDoctype();
        }
        if (rootElementName == null) {
            // let the user choose

            // first get a list of possible root nodes
            java.util.List possibleRoots = dtdEntry.getPossibleRootNames();
            if (possibleRoots != null) {
                // sort the list
                TreeSet set = new TreeSet();
                set.addAll(possibleRoots);

                JComboBox menu = new JComboBox(set.toArray());
                menu.setEditable(true);

                JPanel p = new JPanel(new FlowLayout());
                p.add(new JLabel(MerlotResource.getString(UI,
                        "dtd.root.element.menu.label")));
                p.add(menu);
                int ok = MerlotOptionPane.showInternalOptionDialog(
                        getDesktopPane(), p, MerlotResource.getString(UI,
                                "dtd.root.element.menu.title"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (ok == JOptionPane.OK_OPTION) {
                    rootElementName = (String) menu.getSelectedItem();
                }
            }
        }
        return rootElementName;
    }

    /**
     * Allow the user to select a file to open, and then check to see if it's
     * already open or not. If its not open, open it and create the XMLEditorDoc
     * for it.
     */
    public void openFile() {
        File f = getFileToOpen(new XMLFileFilter(), "*.xml");
        if (f != null && f.exists()) {
            XMLEditorSettings.getSharedInstance().addRecentFile(f);
            addRecentlyUsedFiles();
        }
        openFile(f);
    }

    public void openFile(File f) {
        try {
            XMLEditorDoc document = null;

            if (f != null) {

                // see if it exists in the map of files to frames
                Object o = _fileToFrameMap.get(f.getAbsolutePath());
                if (o != null) {
                    // bring that frame to the front instead
                    if (o instanceof JInternalFrame) {
                        try {
                            ((JInternalFrame) o).setSelected(true);
                        } catch (PropertyVetoException ex) {
                        }

                    }
                } else {
                    document = new XMLEditorDoc(f);
                    JInternalFrame frame = this.add(document);
                    _fileToFrameMap.put(f.getAbsolutePath(), frame);

                }

            }
            if (document != null) {
                XMLFile file = document.getFile();
                if (file != null) {
                    ValidDocument vdoc = file.getValidDocument();
                    if (vdoc != null) {
                        if (vdoc.getMainDTDDocument() == null) {
                            MerlotError.msg(MerlotResource.getString(ERR,
                                    "xml.file.unknown.dtd"));
                        }
                    }
                }
            }
        } catch (MerlotException ex) {
            MerlotDebug.exception(ex);
            MerlotError.exception(ex, MerlotResource.getString(ERR,
                    "xml.file.open.w"));
        }
    }

    /**
     * Present a file chooser dialog for the user to select a file.
     * 
     * @param filter
     *            the file filter for which files to show in the chooser
     * @param selectedFileName
     *            a file to select in the chooser if using the native AWT
     *            fileDialog
     * @return the File the user selected or null if they didn't select one
     */
    public File getFileToOpen(MerlotFileFilter filter, String selectedFileName) {
        return getFileToOpen(filter, selectedFileName, true);
    }

    public File getFileToOpen(MerlotFileFilter filter, String selectedFileName,
            boolean readWrite) {
        return _file_dialog.getFileToOpen(filter, selectedFileName, readWrite);
    }

    /**
     * This closes the foreground XMLEditorDoc document
     */
    public void close() {
        XMLEditorDoc doc = getCurrentDocument();

        // XXX this should be in it's own method since it's used multiple times
        // Remove Window Menu Item.
        if (doc != null) {
            String name = doc.getInternalFrame().getTitle();
            int count = _windowMenu.getItemCount();
            Component item;
            for (int i = 0; i < count; i++) {
                item = _windowMenu.getMenuComponent(i);
                if (item instanceof AbstractButton) {
                    String title = ((AbstractButton) item).getText();
                    if (title.equals(name)) {
                        _windowMenu.remove(i);
                        // done
                        break;
                    }
                }
            }
        }
        if (doc != null) {
            doc.closeDocument();
        }
    }

    /**
     * Revert a document to its previously saved version
     */
    public void revert() {

        XMLEditorDoc doc = getCurrentDocument();
        if (doc != null) {

            String[] docname = new String[1];
            docname[0] = doc._title;
            MessageFormat mf;

            mf = new MessageFormat(MerlotResource.getString(ERR,
                    "doc.revert.sure.q"));
            String ques = mf.format(docname);
            mf = new MessageFormat(MerlotResource.getString(ERR,
                    "doc.revert.sure.t"));
            String title = mf.format(docname);

            int save = MerlotOptionPane.showInternalConfirmDialog(doc._frame,
                    ques, title, JOptionPane.OK_CANCEL_OPTION);
            switch (save) {
                case JOptionPane.CANCEL_OPTION:
                    return;
                case JOptionPane.OK_OPTION:
                    break;
            }

            // XXX this is a dirty way of reverting, but it's the best in our
            // case cause
            // all the DOM objects change, and anything they have open in the
            // editor
            // pane will break. it would be nice to fix this someday
            try {
                doc.setDirty(false);
                File f = new File(doc.getFile().getPath());
                doc.closeDocument();

                this.add(new XMLEditorDoc(f));

            } catch (MerlotException ex) {
                MerlotError.exception(ex, MerlotResource.getString(ERR,
                        "doc.revert.w"));
            }

        }
    }

    /**
     * Sets the most recent document active - needed after a document is closed
     * to activate menu items
     */
    protected void activateDocumentAfterClose() {
        XMLEditorDoc doc = null;

        if (_documents.size() > 0) {
            doc = (XMLEditorDoc) _documents.lastElement();
        }

        if (doc != null) {
            docActivated(doc);
        }
    }

    /**
     * Convenience helper to rename a JInternalFrame and update its Window menu
     * item as well.
     * 
     * @param oldName
     *            the name of the old window
     * @param doc
     *            the document of the window
     */
    // XXX who wrote this? it should be more generalized and take the newName
    // and the internal frame
    //     rather than the doc
    private void renameWindowItem(String oldName, XMLEditorDoc doc) {
        JInternalFrame frame = doc.getInternalFrame();
        String newName = doc.getFile().getName();
        frame.setTitle(newName);
        frame.repaint();
        int count = _windowMenu.getItemCount();
        Component item = null;
        AbstractButton itemButton = null;
        for (int i = 0; i < count; i++) {
            item = _windowMenu.getMenuComponent(i);
            if (item instanceof AbstractButton) {
                itemButton = (AbstractButton) item;
            } else {
                // rename window item
                item = null;
            }
            if ((itemButton != null) && (itemButton.getText().equals(oldName))) {
                itemButton.setText(newName);
            }
        }
    }

    /**
     * Save the current foreground document
     */
    // XXX window menu code should not be neccessary... need to clean up
    // menu renaming stuff could be better handled as a property listener on the
    // document name
    public void save() {
        XMLEditorDoc doc = getCurrentDocument();
        if (doc != null) {
            // Save document and rename window menu item
            //
            JInternalFrame frame = doc.getInternalFrame();
            // keep track of old name to find in the menu
            String oldName = frame.getTitle();

            // save document
            doc.saveDocument();

            // Forcibly rename menu item in case this was the
            // first save - should be save as anyway, but do a
            // sanity check
            renameWindowItem(oldName, doc);

        }
    }

    /**
     * Save the current foreground document with a different filename
     */
    public void saveAs() {
        XMLEditorDoc doc = getCurrentDocument();
        if (doc != null) {
            // Save document and rename window menu item
            //
            JInternalFrame frame = doc.getInternalFrame();
            String oldName = frame.getTitle();

            // save document
            doc.saveDocumentAs();

            // rename window item
            renameWindowItem(oldName, doc);

            XMLEditorSettings.getSharedInstance().addRecentFile(
                        doc.getFile()._file);
            addRecentlyUsedFiles();
        }
    }

    /**
     * Retrieve the current user working directory. This could be the directory
     * the previous file was located in or if that is null, it would be the
     * user.dir from the System properties
     * 
     * @return The currentDir value
     */

    public String getCurrentDir() {
        String dir = _current_dir;
        if (dir == null) {
            dir = System.getProperty("user.dir");
        }
        return dir;
    }

    public String getLibDir() {
        return _lib_dir;
    }

    /**
     * Delegates to cut method in the current foreground XMLEditorDoc
     * 
     * @param evt
     *            Description of the Parameter
     * @see XMLEditorDoc#cut
     */
    public void cut(ActionEvent evt) {
        // get a transferable from whatever internal frame has focus
        XMLEditorDoc listener = getCurrentDocument();
        if (listener != null) {
            listener.cut(evt);
        } else {
            MerlotDebug.msg("No listener available for Copy.");
        }
    }

    /**
     * Delegates to copy method in the current foreground XMLEditorDoc
     * 
     * @param evt
     *            Description of the Parameter
     * @see XMLEditorDoc#copy
     */
    public void copy(ActionEvent evt) {
        // get a transferable from whatever internal frame has focus
        XMLEditorDoc listener = getCurrentDocument();
        if (listener != null) {
            listener.copy(evt);
        } else {
            MerlotDebug.msg("No listener available for Copy.");
        }

    }

    /**
     * Delegates to paste method in the current foreground XMLEditorDoc
     * 
     * @param evt
     *            Description of the Parameter
     * @see XMLEditorDoc#paste
     */
    public void paste(ActionEvent evt) {
        // get a transferable from whatever internal frame has focus
        XMLEditorDoc listener = getCurrentDocument();
        if (listener != null) {
            listener.paste(evt);
        } else {
            MerlotDebug.msg("No listener available for Paste.");
        }
    }

    /**
     * Delegates to undo method in the current foreground XMLEditorDoc
     * 
     * @param evt
     *            Description of the Parameter
     * @see XMLEditorDoc#undo
     */
    public void undo(ActionEvent evt) {

        MerlotDebug.msg("Frame undo");

        XMLEditorDoc listener = getCurrentDocument();
        if (listener != null) {
            listener.undo(evt);
        } else {
            MerlotDebug.msg("No listener available for Undo.");
        }

    }

    /**
     * Updates the undo action with information about what it is going to undo
     * using the mgr getUndoPresentationName() call.
     * 
     * @param mgr
     *            the UndoManager to consult about whether an undo is available,
     *            and what it's presentation name is. If this is passed as null,
     *            the undo action is disabled and set to the defaults from the
     *            UI resource bundle keys "edit.undo"
     */
    protected void resetUndoAction(UndoManager mgr) {
        if (mgr != null) {
            _actions._undoAction.setEnabled(mgr.canUndo());

            // if there's an undoable get the presentation name and change the
            // undo action

            String undo = MerlotResource.getString(UI, "edit.undo");

            String newundo = undo;

            String pname = mgr.getUndoPresentationName();
            MerlotDebug.msg("undo = " + undo + " pname = " + pname);

            if (pname != null && !pname.equalsIgnoreCase("undo")) {
                newundo = undo + " " + pname;
            }
            //_actions._undoAction.putValue(ACTION_NAME,newundo);
            _actions._undoAction.putValue(ACTION_SHORT_DESCRIPTION, newundo);
        } else {
            //_actions._undoAction.putValue(ACTION_NAME,MerlotResource.getString(UI,"edit.undo"));
            _actions._undoAction.putValue(ACTION_SHORT_DESCRIPTION,
                    MerlotResource.getString(UI, "edit.undo.tt"));
            _actions._undoAction.setEnabled(false);
        }

    }

    /**
     * returnts the text clipboard
     * 
     * @return The clipboard value
     */
    public Clipboard getClipboard() {
        return _clipboard;
    }

    /**
     * returns the DOMTree clipboard
     * 
     * @return The treeClipboard value
     */
    public Clipboard getTreeClipboard() {
        return _treeClipboard;
    }

    /**
     * Notification that we lost ownership of a clipboard item
     * 
     * @param c
     *            Description of the Parameter
     * @param t
     *            Description of the Parameter
     */
    public void lostOwnership(Clipboard c, Transferable t) {
    }

    /**
     * returns the JDesktopPane which this Frame uses
     * 
     * @return The desktopPane value
     */
    public JDesktopPane getDesktopPane() {
        return _desktop;
    }

    /**
     * returns the current foreground document or null if none are selected
     * 
     * @return The currentDocument value
     */
    public XMLEditorDoc getCurrentDocument() {
        // go through the set of frames in our desktop, and find out which
        // one is selected.
        JInternalFrame[] frames = _desktop.getAllFrames();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isSelected()) {
                // that's the one, see if we have it in our map
                Object o = _frameToDocumentMap.get(frames[i]);
                if (o != null && o instanceof XMLEditorDoc) {
                    return (XMLEditorDoc) o;
                }
            }
        }
        return null;
    }

    /**
     * Open a library file.
     * 
     * @param libfile
     *            the File to open as a library
     * @param show
     *            if true, show the library frame immediately
     */
    public void openLibrary(File libfile, boolean show) {
        setWaitCursor();
        try {

            if (libfile.exists()) {

                XMLFile file = new XMLEditorFile(libfile);

                JInternalFrame internalf = new MerlotLibrary(file);

                _libraries.add(internalf);

                _desktop.add(internalf, JLayeredPane.DEFAULT_LAYER);
                if (show) {
                    internalf.show();
                }
                _lib_dir = libfile.getParent();

            } else {
                MerlotError.msg(MerlotResource.getString(ERR,
                        "library.notexist"));

                MerlotDebug.msg("Library file " + libfile + " doesn't exist");
            }

        } catch (Exception ex) {
            setDefaultCursor();
            MerlotError.exception(ex, MerlotResource.getString(ERR,
                    "library.open.w"));

            MerlotDebug.exception(ex);
        } finally {
            setDefaultCursor();
        }

    }

    /**
     * Open the specified library file and show the fram
     * 
     * @param libfile
     *            Description of the Parameter
     */
    public void openLibrary(File libfile) {
        openLibrary(libfile, true);

    }

    /**
     * Open a library by giving the user a file chooser and then opening the
     * selected library file if there is one
     */
    public void openLibrary() {
        File f = _file_dialog.openLibraryFile();
        if (f != null) {
            openLibrary(f);
        }
    }

    /**
     * Create a new library file
     */
    public void newLibrary() {
        // get a name for the library
        JPanel p = new JPanel();
        JLabel l = new JLabel(MerlotResource.getString(UI,
                "library.new.nameit.t"));
        JTextField namefield = new JTextField(20);

        StrutLayout lay = new StrutLayout();

        p.setLayout(lay);

        p.add(l);
        p.add(namefield, new StrutLayout.StrutConstraint(l,
                StrutLayout.MID_RIGHT, StrutLayout.MID_LEFT, StrutLayout.EAST,
                5));
        lay.setSprings(namefield, StrutLayout.SPRING_HORIZ);

        int ok = MerlotOptionPane.showInternalConfirmDialog(_desktop, p,
                MerlotResource.getString(UI, "library.new.nameit"),
                JOptionPane.OK_CANCEL_OPTION);

        if (ok == JOptionPane.OK_OPTION) {

            File newLib = MerlotLibrary.getNewLibraryFile(namefield.getText());
            if (newLib != null) {
                openLibrary(newLib);
            }
        }
    }

    /**
     * Called when an XMLEditorDoc is activated. Changes action enabled state
     * for actions that can change depending on which document is in the
     * foreground
     * 
     * @param doc
     *            Description of the Parameter
     */
    protected void docActivated(XMLEditorDoc doc) {

        UndoManager undoManager = doc.getUndoManager();
        resetUndoAction(undoManager);
        //doc.enableActions(_actions);

        _actions._closeFileAction.setEnabled(true);
        _actions._saveAsAction.setEnabled(true);
        _actions._saveAsDavAction.setEnabled(true);
        _actions._saveFileAction.setEnabled(doc.isDirty());
        _actions._revertFileAction.setEnabled(doc.isDirty());
        if (doc.getXMLEditorDocUI().getNodeHistory().size() > 1) {
            _actions._backAction.setEnabled(true);
            // This is the conservative option as we don't know if the node is
            // new or not
            _actions._cancelAction.setEnabled(false);
        }

        doc.getXMLEditorDocUI().updateNodeMenu(_nodeMenu);
        doc.setActive(true);

    }

    /**
     * Called when an XMLEditorDoc is deactivated. Changes action enabled state
     * for actions that can change depending on which document is in the
     * foreground
     * 
     * @param doc
     *            Description of the Parameter
     */

    protected void docDeactivated(XMLEditorDoc doc) {

        _actions._closeFileAction.setEnabled(false);
        _actions._saveAsAction.setEnabled(false);
        _actions._saveAsDavAction.setEnabled(false);
        _actions._saveFileAction.setEnabled(false);
        _actions._revertFileAction.setEnabled(false);
        _actions._backAction.setEnabled(false);
        _actions._cancelAction.setEnabled(false);
        doc.getXMLEditorDocUI().updateNodeMenu(_nodeMenu);
        doc.setActive(false);

    }

    // InternalFrameListener implementation
    /**
     * Implementation of InternalFrameListener. Calls into docActivated to turn
     * on menu items
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameActivated(InternalFrameEvent e) {
        // switch out certain menu items
        Object o = e.getSource();
        if (o instanceof JInternalFrame) {
            Object doc = _frameToDocumentMap.get(o);
            if (doc instanceof XMLEditorDoc) {
                docActivated((XMLEditorDoc) doc);

            }
        }
    }

    /**
     * Turns off window menu actions which handle arrangement of multiple
     * windows on the desctop if there are less than 2 internal frames active
     */

    // XXX wtf kinda comment is this? I don't understand its purpose from this
    // and why is this method smack dab in the middle of the internal frame
    // listener
    // implementation? -- camk
    // Very simple helper primarily to eliminate duplicate code
    // Primarily for future expansion. The helper will probably
    // get larger and more complicated as more window menu items
    // are added. Currently just disables the window menu.
    //
    private void setWindowMenuState() {
        JInternalFrame[] frames = _desktop.getAllFrames();
        if (frames.length < 2) {
            _actions._windowCascadeAction.setEnabled(false);
            _actions._windowTileHorizontalAction.setEnabled(false);
            _actions._windowTileVerticalAction.setEnabled(false);
        }
    }

    /**
     * Called when an internal frame has been closed. This cleans up items in
     * the document maps and the window menu item
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameClosed(InternalFrameEvent e) {
        // remove the frame from our mapping
        Object o = e.getSource();
        if (o instanceof MerlotLibrary) {
            _libraries.remove(o);

        } else {
            if (o instanceof JInternalFrame) {
                Object doc = _frameToDocumentMap.remove(o);
                if (doc instanceof XMLEditorDoc) {
                    docDeactivated((XMLEditorDoc) doc);
                    //BUG0161
                    resetUndoAction(null);
                    // BUG01616
                    _documents.remove(doc);
                    // find the frame in the _fileToFrameMap and remove it
                    Enumeration en = _fileToFrameMap.keys();
                    while (en.hasMoreElements()) {
                        Object key = en.nextElement();
                        Object frame = _fileToFrameMap.get(key);
                        if (frame == o) {
                            _fileToFrameMap.remove(key);
                        }
                    }
                }
                // Remove the Window menu item
                String name = ((JInternalFrame) o).getTitle();
                int count = _windowMenu.getItemCount();
                Component item;
                for (int i = 0; i < count; i++) {
                    item = _windowMenu.getMenuComponent(i);
                    if (item instanceof AbstractButton) {
                        String title = ((AbstractButton) item).getText();
                        if (title.equals(name)) {
                            _windowMenu.remove(i);
                            // done
                            break;
                        }
                    }
                }
                setWindowMenuState();
            }
        }
        if (o instanceof JInternalFrame) {
            _internalFrames.remove(o);

            setWindowMenuState();
        }
    }

    /**
     * Called when a frame is about to close. We don't do anything here. Saves
     * of dirty documents are handled in the close action itself
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameClosing(InternalFrameEvent e) {
    }

    /**
     * A frame has been deactivated. This called the docDeactivated method to
     * change enabled state of affected menu items
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameDeactivated(InternalFrameEvent e) {
        Object o = e.getSource();
        if (o instanceof JInternalFrame) {
            Object doc = _frameToDocumentMap.get(o);
            if (doc instanceof XMLEditorDoc) {
                docDeactivated((XMLEditorDoc) doc);

            }
        }
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    /**
     * Sets the currentDir attribute of the XMLEditorFrame object
     * 
     * @param current
     *            The new currentDir value
     */
    public void setCurrentDir(String current) {
        _current_dir = current;
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameIconified(InternalFrameEvent e) {
    }

    /**
     * Sets the defaultCursor attribute of the XMLEditorFrame class
     */
    public static void setDefaultCursor() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _frame.setCursor(DEFAULT);
                XMLEditorDoc doc = _frame._cursor_doc;
                if (doc != null) {
                    doc.getXMLEditorDocUI().setCursor(DEFAULT);
                }
                Component glasspane = _frame.getGlassPane();
                glasspane.setVisible(false);
                _frame._cursor_doc = null;
            }
        });
    }

    /**
     * Sets the waitCursor attribute of the XMLEditorFrame class
     */
    public static void setWaitCursor() {
        XMLEditorDoc currDoc = _frame._cursor_doc;
        if (currDoc != null) {
            //Avoid events getting out of whack
            return;
        }
        _frame.setCursor(WAIT);
        XMLEditorDoc doc = _frame.getCurrentDocument();
        _frame._cursor_doc = doc;
        if (doc != null) {
            doc.getXMLEditorDocUI().setCursor(WAIT);
        }
        Component glasspane = _frame.getGlassPane();
        glasspane.setVisible(true);
        glasspane.setEnabled(false);
    }

    /**
     * Gets the fileDialogs attribute of the XMLEditorFrame object
     * 
     * @return The fileDialogs value
     */
    public XerlinFileDialogs getFileDialogs() {
        return _file_dialog;
    }

    protected void setFileDialogs(XerlinFileDialogs fileDialogs) {
        _file_dialog = fileDialogs;
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void internalFrameOpened(InternalFrameEvent e) {
    }

    /**
     * ActionListener for when a window is hidden or shown using the window menu
     * item for it.
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    // XXX this might be better off in the XMLEditorActions class
    protected class WindowHideShowListener implements ActionListener {
        /**
         * Description of the Field
         */
        protected JInternalFrame frame;

        /**
         * Constructor for the WindowHideShowListener object
         * 
         * @param aFrame
         *            Description of the Parameter
         */
        public WindowHideShowListener(JInternalFrame aFrame) {
            frame = aFrame;
        }

        /**
         * Description of the Method
         * 
         * @param e
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            try {
                frame.setIcon(false);
            } catch (java.beans.PropertyVetoException ex) {
                MerlotDebug.msg("Show vetoed");
            }
            frame.show();
        }
    }

}
