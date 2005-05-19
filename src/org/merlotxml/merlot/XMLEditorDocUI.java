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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import matthew.awt.StrutLayout;

import org.merlotxml.awt.PercentLayout;
import org.merlotxml.merlot.plugin.PluginConfig;
import org.merlotxml.merlot.plugin.PluginManager;
import org.merlotxml.merlot.plugin.dtd.DTDPluginConfig;
import org.merlotxml.merlot.plugin.dtd.PluginDTDCacheEntry;
import org.merlotxml.merlot.plugin.nodeAction.NodeActionConfig;
import org.merlotxml.merlot.plugin.nodeAction.NodeActionPluginConfig;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.merlotxml.util.xml.DTDConstants;
import org.merlotxml.util.xml.GrammarComplexType;

import com.sun.javax.swing.JTreeTable;
import com.sun.javax.swing.TreeTableModel;

/**
 * XML Document UI L&F code
 * 
 * @author Kelly A. Campbell
 * @created 25 April 2003
 */
public class XMLEditorDocUI extends JPanel implements MouseListener,
        MerlotConstants, ChangeListener, StatusListener {

    /**
     * Description of the Field
     */
    protected DOMTreeTableAdapter _XMLmodel;

    /**
     * Description of the Field
     */
    protected XMLEditorDoc _doc;

    /**
     * Description of the Field
     */
    protected JTreeTable _table;

    /**
     * Description of the Field
     */
    protected JPanel _treePanel;

    /**
     * Description of the Field
     */
    protected JSplitPane _splitPane;

    /**
     * The panel which contains the node editor
     */
    protected JPanel _editPanel;

    /**
     * Description of the Field
     */
    protected JPanel _statusPanel;

    /**
     * Description of the Field
     */
    protected JLabel _status1;

    /**
     * Description of the Field
     */
    protected JLabel _status2;

    /**
     * Description of the Field
     */
    protected JLabel _status3;

    /**
     * Description of the Field
     */
    protected JMenuBar _menuBar;

    /**
     * Description of the Field
     */
    protected JMenu _treePasteMenu = null;

    /**
     * The current node this editor is editing in the right-hand pane
     */
    protected MerlotDOMNode _currentNode;

    /**
     * The current edit panel which is displayed in the right-hand pane
     */
    protected JPanel _currentEditPanel;

    /**
     * The history of nodes which have been opened and edited
     */
    private Vector _nodeHistory;

    /**
     * Keep track of removed table columns
     */
    private HashMap _removedColumns;

    /**
     * Description of the Field
     */
    protected final static long STATUS_DISPLAY_TIME = (5 * 1000);

    // display status msgs for 5 secs

    /**
     * Constructor for the XMLEditorDocUI object
     * 
     * @param doc
     *            Description of the Parameter
     */
    public XMLEditorDocUI(XMLEditorDoc doc) {
        super();

        this.setLayout(new BorderLayout());

        _doc = doc;
        _nodeHistory = new Vector();

        setupPanel();

    }

    /**
     * This panel looks a little like Windows Explorer. It has a treetable at
     * the left, a JSplitPane slider in the middle, and then an editing
     * workspace at the right.
     */
    protected void setupPanel() {
        XMLEditorSettings settings  = XMLEditorSettings.getSharedInstance();
        
        // setup the edit panel
        _editPanel = new JPanel();
        StrutLayout layout = new StrutLayout();
        _editPanel.setLayout(layout);
        _editPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Try to set the default editPanel preferred size based on stored
        // settings
        Dimension d = XMLEditorSettings.getSharedInstance()
                .getEditPanelDimension();
        Dimension dmin = _editPanel.getMinimumSize();
        if (d != null && d.width > dmin.width && d.height > dmin.height) {
            _editPanel.setPreferredSize(d);
        } else
            _editPanel.setPreferredSize(dmin);

        // Tree Panel
        _treePanel = setupTreeTable();

        // Try to set the default treePanel preferred size based on stored
        // settings
        d = settings.getTreePanelDimension();
        dmin = _treePanel.getMinimumSize();
        if (d != null && d.width > dmin.width && d.height > dmin.height) {
            _treePanel.setPreferredSize(d);
        } else {
            _treePanel.setPreferredSize(dmin);
        }       

        // the minimum has to be set if the split pane is gonna move
        _editPanel.setMinimumSize(new Dimension(10, 10));

        // Windows does not set this wide enough...
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            UIManager.put("SplitPane.dividerSize", new Integer(8));
        }

        // setup the split pane
        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _treePanel,
                _editPanel);
        _splitPane.setOneTouchExpandable(true);

        // see if we're using jdk1.3 or later and set the weighting
        try {
            Class spclass = JSplitPane.class;
            Class[] paramClass = { double.class };
            Object[] params = { new Double(0.33) };

            Method m = spclass.getDeclaredMethod("setResizeWeight", paramClass);
            if (m != null) {
                m.invoke(_splitPane, params);
            }
        } catch (NoSuchMethodException nsm) {
        } catch (Exception ex) {
            MerlotDebug.exception(ex);
        }

        this.add(_splitPane, BorderLayout.CENTER);
        setSplitPanelSizes(false);

        _statusPanel = new JPanel();

        PercentLayout statusLay = new PercentLayout(PercentLayout.HORIZONTAL, 5);
        //	statusLay.DEBUG = true;

        _statusPanel.setLayout(statusLay);

        _status1 = new JLabel();

        _status2 = new JLabel();
        _status2.setPreferredSize(new Dimension(100, 20));

        _status3 = new JLabel();
        _statusPanel.add(_status1, new PercentLayout.Constraints(60,
                PercentLayout.BOTH));
        _statusPanel.add(_status2, new PercentLayout.Constraints(10,
                PercentLayout.BOTH));
        _statusPanel.add(_status3, new PercentLayout.Constraints(30,
                PercentLayout.BOTH));

        _status1.setBorder(new BevelBorder(BevelBorder.LOWERED));
        _status2.setBorder(new BevelBorder(BevelBorder.LOWERED));
        _status3.setBorder(new BevelBorder(BevelBorder.LOWERED));

        _statusPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        //_statusPanel.setInsets(new Insets(2,2,2,2));

        this.add(_statusPanel, BorderLayout.SOUTH);
        
        d = XMLEditorSettings.getSharedInstance().getDeskTopDimension();
        if (d != null)
            this.setPreferredSize(d);
        else
            this.setPreferredSize(new Dimension(450, 500));

    }
    
    /*
     * This is particularly nasty buggy Swing code We're trying to set the
     * sliders to the previously stored spot. Here's what I discovered on the
     * web and by trial and error: - the preferred sizes of the components must
     * be se to give it a chance of working - the panels must be showing to give
     * it a chnace of working - it must be called in then EDT I aslo found that
     * unless it's called twice - here and from the XMLEditorFrame it wasn't
     * working properly in Linux. MimimumSize is important if the panel is less
     * than this you can't slide the slider. resetToPreferredSize overcomes this -
     * JML
     *  
     */
    protected void setSplitPanelSizes(final boolean later) {
        Runnable setSizes = new Runnable() {
            public void run() {
                if (_splitPane == null)
                    return;

                int hpos = XMLEditorSettings.getSharedInstance()
                        .getHorizontalSplitPanePercentage();

                if (hpos != -1 || XMLEditorFrame.getSharedInstance()==null) {
                    // How will we handle extra space
                    // attribute panel is lowest priority
                    // editPanel is highest
                    _splitPane.setResizeWeight(0);

                    _splitPane.setDividerLocation(hpos);
                    _splitPane.setLastDividerLocation(hpos);
                    _splitPane.getUI().setDividerLocation(_splitPane, hpos);
                    _splitPane.putClientProperty(
                            JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY,
                            new Integer(hpos));
                    _splitPane.resetToPreferredSizes();
                } else {
                    double weight = 0.35;
                    try {
                        weight = Double.parseDouble(MerlotResource.getString(
                                UI, "horizontal.splitPane.percentage")) / 100;
                    } catch (Exception ex) {
                    }
                    _splitPane.setResizeWeight(weight);
                }
            }
        };

        if (later) {
            // Sizes should already be set
            SwingUtilities.invokeLater(setSizes);
        } else {
            setSizes.run();
        }
    }

    /**
     * Description of the Method
     * 
     * @return Description of the Return Value
     */
    protected JMenu createTreePasteMenu() {

        JMenu treePasteMenu = new JMenu(MerlotResource.getString(UI,
                "edit.paste"));
        treePasteMenu.add(_doc._pasteBeforeAction);
        treePasteMenu.add(_doc._pasteAfterAction);
        treePasteMenu.add(_doc._pasteIntoAction);
        _treePasteMenu = treePasteMenu;
        return treePasteMenu;
    }

    /**
     * Gets the menuBar attribute of the XMLEditorDocUI object
     * 
     * @return The menuBar value
     */
    protected JMenuBar getMenuBar() {
        return _menuBar;
    }

    /**
     * Gets the nodeHistory attribute of the XMLEditorDocUI object
     * 
     * @return The nodeHistory value
     */
    public Vector getNodeHistory() {
        return _nodeHistory;
    }

    /**
     * Gets the currentNode attribute of the XMLEditorDocUI object
     * 
     * @return The currentNode value
     */
    public MerlotDOMNode getCurrentNode() {
        return _currentNode;
    }

    /**
     * Description of the Method
     * 
     * @return Description of the Return Value
     */
    protected JPanel setupTreeTable() {
        JPanel p = new JPanel();

        p.setLayout(new PercentLayout());

        _XMLmodel = _doc.getTreeTableModel();
        if (_XMLmodel != null) {
            _XMLmodel.addStatusListener(this);

            _table = new DNDJTreeTable(_XMLmodel);
            //		_table.setRootVisible(true);
            DNDJScrollPane sp = new DNDJScrollPane(_table);

            JTreeTable.TreeTableCellRenderer tree = (JTreeTable.TreeTableCellRenderer) _table
                    .getDefaultRenderer(TreeTableModel.class);
            tree.setRootVisible(false);
            tree.setShowsRootHandles(true);

            // setup the tree to use Node.getName() instead of
            // toString() to render the Node
            tree.setCellRenderer(new MerlotNodeRenderer());
            _XMLmodel.setJTreeTable(_table);

            // Allow table column customistaion
            // Here's where the tree table columns get customised...
            XMLEditorSettings settings = XMLEditorSettings.getSharedInstance();
            JTableHeader jth = _table.getTableHeader();
            _removedColumns = new HashMap();
            
            int cols = _table.getColumnCount();
            ArrayList removeCols = new ArrayList(cols);
            for (int i = 0; i < cols; i++) {
                String name = _table.getColumnName(i);
                int width = settings.getColumnWidth(name.toLowerCase());
                if (width != -1)
                    setTreeTableColumnWidth(name, width);

                if (!name.toLowerCase().equals(
                        MerlotResource.getString(UI, "xml.element")
                                .toLowerCase())) {
                    String property = "xerlin.show." + name.toLowerCase()
                            + ".column.startup";
                    // store cols for removal
                    if (!("true".equals(settings.getProperty(property))))
                        removeCols.add(name);
                }
            }

            // Actually remove the cols
            for (int i = 0; i < removeCols.size(); i++) {
                String name = (String) removeCols.get(i);
                removeTreeTableColumn(name);
            }

            jth.addMouseListener(new EditTableColumnsAction());
            
            _table.setShowGrid(true);

            _table.setIntercellSpacing(new Dimension(1, 0));

            _table
                    .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            tree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

            _table.addMouseListener(this);

            p.add(sp, new PercentLayout.Constraints(100, PercentLayout.BOTH));

        }
        p.setPreferredSize(new Dimension(300, 500));
        p.setMinimumSize(new Dimension(10, 10));
        return p;
    }

    /**
     * Displays a node for editing in the workspace panel.
     * 
     * @param node
     *            the node to edit
     * @param brandSpankinNew
     *            Description of the Parameter
     */
    public void editNode(MerlotDOMNode node, boolean brandSpankinNew) {
        if (_currentNode == node || node == null) {
            return;
        }

        // first save anything in the previously open node
        // if there is a problem highlight the problem node in the tree
        // and keep it open
        if (saveOpenEditors()) {
            XMLEditorActions actions = XMLEditorActions.getSharedInstance();
            _currentNode = node;

            //Reorder the history
            if (_nodeHistory.contains(node)) {
                _nodeHistory.remove(node);
            }
            _nodeHistory.add(node);

            if (actions != null) {
                if (_nodeHistory.size() > 1) {
                    actions._backAction.setEnabled(true);
                    actions._cancelAction.setEnabled(!brandSpankinNew);
                } else {
                    actions._backAction.setEnabled(false);
                    actions._cancelAction.setEnabled(false);
                }
            }

            _currentEditPanel = node.getEditor().getEditPanel(node);

            _currentEditPanel.setMinimumSize(new Dimension(50, 50));

            _editPanel.removeAll();
            _editPanel.add(_currentEditPanel);
            ((StrutLayout) _editPanel.getLayout()).setSprings(
                    _currentEditPanel, StrutLayout.SPRING_BOTH);

            _editPanel.revalidate();

            // tell the editor to get the focus RFE01137
            node.getEditor().grabFocus(_currentEditPanel);
        }

        showNode(_currentNode);
    }

    /**
     * Description of the Method
     * 
     * @param node
     *            Description of the Parameter
     */
    public void editText(MerlotDOMNode node) {
        // edits the text and children directly under this node
        MerlotDebug.msg("edit Text");
        JPanel p = new JPanel();
        MerlotTextPane textpane = new MerlotTextPane(node);
        JScrollPane sp = new JScrollPane(textpane);

        sp.setPreferredSize(new Dimension(350, 250));

        p.add(sp);
        MerlotOptionPane.showInternalMessageDialog(_editPanel, p);

    }

    /**
     * Description of the Method
     * 
     * @param node
     *            Description of the Parameter
     */
    public void deleteNode(MerlotDOMNode node) {
        //	throws MerlotException

        node.delete();
        refreshNode(node, true);
        _doc.setDirty(true);

    }

    /**
     * Description of the Method
     * 
     * @return Description of the Return Value
     */
    public boolean saveOpenEditors() {
        boolean success = true;
        if (_currentNode != null) {
            try {
                _currentNode.getEditor().savePanel(_currentEditPanel);
                _doc.setDirty(true);
            } catch (PropertyVetoException ex) {
                success = false;
                // Force user to set this property type
                // This cahnge cannot be cancelled without corrupting the
                // document
                PropertyChangeEvent evt = ex.getPropertyChangeEvent();
                if (evt.getOldValue().equals(evt.getNewValue())) {
                    XMLEditorActions.getSharedInstance()._cancelAction
                            .setEnabled(false);
                }
                MerlotError.msg(ex.getMessage(), MerlotResource.getString(ERR,
                        "error"));
            } catch (Exception ex) {
                MerlotError.exception(ex,
                        "Not a fatal error. Saving will continue.");
            }
        }
        return success;
    }

    /**
     * Gets the nodeMenuItems attribute of the XMLEditorDocUI object
     * 
     * @param node
     *            Description of the Parameter
     * @return The nodeMenuItems value
     */
    protected java.util.List getNodeMenuItems(MerlotDOMNode node) {
        long start = System.currentTimeMillis();
        ArrayList result = new ArrayList();
        XMLEditorActions actions = XMLEditorActions.getSharedInstance();
        Object[] path = _XMLmodel.getTreePathForNode(node);

		boolean mayBeRemoved = path.length > 2 && node.mayBeRemoved();
        result.add(getNodeAddMenu(node, INTO));

        result.add(MerlotUtils.createActionMenuItem(_doc._editNodeAction));
        result.add(MerlotUtils.createActionMenuItem(_doc._expandNodeAction));
        result.add(MerlotUtils.createActionMenuItem(_doc._collapseNodeAction));

        result.add(MerlotUtils.createActionMenuItem(_doc._deleteNodeAction));

        _doc._deleteNodeAction.setEnabled(mayBeRemoved); // BUG01136

        result.add(new Object());
        // separator

        result.add(MerlotUtils.createActionMenuItem(_doc._cutNodeAction));

        // Disable cut action in node menu and toolbar
        _doc._cutNodeAction.setEnabled(path.length > 2);
        if (actions != null) {
            actions._cutAction.setEnabled(path.length > 2);
        }

        result.add(MerlotUtils.createActionMenuItem(_doc._copyNodeAction));

        // Is there sense to copy root node?
        _doc._copyNodeAction.setEnabled(path.length > 2);
        if (actions != null) {
            actions._copyAction.setEnabled(path.length > 2);
        }

        result.add(createTreePasteMenu());

        enablePasteItems(node);

        try {
            MerlotDOMEditor editor = node.getEditor();

            JMenuItem[] menu_additions = editor.getMenuItems(node);
            if (menu_additions != null && menu_additions.length > 0) {
                result.add(new Object());
                // separator

                for (int i = 0; i < menu_additions.length; i++) {
                    result.add(menu_additions[i]);
                }

            }
        } catch (Throwable t) {
            MerlotDebug.exception(t);
        }
        long end = System.currentTimeMillis();
        System.out.println("Duration - getting node menu items: "
                + (end - start) + " ms.");
        return result;
    }

    /**
     * Gets the nodePluginMenuItems attribute of the XMLEditorDocUI object
     * 
     * @param node
     *            Description of the Parameter
     * @return The nodePluginMenuItems value
     */
    protected java.util.List getNodePluginMenuItems(MerlotDOMNode node) {
        ArrayList result = new ArrayList();
        Iterator iter = PluginManager.getInstance().getPlugins().iterator();
        Iterator iterAction;
        PluginConfig nextConfig;
        NodeActionConfig nextAction;
        JMenuItem actionMenuItem = null;

        while (iter.hasNext()) {
            nextConfig = (PluginConfig) iter.next();
            if (nextConfig instanceof NodeActionPluginConfig) {
                iterAction = ((NodeActionPluginConfig) nextConfig)
                        .getActionConfigs().iterator();
                while (iterAction.hasNext()) {
                    nextAction = (NodeActionConfig) iterAction.next();
                    actionMenuItem = nextAction.getMenuItem(node);
                    result.add(actionMenuItem);
                }
            }
        }
        ArrayList ret = new ArrayList();
        if (result.size() > 0) {
            ret.add(new Object());
            // separator
            ret.addAll(result);
        }
        return ret;
    }

    /**
     * Adds a feature to the ItemsToMenu attribute of the XMLEditorDocUI object
     * 
     * @param menuItems
     *            The feature to be added to the ItemsToMenu attribute
     * @param menu
     *            The feature to be added to the ItemsToMenu attribute
     */
    protected void addItemsToMenu(java.util.List menuItems, MenuElement menu) {
        Iterator iter = menuItems.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (menu instanceof JMenu) {
                if (o instanceof JMenuItem) {
                    ((JMenu) menu).add((JMenuItem) o);
                } else {
                    ((JMenu) menu).addSeparator();
                }
            } else if (menu instanceof JPopupMenu) {
                if (o instanceof JMenuItem) {
                    ((JPopupMenu) menu).add((JMenuItem) o);
                } else {
                    ((JPopupMenu) menu).addSeparator();
                }
            }
        }

    }

    /**
     * Gets the nodePopupMenu attribute of the XMLEditorDocUI object
     * 
     * @param node
     *            Description of the Parameter
     * @return The nodePopupMenu value
     */
    protected JPopupMenu getNodePopupMenu(MerlotDOMNode node) {
        SizeableJPopupMenuByHeight popup = new SizeableJPopupMenuByHeight();
        addItemsToMenu(getNodeMenuItems(node), popup);
        addItemsToMenu(getNodePluginMenuItems(node), popup);
        return popup;
    }

    /**
     * This enables or disables the proper paste items in the paste menu based
     * on the node parameter as the parent or sibling and a peek at the
     * Transferable in the clipboard.
     * 
     * @param node
     *            Description of the Parameter
     */
    protected void enablePasteItems(MerlotDOMNode node) {
        if (_treePasteMenu != null) {

            _treePasteMenu.setEnabled(false);
            Transferable t;
            if (XMLEditorFrame.getSharedInstance() != null) {
                t = XMLEditorFrame.getSharedInstance().getTreeClipboard()
                        .getContents(this);
            } else {
                t = XMLEditor.getSharedInstance().getXerlinPanel()
                        .getTreeClipboard().getContents(this);
            }
            MerlotDebug.msg("enablePasteItems: t = " + t);

            if (t instanceof MerlotDOMFragment) {
                MerlotDOMFragment frag = (MerlotDOMFragment) t;
                // get the frag's child (should usually just have one
                // child for a copy'n'paste op
                if (frag != null) {
                    MerlotDOMNode[] nodes = frag.getChildNodes();
                    MerlotDOMNode nd = null;
                    if (nodes != null && nodes.length > 0) {
                        nd = frag.getFirstChild();
                        MerlotDebug.msg("nd = " + nd);

                        if (nd instanceof MerlotLibraryItem) {
                            nd = nd.getFirstChild();
                            MerlotDebug.msg("nd was a libitem, is now: " + nd);

                        }
                    }

                    if (nd != null) {
                        // check and see where we can shove it
                        String nodeType = nd.getNodeName();
                        if (_doc != null) {
                            // first check for insertInto
                            _doc._pasteIntoAction.setEnabled(false);
                            _doc._pasteAfterAction.setEnabled(false);
                            _doc._pasteBeforeAction.setEnabled(false);

                            // Enable pasting of #text nodes
                            if (nodeType.equals("#text")) {
                                if (node instanceof MerlotDOMElement) {
                                    if (node.getGrammarComplexType() != null
                                            && node
                                                    .getGrammarComplexType()
                                                    .getIsSimpleContentAllowed()) {
                                        _doc._pasteIntoAction.setEnabled(true);
                                        _treePasteMenu.setEnabled(true);
                                    }
                                    MerlotDOMNode parent = node.getParentNode();
                                    if (!(parent instanceof MerlotDOMElement)) {
                                        return;
                                    }
                                    if (parent.getGrammarComplexType() != null
                                            && parent
                                                    .getGrammarComplexType()
                                                    .getIsSimpleContentAllowed()) {
                                        _doc._pasteBeforeAction
                                                .setEnabled(true);
                                        _doc._pasteAfterAction.setEnabled(true);
                                        _treePasteMenu.setEnabled(true);
                                    }
                                }
                                return;
                            }

                            /*
                             * Enumeration elements =
                             * node.getInsertableElements(); if (elements !=
                             * null) { // BUG01407 while
                             * (elements.hasMoreElements()) { DTDElement el =
                             * (DTDElement)elements.nextElement(); if
                             * (el.getName().equals(nodeType)) {
                             * _doc._pasteIntoAction.setEnabled(true);
                             * _treePasteMenu.setEnabled(true); } } } / now
                             * check for sibling availablilty MerlotDOMNode
                             * parent = node.getParentNode(); if (parent !=
                             * null) { int location =
                             * parent.getChildIndex(node); elements =
                             * parent.getInsertableElements(location); if
                             * (elements != null) { while
                             * (elements.hasMoreElements()) { DTDElement el =
                             * (DTDElement)elements.nextElement(); if
                             * (el.getName().equals(nodeType)) {
                             * _doc._pasteBeforeAction.setEnabled(true);
                             * _treePasteMenu.setEnabled(true); break; } } }
                             * location++; elements =
                             * parent.getInsertableElements(location); if
                             * (elements != null) { while
                             * (elements.hasMoreElements()) { DTDElement el =
                             * (DTDElement)elements.nextElement(); if
                             * (el.getName().equals(nodeType)) {
                             * _doc._pasteAfterAction.setEnabled(true);
                             * _treePasteMenu.setEnabled(true); break; } } } }
                             */
                            GrammarComplexType complexType = node
                                    .getGrammarComplexType();
                            GrammarComplexType[] insertables = complexType
                                    .getInsertableElements(node.getRealNode());
                            for (int i = 0; i < insertables.length; i++) {
                                GrammarComplexType insertable = insertables[i];
                                if (insertable != null
                                        && insertable.getName() != null
                                        && insertable.getName()
                                                .equals(nodeType)) {
                                    _doc._pasteIntoAction.setEnabled(true);
                                    _treePasteMenu.setEnabled(true);
                                    break;
                                }
                            }

                            MerlotDOMNode parent = node.getParentNode();
                            complexType = parent.getGrammarComplexType();
                            if (parent != null && complexType != null) {
                                int location = parent.getChildIndex(node);
                                insertables = complexType
                                        .getInsertableElements(parent
                                                .getRealNode(), location);
                                for (int i = 0; i < insertables.length; i++) {
                                    GrammarComplexType insertable = insertables[i];
                                    if (insertable.getName().equals(nodeType)) {
                                        _doc._pasteBeforeAction
                                                .setEnabled(true);
                                        _treePasteMenu.setEnabled(true);
                                        break;
                                    }
                                }
                                insertables = complexType
                                        .getInsertableElements(parent
                                                .getRealNode(), location + 1);
                                for (int i = 0; i < insertables.length; i++) {
                                    GrammarComplexType insertable = insertables[i];
                                    if (insertable.getName().equals(nodeType)) {
                                        _doc._pasteAfterAction.setEnabled(true);
                                        _treePasteMenu.setEnabled(true);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates the add-> menu popup for right-click actions on a particular
     * node.
     * 
     * @param node
     *            the node this is acting on.
     * @param where
     *            what the add is for.. INTO, BEFORE, AFTER the context node
     * @return a menu containing elements that can be added to a node. If the
     *         INTO param is passed, this also adds the add after and before
     *         submenus.
     */

    protected JMenu getNodeAddMenu(MerlotDOMNode node, int where) {
        MerlotEditorFactory factory = MerlotEditorFactory.getInstance();
        MerlotDOMNode parent = node.getParentNode();
        MerlotDOMEditor editor = node.getEditor();
        boolean suppressItems = XMLEditorSettings.getSharedInstance()
                .getSuppressAddMenuItems();
        MerlotDOMNode contextNode = null;
        int contextLocation = -1;

        JMoreMenu menu;
        switch (where) {
            case INTO:
            default:
                menu = new JMoreMenu(MerlotResource.getString(UI,
                        "node.popup.add"));
                editor = node.getEditor();
                contextNode = node;
                parent = node;

                break;
            case BEFORE:
                menu = new JMoreMenu(MerlotResource.getString(UI,
                        "node.popup.add.before"));
                if (parent != null) {
                    editor = parent.getEditor();
                    contextNode = parent;
                    contextLocation = parent.getChildIndex(node);
                }
                break;
            case AFTER:
                menu = new JMoreMenu(MerlotResource.getString(UI,
                        "node.popup.add.after"));
                if (parent != null) {
                    editor = parent.getEditor();
                    contextNode = parent;
                    contextLocation = parent.getChildIndex(node) + 1;
                }
                break;
        }

        // get list of possible sub-elements for the node here
        //Enumeration elements;
        GrammarComplexType[] elements = new GrammarComplexType[0];
        GrammarComplexType complexType = contextNode.getGrammarComplexType();
        if (complexType != null) {
            if (contextLocation < 0) {
                elements = complexType.getInsertableElements(contextNode
                        .getRealNode());
            } else {
                elements = complexType.getInsertableElements(contextNode
                        .getRealNode(), contextLocation);
            }
        } else {
            MerlotDebug.msg(" Cannot get insertable elements - complexType is null.");
        }
        if (elements != null) {
            //TreeSet set = new TreeSet();
            //while (elements.hasMoreElements()) {
            //    set.add(elements.nextElement());
            //}
            //Iterator it = set.iterator();

            ActionListener listener = new NodeAddActionListener(parent, node,
                    where);
            //while (it.hasNext()) {
            for (int i = 0; i < elements.length; i++) {
                //DTDElement el = (DTDElement)it.next();
                GrammarComplexType el = elements[i];
                if (el == null) {
                    continue;
                }
                DTDCacheEntry dtdentry = _doc.getDTDCacheEntry();
                DTDPluginConfig pluginConfig = null;

                if (dtdentry instanceof PluginDTDCacheEntry) {
                    pluginConfig = ((PluginDTDCacheEntry) dtdentry)
                            .getPluginConfig();
                }
                //MerlotDOMEditor e =
                // factory.getEditor(el.getName(),null,_doc.getPlugin());
                MerlotDOMEditor e = null;
                try {
                    e = factory.getEditor(el.getName(), pluginConfig);
                } catch (InstantiationException ex) {
                } catch (IllegalAccessException ex) {
                }

                if (suppressItems
                        && e != null
                        && (editor.suppressAddType(el) || e.suppressAddType(el))) {
                    continue;
                }

                Icon icon = null;
                DTDPluginConfig config = _doc.getDTDPluginConfig();
                if (config != null) {
                    icon = config.getIconFor(el.getName(),
                            DTDPluginConfig.ICON_SIZE_SMALL);
                }

                JMenuItem item = new JMenuItem(el.getName(), icon);
                item.addActionListener(listener);
                menu.add(item);
            }

            // Allow #text nodes to be added
            if (contextNode instanceof MerlotDOMElement && complexType != null
            //&& complexType.getIsSimpleContentAllowed()
                    && complexType.isMixedType()) {
                JMenuItem item = new JMenuItem("#text", null);
                item.addActionListener(listener);
                menu.add(item);
            }
        } else {
            //menu.setEnabled(false);
        }
        menu.addSeparator();
        JMenuItem addSpecial = getNodeAddSpecialMenu(node, where);
        menu.insertStatic(addSpecial);
        if (where == INTO) {
            JMenuItem addAfter = getNodeAddMenu(node, AFTER);
            JMenuItem addBefore = getNodeAddMenu(node, BEFORE);
            menu.insertStatic(addBefore);
            menu.insertStatic(addAfter);
        }

        return menu;
    }

    /**
     * Gets the nodeAddSpecialMenu attribute of the XMLEditorDocUI object
     * 
     * @param node
     *            Description of the Parameter
     * @return The nodeAddSpecialMenu value
     */
    protected JMenu getNodeAddSpecialMenu(MerlotDOMNode node, int context) {
        JMenu menu = new JMoreMenu(MerlotResource.getString(UI,
                "node.popup.add.special"));
        ActionListener listener = new NodeAddSpecialCommentActionListener(node,
                context);

        JMenuItem item = new JMenuItem(MerlotResource.getString(UI,
                "node.popup.add.special.comment"));
        item.addActionListener(listener);
        if (context == INTO && !(node instanceof MerlotDOMElement))
            item.setEnabled(false);
        menu.add(item);

        ActionListener piListener = new NodeAddSpecialPIActionListener(node,
                context);
        JMenuItem piItem = new JMenuItem(MerlotResource.getString(UI,
                "node.popup.add.special.pi"));
        piItem.addActionListener(piListener);
        if (context == INTO && !(node instanceof MerlotDOMElement))
            piItem.setEnabled(false);

        menu.add(piItem);
        return menu;
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void mouseClicked(MouseEvent e) {
        // make sure it's a regular click
        if (e.isPopupTrigger()) {
            return;
        }
        // not a popup,
        // trigger the editor display for the current row

        //			MerlotDebug.msg("Edit "+_table.getSelectedRow());
        MerlotDOMNode node = getSelectedNode();
        if (node != null) {
            editNode(node, false);
        }
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void mousePressed(MouseEvent e) {

        // setup the Node menu
        if (XMLEditorFrame.getSharedInstance() != null) {
            //updateNodeMenu(XMLEditorFrame.getSharedInstance()._nodeMenu);

            // check if this is a popup menu event
            if (e.isPopupTrigger()) {
                doPopup(e);
            }
        }
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void mouseReleased(MouseEvent e) {
        // check if this is a popup menu event
        if (e.isPopupTrigger()) {
            doPopup(e);

        }
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    protected void doPopup(MouseEvent e) {
        Point pt = e.getPoint();
        JPopupMenu menu = null;
        int row = -1;

        int rowcount = _table.getSelectedRowCount();
        if (rowcount <= 1) {
            row = _table.rowAtPoint(pt);
            try {
                _table.setRowSelectionInterval(row, row);
            } catch (IllegalArgumentException iae) {
                MerlotDebug.msg("IllegalArgumentException in drag");
                return;
            }
        }

        //replace code above because _table.rowAtPoint(pt) can be -1
        //and _table.setRowSelectionInterval can not set -l value.
        int[] rows = _table.getSelectedRows();
        if (rows.length > 0) {
            row = rows[0];
        }

        MerlotDOMNode node = getNodeForRow(row);
        if (node != null) {
            // Make sure node is selected
            editNode(node, false);
            menu = getNodePopupMenu(node);
        }

        if (menu != null) {
            //XXX make sure menu is all the way on the screen
            menu.pack();
            Dimension menusize = menu.getPreferredSize();
            MerlotDebug.msg("menusize = [" + menusize.width + ", "
                    + menusize.height + "]  pt.x = " + pt.x + "  pt.y = "
                    + pt.y);

            Point screenpoint = (Point) pt.clone();

            javax.swing.SwingUtilities
                    .convertPointToScreen(screenpoint, _table);
            Dimension screensize = java.awt.Toolkit.getDefaultToolkit()
                    .getScreenSize();

            Point endpoint = new Point();
            endpoint.x = screenpoint.x + menusize.width;
            endpoint.y = screenpoint.y + menusize.height;

            if (screenpoint.x < 0) {
                screenpoint.x = 0;
            }
            if (screenpoint.y < 0) {
                screenpoint.y = 0;
            }
            if (endpoint.x > (int) screensize.getWidth()) {
                screenpoint.x = (int) screensize.getWidth() - menusize.width;
            }
            if (endpoint.y > (int) screensize.getHeight()) {
                // Add 30 points for a possible Windows Taskbar
                // (i.e. - programmer couldn't figure out how to ask the system
                // if it was auto-hidden.) - empirically determined value
                // if anybody has a better idea.....
                screenpoint.y = (int) screensize.getHeight() - menusize.height
                        - 30;
            }

            Point componentpoint = (Point) screenpoint.clone();
            javax.swing.SwingUtilities.convertPointFromScreen(componentpoint,
                    _table);
            pt = componentpoint;
            menu.show(_table, pt.x, pt.y);
        }
        //	MerlotDebug.msg("node = "+node+ " real node class = "
        // +node.getRealNode().getClass());

    }

    /**
     * updates the given node menu for the selected node
     * 
     * @param menu
     *            Description of the Parameter
     */
    public void updateNodeMenu(JMenu menu) {
        MerlotDOMNode node = getSelectedNode();
        if (node == null) {
            menu.setEnabled(false);
        } else {
            // remove everything from the menu
            menu.removeAll();
            addItemsToMenu(getNodeMenuItems(node), menu);
            menu.setEnabled(true);

        }
    }

    /**
     * Gets the Node object that is the current selection in the document tree.
     * 
     * @return The selectedNode value
     */
    public MerlotDOMNode getSelectedNode() {
        MerlotDOMNode node = null;

        int selected_row = _table.getSelectedRow();
        if (selected_row >= 0) {
            node = getNodeForRow(selected_row);
        } else {
            MerlotDebug.msg("No row is currently selected.");
        }

        return node;
    }

    /**
     * Gets the selectedNodes attribute of the XMLEditorDocUI object
     * 
     * @return The selectedNodes value
     */
    public MerlotDOMNode[] getSelectedNodes() {
        MerlotDOMNode[] nodes = null;
        TreePath[] selection = _table.getTree().getSelectionPaths();
        if (selection != null) {
            nodes = new MerlotDOMNode[selection.length];
            for (int i = 0; i < selection.length; i++) {
                nodes[i] = getNodeForPath(selection[i]);
            }
        }
        return nodes;
    }

    /**
     * Gets the selectedRow attribute of the XMLEditorDocUI object
     * 
     * @return The selectedRow value
     */
    public int getSelectedRow() {
        return _table.getSelectedRow();
    }

    /**
     * Description of the Method
     * 
     * @param node
     *            Description of the Parameter
     */
    public void selectNode(MerlotDOMNode node) {
        Object[] path = _XMLmodel.getTreePathForNode(node);

        _table.getTree().setSelectionPath(new TreePath(path));

        if (XMLEditorFrame.getSharedInstance() != null)
            updateNodeMenu(XMLEditorFrame.getSharedInstance()._nodeMenu);
    }

    /**
     * Gets the nodeForPath attribute of the XMLEditorDocUI object
     * 
     * @param tp
     *            Description of the Parameter
     * @return The nodeForPath value
     */
    public MerlotDOMNode getNodeForPath(TreePath tp) {
        MerlotDOMNode node = null;

        Object n = tp.getLastPathComponent();
        if (n instanceof MerlotDOMNode) {
            node = (MerlotDOMNode) n;
        } else {
            MerlotDebug.msg("unknown tree node object type: " + n);
        }
        return node;
    }

    /**
     * Gets the nodeForRow attribute of the XMLEditorDocUI object
     * 
     * @param row
     *            Description of the Parameter
     * @return The nodeForRow value
     */
    public MerlotDOMNode getNodeForRow(int row) {
        // get the object
        javax.swing.tree.TreePath treePath = _table.getTree()
                .getPathForRow(row);
        if (treePath != null) {
            return getNodeForPath(treePath);
        }
        return null;
    }

    /**
     * Refreshs a panel (accessible via the node). Works by cancelling and
     * re-opening the node to edit. cancelOnly option cancels without
     * re-opening.
     * 
     * @param node
     *            Description of the Parameter
     * @param cancelOnly
     *            Description of the Parameter
     */
    public void refreshNode(MerlotDOMNode node, boolean cancelOnly) {
        if (_currentNode == node) {
            _currentNode = null;
            if (cancelOnly) {
                _nodeHistory.remove(node);
                MerlotDOMNode toEdit = _XMLmodel.getDocument().getFirstChild();
                if (_nodeHistory.size() > 1) {
                    toEdit = (MerlotDOMNode) _nodeHistory.lastElement();
                    _nodeHistory.remove(toEdit);
                }
                editNode(toEdit, false);
            } else {
                editNode(node, false);
            }
        }
    }

    /**
     * Highlights the node in the JTree
     * 
     * @param node
     *            Description of the Parameter
     */
    public void showNode(MerlotDOMNode node) {
        Object[] path = _XMLmodel.getTreePathForNode(node);
        _table.getTree().setSelectionPath(new TreePath(path));
    }

    /**
     * Returns the dimensions of the workspace
     * 
     * @return The workspaceSize value
     */

    public Dimension getWorkspaceSize() {
        return _editPanel.getSize();
    }

    /**
     * Set the tree root and highlight the node in the JTree
     * 
     * @param node
     *            The new root value
     */
    public void setRoot(MerlotDOMNode node) {
        if (node == null) {
            return;
        }

        _XMLmodel.setRoot(node);

        Object[] pathRoot = _XMLmodel
                .getTreePathForNode((MerlotDOMNode) _XMLmodel.getRoot());
        _XMLmodel.fireTreeStructureChanged(_XMLmodel.getRoot(), pathRoot);

        // Make sure root is visible - except initially
        if (_table.getTree().isRootVisible() == false) {
            _table.getTree().setRootVisible(true);
        }

        // Highlight the path to this node in the tree
        selectNode(node);
    }

    /**
     * gets the transfer data for the current selection, or null if no selection
     * exists
     * 
     * @return The transferable value
     */
    public Transferable getTransferable() {
        Transferable t = null;
        TreePath[] selection = _table.getTree().getSelectionPaths();
        if (selection != null) {
            t = _XMLmodel.getTransferable(selection);
        }
        return t;
    }

    /**
     * Sets the status attribute of the XMLEditorDocUI object
     * 
     * @param s
     *            The new status value
     */
    public void setStatus(String s) {
        setStatus(s, 1);
    }

    /**
     * Sets the status attribute of the XMLEditorDocUI object
     * 
     * @param s
     *            The new status value
     * @param i
     *            The new status value
     */
    public void setStatus(String s, int i) {
        switch (i) {
            case 1:
            default:
                _status1.setText(s);
                break;
            case 2:
                _status2.setText(s);
                break;
            case 3:
                _status3.setText(s);
                break;
        }

    }

    /**
     * Description of the Method
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void statusChanged(StatusEvent evt) {
        setStatus(evt.getMessage(), evt.getIndex());
        Thread r = new StatusCleaner(evt);
        r.start();

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    public class StatusCleaner extends Thread {
        StatusEvent _evt;

        /**
         * Constructor for the StatusCleaner object
         * 
         * @param evt
         *            Description of the Parameter
         */
        public StatusCleaner(StatusEvent evt) {
            _evt = evt;
        }

        /**
         * Main processing method for the StatusCleaner object
         */
        public void run() {
            try {
                Thread.sleep(STATUS_DISPLAY_TIME);
            } catch (Exception ex) {
            }

            String s;
            JLabel l;

            switch (_evt.getIndex()) {
                // XXX how stupid is this? I shoulda just used an array of
                // status labels
                default:
                case 1:
                    l = _status1;
                    break;
                case 2:
                    l = _status2;
                    break;
                case 3:
                    l = _status3;
                    break;
            }
            s = l.getText();
            if (s != null && s.equals(_evt.getMessage())) {
                l.setText(null);
            }
        }

    }

    /**
     * Description of the Method
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void stateChanged(ChangeEvent evt) {
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    protected class NodeAddActionListener implements ActionListener {
        MerlotDOMNode _parent, _sibling;

        int _action = INTO;

        /**
         * Constructor for the NodeAddActionListener object
         * 
         * @param parent
         *            Description of the Parameter
         */
        public NodeAddActionListener(MerlotDOMNode parent) {
            _parent = parent;

        }

        /**
         * Constructor for the NodeAddActionListener object
         * 
         * @param parent
         *            Description of the Parameter
         * @param sibling
         *            Description of the Parameter
         * @param action
         *            Description of the Parameter
         */
        public NodeAddActionListener(MerlotDOMNode parent,
                MerlotDOMNode sibling, int action) {
            _parent = parent;
            _sibling = sibling;
            _action = action;

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (source instanceof JMenuItem) {
                String s = ((JMenuItem) source).getText();
                MerlotDebug.msg("Add: " + s);
                // create an element
                if (saveOpenEditors()) {
                    _doc.addNewNode(_parent, s, _sibling, _action);
                }
            }
        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    protected class NodeAddSpecialCommentActionListener implements
            ActionListener {
        MerlotDOMNode _parent;

        int _where;

        /**
         * Constructor for the NodeAddSpecialCommentActionListener object
         * 
         * @param parent
         *            Description of the Parameter
         */
        public NodeAddSpecialCommentActionListener(MerlotDOMNode parent,
                int where) {
            _parent = parent;
            _where = where;

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (source instanceof JMenuItem) {
                MerlotDebug.msg("Add: Comment");
                // create an element
                if (_where == INTO)
                    _doc.addNewNode(_parent, DTDConstants.COMMENT_KEY);
                else
                    _doc.addNewNode(_parent.getParentNode(),
                            DTDConstants.COMMENT_KEY, _parent, _where);
            }
        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    protected class NodeAddSpecialPIActionListener implements ActionListener {
        MerlotDOMNode _parent;

        int _where;

        /**
         * Constructor for the NodeAddSpecialPIActionListener object
         * 
         * @param parent
         *            Description of the Parameter
         */
        public NodeAddSpecialPIActionListener(MerlotDOMNode parent, int where) {
            _parent = parent;
            _where = where;

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (source instanceof JMenuItem) {
                MerlotDebug.msg("Add: PI");
                // create an element
                if (_where == INTO)
                    _doc.addNewNode(_parent,
                            DTDConstants.PROCESSING_INSTRUCTION_KEY);
                else
                    _doc.addNewNode(_parent.getParentNode(),
                            DTDConstants.PROCESSING_INSTRUCTION_KEY, _parent,
                            _where);

            }
        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    protected class ShowNodeActionListener implements ActionListener {
        MerlotDOMNode _node;

        /**
         * Constructor for the ShowNodeActionListener object
         * 
         * @param node
         *            Description of the Parameter
         */
        public ShowNodeActionListener(MerlotDOMNode node) {
            _node = node;
        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            // hilight the path to this node in the tree
            selectNode(_node);
        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    public class EditorNodeListener implements MerlotNodeListener {
        JPanel _edit, _tab;

        MerlotDOMNode _node;

        MerlotDOMNode _parent;

        /**
         * Constructor for the EditorNodeListener object
         * 
         * @param parent
         *            Description of the Parameter
         * @param node
         *            Description of the Parameter
         * @param edit
         *            Description of the Parameter
         * @param tab
         *            Description of the Parameter
         */
        public EditorNodeListener(MerlotDOMNode parent, MerlotDOMNode node,
                JPanel edit, JPanel tab) {
            _edit = edit;
            _tab = tab;
            _node = node;
            _parent = parent;

        }

        /**
         * Description of the Method
         * 
         * @param parent
         *            Description of the Parameter
         * @param indices
         *            Description of the Parameter
         * @param children
         *            Description of the Parameter
         */
        public void nodeInserted(MerlotDOMNode parent, int[] indices,
                MerlotDOMNode[] children) {
        }

        /**
         * Description of the Method
         * 
         * @param parent
         *            Description of the Parameter
         * @param indices
         *            Description of the Parameter
         * @param children
         *            Description of the Parameter
         */
        public void nodeRemoved(MerlotDOMNode parent, int[] indices,
                MerlotDOMNode[] children) {
        }

        /**
         * Description of the Method
         * 
         * @param parent
         *            Description of the Parameter
         * @param indices
         *            Description of the Parameter
         * @param children
         *            Description of the Parameter
         */
        public void nodeChanged(MerlotDOMNode parent, int[] indices,
                MerlotDOMNode[] children) {
        }

        /**
         * Description of the Method
         * 
         * @param node
         *            Description of the Parameter
         */
        public void nodeDeleted(MerlotDOMNode node) {

            MerlotDebug.msg("XMLEditorDocUI.EditorNodeListener: nodeDeleted");
            if (node.equals(_node)) {
                if (_node == _currentNode) {
                    _editPanel.removeAll();
                    _editPanel.revalidate();
                }
            }
        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 25 April 2003
     */
    protected class EditTableColumnsAction extends MouseAdapter {
        final String SHOW = MerlotResource.getString(UI, "tree.column.show");

        final String HIDE = MerlotResource.getString(UI, "tree.column.hide");

        final String TREE = MerlotResource.getString(UI, "xml.element");

        /**
         * Description of the Method
         * 
         * @param e
         *            Description of the Parameter
         */
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        /**
         * Description of the Method
         * 
         * @param e
         *            Description of the Parameter
         */
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        /**
         * Description of the Method
         * 
         * @param e
         *            Description of the Parameter
         */
        private void showPopup(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            JPopupMenu jp = new JPopupMenu();
            Vector removedCols = new Vector(_removedColumns.keySet());
            for (int i = 0; i < removedCols.size(); i++) {
                String name = (String) removedCols.elementAt(i);
                TableColumn tc = (TableColumn) _removedColumns.get(name);
                jp.add(addRemoveColumnMenuItem(name, tc, false));
            }

            int cols = _table.getColumnCount();
            TableColumnModel tcm = _table.getColumnModel();
            for (int i = 0; i < cols; i++) {
                final TableColumn tc = tcm.getColumn(i);
                final String name = _table.getColumnName(i);
                if (!name.equals(TREE)) {
                    jp.add(addRemoveColumnMenuItem(name, tc, true));
                }
            }
            if (jp.getComponentCount() > 0) {
                jp.show((Component) e.getSource(), e.getX(), e.getY());
            }
        }

        /**
         * Adds a feature to the RemoveColumnMenuItem attribute of the
         * EditTableColumnsAction object
         * 
         * @param name
         *            The feature to be added to the RemoveColumnMenuItem
         *            attribute
         * @param tc
         *            The feature to be added to the RemoveColumnMenuItem
         *            attribute
         * @param remove
         *            The feature to be added to the RemoveColumnMenuItem
         *            attribute
         * @return Description of the Return Value
         */
        private JMenuItem addRemoveColumnMenuItem(final String name,
                final TableColumn tc, final boolean remove) {
            /*
             * JMenuItem jmi;
             * 
             * if (remove) { jmi = new JMenuItem(HIDE + " " + name); } else {
             * jmi = new JMenuItem(SHOW + " " + name); }
             */
            JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(SHOW + " " + name);
            jmi.setState(remove);

            jmi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addRemoveTreeTableColumn(name, tc, remove);
                }
            });
            return jmi;
        }
    }
    
    private void removeTreeTableColumn(String name) {
        int cols = _table.getColumnCount();
        TableColumnModel tcm = _table.getColumnModel();
        TableColumn tc = null;

        for (int i = 0; i < cols; i++) {
            String column_name = _table.getColumnName(i);
            if (name.equals(column_name)) {
                tc = tcm.getColumn(i);
                break;
            }
        }
        if (tc != null)
            addRemoveTreeTableColumn(name, tc, true);
    }
    
    private void addRemoveTreeTableColumn(String name, TableColumn tc,
            boolean remove) {
        XMLEditorSettings settings = XMLEditorSettings.getSharedInstance();
        String property = "xerlin.show." + name.toLowerCase()
                + ".column.startup";
        if (remove) {
            _table.removeColumn(tc);
            _removedColumns.put(name, tc);
            settings._userProps.put(property, "false");
        } else {
            _table.addColumn(tc);
            _removedColumns.remove(name);
            settings._userProps.put(property, "true");
        }
    }
    
    protected int getTreeTableColumnWidth(String name) {
        int cols = _table.getColumnCount();
        TableColumnModel tcm = _table.getColumnModel();
        TableColumn tc = null;

        for (int i = 0; i < cols; i++) {
            String column_name = _table.getColumnName(i);
            if (name.equals(column_name)) {
                tc = tcm.getColumn(i);
                break;
            }
        }
        if (tc != null) {
            return tc.getPreferredWidth();
        }
        return -1;
    }

    protected void setTreeTableColumnWidth(String name, int width) {
        int cols = _table.getColumnCount();
        TableColumnModel tcm = _table.getColumnModel();
        TableColumn tc = null;

        for (int i = 0; i < cols; i++) {
            String column_name = _table.getColumnName(i);
            if (name.equals(column_name)) {
                tc = tcm.getColumn(i);
                break;
            }
        }
        if (tc != null)
            tc.setPreferredWidth(width);
    }
}

