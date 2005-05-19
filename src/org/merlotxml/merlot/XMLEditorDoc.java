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
 *  Merlot XML Editor (http://www.merlotxm.org/)."
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
 *  http://www.merlotxml.org.
 */
// Copyright 1999 ChannelPoint, Inc., All Rights Reserved.
package org.merlotxml.merlot;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.merlotxml.merlot.plugin.dtd.DTDPluginConfig;
import org.merlotxml.merlot.plugin.dtd.PluginDTDCacheEntry;
import org.merlotxml.merlot.plugin.dtd.TreeTableConfig;
import org.merlotxml.util.xml.DTDCacheEntry;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * XMLEditorDoc contains information and classes for a single XML file. It
 * provides the actions for a specific document such as cut, copy, paste.
 * 
 * @author Kelly A. Campbell
 * @created 16 March 2003
 */
public class XMLEditorDoc implements MerlotTransferableListener,
        ClipboardOwner, MerlotConstants, PropertyChangeListener {

    /**
     * The XML file object for this document
     */
    protected XMLFile _xmlFile;

    /**
     * The user interface class for the document
     */
    protected XMLEditorDocUI _ui;

    /**
     * This provides the data model mapping DOM to JTreeTableModel
     */
    protected DOMTreeTableAdapter _domTree = null;

    /**
     * The frame for this document
     */
    protected JInternalFrame _frame;

    /**
     * variable used while checking if the document needs saved when the user
     * clicks the close box
     */
    protected boolean _ok_to_close = false;

    /**
     * Sequence which is incremented each time a new document is created. used
     * to generate the Untitled-# filename.
     */
    protected static int _docseq = 1;

    /**
     * Title for the document frame
     */
    protected String _title;

    /**
     * Action to perform when the user chooses to edit a node
     */
    protected Action _editNodeAction;

    /**
     * Description of the Field
     */
    protected Action _expandNodeAction;

    /**
     * Description of the Field
     */
    protected Action _collapseNodeAction;

    /**
     * Action to perform when the user chooses to edit some text
     */
    protected Action _editTextAction;

    /**
     * Action to perform when the user deletes a node
     */
    protected Action _deleteNodeAction;

    /**
     * Action for cutting node(s) from the document
     */
    protected Action _cutNodeAction;

    /**
     * Action for copying node(s) from the document
     */
    protected Action _copyNodeAction;

    /**
     * Paste before current node action
     */
    protected Action _pasteBeforeAction;

    /**
     * Paste after current node action
     */
    protected Action _pasteAfterAction;

    /**
     * Paste into the current node
     */
    protected Action _pasteIntoAction;

    /**
     * The undo manager for document node actions
     */
    protected UndoManager _undoManager;

    /**
     * true when this document is the active window in the main frame (actions
     * are enabled)
     */
    protected boolean _docActive = false;

    /**
     * Constructor for a document from a File
     * 
     * @param f
     *            Description of the Parameter
     * @exception MerlotException
     *                Description of the Exception
     */
    public XMLEditorDoc(File f) throws MerlotException {
        _xmlFile = new XMLEditorFile(f);
        _xmlFile.setXMLEditorDoc(this);
        _xmlFile.addPropertyChangeListener(this);

        // for testing
        //if (XMLEditorSettings.getSharedInstance().isDebugOn()) {
            //_xmlFile.printRawXML(System.out, true);
        //}

        init();

    }

    /**
     * Constructor from a XMLFile object
     * 
     * @param f
     *            Description of the Parameter
     * @exception MerlotException
     *                Description of the Exception
     */
    public XMLEditorDoc(XMLFile f) throws MerlotException {
        _xmlFile = f;
    _xmlFile.setXMLEditorDoc(this);
        _xmlFile.addPropertyChangeListener(this);
        // for testing
        if (XMLEditorSettings.getSharedInstance().isDebugOn()) {
            _xmlFile.printRawXML(System.out, true);
        }
        init();

    }

    /**
     * Default constructor. Doesn't really do anything
     */
    public XMLEditorDoc() {
        _xmlFile = null;
        init();

    }

    /**
     * Setup the ui and the internal frame stuff
     */
    protected void init() {

        if (_xmlFile != null && !_xmlFile.isNew()) {
            _title = _xmlFile.getName();
        } else {
            _title = MerlotResource.getString(UI, "untitled") + " " + _docseq++;
        }
        _undoManager = new UndoManager();
        _undoManager.setLimit(XMLEditorSettings.getSharedInstance()
                .getUndoLimit());

        setupTree();
        setupActions();

        _ui = new XMLEditorDocUI(this);
        _frame = new JInternalFrame(_title, true, true, true, true);
        _frame.setJMenuBar(_ui.getMenuBar());
        _frame.setContentPane(_ui);
        _frame.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent evt)
                    throws PropertyVetoException {
                String s = evt.getPropertyName();
                if (s.equals(JInternalFrame.IS_CLOSED_PROPERTY)
                        && !_ok_to_close) {
                    Object o = evt.getNewValue();
                    if (o instanceof Boolean) {
                        if (((Boolean) o).booleanValue()) {
                            if (!checkClose()) {
                                throw new PropertyVetoException(
                                        "User chose to not close unsaved document.",
                                        evt);
                            }
                            try {
                                _frame.setClosed(true);
                                //Don't use the default
                                XMLEditorFrame.getSharedInstance()
                                        .activateDocumentAfterClose();
                            } catch (java.beans.PropertyVetoException ex) {
                                MerlotDebug.exception(ex);
                            }

                        }
                    }
                }
            }
        });
        // _frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        try {
            Icon icon = getFrameIcon();
            if (icon != null) {
                _frame.setFrameIcon(icon);
            }
        } catch (Throwable t) {
            MerlotDebug.exception(t);
        }

        _frame.pack();
    }

    /**
     * Description of the Method
     */
    protected void setupActions() {

        _editNodeAction = new DocEditNodeAction();
        _expandNodeAction = new ExpandNodeAction();
        _collapseNodeAction = new CollapseNodeAction();
        _editTextAction = new DocEditTextAction();

        _deleteNodeAction = new DocDeleteNodeAction();

        _cutNodeAction = new DocCutAction();
        _copyNodeAction = new DocCopyAction();

        _pasteBeforeAction = new DocPasteBeforeAction();
        _pasteAfterAction = new DocPasteAfterAction();
        _pasteIntoAction = new DocPasteIntoAction();

    }

    /**
     * Creates the treetable model from the document.
     */
    protected void setupTree() {
        if (_xmlFile != null) {
            _domTree = new DOMTreeTableAdapter(_xmlFile);
            _domTree.setUndoManager(_undoManager);

            DTDPluginConfig dtdConfig = getDTDPluginConfig();

            TreeTableConfig treeConfig = null;
            if (dtdConfig != null) {
                treeConfig = dtdConfig.getTreeTableConfig();
            }

            java.util.List columnNames = null;
            java.util.List columnAttributes = null;
            if (treeConfig != null) {
                columnNames = treeConfig.getColumnNames();
                columnAttributes = treeConfig.getColumnAttributes();
            }

            Vector v_cols = new Vector();
            Vector v_col_names = new Vector();

            v_cols.add(DOMTreeTableAdapter.ELEMENT);
            v_col_names.add(MerlotResource.getString(UI, "xml.element"));
            if ((columnNames != null) && (columnAttributes != null)) {
                v_cols.addAll(columnAttributes);
                v_col_names.addAll(columnNames);
            } else {
                v_cols.add("name");
                v_col_names.add(MerlotResource.getString(UI, "xml.name"));
            }

            String[] cols = new String[1];
            String[] col_names = new String[1];

            cols = (String[]) v_cols.toArray(cols);
            col_names = (String[]) v_col_names.toArray(col_names);
            _domTree.setColumns(cols, col_names);

		} else
            MerlotDebug.msg("Cannot setup tree: _xmlFile is null.");
    }

    /**
     * Gets the xMLEditorDocUI attribute of the XMLEditorDoc object
     * 
     * @return The xMLEditorDocUI value
     */
    public XMLEditorDocUI getXMLEditorDocUI() {
        return _ui;
    }

    /**
     * Gets the frameIcon attribute of the XMLEditorDoc object
     * 
     * @return The frameIcon value
     */
    protected Icon getFrameIcon() {
        Icon rtn = null;

        Document doc = _xmlFile.getDocument();
        // get the doctype
        DocumentType doctype = doc.getDoctype();
        if (doctype != null) {
            String nm = doctype.getName();
            rtn = XMLEditorSettings.getSharedInstance().getIcon(nm,
                    XMLEditorSettings.SMALL_ICON);
        }
        if (rtn == null) {
            rtn = XMLEditorSettings.getSharedInstance().getAppIconSmall();
        }

        return rtn;
    }

    /**
     * Mark this document to be saved
     * 
     * @param d
     *            The new dirty value
     */
    public void setDirty(boolean d) {
        _xmlFile.setDirty(d);

    }

    /**
     * returns true if this document is marked as dirty (needs saved)
     * 
     * @return The dirty value
     */
    public boolean isDirty() {
        return _xmlFile.isDirty();
    }

    /**
     * Get property change event for dirty attribute and enable/disable actions
     * depending on the dirty attribute
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void propertyChange(PropertyChangeEvent evt) {
        MerlotDebug.msg("XMLEditorDoc propertyChange: " + evt.getPropertyName()
                + " _docActive=" + _docActive);

        if (_docActive) {
            XMLEditorActions a = XMLEditorActions.getSharedInstance();

            if (evt.getPropertyName().equalsIgnoreCase("dirty")) {
                a._saveFileAction.setEnabled(_xmlFile.isDirty());
                a._revertFileAction.setEnabled(_xmlFile.isDirty());
            }
        }
    }

    /**
     * Sets the active attribute of the XMLEditorDoc object
     * 
     * @param tf
     *            The new active value
     */
    public void setActive(boolean tf) {
        _docActive = tf;
    }

    /**
     * Gets the treeTableModel attribute of the XMLEditorDoc object
     * 
     * @return The treeTableModel value
     */
    public DOMTreeTableAdapter getTreeTableModel() {
        return _domTree;
    }

    /**
     * Gets the internalFrame attribute of the XMLEditorDoc object
     * 
     * @return The internalFrame value
     */
    public JInternalFrame getInternalFrame() {
        return _frame;
    }

    /**
     * Gets the file attribute of the XMLEditorDoc object
     * 
     * @return The file value
     */
    public XMLFile getFile() {
        return _xmlFile;
    }

    /**
     * Gets the document attribute of the XMLEditorDoc object
     * 
     * @return The document value
     */
    public Document getDocument() {
        return _xmlFile.getDocument();
    }

    /**
     * Gets the dTDCacheEntry attribute of the XMLEditorDoc object
     * 
     * @return The dTDCacheEntry value
     */
    public DTDCacheEntry getDTDCacheEntry() {
        DTDCacheEntry dtdentry = _xmlFile.getDTDCacheEntry();
        return dtdentry;
    }

    /**
     * Gets the dTDPluginConfig attribute of the XMLEditorDoc object
     * 
     * @return The dTDPluginConfig value
     */
    public DTDPluginConfig getDTDPluginConfig() {
        DTDCacheEntry dtdentry = getDTDCacheEntry();
        if (dtdentry instanceof PluginDTDCacheEntry) {
            return ((PluginDTDCacheEntry) dtdentry).getPluginConfig();
        }
        return null;
    }

    /**
     * Saves the document in the current file. Returns true if the document was
     * saved, false if the user hit cancel.
     * 
     * @return Description of the Return Value
     */
    public boolean saveDocument() {
        boolean exceptions = false;
        if (_xmlFile.isNew()) {
            return saveDocumentAs();
        }

        try {
            exceptions = _ui.saveOpenEditors();
        } catch (Throwable t) {
            MerlotDebug.exception(t);
            MerlotError.exception(t, MerlotResource.getString(ERR,
                    "openeditors.save.w"));
        }

        if (!exceptions) {
            return false;
        }

        try {

            _xmlFile.save();
        } catch (Throwable ex) {
            MerlotDebug.exception(ex);
            MerlotError.exception(ex, MerlotResource.getString(ERR,
                    "document.save.w"));
        }

        return true;
    }

    /**
     * Save a new document, or save the document under a new name. Returns true
     * if the document was saved, or false if the user hit cancel
     * 
     * @return Description of the Return Value
     */
    public boolean saveDocumentAs() {
        boolean ret = false;

        boolean exceptions = _ui.saveOpenEditors();

        if (!exceptions) {
            return false;
        }

        XMLEditorFrame xef = XMLEditorFrame.getSharedInstance();
        XerlinFileDialogs dialogs = xef.getFileDialogs();
        File f = dialogs.getFileToSave();

        if (f == null) {
            return false;
        }
        try {
            MerlotDebug.msg("Approved: file = " + f);
            _xmlFile.saveAs(f);
            _title = _xmlFile.getName();
            _frame.setTitle(_title);

            ret = true;

        } catch (MerlotException ex) {
            MerlotDebug.exception(ex);
            MerlotError.exception(ex, MerlotResource.getString(ERR,
                    "document.save.w"));

        }

        return ret;
    }

    /**
     * returns true if the document window can be closed, false otherwise
     * 
     * @return Description of the Return Value
     */
    public boolean checkClose() {
        boolean ret = true;

        if (isDirty()) {

            String[] docname = new String[1];
            docname[0] = _title;
            MessageFormat mf;

            mf = new MessageFormat(MerlotResource.getString(ERR,
                    "doc.checkClose.dirty.q"));
            String ques = mf.format(docname);
            mf = new MessageFormat(MerlotResource.getString(ERR,
                    "doc.checkClose.dirty.t"));
            String title = mf.format(docname);

            int save = MerlotOptionPane.showInternalConfirmDialog(_frame, ques,
                    title, JOptionPane.YES_NO_CANCEL_OPTION);

            switch (save) {
                case JOptionPane.CANCEL_OPTION:
                    ret = false;
                    break;
                case JOptionPane.YES_OPTION:
                    ret = saveDocument();
                    break;
                case JOptionPane.NO_OPTION:
                    ret = true;
                    break;
                default:
                    // There is a bug in the current JDK Yes/No/Cancel
                    // key mappings, ESC is mapped to something
                    // odd - value is NONE OF THE ABOVE, assume
                    // cancel behavior. On Linux - save = -1
                    // Assume cancel behavior for cross-platform.
                    ret = false;
                    break;
            }
        }
        _ok_to_close = ret;
        if (ret) {
            XMLEditorSettings.getSharedInstance().saveSplitPaneSetup(_ui);
            _xmlFile.close();
        }
        return ret;
    }

    /**
     * Closes the document unless the document is dirty and the user hits cancel
     * when prompted.
     * 
     * @return true if the document was closed, false otherwise
     */
    public boolean closeDocument() {
        if (checkClose()) {
            try {
                _frame.setClosed(true);
                //	_frame.dispose();
                // dispose should be automagic from
                // _frame.setDefaultCloseOperation
                XMLEditorFrame.getSharedInstance().activateDocumentAfterClose();
                return true;
            } catch (java.beans.PropertyVetoException ex) {
                MerlotDebug.exception(ex);
            }
        }
        return false;
    }

    /**
     * Description of the Method
     */
    public void deleteNodes() {
        //		throws MerlotException

        MerlotDOMNode[] nodes = _ui.getSelectedNodes();
        if (nodes == null) {
            //MerlotDebug.msg("Not deleting nodes. Selected nodes in ui null.");
            return;
        }
        CompoundEdit bigedit = new CompoundEdit();

        for (int i = nodes.length - 1; i >= 0; i--) {
            MerlotDOMNode node = nodes[i];

            int[] where = _domTree.getLocationPathForNode(node);

            _ui.deleteNode(node);

            MerlotUndoableEdit edit = new MerlotUndoableEdit("delete",
                    MerlotUndoableEdit.DELETE, _domTree, node, where);
            bigedit.addEdit(edit);
        }
        bigedit.end();

        addUndoableEdit(bigedit);

    }

    /**
     * Description of the Method
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void cut(ActionEvent evt) {
        boolean pasteAble = false;

        Object o = evt.getSource();
        MerlotDebug.msg("cut: evt source = " + o);

        if (o instanceof JTextComponent) {
            ((JTextComponent) o).cut();
            pasteAble = true;

        } else {
            Clipboard cb;
            if (XMLEditorFrame.getSharedInstance() != null) {
                cb = XMLEditorFrame.getSharedInstance().getTreeClipboard();
            } else {
                cb = XMLEditor.getSharedInstance().getXerlinPanel()
                        .getTreeClipboard();
            }
            Transferable t = _ui.getTransferable();

            cb.setContents(t, this);
            pasteAble = true;
            MerlotDOMNode[] nodes = _ui.getSelectedNodes();
            CompoundEdit bigedit = new CompoundEdit();
            for (int i = 0; i < nodes.length; i++) {
                MerlotDOMNode node = nodes[i];
                int[] where = _domTree.getLocationPathForNode(node);
                _ui.deleteNode(node);
                MerlotUndoableEdit edit = new MerlotUndoableEdit("cut",
                        MerlotUndoableEdit.DELETE, _domTree, node, where);
                bigedit.addEdit(edit);

            }
            bigedit.end();

            addUndoableEdit(bigedit);
        }
        if (XMLEditorActions.getSharedInstance() != null) {
            XMLEditorActions.getSharedInstance()._pasteAction
                    .setEnabled(pasteAble);
        }
    }

    /**
     * Description of the Method
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void copy(ActionEvent evt) {
        boolean pasteAble = false;
        Object o = evt.getSource();
        MerlotDebug.msg("copy: evt source = " + o);
        if (o instanceof JTextComponent) {
            ((JTextComponent) o).copy();
            pasteAble = true;
        } else {
            Clipboard cb;
            if (XMLEditorFrame.getSharedInstance() != null) {
                cb = XMLEditorFrame.getSharedInstance().getTreeClipboard();
            } else {
                cb = XMLEditor.getSharedInstance().getXerlinPanel()
                        .getTreeClipboard();
            }
            Transferable t = _ui.getTransferable();

            cb.setContents(t, this);
            pasteAble = true;
        }

        if (XMLEditorActions.getSharedInstance() != null) {
            XMLEditorActions.getSharedInstance()._pasteAction
                    .setEnabled(pasteAble);
        }
    }

    /**
     * Pastes what's on the clipboard after the given node. We assume this is a
     * valid operation without checking it because it should have been checked
     * via enablePasteItems when building the menu.
     * <P>
     * 
     * 
     * 
     * @param row
     *            Description of the Parameter
     */

    public void pasteAfter(int row) {
        Clipboard cb;
        if (XMLEditorFrame.getSharedInstance() != null) {
            cb = XMLEditorFrame.getSharedInstance().getTreeClipboard();
        } else {
            cb = XMLEditor.getSharedInstance().getXerlinPanel()
                    .getTreeClipboard();
        }

        // when pasting we just insert the node. we leave it up to copy or cut
        // to handle any deep copying that needs to be done
        Object o = cb.getContents(this);
        if (o instanceof MerlotDOMFragment) {
            UndoableEdit edit = _domTree.importFragment(row,
                    (MerlotDOMFragment) o, DNDJTreeTableModel.AFTER, true);
            if (edit != null) {

                addUndoableEdit(edit);

            }
        } else {
            MerlotDebug.msg("Clipboard contents not a DocumentFragment");
        }

    }

    //	public void pasteBefore (MerlotDOMNode node)
    /**
     * Description of the Method
     * 
     * @param row
     *            Description of the Parameter
     */
    public void pasteBefore(int row) {
        Clipboard cb;
        if (XMLEditorFrame.getSharedInstance() != null) {
            cb = XMLEditorFrame.getSharedInstance().getTreeClipboard();
        } else {
            cb = XMLEditor.getSharedInstance().getXerlinPanel()
                    .getTreeClipboard();
        }

        Object o = cb.getContents(this);
        if (o instanceof MerlotDOMFragment) {
            UndoableEdit edit = _domTree.importFragment(row,
                    (MerlotDOMFragment) o, DNDJTreeTableModel.BEFORE, true);

            if (edit != null) {

                addUndoableEdit(edit);

            }
        } else {
            MerlotDebug.msg("Clipboard contents not a DocumentFragment");
        }

    }

    //	public void pasteInto (MerlotDOMNode parent)
    /**
     * Description of the Method
     * 
     * @param row
     *            Description of the Parameter
     */
    public void pasteInto(int row) {
        Clipboard cb;
        if (XMLEditorFrame.getSharedInstance() != null) {
            cb = XMLEditorFrame.getSharedInstance().getTreeClipboard();
        } else {
            cb = XMLEditor.getSharedInstance().getXerlinPanel()
                    .getTreeClipboard();
        }

        Object o = cb.getContents(this);
        if (o instanceof MerlotDOMFragment) {
            UndoableEdit edit = _domTree.importFragment(row,
                    (MerlotDOMFragment) o, DNDJTreeTableModel.INTO, true);
            if (edit != null) {

                addUndoableEdit(edit);
            }
        } else {
            MerlotDebug.msg("Clipboard contents not a DocumentFragment");
        }

    }

    /**
     * Description of the Method
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void paste(ActionEvent evt) {
        Object o = evt.getSource();
        MerlotDebug.msg("paste: evt source = " + o);

        if (o instanceof JTextComponent) {
            ((JTextComponent) o).paste();
        } else {

            // asks where the user wants to paste and then calls one of after,
            // before, into
        }

    }

    /**
     * Description of the Method
     * 
     * @param evt
     *            Description of the Parameter
     */
    public void undo(ActionEvent evt) {
        try {
            MerlotDebug.msg("Undoing...");

            _undoManager.undo();
            XMLEditorFrame.getSharedInstance().resetUndoAction(_undoManager);
        } catch (CannotUndoException ex) {
            MerlotError.exception(ex, MerlotResource.getString(ERR, "undo.w"));
            MerlotDebug.exception(ex);

        }
    }

    /**
     * Adds a feature to the NewNode attribute of the XMLEditorDoc object
     * 
     * @param parent
     *            The feature to be added to the NewNode attribute
     * @param elementname
     *            The feature to be added to the NewNode attribute
     */
    public void addNewNode(MerlotDOMNode parent, String elementname) {
        addNewNode(parent, elementname, null, INTO);

    }

    /**
     * Adds a feature to the NewNode attribute of the XMLEditorDoc object
     * 
     * @param parent
     *            The feature to be added to the NewNode attribute
     * @param elementname
     *            The feature to be added to the NewNode attribute
     * @param sibling
     *            The feature to be added to the NewNode attribute
     * @param action
     *            The feature to be added to the NewNode attribute
     */
    public void addNewNode(MerlotDOMNode parent, String elementname,
            MerlotDOMNode sibling, int action) {
        MerlotDebug.msg("DEBUG: addNewNode(sibling=" + sibling + " action="
                + action + ")");

        try {
            MerlotDOMNode nd = null;

            if (sibling != null && action != INTO) {
                if (action == AFTER) {
                    nd = sibling.newSiblingAfter(elementname);
                } else if (action == BEFORE) {
                    nd = sibling.newSiblingBefore(elementname);
                }
            } else {
                nd = parent.newChild(elementname);
            }
            if (nd != null) {
                int[] where = _domTree.getLocationPathForNode(nd);

                MerlotUndoableEdit edit = new MerlotUndoableEdit("add",
                        MerlotUndoableEdit.INSERT, _domTree, nd, where);
                addUndoableEdit(edit);
                // set the node as the current selection
                // have to wait until the tree table is updated though
                SwingUtilities.invokeLater(new SelectNodeRunnable(nd));
                _ui.editNode(nd, true);
            }
        } catch (DOMException ex) {
            MerlotDebug.exception(ex);
            MerlotError.exception(ex, MerlotResource.getString(ERR,
                    "dom.add.new.err"));

        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    private class SelectNodeRunnable implements Runnable {
        private MerlotDOMNode _selectedNode;

        /**
         * Constructor for the SelectNodeRunnable object
         * 
         * @param node
         *            Description of the Parameter
         */
        public SelectNodeRunnable(MerlotDOMNode node) {
            _selectedNode = node;
        }

        /**
         * Main processing method for the SelectNodeRunnable object
         */
        public void run() {
            _ui.selectNode(_selectedNode);
        }
    }

    /**
     * Adds a feature to the UndoableEdit attribute of the XMLEditorDoc object
     * 
     * @param e
     *            The feature to be added to the UndoableEdit attribute
     */
    public void addUndoableEdit(UndoableEdit e) {
        if (XMLEditorFrame.getSharedInstance() == null) {
            return;
        }
        MerlotDebug.msg("undoableEdit added: " + e);

        _undoManager.addEdit(e);
        XMLEditorFrame.getSharedInstance().resetUndoAction(_undoManager);

    }

    /**
     * Gets the undoManager attribute of the XMLEditorDoc object
     * 
     * @return The undoManager value
     */
    public UndoManager getUndoManager() {
        return _undoManager;
    }

    /**
     * for implementing the ClipboardOwner interface
     * 
     * @param cb
     *            Description of the Parameter
     * @param t
     *            Description of the Parameter
     */
    public void lostOwnership(Clipboard cb, Transferable t) {
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocSaveAction extends AbstractAction {
        /**
         * Constructor for the DocSaveAction object
         */
        public DocSaveAction() {
            MerlotUtils.loadActionResources(this, UI, "file.save");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            saveDocument();

        }

    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocSaveAsAction extends AbstractAction {
        /**
         * Constructor for the DocSaveAsAction object
         */
        public DocSaveAsAction() {
            MerlotUtils.loadActionResources(this, UI, "file.saveas");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            saveDocumentAs();

        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocCloseAction extends AbstractAction {
        /**
         * Constructor for the DocCloseAction object
         */
        public DocCloseAction() {
            MerlotUtils.loadActionResources(this, UI, "file.close");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            closeDocument();
        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocEditNodeAction extends AbstractAction {
        /**
         * Constructor for the DocEditNodeAction object
         */
        public DocEditNodeAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.editnode");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            _ui.editNode(_ui.getSelectedNode(), false);

        }
    }

    /**
     * Description of the Method
     * 
     * @param parent
     *            Description of the Parameter
     * @param expand
     *            Description of the Parameter
     */
    void expandCollapseNode(TreePath parent, boolean expand) {
        Component f = XMLEditorFrame.getSharedInstance();
        if (f == null) {
            f = XMLEditor.getSharedInstance().getXerlinPanel();
        }
        f.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MerlotDOMNode node = (MerlotDOMNode) parent.getLastPathComponent();
        MerlotDOMNode kids[] = node.getChildNodes();
        for (int i = 0; i < kids.length; i++) {
            TreePath path = parent.pathByAddingChild(kids[i]);
            expandCollapseNode(path, expand);
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            _domTree._table.getTree().expandPath(parent);
        } else {
            _domTree._table.getTree().collapsePath(parent);
        }
        f.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class CollapseNodeAction extends AbstractAction {
        /**
         * Constructor for the CollapseNodeAction object
         */
        public CollapseNodeAction() {
            MerlotUtils.loadActionResources(this, UI, "tree.collapse");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            MerlotDOMNode node = getXMLEditorDocUI().getSelectedNode();
            if (node == null) {
                return;
            }
            Object[] pathToChild = _domTree.getTreePathForNode(node);
            TreePath path = new TreePath(pathToChild);
            expandCollapseNode(path, false);
        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class ExpandNodeAction extends AbstractAction {
        /**
         * Constructor for the ExpandNodeAction object
         */
        public ExpandNodeAction() {
            MerlotUtils.loadActionResources(this, UI, "tree.expand");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            MerlotDOMNode node = getXMLEditorDocUI().getSelectedNode();
            if (node == null) {
                return;
            }
            Object[] pathToChild = _domTree.getTreePathForNode(node);
            TreePath path = new TreePath(pathToChild);
            expandCollapseNode(path, true);
        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocEditTextAction extends AbstractAction {
        /**
         * Constructor for the DocEditTextAction object
         */
        public DocEditTextAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.edittext");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            _ui.editText(_ui.getSelectedNode());

        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocDeleteNodeAction extends AbstractAction {
        /**
         * Constructor for the DocDeleteNodeAction object
         */
        public DocDeleteNodeAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.delnode");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            deleteNodes();

        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocPasteBeforeAction extends AbstractAction {
        /**
         * Constructor for the DocPasteBeforeAction object
         */
        public DocPasteBeforeAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.paste.before");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            int row = _ui.getSelectedRow();
            pasteBefore(row);

        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocPasteAfterAction extends AbstractAction {
        /**
         * Constructor for the DocPasteAfterAction object
         */
        public DocPasteAfterAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.paste.after");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {

            int row = _ui.getSelectedRow();
            pasteAfter(row);

        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocPasteIntoAction extends AbstractAction {
        /**
         * Constructor for the DocPasteIntoAction object
         */
        public DocPasteIntoAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.paste.into");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            int row = _ui.getSelectedRow();
            pasteInto(row);

        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocCutAction extends AbstractAction {
        /**
         * Constructor for the DocCutAction object
         */
        public DocCutAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.cut");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            cut(evt);
        }
    }

    /**
     * Description of the Class
     * 
     * @author Administrator
     * @created 16 March 2003
     */
    protected class DocCopyAction extends AbstractAction {
        /**
         * Constructor for the DocCopyAction object
         */
        public DocCopyAction() {
            MerlotUtils.loadActionResources(this, UI, "xml.copy");

        }

        /**
         * Description of the Method
         * 
         * @param evt
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent evt) {
            copy(evt);

        }
    }
    
    ValidationThread _validationThread = null;
    
    public ValidationThread getValidationThread() {
        if (_validationThread == null) {
            _validationThread = new ValidationThread(this);
		}        
		return _validationThread;
    }
}
